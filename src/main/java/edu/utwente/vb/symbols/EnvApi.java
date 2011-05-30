package edu.utwente.vb.symbols;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

public interface EnvApi<T extends BaseTree> {
	public void put(final Id<T> i);
	public Id<T> get(final String w);
}
