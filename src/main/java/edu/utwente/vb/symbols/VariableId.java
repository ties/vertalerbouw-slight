package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

public class VariableId implements Id{
	private final Type type;
	private final String name;
	
	public VariableId(String n, Type t){
		this.type =	checkNotNull(t);
		this.name = checkNotNull(n);
	}
	
	public String getName(){
		return this.name;
	}
	
	public Type getType() {
		return type;
	}
}
