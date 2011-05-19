package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;

public class SymbolTable implements EnvApi{
	private Env inner;
	private int level;
	
	public SymbolTable(){
		inner = new Env();
		level = 0;
	}
	
	public void openScope(){
		level++;
	}
	
	public void closeScope(){
		checkArgument(level > 0, "Can not close level 0 - unbalanced indents");
		inner = inner.prev;
		level--;
	}
	
	@Override
	public void put(Id i) {
		inner.put(i);
	}
	
	@Override
	public Id get(String w) {
		return inner.get(w);
	}
	
	public int getLevel() {
		return level;
	}
}
