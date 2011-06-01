package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkArgument;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;

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
	
	
	/**
	 * errorNode moet overschreven worden om subtiele, rare fouten te voorkomen.
	 * De oplossing staat op http://www.antlr.org/wiki/display/ANTLR3/Tree+construction
	 */
	@Override
	public TypedErrorNode errorNode(TokenStream input, Token start, Token stop,
			RecognitionException e) {
		return new TypedErrorNode(input, start, stop, e);
	}
}
