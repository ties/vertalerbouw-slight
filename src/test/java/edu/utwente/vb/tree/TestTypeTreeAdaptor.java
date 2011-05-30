package edu.utwente.vb.tree;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import static junit.framework.Assert.*;

public class TestTypeTreeAdaptor{
	private Token testToken;
	private TypedNode testNode;
	private TypeTreeAdaptor testAdaptor;

	@Before
	public void setUp() throws Exception {
		testToken = new CommonToken(0,"TestToken");
		testNode = new TypedNode(testToken);
		testAdaptor = new TypeTreeAdaptor();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExceptionOnWrongNodeType(){
		CommonTree node = new CommonTree(testToken);
		testAdaptor.dupNode(node);
	}
	
	@Test
	public void testCorrectNodeType(){
		assertTrue(testAdaptor.create(testToken) instanceof TypedNode);
	}
}
