package edu.utwente.vb.symbols;

import java.util.List;
import static junit.framework.Assert.*;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.SymbolTable;

import junit.framework.TestCase;

public class TestSymbolTable{
	private List<VariableId> variables1 = Lists.newArrayList();
	private List<VariableId> variables2 = Lists.newArrayList();
	SymbolTable<Token> tab;
	private static final int SCOPES = 10;
	
	/**
	 * Maak twee lijsten met VariableId's aan (variables1, variables2). De namen overlappen, maar de typen zijn anders.
	 */
	@Before
	public void setUp() throws Exception {
		tab = new SymbolTable<Token>();
		for(int i = 0; i < 5; i++){
			String varNaam = String.valueOf((char)('a' + i));
			
			Token token1 = new CommonToken(0, varNaam);
			
			variables1.add(new VariableId<Token>(token1, Type.values()[(1 + i) % Type.values().length]));
			variables2.add(new VariableId<Token>(token1, Type.values()[i % Type.values().length]));
		}
	}
	
	@Test
	public void testOpenClose() throws Exception {
		SymbolTable<Token> a = new SymbolTable<Token>();
		assertEquals(a.getLevel(), 0);
		//
		a.openScope();
		assertEquals(a.getLevel(), 1);
		//
		a.closeScope();
		assertEquals(a.getLevel(), 0);
	}
	
	@Test(expected=SymbolTableException.class)
	public void testCloseLevelZero(){
		// nu de error bij het closen van een scope op level 0
		tab.closeScope();
	}
	
	@Test
	public void testSimpleput() throws Exception {
		VariableId<Token> a = variables1.get(0);
		//
		assertNull(tab.get(a.getText()));
		//
		tab.put(a);
		//
		assertEquals(tab.get(a.getText()), a);
	}
	
	@Test
	public void testMasking() throws Exception{
		//We voegen alle variabelen toe, daarna openen we een scope, voegen we alles toe
		for(VariableId<Token> i : variables1){
			tab.put(i);
		}
		tab.openScope();
		for(VariableId<Token> i : variables1){
			assertEquals(tab.get(i.getText()), i);
		}
		//Nu voegen we alles toe dat masked
		for(VariableId<Token> i : variables2){
			tab.put(i);
		}
		for(VariableId<Token> i : variables2){
			assertEquals(tab.get(i.getText()), i);
		}
		//Nu gaan we +1 level, zelfde result
		tab.openScope();
		for(VariableId<Token> i : variables2){
			assertEquals(tab.get(i.getText()), i);
		}
		//close, nog dezelfde
		tab.closeScope();
		for(VariableId<Token> i : variables2){
			assertEquals(tab.get(i.getText()), i);
		}
		//2e, ze zijn er nu uit en je krijgt de 1e weer
		tab.closeScope();
		for(VariableId<Token> i : variables1){
			assertEquals(tab.get(i.getText()), i);
		}
	}
	
	@Test(expected=SymbolTableException.class)
	public void testRedefineOnSameLevel(){
		tab.put(variables1.get(0));
		//
		assertEquals(tab.get(variables1.get(0).getText()), variables1.get(0));
		//Duplicate put
		tab.put(variables1.get(0));
	}
}
