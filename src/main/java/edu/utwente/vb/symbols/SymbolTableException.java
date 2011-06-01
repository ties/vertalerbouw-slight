package edu.utwente.vb.symbols;

import org.antlr.runtime.RecognitionException;

import com.google.common.base.Objects;

public class SymbolTableException extends RuntimeException {
	private final String reason;
	
	public SymbolTableException(String rsn){
		super();
		this.reason = rsn;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("reason", reason).toString();
	}
}