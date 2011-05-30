package edu.utwente.vb.symbols;

import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionId<T extends BaseTree> implements Id<T>{
	private Type returnType;
	private T token;
	private List<VariableId<T>> formalParameters;
	
	/**
	 * A function identifier
	 * 
	 * Function identifiers can return Type.VOID
	 * If a function identifier has no formal parameters, it's parameters will be the empty list
	 * The formal parameters can NOT contain Type.VOID
	 * 
	 * @param t Node
	 * @param r return type
	 * @param p formal parameters.
	 */
	public FunctionId(T t, Type r, VariableId<T>... p){
		this.token = checkNotNull(t);
		this.returnType = checkNotNull(r);
		//
		formalParameters = ImmutableList.copyOf(checkNotNull(p));
		if(formalParameters.contains(Type.VOID))
			throw new IllegalArgumentException("A function argument can not have the VOID type");
	}
	
	@Override
	public Type getType() {
		return returnType;
	}
	
	@Override
	public T getNode() {
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
			return Objects.equal(this.token, that.token) && Objects.equal(this.returnType, that.returnType) && Objects.equal(this.formalParameters, that.formalParameters);
		}
		return false;
	}
	
	
	@Override
	public boolean equalsSignature(String name, Type... applied) {
		if(Objects.equal(this.token.getText(), name) && applied.length == formalParameters.size()){
			for(int i = 0; i < applied.length; i++){
				if(!Objects.equal(applied[i], formalParameters.get(i)))
						return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Transformeer een Lijst van VariableId's in een lijst van de Type's die in de VariableId's zitten
	 * @param params 
	 * @return
	 */
	private static <Q extends BaseTree> List<Type> extractTypes(List<VariableId<Q>> params){
		return Lists.transform(params, new Function<VariableId<Q>, Type>() {
			@Override
			public Type apply(VariableId<Q> input) {
				return input.getType();
			}
		});
	}
}
