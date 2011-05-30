package edu.utwente.vb.symbols;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
/**
 * A Symbol Table in the style of Aho, Sethi and Ullman - Compilers: Principles, Techniques, and Tools [pg. 85, 970]
 * @author Ties
 * @param T the custom Token type
 */
public class Env<T extends BaseTree> implements EnvApi<T>{
	/** The table for this level */
	private final SetMultimap<String, Id<T>> table = HashMultimap.create(); 
	/** The previous level */
	protected final Env prev;
	
	/**
	 * Construct a new Environment
	 * @param p previous level
	 */
	public Env(final Env p) {
		checkNotNull(p);
		this.prev = p; 
	}
	
	public Env(){
		this.prev = null;
	}
	
	/**
	 * Put a token into the symbol table, checking if it's name is unique at the current level
	 */
	public void put(final Id<T> i){
		checkNotNull(i); checkNotNull(i.getText());
		if(table.containsKey(i.getText())){//duplicate definition in this scope level
			for(Id<T> potential : table.get(i.getText())){
				if(i instanceof FunctionId){
					FunctionId f = (FunctionId)i;
					if(potential.equalsSignature(f.getText(), f.getTypeParameters()))
						throw new SymbolTableException("duplicate definition for function \"" + f.getText() + "\" with signature " + f.getTypeParameters().toString() + "in the current scope");		

				} else{
					if(potential.equalsSignature(i.getText(), null))
						throw new SymbolTableException("duplicate definition for token \"" + i.getText() + "\" in the current scope");
				}
			}
		}
		table.put(i.getText(), i);
	}
	
	/**
	 * Put a function into the symbol table, checking if it's name is unique at the current level w/ the same type of opperands
	 */
	public void put(final FunctionId<T> i){
		checkNotNull(i); checkNotNull(i.getText());
		if(table.containsKey(i.getText())){//duplicate definition in this scope level
			for(Id<T> potential : table.get(i.getText())){
				if(potential.equalsSignature(i.getText(), i.getTypeParameters()))
					throw new SymbolTableException("duplicate definition for function \"" + i.getText() + "\" with signature " + i.getTypeParameters().toString() + "in the current scope");		
			}
		}
		table.put(i.getText(), i);
	}
	
	
	
	/**
	 * Get all the occurrences of a Id matching the given identifier
	 */
	public Set<Id<T>> get(final String w){
		checkNotNull(w);
		ImmutableSet.Builder<Id<T>> mappings = ImmutableSet.builder();
		for(Env e = this; e != null; e = e.prev){
			mappings.addAll(e.table.get(w));
		}
		return mappings.build();
	}
	
	/**
	 * Get the binding occurrence of this applied occurrence
	 * @require !applied.contains(Type.VOID) || applied.size() == 1
	 * 
	 * *: Invariant: *als* er een match was die gelijk was op hetzelfde niveau, was die gevonden bij put
	 * lijkt veel op get, maar kan de Set van daar niet hergebruiken, want geen garanties over order
	 * daarnaast is dit efficienter, hij springt er eerder uit
	 */
	@Override
	public Id<T> apply(String n, List<Type> applied) {
		Set<Id<T>> matches = get(n);
		//zie (*)
		for(Env e = this; e != null; e = e.prev){
			for(Id<T> id : (Set<Id<T>>)e.table.get(n)){
				if(id.equalsSignature(n, applied))
					return id;
			}
		}
		throw new SymbolTableException("No matching entry for token text " + n + " and types "  + applied.toString() + " in the current symbol table.");
	}
}