package edu.utwente.vb.symbols;

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
	 * Voeg een ID aan de environment toe
	 * @param i
	 */
	public void put(final Id<T> i);
	
	/**
	 * Haal alle mappings van de tekst van dit ID op
	 * @param w string van het label
	 * @return Set van de Id's
	 */
	public Set<Id<T>> get(final String w);
	/**
	 * Apply a variable
	 * @param n identifier name
	 * @param applied applied parameter types
	 * @return the id
	 */
	public Id<T> apply(String n, Type... applied);
}
