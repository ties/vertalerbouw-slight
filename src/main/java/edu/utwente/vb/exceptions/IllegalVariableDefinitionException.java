package edu.utwente.vb.exceptions;

import edu.utwente.vb.symbols.SymbolTableException;

public class IllegalVariableDefinitionException extends SymbolTableException {
	public IllegalVariableDefinitionException(String rsn){
		super(rsn);
	}
}
