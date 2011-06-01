package edu.utwente.vb.exceptions;

import edu.utwente.vb.symbols.SymbolTableException;

public class IllegalFunctionDefinitionException extends SymbolTableException {
	public IllegalFunctionDefinitionException(String reason) {
		super(reason);
	}
}
