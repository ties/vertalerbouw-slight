package edu.utwente.vb.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;

import static com.google.common.base.Preconditions.checkArgument;;

public class TypedNodeAdaptor extends CommonTreeAdaptor {
	/** Duplicate a node.  This is part of the factory;
	 *	override if you want another kind of node to be built.
	 *
	 *  I could use reflection to prevent having to override this
	 *  but reflection is slow.
	 */
	public TypedNode dupNode(Object t) {
		checkArgument(t instanceof TypedNode);
		//
		if ( t==null ) return null;
		
		return ((TypedNode)t).dupNode();
	}

	public Object create(Token payload) {
		return new TypedNode(payload);
	}
}
