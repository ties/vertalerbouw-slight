package edu.utwente.vb.symbols;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestType {
	@Test
	public void testTypeByName(){
		assertEquals(ExampleType.VOID, ExampleType.byName("void"));
		assertEquals(ExampleType.INT, ExampleType.byName("int"));
		assertEquals(ExampleType.CHAR, ExampleType.byName("char"));
		assertEquals(ExampleType.STRING, ExampleType.byName("string"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnknownType(){
		ExampleType.byName("UNKNOWN_ERROR");
	}
	
	@Test
	public void testIsInteger(){
		assertTrue(ExampleType.isNumeric(ExampleType.INT));
		assertFalse(ExampleType.isNumeric(ExampleType.BOOL));
		assertFalse(ExampleType.isNumeric(ExampleType.CHAR));
		assertFalse(ExampleType.isNumeric(ExampleType.STRING));
		assertFalse(ExampleType.isNumeric(ExampleType.UNKNOWN));
	}
	
	@Test
	public void testTypeAsArray(){
		ExampleType[] asArray = ExampleType.asArray(ExampleType.VOID, ExampleType.INT, ExampleType.STRING, ExampleType.UNKNOWN, ExampleType.CHAR, ExampleType.BOOL);
		ExampleType[] asArrayFromList = ExampleType.asArray(Lists.newArrayList(ExampleType.VOID, ExampleType.INT, ExampleType.STRING, ExampleType.UNKNOWN, ExampleType.CHAR, ExampleType.BOOL));
		assertTrue(Arrays.deepEquals(asArray, asArrayFromList));
	}
}

