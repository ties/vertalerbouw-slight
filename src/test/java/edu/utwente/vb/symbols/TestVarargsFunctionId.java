package edu.utwente.vb.symbols;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;
import static org.junit.Assert.*;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;

import static edu.utwente.vb.example.util.CheckerHelper.*;

public class TestVarargsFunctionId {
	@Test
	public void testDoesMatch() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT);
		
		assertTrue(v1.equalsSignature("read", ExampleType.INT, ExampleType.INT));
		assertTrue(v1.equalsSignature("read", ExampleType.INT));
	}
	
	@Test
	public void testDoesNotMatch() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT);
		
		assertFalse(v1.equalsSignature("read", ExampleType.CHAR));		
	}
	
	@Test
	public void testCanMatchMultiple() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT, ExampleType.STRING);
		
		assertTrue(v1.equalsSignature("read", ExampleType.STRING, ExampleType.INT));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testCanNotGetTypeParameters() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT, ExampleType.STRING);
		
		v1.getTypeParameters();
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testCanNotUpdateType() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT, ExampleType.STRING);
		
		v1.updateType(null);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testCanNotConvert() throws IllegalFunctionDefinitionException{
		VarArgsFunctionId<CommonTree> v1 = new VarArgsFunctionId<CommonTree>(byToken("read", ExampleType.INT), ExampleType.INT, ExampleType.INT, ExampleType.STRING);
		
		v1.asMethod();
	}
}
