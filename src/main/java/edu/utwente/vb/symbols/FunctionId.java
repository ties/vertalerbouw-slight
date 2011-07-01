package edu.utwente.vb.symbols;

import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.objectweb.asm.commons.Method;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class FunctionId<T extends BaseTree> implements Id<T>{
	private ExampleType returnType;
	private T node;
	private List<VariableId<T>> formalParameters;
	private IdType idType = IdType.FUNCTION;
	
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
	public FunctionId(T t, ExampleType r, VariableId<T>... p) throws IllegalFunctionDefinitionException{
		this.node = checkNotNull(t);
		this.returnType = checkNotNull(r);
		
		formalParameters = ImmutableList.copyOf(checkNotNull(p));
		if(formalParameters.contains(ExampleType.VOID))
			throw new IllegalFunctionDefinitionException("A function argument can not have the VOID type");
	}
	
	public FunctionId(T t, ExampleType r, List<VariableId<T>> p) throws IllegalFunctionDefinitionException{
		this.node = checkNotNull(t);
		this.returnType = checkNotNull(r);
		
		formalParameters = ImmutableList.copyOf(checkNotNull(p));
		if(extractTypes(formalParameters).contains(ExampleType.VOID))
			throw new IllegalFunctionDefinitionException("A function argument can not have the VOID type");
	}
	
	public void setIdType(IdType idType) {
		this.idType = idType;
	}
	
	
	@Override
	public ExampleType getType() {
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
	
	public List<ExampleType> getTypeParameters() {
		return extractTypes(formalParameters);
	}
	
	@Override
	public edu.utwente.vb.symbols.Id.IdType getIdType() {
		return idType;
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
	public boolean equalsSignature(String name, ExampleType... applied) {
		return Objects.equal(this.node.getText(), name) && Arrays.deepEquals(applied, ExampleType.asArray(extractTypes(formalParameters)));
	}

	/**
	 * Converteer deze FunctionId naar een ASM-stijl Method descriptor.
	 * @return
	 */
	public Method asMethod(){
		return new Method(getText(), returnType.toASM(), ExampleType.listToASM(extractTypes(formalParameters)));
	}
	
	/**
	 * Transformeer een Lijst van VariableId's in een lijst van de Type's die in de VariableId's zitten
	 * @param params 
	 * @return
	 */
	private static <Q extends BaseTree> List<ExampleType> extractTypes(List<VariableId<Q>> params){
		return Lists.transform(params, new Function<VariableId<Q>, ExampleType>() {
			@Override
			public ExampleType apply(VariableId<Q> input) {
				return input.getType();
			}
		});
	}
	
	/**
	 * Update het return type van deze methode 
	 */
	@Override
	public void updateType(ExampleType t) {
		checkArgument(ExampleType.UNKNOWN.equals(this.returnType), "Return type is not unknown");
		checkArgument(!ExampleType.UNKNOWN.equals(t), "Can not update to type UNKNOWN");
		this.returnType = t;
	}
}
