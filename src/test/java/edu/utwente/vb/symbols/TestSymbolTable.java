package edu.utwente.vb.symbols;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.SymbolTable;

import junit.framework.TestCase;

public class TestSymbolTable extends TestCase{
	private List<VariableId> variables1 = Lists.newArrayList();
	private List<VariableId> variables2 = Lists.newArrayList();
	private static final int SCOPES = 10;
	
	/**
	 * Maak twee lijsten met VariableId's aan (variables1, variables2). De namen overlappen, maar de typen zijn anders.
	 */
	@Override
	protected void setUp() throws Exception {
		for(int i = 0; i < 20; i++){
			String varNaam = String.valueOf((char)('a' + i));
			variables1.add(new VariableId(varNaam, Type.values()[(1 + i) % Type.values().length]));
			variables2.add(new VariableId(varNaam, Type.values()[i % Type.values().length]));
		}
	}
	
	@Test
	public void testOpenClose() throws Exception {
		SymbolTable a = new SymbolTable();
				
		for(int i = 0; i < SCOPES; i++){
			assertEquals(i, a.getLevel());
			a.openScope();
		}
		
		for(int i = 0; i < SCOPES; i++){
			assertEquals(SCOPES - i, a.getLevel());
			a.closeScope();
		}
		
	}
}
