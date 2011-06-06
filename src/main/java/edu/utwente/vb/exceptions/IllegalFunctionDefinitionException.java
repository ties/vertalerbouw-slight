package edu.utwente.vb.exceptions;


public class IllegalFunctionDefinitionException extends SymbolTableException {
	public IllegalFunctionDefinitionException(String reason) {
		super(reason);
	}
}
