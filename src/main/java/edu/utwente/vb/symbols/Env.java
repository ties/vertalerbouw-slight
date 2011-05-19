package edu.utwente.vb.symbols;

import java.util.Map;

import org.antlr.runtime.Token;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * A Symbol Table in the style of Aho, Sethi and Ullman - Compilers: Principles, Techniques, and Tools [pg. 85, 970]
 * @author Ties
 * @param T the custom Token type
 */
public class Env implements EnvApi{
	/** The table for this level */
	private final Map<String, Id> table = Maps.newHashMap();;
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
	
	public void put(final Id i){
		checkNotNull(i); checkNotNull(i.getName());
		table.put(i.getName(), i);
	}
	
	public Id get(final String w){
		checkNotNull(w);
		for(Env e = this; e != null; e = e.prev){
			Id found = (Id)e.table.get(w);
			if(found != null)
				return found;
		}
		return null;
	}
}
