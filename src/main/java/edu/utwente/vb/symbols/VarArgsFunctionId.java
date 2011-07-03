package edu.utwente.vb.symbols;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.tree.BaseTree;
import org.objectweb.asm.commons.Method;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;

public class VarArgsFunctionId<T extends BaseTree> extends FunctionId<T> {
    private Set<ExampleType> acceptableTypes;
    private int minNumArgs;

    /**
     * A function identifier that accept any number of arguments that match the
     * list of allowed types.
     * 
     * @param t
     *            Node
     * @param r
     *            return type
     * @param acceptable
     *            acceptable types
     * @throws IllegalFunctionDefinitionException
     */
    public VarArgsFunctionId(T t, ExampleType r, ExampleType... acceptable) throws IllegalFunctionDefinitionException {
	super(t, r);
	acceptableTypes = Sets.newHashSet(acceptable);
    }

    public VarArgsFunctionId(T t, ExampleType r, int minNumArgs, ExampleType... acceptable) throws IllegalFunctionDefinitionException {
	this(t, r, acceptable);
	this.minNumArgs = minNumArgs;
    }

    @Override
    public Method asMethod() {
	throw new UnsupportedOperationException("Can not convert varArgs to fixed args");
    }

    @Override
    public boolean equalsSignature(String name, ExampleType... applied) {
	return getText().equals(name) && applied.length >= minNumArgs && acceptableTypes.containsAll(Lists.newArrayList(applied));
    }

    @Override
    public List<ExampleType> getTypeParameters() {
	ImmutableList.Builder<ExampleType> builder = ImmutableList.builder();
	for(ExampleType type : acceptableTypes){
	    for(int i=0; i< minNumArgs; i++){
		builder.add(type);
	    }
	}
	return builder.build();
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
