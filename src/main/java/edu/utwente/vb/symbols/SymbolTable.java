package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

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
	public void put(Id<T> i) {
		inner.put(i);
	}
	
	@Override
	public Set<Id<T>> get(String w) {
		return inner.get(w);
	}
	
	public Id<T> apply(String w, Type... applied){
		return inner.apply(w, applied);
	}
	
	public int getLevel() {
		return level;
	}
}