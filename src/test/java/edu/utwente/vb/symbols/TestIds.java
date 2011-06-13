package edu.utwente.vb.symbols;

import org.junit.Test;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import static edu.utwente.vb.example.util.CheckerHelper.*;
import static junit.framework.Assert.*;

public class TestIds {
	@Test
	public void testVarEqualsSigNull(){
		VariableId v1 = createVariableId("A", Type.INT);
		assertTrue(v1.equalsSignature("A", null));
	}
	
	@Test
	public void testVarEqualsSigArray(){
		VariableId v1 = createVariableId("A", Type.INT);
		assertTrue(v1.equalsSignature("A", Type.asArray()));
	}
	
	
	/**
	 * Remark: equalsSignature("A", null) is tested above
	 */
	@Test
	public void testVarEqualsWrongName(){
		VariableId v1 = createVariableId("A", Type.INT);
		assertFalse(v1.equalsSignature("B", null));
	}
	
	@Test
	public void testVarNotEqualsSig(){
		VariableId v1 = createVariableId("A", Type.INT);
		
		assertFalse(v1.equalsSignature("v1", Type.asArray(Type.INT)));
	}
	
	@Test
	public void testFunctionEqualsSig() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertTrue(f1.equalsSignature("+", Type.asArray(Type.CHAR, Type.CHAR)));
	}
	
	@Test
	public void testFunctionNotEqualsSig() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertFalse(f1.equalsSignature("+", Type.asArray(Type.INT, Type.INT)));
	} 
	
	@Test
	public void testFunctionNotEqualsWrongName() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertFalse(f1.equalsSignature("-", Type.asArray(Type.CHAR, Type.CHAR)));
	} 
	
	@Test
	public void testUpdateType() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("/", Type.UNKNOWN, Type.INT, Type.INT);
		assertEquals(f1.getType(), Type.UNKNOWN);
		
		f1.updateType(Type.CHAR);
		
		assertEquals(f1.getType(), Type.CHAR);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFunctionUpdateNotUnknown() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		f1.updateType(Type.BOOL);
	}
	
	@Test
	public void testUpdateVariableType() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", Type.UNKNOWN);
		
		assertEquals(v1.getType(), Type.UNKNOWN);
		
		v1.updateType(Type.INT);
		
		assertEquals(v1.getType(), Type.INT);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUpdateVariableTypeNotUnknown() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", Type.INT);
		v1.updateType(Type.INT);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUpdateVariableTypeToUnknown() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", Type.UNKNOWN);
		v1.updateType(Type.UNKNOWN);
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testFunctionUpdateToUnknown() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", Type.UNKNOWN, Type.CHAR, Type.CHAR);
		
		f1.updateType(Type.UNKNOWN);
	}
}
