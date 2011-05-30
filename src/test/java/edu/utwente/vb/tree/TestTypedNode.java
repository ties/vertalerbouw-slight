package edu.utwente.vb.tree;

import org.antlr.runtime.CommonToken;
import junit.framework.TestCase;
import static junit.framework.Assert.*;
import org.antlr.runtime.Token;
import org.junit.Before;
import org.junit.Test;

import edu.utwente.vb.symbols.Type;


import junit.framework.TestCase;

public class TestTypedNode{
	private Token testToken;
	private TypedNode testNode;
	
	@Before
	public void setUp() throws Exception {
		testToken = new CommonToken(0,"TestToken");
		testNode = new TypedNode(testToken);
	}
	
	
	@Test
	public void testDuplicatedTestTypeTree(){	
		assertNotNull(testNode);
		
		TypedNode t2 = testNode.dupNode();
		
		assertTrue(t2 instanceof TypedNode);
	}
	
	@Test(expected=NullPointerException.class)
	public void testTypeNotNull(){
		testNode.setType(null);
	}
	
	@Test
	public void testRemembersType(){
		assertNull(testNode.getNodeType());
		
		testNode.setType(Type.CHAR);
		
		assertEquals(testNode.getNodeType(), Type.CHAR);
	}
}
