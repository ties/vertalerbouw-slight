package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.exceptions.IllegalVariableDefinitionException;
import edu.utwente.vb.exceptions.SymbolTableException;

public class SymbolTable<T extends BaseTree> implements EnvApi<T>{
	private Logger log = LoggerFactory.getLogger(SymbolTable.class);
	private Env inner;
	private int level;
	
	public SymbolTable(){
		inner = new Env();
		level = 0;
	}
	
	public void openScope(){
		log.info("openScope() " + level + " -> " + (level + 1));
		inner = new Env(inner);		
		level++;
	}
	
	public void closeScope() throws SymbolTableException{
		log.info("closeScope() " + level + " -> " + (level - 1));
		if(level <= 0)
			throw new SymbolTableException("Can not close level 0 - unbalanced indents");
		inner = inner.prev;
		level--;
	}
	
	@Override
	public void put(VariableId<T> i) throws IllegalVariableDefinitionException {
		inner.put(i);
	}
	
	@Override
	public void put(FunctionId<T> i) throws IllegalFunctionDefinitionException {
		inner.put(i);
	}
	
	@Override
	public List<Id<T>> get(String w) {
		return inner.get(w);
	}
	
	public FunctionId<T> apply(String w, List<Type> applied) throws SymbolTableException{
		return apply(w, Type.asArray(applied));
	}
	
	public FunctionId<T> apply(String w, Type... applied) throws SymbolTableException{
		return inner.apply(w, applied);
	}
	
	public VariableId<T> apply(String n) throws SymbolTableException{
		return inner.apply(n);
	}
	
	public int getCurrentScope() {
		return level;
	}
}