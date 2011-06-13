package edu.utwente.vb.symbols;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.exceptions.IllegalVariableDefinitionException;
import edu.utwente.vb.exceptions.SymbolTableException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
/**
 * A Symbol Table in the style of Aho, Sethi and Ullman - Compilers: Principles, Techniques, and Tools [pg. 85, 970]
 * @author Ties
 * @param T the custom Token type
 */
public class Env<T extends BaseTree> implements EnvApi<T>{
	private Logger log = LoggerFactory.getLogger(Env.class);
	/** The table of functions for this level */
	private final SetMultimap<String, FunctionId<T>> functions = HashMultimap.create();
	/** The table of variables for this level */
	private final Map<String, VariableId<T>> variables = Maps.newHashMap();
	/** The previous level */
	protected final Env<T> prev;
	
	/**
	 * Construct a new Environment
	 * @param p previous level
	 */
	public Env(final Env<T> p) {
		checkNotNull(p);
		this.prev = p; 
	}
	
	public Env(){
		this.prev = null;
	}
	
	/**
	 * Put a variable into the symbol table, checking if it's name is unique at the current level
	 * @throws IllegalVariableDefinitionException 
	 */
	public void put(final VariableId<T> i) throws IllegalVariableDefinitionException{
		checkNotNull(i); checkNotNull(i.getText());
		if(variables.containsKey(i.getText())){//duplicate definition in this scope level
			throw new IllegalVariableDefinitionException("duplicate definition of variable \"" + i.getText() + "\" in the current scope");
		}
		variables.put(i.getText(), i);
	}
	
	/**
	 * Put a function into the symbol table, checking if it's name is unique at the current level w/ the same type of operands
	 * @throws IllegalFunctionDefinitionException 
	 */
	public void put(final FunctionId<T> i) throws IllegalFunctionDefinitionException{
		checkNotNull(i); checkNotNull(i.getText());
		if(functions.containsKey(i.getText())){//duplicate definition in this scope level
			for(FunctionId<T> potential : functions.get(i.getText())){
				if(potential.equalsSignature(i.getText(), Type.asArray(i.getTypeParameters())))
					throw new IllegalFunctionDefinitionException("Function " + i.getText() + " with signature " + i.getTypeParameters().toString() + " is already defined in the current scope");		
			}
		}
		functions.put(i.getText(), i);
	}
	
	
	
	/**
	 * Get all the occurrences of a Id matching the given identifier
	 */
	public List<Id<T>> get(final String w){
		checkNotNull(w);
		ImmutableList.Builder<Id<T>> mappings = ImmutableList.builder();
		for(Env e = this; e != null; e = e.prev){
			mappings.addAll(e.functions.get(w));
			if(e.variables.containsKey(w))
				mappings.add((VariableId<T>)e.variables.get(w));
		}
		return mappings.build();
	}
	
	/**
	 * Get the binding occurrence of this applied occurrence
	 * @throws SymbolTableException 
	 * @require !applied.contains(Type.VOID) || applied.size() == 1
	 * 
	 * *: Invariant: *als* er een match was die gelijk was op hetzelfde niveau, was die gevonden bij put
	 * lijkt veel op get, maar kan de Set van daar niet hergebruiken, want geen garanties over order
	 * daarnaast is dit efficienter, hij springt er eerder uit
	 */
	@Override
	public FunctionId<T> apply(final String n, final Type... applied) throws SymbolTableException {
		log.debug("applyFunction {} ({})", n, Arrays.toString(applied));
		//zie (*)
		for(Env e = this; e != null; e = e.prev){
			for(FunctionId<T> id : (Set<FunctionId<T>>)e.functions.get(n)){
				if(id.equalsSignature(n, applied))
					return id;
			}
		}
		throw new SymbolTableException("No matching entry for " + n + " ("  + Arrays.toString(applied) + ")");
	}
	
	@Override
	public VariableId<T> apply(final String n) throws SymbolTableException {
		for(Env e = this; e != null; e = e.prev){
			if(e.variables.containsKey(n))
				return (VariableId<T>) e.variables.get(n);
		}
		throw new SymbolTableException("No matching entry for variable name " + n);
	}
	
}