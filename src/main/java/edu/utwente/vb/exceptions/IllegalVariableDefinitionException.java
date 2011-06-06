package edu.utwente.vb.exceptions;


public class IllegalVariableDefinitionException extends SymbolTableException {
	public IllegalVariableDefinitionException(String rsn){
		super(rsn);
	}
}
