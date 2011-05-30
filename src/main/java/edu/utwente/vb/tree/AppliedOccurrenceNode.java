package edu.utwente.vb.tree;

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
		super();
		bindingNode = ct;
	}
	
	public void setBindingNode(TypedNode ct){
		bindingNode = ct;
	}
	
	public TypedNode getBindingNode(){
		return bindingNode;
	}
}
