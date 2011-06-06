package edu.utwente.vb.tree;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.junit.Before;
import org.junit.Test;

import edu.utwente.vb.TestUtilities;
import edu.utwente.vb.example.util.CheckerHelper;
import edu.utwente.vb.symbols.Type;

public class TestAppliedOccurrenceNode{
	private Token testToken;
	private AppliedOccurrenceNode testNode;
	
	@Before
	public void setUp() throws Exception {
		testToken = new CommonToken(0,"TestToken");
		testNode = new AppliedOccurrenceNode(testToken);
	}
	
	
	@Test
	public void testDuplicatedTestTypeTree(){	
		assertNotNull(testNode);
		
		assertTrue(testNode instanceof AppliedOccurrenceNode);
		
		TypedNode t2 = testNode.dupNode();
		
		assertTrue(t2 instanceof AppliedOccurrenceNode);
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void testCanNotSetType(){
		assertNull(testNode.getNodeType());
		
		testNode.setNodeType(Type.CHAR);
	}
	
	public void testCanShowReferencedType(){
		assertNull(testNode.getNodeType());
		
		TypedNode other = CheckerHelper.byToken("AAAA");
		other.setType(Type.CHAR);
		
		testNode.setBindingNode(other);
		
		assertEquals(testNode.getNodeType(), Type.CHAR);
	}
	
	@Test
	public void testBindingOccurrence(){
		assertNull(testNode.getBindingNode());
		TypedNode other = CheckerHelper.byToken("AAAA");
		
		testNode.setBindingNode(other);
		
		assertEquals(testNode.getBindingNode(), other);
	}

	@Test(expected=NullPointerException.class)
	public void testBindingOccurrenceNotNull(){
		testNode.setBindingNode(null);
	}
}