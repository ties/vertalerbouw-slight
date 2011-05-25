package edu.utwente.vb.symbols;

import java.util.Map;

import org.antlr.runtime.Token;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
/**
 * A Symbol Table in the style of Aho, Sethi and Ullman - Compilers: Principles, Techniques, and Tools [pg. 85, 970]
 * @author Ties
 * @param T the custom Token type
 */
public class Env<T extends Token> implements EnvApi<T>{
	/** The table for this level */
	private final Map<String, Id<T>> table = Maps.newHashMap();
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
		if(!table.containsKey(i.getText())){//duplicate definition in this scope level
			table.put(i.getText(), i);
		} else {
			throw new SymbolTableException("duplicate definition for token \"" + i.getText() + "\" in the current scope");			
		}
	}
	
	public Id<T> get(final String w){
		checkNotNull(w);
		for(Env e = this; e != null; e = e.prev){
			Id<T> found = (Id<T>)e.table.get(w);
			if(found != null)
				return found;
		}
		return null;
	}
}
