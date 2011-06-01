package edu.utwente.vb.symbols;

import org.junit.Test;
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
	public void testFunctionEqualsSig(){
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertTrue(f1.equalsSignature("+", Type.asArray(Type.CHAR, Type.CHAR)));
	}
	
	@Test
	public void testFunctionNotEqualsSig(){
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertFalse(f1.equalsSignature("+", Type.asArray(Type.INT, Type.INT)));
	} 
	
	@Test
	public void testFunctionNotEqualsWrongName(){
		FunctionId f1 = createBuiltin("+", Type.INT, Type.CHAR, Type.CHAR);
		
		assertFalse(f1.equalsSignature("-", Type.asArray(Type.CHAR, Type.CHAR)));
	} 
}
