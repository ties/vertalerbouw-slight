package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

public class BindingOccurrenceNode extends TypedNode{
	public enum VariableType { LOCAL, FIELD, ARGUMENT }
	private VariableType type;
	private int varNumber = 0;
	private int ttype;
	
	/*
	 * Extra constructor volgens
	 * http://www.antlr.org/wiki/display/ANTLR3/Tree+construction
	 */
	public BindingOccurrenceNode(int ttype, Token t) {
		super(t);
		this.ttype = ttype;
		
	}
	
	public BindingOccurrenceNode(int ttype) {
		super();
		this.ttype = ttype;
	}

	
	public BindingOccurrenceNode(BindingOccurrenceNode node) {
		super(node);
		this.ttype = node.ttype;
		this.varNumber = node.varNumber;
		this.type = node.type;
	}

	public BindingOccurrenceNode(Token t) {
		super(t);
	}
	
	@Override
	public BindingOccurrenceNode dupNode() {
		return new BindingOccurrenceNode(this);
	}
	
	public void setNumber(int num){
		varNumber = num;
	}
	
	public int getNumber(){
		assert varNumber >= 0;
		return varNumber;
	}
	
	public VariableType getVariableType(){
		return type;
	}
	
	public void setVariableType(VariableType type){
		this.type = checkNotNull(type);
	}
}
