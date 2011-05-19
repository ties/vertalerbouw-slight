package edu.utwente.vb.symbols;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import junit.framework.TestCase;

public class TestSymbolTable extends TestCase{
	private List<VariableId> variables1 = Lists.newArrayList();
	private List<VariableId> variables2 = Lists.newArrayList();
	
	/**
	 * Maak twee lijsten met VariableId's aan (variables1, variables2). De namen overlappen, maar de typen zijn anders.
	 */
	@Override
	protected void setUp() throws Exception {
		for(int i = 0; i < 20; i++){
			String varNaam = String.valueOf((char)('a' + i));
			variables1.add(new VariableId(varNaam, Type.values()[1 + i % Type.values().length]));
			variables2.add(new VariableId(varNaam, Type.values()[i % Type.values().length]));
		}
	}
	
	@Test
	public void testOpenClose() throws Exception {
		SymbolTable a = new SymbolTable();
		assertEquals(a.getLevel(), 0);
		
		for(int i = 0; i < 10; i++){
			a.openScope();
			assertEquals(i, a.getLevel());
		}
		
		for(int i = 0; i < 10; i++){
			a.closeScope();
			assertEquals(10 - i, a.getLevel());
		}
		
		assertEquals(a.getLevel(), 0);
	}
}
