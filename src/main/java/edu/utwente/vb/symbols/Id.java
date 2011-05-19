package edu.utwente.vb.symbols;

import org.antlr.runtime.Token;

public interface Id {
	public Type getType();
	
	public String getName();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
}
