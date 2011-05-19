package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.Token;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionId implements Id{
	private Type returnType;
	private String name;
	private List<VariableId> parameters;
	
	public FunctionId(String t, Type r, List<VariableId> p){
		this.name = checkNotNull(t);
		this.returnType = checkNotNull(r);
		this.parameters = ImmutableList.copyOf(checkNotNull(p));
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public Type getType() {
		return returnType;
	}
}
