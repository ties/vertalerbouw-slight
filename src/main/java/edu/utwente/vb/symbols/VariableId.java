package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

import com.google.common.base.Objects;

public class VariableId<T extends Token> implements Id<T>{
	private final Type type;
	private final T token;
	
	public VariableId(T n, Type t){
		this.type =	checkNotNull(t);
		this.token = checkNotNull(n);
	}
	
	@Override
	public String getText() {
		return token.getText();
	}
	
	@Override
	public T getToken() {
		return token;
	}
	
	public Type getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(token, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VariableId){
			VariableId that = (VariableId)obj;
			return Objects.equal(this.token, that.token) && Objects.equal(this.type, that.type);
		}
		return false;
	}
}
