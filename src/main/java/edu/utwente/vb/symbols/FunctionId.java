package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.Token;

import com.google.common.base.Objects;
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
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, returnType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FunctionId){
			FunctionId that = (FunctionId)obj;
			return Objects.equal(this.name, that.name) && Objects.equal(this.returnType, that.returnType) && Objects.equal(this.parameters, that.parameters)
		}
		return false;
	}
}
