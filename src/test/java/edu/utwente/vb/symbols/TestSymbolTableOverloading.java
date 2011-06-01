package edu.utwente.vb.symbols;

import org.junit.Before;
import org.junit.Test;

import edu.utwente.vb.tree.TypedNode;
import static edu.utwente.vb.example.util.CheckerHelper.*;

public class TestSymbolTableOverloading {
	private SymbolTable<TypedNode> symtab;
	
	@Before
	public void setup(){
		symtab = new SymbolTable<TypedNode>();
	}
	
	@Test
	public void testVariableAndFunction(){
		VariableId<TypedNode> t1 = createVariableId("a", Type.INT);
		FunctionId<TypedNode> f1 = createBuiltin("a", Type.INT, Type.INT, Type.INT);
		
		symtab.put(t1);
		symtab.put(f1);
	}
	
	@Test
	public void testVariableFunctions(){
		FunctionId<TypedNode> f1 = createBuiltin("a", Type.INT, Type.INT, Type.INT);
		FunctionId<TypedNode> f2 = createBuiltin("a", Type.INT, Type.INT, Type.CHAR);
		
		symtab.put(f1);
		symtab.put(f2);
	} 
	
	@Test
	public void testOverlappingVariables(){
		VariableId<TypedNode> v1 = createVariableId("a", Type.INT);
		VariableId<TypedNode> v2 = createVariableId("a", Type.CHAR);
		
		symtab.put(v1);
		symtab.put(v2);
	}
}
