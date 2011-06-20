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

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionId<T extends BaseTree> implements Id<T>{
	private Type returnType;
	private T node;
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
	 * @throws IllegalFunctionDefinitionException 
	 */
	public FunctionId(T t, Type r, VariableId<T>... p) throws IllegalFunctionDefinitionException{
		this.node = checkNotNull(t);
		this.returnType = checkNotNull(r);
		
		formalParameters = ImmutableList.copyOf(checkNotNull(p));
		if(formalParameters.contains(Type.VOID))
			throw new IllegalFunctionDefinitionException("A function argument can not have the VOID type");
	}
	
	public FunctionId(T t, Type r, List<VariableId<T>> p) throws IllegalFunctionDefinitionException{
		this.node = checkNotNull(t);
		this.returnType = checkNotNull(r);
		
		formalParameters = ImmutableList.copyOf(checkNotNull(p));
		if(extractTypes(formalParameters).contains(Type.VOID))
			throw new IllegalFunctionDefinitionException("A function argument can not have the VOID type");
	}
	
	
	@Override
	public Type getType() {
		return returnType;
	}
	
	@Override
	public T getNode() {
		return node;
	}
	
	@Override
	public String getText() {
		return node.getText();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(node, returnType);
	}
	
	public List<Type> getTypeParameters() {
		return extractTypes(formalParameters);
	}
	
	@Override
	public String toString() {
		return node.getText() + " (" + Objects.toStringHelper(formalParameters) + ") -> " + returnType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FunctionId){
			FunctionId that = (FunctionId)obj;
			return Objects.equal(this.node, that.node) && Objects.equal(this.returnType, that.returnType) && Objects.equal(this.formalParameters, that.formalParameters);
		}
		return false;
	}
	
	
	@Override
	public boolean equalsSignature(String name, Type... applied) {
		return Objects.equal(this.node.getText(), name) && Arrays.deepEquals(applied, Type.asArray(extractTypes(formalParameters)));
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
	
	/**
	 * Update het return type van deze methode 
	 */
	@Override
	public void updateType(Type t) {
		checkArgument(Type.UNKNOWN.equals(this.returnType), "Return type is not unknown");
		checkArgument(!Type.UNKNOWN.equals(t), "Can not update to type UNKNOWN");
		this.returnType = t;
	}
}
