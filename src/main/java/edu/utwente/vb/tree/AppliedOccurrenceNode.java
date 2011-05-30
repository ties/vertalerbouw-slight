package edu.utwente.vb.tree;

import org.antlr.runtime.Token;

/**
 * Node type used for applied occurences of variables. Can hold a reference to the node which binds this variable.
 * 
 * @author Niek Tax
 *
 */

public class AppliedOccurrenceNode extends TypedNode{

	private TypedNode bindingNode;
	
	public AppliedOccurrenceNode(){
		super();
	}
	
	public AppliedOccurrenceNode(TypedNode ct){
		super(ct);
		bindingNode = ct;
	}
	
	public AppliedOccurrenceNode(Token t){
		super(t);
	}
	
	public void setBindingNode(TypedNode ct){
		bindingNode = ct;
	}
	
	public TypedNode getBindingNode(){
		return bindingNode;
	}
}
