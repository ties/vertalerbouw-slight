package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

import com.google.common.base.Objects;

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
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VariableId){
			VariableId that = (VariableId)obj;
			return Objects.equal(this.name, that.name) && Objects.equal(this.type, that.type);
		}
		return false;
	}
}
