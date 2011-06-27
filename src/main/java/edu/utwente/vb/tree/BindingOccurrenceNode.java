package edu.utwente.vb.tree;

import org.antlr.runtime.Token;

public class BindingOccurrenceNode extends TypedNode{
	private int varNumber = -1;
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
	
	public boolean isLocal(){
		return varNumber >= 0;
	}
}
