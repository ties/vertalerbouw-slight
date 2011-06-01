package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.collect.ImmutableList;

public class SymbolTable<T extends BaseTree> implements EnvApi<T>{
	private Env inner;
	private int level;
	
	public SymbolTable(){
		inner = new Env();
		level = 0;
	}
	
	public void openScope(){
		inner = new Env(inner);		
		level++;
	}
	
	public void closeScope(){
		if(level <= 0)
			throw new SymbolTableException("Can not close level 0 - unbalanced indents");
		inner = inner.prev;
		level--;
	}
	
	@Override
	public void put(VariableId<T> i) {
		inner.put(i);
	}
	
	@Override
	public void put(FunctionId<T> i) {
		inner.put(i);
	}
	
	@Override
	public Set<Id<T>> get(String w) {
		return inner.get(w);
	}
	
	public FunctionId<T> apply(String w, List<Type> applied){
		return apply(w, Type.asArray(applied));
	}
	
	public FunctionId<T> apply(String w, Type... applied){
		return inner.apply(w, applied);
	}
	
	public VariableId<T> apply(String n){
		return inner.apply(n);
	}
	
	public int getLevel() {
		return level;
	}
}