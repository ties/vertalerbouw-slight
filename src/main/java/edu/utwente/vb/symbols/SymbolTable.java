package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.tree.BaseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		log.debug("openScope() " + level + " -> " + (level + 1));
		inner = new Env(inner);		
		level++;
	}
	
	public void closeScope() throws SymbolTableException{
		log.debug("closeScope() " + level + " -> " + (level - 1));
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
	
	public FunctionId<T> apply(String w, List<ExampleType> applied) throws SymbolTableException{
		return apply(w, ExampleType.asArray(applied));
	}
	
	public FunctionId<T> apply(String w, ExampleType... applied) throws SymbolTableException{
		return inner.apply(w, applied);
	}
	
	public VariableId<T> apply(String n) throws SymbolTableException{
		return inner.apply(n);
	}
	
	public int getCurrentScope() {
		return level;
	}
}