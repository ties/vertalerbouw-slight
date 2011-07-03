package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;

import edu.utwente.vb.symbols.FunctionId;

public class FunctionNode extends TypedNode{
	private FunctionId<TypedNode> bound;
	private int ttype;
	
	/*
	 * Extra constructor volgens
	 * http://www.antlr.org/wiki/display/ANTLR3/Tree+construction
	 */
	public FunctionNode(int ttype, Token t) {
		super(t);
		this.ttype = ttype;
	}
	
	public FunctionNode(int ttype) {
		super();
		this.ttype = ttype;
	}

	
	public FunctionNode(FunctionNode node) {
		super(node);
		this.ttype = node.ttype;
		this.bound = node.bound;
	}

	public FunctionNode(Token t) {
		super(t);
	}
	
	@Override
	public FunctionNode dupNode() {
		return new FunctionNode(this);
	}
	
	public FunctionId<TypedNode> getBoundMethod(){
		return bound;
	}

	public void setBoundMethod(FunctionId<TypedNode> arg){
		assert this.bound == null;
		this.bound = checkNotNull(arg);
	}
}
