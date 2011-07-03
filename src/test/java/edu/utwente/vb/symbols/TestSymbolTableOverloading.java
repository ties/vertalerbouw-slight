package edu.utwente.vb.symbols;

import static edu.utwente.vb.example.util.CheckerHelper.createBuiltin;
import static edu.utwente.vb.example.util.CheckerHelper.createVariableId;

import org.junit.Before;
import org.junit.Test;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.exceptions.IllegalVariableDefinitionException;
import edu.utwente.vb.tree.TypedNode;

public class TestSymbolTableOverloading {
	private SymbolTable<TypedNode> symtab;
	
	@Before
	public void setup(){
		symtab = new SymbolTable<TypedNode>();
	}
	
	@Test
	public void testVariableAndFunction() throws IllegalFunctionDefinitionException, IllegalVariableDefinitionException{
		VariableId<TypedNode> t1 = createVariableId("a", ExampleType.INT);
		FunctionId<TypedNode> f1 = createBuiltin("a", ExampleType.INT, ExampleType.INT, ExampleType.INT);
		
		symtab.put(t1);
		symtab.put(f1);
	}
	
	@Test
	public void testVariableFunctions() throws IllegalFunctionDefinitionException{
		FunctionId<TypedNode> f1 = createBuiltin("a", ExampleType.INT, ExampleType.INT, ExampleType.INT);
		FunctionId<TypedNode> f2 = createBuiltin("a", ExampleType.INT, ExampleType.INT, ExampleType.CHAR);
		
		symtab.put(f1);
		symtab.put(f2);
	} 
	
	@Test(expected=IllegalVariableDefinitionException.class)
	public void testOverlappingVariables() throws IllegalVariableDefinitionException{
		VariableId<TypedNode> v1 = createVariableId("a", ExampleType.INT);
		VariableId<TypedNode> v2 = createVariableId("a", ExampleType.CHAR);
		
		symtab.put(v1);
		symtab.put(v2);
	}
}
