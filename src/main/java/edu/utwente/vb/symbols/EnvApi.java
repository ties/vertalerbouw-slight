package edu.utwente.vb.symbols;

import org.antlr.runtime.Token;

public interface EnvApi<T extends Token> {
	public void put(final Id<T> i);
	public Id<T> get(final String w);
}
