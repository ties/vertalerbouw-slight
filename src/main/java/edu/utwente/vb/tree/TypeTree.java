package edu.utwente.vb.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.utwente.vb.symbols.Type;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A tree node that saves it's type
 * @author ties
 *
 */
public class TypeTree extends CommonTree {
	private Type type;
	
	public TypeTree() { }
	
	public TypeTree(TypeTree node) {
		super(node);
	}

	public TypeTree(Token t) {
		super(t);
	}
	
	@Override
	public TypeTree dupNode() {
		return new TypeTree(this);
	}
	
	/**
	 * Get the node type
	 * @return Type of this node (or null)
	 */
	public Type getNodeType(){
		return type;
	}
	
	/**
	 * Set the node type
	 * @param type type of the node
	 * @require type != null
	 */
	public void setType(Type type) {
		checkNotNull(type);
		this.type = type;
	}

}
