package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

import edu.utwente.vb.symbols.Type;

/**
 * Node type used for applied occurences of variables. Can hold a reference to the node which binds this variable.
 * 
 * @author Niek Tax
 *
 */

public class AppliedOccurrenceNode extends TypedNode{
	private TypedNode bindingNode;

	public AppliedOccurrenceNode(AppliedOccurrenceNode node) {
		super(node);
		this.bindingNode = node.bindingNode;
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
	public Type getNodeType() {
		return bindingNode != null ? bindingNode.getNodeType() : null;
	}
	
	@Override
	public void setType(Type nodeType) {
		throw new IllegalStateException("Type of AppliedOccurrenceNode can not be set");
	}
	
	@Override
	public void setNodeType(Type nodeType) {
		throw new IllegalStateException("Type of AppliedOccurrenceNode can not be set");
	}
}
