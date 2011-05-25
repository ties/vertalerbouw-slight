package edu.utwente.vb.tree;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.junit.Test;

import edu.utwente.vb.symbols.Type;


import junit.framework.TestCase;

public class TestTypeTree extends TestCase{
	private Token testToken;
	private TypeTree testNode;
	
	@Override
	protected void setUp() throws Exception {
		testToken = new CommonToken(0,"TestToken");
		testNode = new TypeTree(testToken);
	}
	
	
	@Test
	public void testDuplicatedTestTypeTree(){	
		assertNotNull(testNode);
		
		TypeTree t2 = testNode.dupNode();
		
		assertTrue(t2 instanceof TypeTree);
	}
	
	@Test(expected=IllegalArgumentException.class)
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
