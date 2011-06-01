package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.collect.Lists;

public interface Id<T extends BaseTree> {
	public Type getType();
	
	public T getNode();
	
	public String getText();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
	
	public boolean equalsSignature(String name, Type... params);
}
