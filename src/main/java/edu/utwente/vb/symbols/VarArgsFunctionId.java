package edu.utwente.vb.symbols;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.tree.BaseTree;
import org.objectweb.asm.commons.Method;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;

public class VarArgsFunctionId<T extends BaseTree> extends FunctionId<T>{
	private Set<ExampleType> acceptableTypes;
	
	/**
	 * A function identifier that accept any number of arguments that match the list of allowed types.
	 * 
	 * @param t Node
	 * @param r return type
	 * @param acceptable acceptable types
	 * @throws IllegalFunctionDefinitionException 
	 */
	public VarArgsFunctionId(T t, ExampleType r, ExampleType... acceptable) throws IllegalFunctionDefinitionException{
		super(t, r);
		acceptableTypes = Sets.newHashSet(acceptable);
	}
	
	@Override
	public Method asMethod() {
		throw new UnsupportedOperationException("Can not convert varArgs to fixed args");
	}
	
	@Override
	public boolean equalsSignature(String name, ExampleType... applied) {
		return getText().equals(name) && acceptableTypes.containsAll(Lists.newArrayList(applied));
	}
	
	@Override
	public List<ExampleType> getTypeParameters() {
		throw new UnsupportedOperationException("Can not get type parameters");
	}
	
	@Override
	public void updateType(ExampleType t) {
		throw new UnsupportedOperationException("Can not update type");
	}	
	
	@Override
	public edu.utwente.vb.symbols.Id.IdType getIdType() {
		return IdType.VARARGS;
	}
}
