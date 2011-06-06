package edu.utwente.vb.exceptions;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import com.google.common.base.Objects;

public class SymbolTableException extends RecognitionException{
	private final String reason;
	
	public SymbolTableException(String rsn){
		super();
		this.reason = rsn;
	}
	
	public SymbolTableException(Tree tree, String message) {
		super();
		this.reason = tree.getText() + " (" +
			tree.getLine() + ":" + 
			tree.getCharPositionInLine() + ") " +
			message;
	}

	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("reason", reason).toString();
	}
}

