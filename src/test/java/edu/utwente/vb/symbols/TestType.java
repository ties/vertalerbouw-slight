package edu.utwente.vb.symbols;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.Lists;

import static junit.framework.Assert.*;

public class TestType {
	@Test
	public void testTypeByName(){
		assertEquals(Type.VOID, Type.byName("void"));
		assertEquals(Type.INT, Type.byName("int"));
		assertEquals(Type.CHAR, Type.byName("char"));
		assertEquals(Type.STRING, Type.byName("string"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnknownType(){
		Type.byName("UNKNOWN_ERROR");
	}
	
	@Test
	public void testIsInteger(){
		assertTrue(Type.isNumeric(Type.INT));
		assertFalse(Type.isNumeric(Type.BOOL));
		assertFalse(Type.isNumeric(Type.CHAR));
		assertFalse(Type.isNumeric(Type.STRING));
		assertFalse(Type.isNumeric(Type.FUNCTION));
		assertFalse(Type.isNumeric(Type.UNKNOWN));
	}
	
	@Test
	public void testTypeAsArray(){
		Type[] asArray = Type.asArray(Type.VOID, Type.INT, Type.STRING, Type.UNKNOWN, Type.CHAR, Type.BOOL);
		Type[] asArrayFromList = Type.asArray(Lists.newArrayList(Type.VOID, Type.INT, Type.STRING, Type.UNKNOWN, Type.CHAR, Type.BOOL));
		assertTrue(Arrays.deepEquals(asArray, asArrayFromList));
	}
}

