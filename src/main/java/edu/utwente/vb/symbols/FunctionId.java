package edu.utwente.vb.symbols;

import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionId<T extends BaseTree> implements Id<T>{
	private Type returnType;
	private T token;
	private List<VariableId> parameters;
	
	public FunctionId(T t, Type r, List<VariableId> p){
		this.token = checkNotNull(t);
		this.returnType = checkNotNull(r);
		this.parameters = ImmutableList.copyOf(checkNotNull(p));
	}
	
	@Override
	public Type getType() {
		return returnType;
	}
	
	@Override
	public T getToken() {
		return token;
	}
	
	@Override
	public String getText() {
		return token.getText();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(token, returnType);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FunctionId){
			FunctionId that = (FunctionId)obj;
			return Objects.equal(this.token, that.token) && Objects.equal(this.returnType, that.returnType) && Objects.equal(this.parameters, that.parameters);
		}
		return false;
	}
}
