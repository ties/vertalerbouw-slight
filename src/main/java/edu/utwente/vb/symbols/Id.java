package edu.utwente.vb.symbols;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

public interface Id<T extends BaseTree> {
	public Type getType();
	
	public T getNode();
	
	public String getText();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
}
