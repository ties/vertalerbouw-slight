package edu.utwente.vb.symbols;

import org.junit.Test;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import static edu.utwente.vb.example.util.CheckerHelper.*;
import static junit.framework.Assert.*;

public class TestIds {
	@Test
	public void testVarEqualsSigNull(){
		VariableId v1 = createVariableId("A", ExampleType.INT);
		assertTrue(v1.equalsSignature("A", null));
	}
	
	@Test
	public void testVarEqualsSigArray(){
		VariableId v1 = createVariableId("A", ExampleType.INT);
		assertTrue(v1.equalsSignature("A", ExampleType.asArray()));
	}
	
	
	/**
	 * Remark: equalsSignature("A", null) is tested above
	 */
	@Test
	public void testVarEqualsWrongName(){
		VariableId v1 = createVariableId("A", ExampleType.INT);
		assertFalse(v1.equalsSignature("B", null));
	}
	
	@Test
	public void testVarNotEqualsSig(){
		VariableId v1 = createVariableId("A", ExampleType.INT);
		
		assertFalse(v1.equalsSignature("v1", ExampleType.asArray(ExampleType.INT)));
	}
	
	@Test
	public void testFunctionEqualsSig() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", ExampleType.INT, ExampleType.CHAR, ExampleType.CHAR);
		
		assertTrue(f1.equalsSignature("+", ExampleType.asArray(ExampleType.CHAR, ExampleType.CHAR)));
	}
	
	@Test
	public void testFunctionNotEqualsSig() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", ExampleType.INT, ExampleType.CHAR, ExampleType.CHAR);
		
		assertFalse(f1.equalsSignature("+", ExampleType.asArray(ExampleType.INT, ExampleType.INT)));
	} 
	
	@Test
	public void testFunctionNotEqualsWrongName() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", ExampleType.INT, ExampleType.CHAR, ExampleType.CHAR);
		
		assertFalse(f1.equalsSignature("-", ExampleType.asArray(ExampleType.CHAR, ExampleType.CHAR)));
	} 
	
	@Test
	public void testUpdateType() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("/", ExampleType.UNKNOWN, ExampleType.INT, ExampleType.INT);
		assertEquals(f1.getType(), ExampleType.UNKNOWN);
		
		f1.updateType(ExampleType.CHAR);
		
		assertEquals(f1.getType(), ExampleType.CHAR);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFunctionUpdateNotUnknown() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", ExampleType.INT, ExampleType.CHAR, ExampleType.CHAR);
		
		f1.updateType(ExampleType.BOOL);
	}
	
	@Test
	public void testUpdateVariableType() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", ExampleType.UNKNOWN);
		
		assertEquals(v1.getType(), ExampleType.UNKNOWN);
		
		v1.updateType(ExampleType.INT);
		
		assertEquals(v1.getType(), ExampleType.INT);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUpdateVariableTypeNotUnknown() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", ExampleType.INT);
		v1.updateType(ExampleType.INT);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUpdateVariableTypeToUnknown() throws IllegalFunctionDefinitionException{
		VariableId v1 = createVariableId("+", ExampleType.UNKNOWN);
		v1.updateType(ExampleType.UNKNOWN);
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testFunctionUpdateToUnknown() throws IllegalFunctionDefinitionException{
		FunctionId f1 = createBuiltin("+", ExampleType.UNKNOWN, ExampleType.CHAR, ExampleType.CHAR);
		
		f1.updateType(ExampleType.UNKNOWN);
	}
}
