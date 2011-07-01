package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.collect.Lists;

public interface Id<T extends BaseTree> {
	public enum IdType{VARIABLE, FUNCTION, BUILTIN, VARARGS}
	
	public IdType getIdType();
	
	public ExampleType getType();
	
	public T getNode();
	
	public String getText();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
	
	public boolean equalsSignature(String name, ExampleType... params);
	
	public void updateType(ExampleType t);
}
