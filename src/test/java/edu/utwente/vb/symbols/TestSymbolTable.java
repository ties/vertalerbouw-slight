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
		for(int i = 0; i < 5; i++){
			String varNaam = String.valueOf((char)('a' + i));
			variables1.add(new VariableId(varNaam, Type.values()[(1 + i) % Type.values().length]));
			variables2.add(new VariableId(varNaam, Type.values()[i % Type.values().length]));
		}
	}
	
	@Test
	public void testOpenClose() throws Exception {
		SymbolTable a = new SymbolTable();
		assertEquals(a.getLevel(), 0);
		//
		a.openScope();
		assertEquals(a.getLevel(), 1);
		//
		a.closeScope();
		assertEquals(a.getLevel(), 0);
	}
	
	@Test
	public void testCloseLevelZero(){
		SymbolTable a = new SymbolTable();
		// nu de error bij het closen van een scope op level 0
		boolean error = false;
		try{
			a.closeScope();
		} catch(IllegalArgumentException e){
			error = true;
		}
		assertTrue(error);

	}
	
	@Test
	public void testSimpleput() throws Exception {
		VariableId a = variables1.get(0);
		//
		SymbolTable tab = new SymbolTable();
		//
		assertNull(tab.get(a.getName()));
		//
		tab.put(a);
		//
		assertEquals(tab.get(a.getName()), a);
	}
	
	@Test
	public void testMasking() throws Exception{
		SymbolTable tab = new SymbolTable();
		//We voegen alle variabelen toe, daarna openen we een scope, voegen we alles toe
		for(VariableId i : variables1){
			tab.put(i);
		}
		tab.openScope();
		for(VariableId i : variables1){
			assertEquals(tab.get(i.getName()), i);
		}
		//Nu voegen we alles toe dat masked
		for(VariableId i : variables2){
			tab.put(i);
		}
		for(VariableId i : variables2){
			assertEquals(tab.get(i.getName()), i);
		}
		//Nu gaan we +1 level, zelfde result
		tab.openScope();
		for(VariableId i : variables2){
			assertEquals(tab.get(i.getName()), i);
		}
		//close, nog dezelfde
		tab.closeScope();
		for(VariableId i : variables2){
			assertEquals(tab.get(i.getName()), i);
		}
		//2e, ze zijn er nu uit en je krijgt de 1e weer
		tab.closeScope();
		for(VariableId i : variables1){
			assertEquals(tab.get(i.getName()), i);
		}
	}
}
