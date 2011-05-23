package edu.utwente.vb.symbols;

import org.antlr.runtime.Token;

public interface Id<T extends Token> {
	public Type getType();
	
	public T getToken();
	
	public String getText();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
}
