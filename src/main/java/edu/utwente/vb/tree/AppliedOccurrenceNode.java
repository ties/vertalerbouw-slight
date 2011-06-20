package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

import com.google.common.base.Objects;

import edu.utwente.vb.symbols.ExampleType;

/**
 * Node type used for applied occurences of variables. Can hold a reference to the node which binds this variable.
 * 
 * @author Niek Tax
 *
 */

public class AppliedOccurrenceNode extends TypedNode{
	private TypedNode bindingNode;
	private int ttype;

	/*
	 * Extra constructor volgens
	 * http://www.antlr.org/wiki/display/ANTLR3/Tree+construction
	 */
	public AppliedOccurrenceNode(int ttype, Token t) {
		super(t);
		this.ttype = ttype;
	}
	
	public AppliedOccurrenceNode(int ttype) {
		super();
		this.ttype = ttype;
	}

	
	public AppliedOccurrenceNode(AppliedOccurrenceNode node) {
		super(node);
		this.bindingNode = node.bindingNode;
		this.ttype = node.ttype;
	}

	public AppliedOccurrenceNode(Token t) {
		super(t);
	}
	
	@Override
	public AppliedOccurrenceNode dupNode() {
		return new AppliedOccurrenceNode(this);
	}
	
	
	public void setBindingNode(TypedNode ct){
		bindingNode = checkNotNull(ct);
	}
	
	public TypedNode getBindingNode(){
		return bindingNode;
	}
	
	@Override
	public ExampleType getNodeType() {
		return bindingNode != null ? bindingNode.getNodeType() : null;
	}
	
	/**
	 * Set het node type.
	 * Het node type van een Applied Occurrence kan alleen geset worden als de binding occurrence nog geen type heeft.
	 */
	@Override
	public void setNodeType(ExampleType nodeType) {
		if(bindingNode == null || (bindingNode.getNodeType() != ExampleType.UNKNOWN && bindingNode.getNodeType() != null))
			throw new IllegalStateException("Type of AppliedOccurrenceNode can not be set");
		bindingNode.setNodeType(nodeType);
	}
	
	
	@Override
	public String toString() {
		return Objects.toStringHelper(AppliedOccurrenceNode.class).add("ttype", ttype).add("binding", bindingNode).add("returnType", getNodeType()).toString();
	}
}
