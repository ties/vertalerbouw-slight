package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;

import org.antlr.runtime.Token;

public class SymbolTable<T extends Token> implements EnvApi<T>{
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
		checkArgument(level > 0, "Can not close level 0 - unbalanced indents");
		inner = inner.prev;
		level--;
	}
	
	@Override
	public void put(Id<T> i) {
		inner.put(i);
	}
	
	@Override
	public Id<T> get(String w) {
		return inner.get(w);
	}
	
	public int getLevel() {
		return level;
	}
}
