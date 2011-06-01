package edu.utwente.vb.symbols;

import java.util.List;
import java.util.Set;

import static edu.utwente.vb.example.util.CheckerHelper.*;
import static junit.framework.Assert.*;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.symbols.SymbolTable;

import junit.framework.TestCase;

public class TestSymbolTable{
	private List<VariableId> variables1 = Lists.newArrayList();
	private List<VariableId> variables2 = Lists.newArrayList();
	private List<FunctionId> functions1 = Lists.newArrayList();
	private List<FunctionId> functions2 = Lists.newArrayList();
	
	SymbolTable<BaseTree> tab;
	private static final int SCOPES = 10;
	
	/**
	 * Maak twee lijsten met VariableId's aan (variables1, variables2). De namen overlappen, maar de typen zijn anders.
	 */
	@Before
	public void setUp() throws Exception {
		tab = new SymbolTable<BaseTree>();
		for(int i = 0; i < 5; i++){
			String varNaam = String.valueOf((char)('a' + i));
			
			
			Token token1 = new CommonToken(0, varNaam);
			BaseTree tree = new CommonTree(token1);
			
			variables1.add(new VariableId<BaseTree>(tree, Type.values()[(1 + i) % Type.values().length]));
			variables2.add(new VariableId<BaseTree>(tree, Type.values()[i % Type.values().length]));
			
			try{
				functions1.add(createBuiltin(varNaam, Type.values()[i % Type.values().length], Type.values()[(i) % Type.values().length], Type.values()[(i) % Type.values().length]));
			} catch(IllegalFunctionDefinitionException e){
				//Een van de argumenten van de functie was void
			}
			try{
				functions2.add(createBuiltin(varNaam, Type.values()[i+1 % Type.values().length], Type.values()[(i+1) % Type.values().length], Type.values()[(i+1) % Type.values().length]));
			} catch(IllegalFunctionDefinitionException e){
				//Een van de argumenten van de functie was void
			}
		}
	}
	
	/**
	 * Add a function on two levels and make sure it's in there.
	 * @throws Exception
	 */
	@Test
	public void addFunctionTwoLevels() throws Exception{
		for(FunctionId f : functions1){
			tab.put(f);
		}
		tab.openScope();
		for(FunctionId f : functions1){
			tab.put(f);
		}
		for(FunctionId f : functions1){
			List<Id<BaseTree>> res = tab.get(f.getText());
			assertTrue(res.size() == 2);
		}
		tab.closeScope();
		for(FunctionId f : functions1){
			assertTrue(tab.get(f.getText()).size() == 1);
		}
	}
	
	@Test
	public void addFunctionMultipleArguments() throws Exception{
		for(FunctionId f : functions1){
			tab.put(f);
		}
		for(FunctionId f : functions2){
			tab.put(f);
		}
		//Alles goed, hoop ik
	}
	
	@Test(expected=SymbolTableException.class)
	public void errorRepeatedFunctionadd() throws Exception{
		tab.put(functions1.get(0));
		tab.put(functions1.get(0));
	}
	
	@Test
	public void testOpenClose() throws Exception {
		SymbolTable<BaseTree> a = new SymbolTable<BaseTree>();
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
		VariableId<BaseTree> a = variables1.get(0);
		//
		tab.put(a);
		//
		assertEquals(tab.apply(a.getText()), a);
	}
	
	@Test(expected=SymbolTableException.class)
	public void testNotInThereYet() throws Exception{
		VariableId<BaseTree> a = variables1.get(0);
		//
		assertNull(tab.apply(a.getText()));
	}
	
	@Test
	public void testMasking() throws Exception{
		//We voegen alle variabelen toe, daarna openen we een scope, voegen we alles toe
		for(VariableId<BaseTree> i : variables1){
			tab.put(i);
		}
		tab.openScope();
		for(VariableId<BaseTree> i : variables1){
			assertEquals(tab.apply(i.getText()), i);
		}
		//Nu voegen we alles toe dat masked
		for(VariableId<BaseTree> i : variables2){
			tab.put(i);
		}
		for(VariableId<BaseTree> i : variables2){
			assertEquals(tab.apply(i.getText()), i);
		}
		//Nu gaan we +1 level, zelfde result
		tab.openScope();
		for(VariableId<BaseTree> i : variables2){
			assertEquals(tab.apply(i.getText()), i);
		}
		//close, nog dezelfde
		tab.closeScope();
		for(VariableId<BaseTree> i : variables2){
			assertEquals(tab.apply(i.getText()), i);
		}
		//2e, ze zijn er nu uit en je krijgt de 1e weer
		tab.closeScope();
		for(VariableId<BaseTree> i : variables1){
			assertEquals(tab.apply(i.getText()), i);
		}
	}
	
	@Test(expected=SymbolTableException.class)
	public void testRedefineOnSameLevel(){
		tab.put(variables1.get(0));
		//
		assertEquals(tab.apply(variables1.get(0).getText()), variables1.get(0));
		//Duplicate put
		tab.put(variables1.get(0));
	}
}
