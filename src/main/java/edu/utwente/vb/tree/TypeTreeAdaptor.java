package edu.utwente.vb.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;

import static com.google.common.base.Preconditions.checkArgument;;

public class TypeTreeAdaptor extends CommonTreeAdaptor {
	/** Duplicate a node.  This is part of the factory;
	 *	override if you want another kind of node to be built.
	 *
	 *  I could use reflection to prevent having to override this
	 *  but reflection is slow.
	 */
	public TypeTree dupNode(Object t) {
		checkArgument(t instanceof TypeTree);
		//
		if ( t==null ) return null;
		
		return ((TypeTree)t).dupNode();
	}

	public Object create(Token payload) {
		return new TypeTree(payload);
	}
}
