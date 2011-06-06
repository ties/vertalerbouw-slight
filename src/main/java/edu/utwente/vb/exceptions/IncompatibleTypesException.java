package edu.utwente.vb.exceptions;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;


public class IncompatibleTypesException extends SymbolTableException {

	/**
	 * Exception thrown when operation is called on two variabeles of incompatible types
	 */
	
	public IncompatibleTypesException(String reason) {
		super(reason);
	}
	
	public IncompatibleTypesException(Tree tree, String reason) {
		super(tree, reason);
	}

}
