package edu.utwente.vb.symbols;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

/**
 * API voor een Symbol Table (laag)
 * @author ties
 *
 * @param <T> Type van de AST Nodes
 */
public interface EnvApi<T extends BaseTree> {
	/**
	 * Voeg een functie aan de Environment toe
	 * @param i
	 */
	public void put(final FunctionId<T> i);
	
	/**
	 * 
	 */
	public void put(final VariableId<T> var);
	
	/**
	 * Haal alle mappings van de tekst van dit ID op
	 * @param w string van het label
	 * @return Set van de Id's
	 */
	public List<Id<T>> get(final String w);
	
	/**
	 * Apply a variable
	 * @param n identifier name
	 * @param applied applied parameter types
	 * @return the id
	 */
	public VariableId<T> apply(final String n);
	
	/**
	 * Apply a function
	 * @param n function name
	 * @param applied types of the parameters
	 * @return
	 */
	public FunctionId<T> apply(final String n, final Type... applied);
}
