package edu.utwente.vb.symbols;

import static edu.utwente.vb.example.util.CheckerHelper.byToken;
import static edu.utwente.vb.example.util.CheckerHelper.createBuiltin;
import static edu.utwente.vb.example.util.CheckerHelper.createFunctionId;
import static edu.utwente.vb.example.util.CheckerHelper.createVariableId;
import static edu.utwente.vb.symbols.ExampleType.BOOL;
import static edu.utwente.vb.symbols.ExampleType.CHAR;
import static edu.utwente.vb.symbols.ExampleType.INT;
import static edu.utwente.vb.symbols.ExampleType.STRING;

import java.util.Set;

import org.objectweb.asm.Type;

import com.google.common.collect.ImmutableSet;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.symbols.Id.IdType;
import edu.utwente.vb.tree.FunctionNode;
import edu.utwente.vb.tree.TypedNode;

public class Prelude {
	public final Set<FunctionId<TypedNode>> builtins;
	
	@SuppressWarnings("unchecked")
	public Prelude() throws IllegalFunctionDefinitionException{
		ImmutableSet.Builder<FunctionId<TypedNode>> builder = ImmutableSet.builder();
		//Voeg operatoren toe
		// apply/becomes varianten
		builder.add(createBuiltin("=", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("=", INT, INT, INT));
		builder.add(createBuiltin("=", CHAR, CHAR, CHAR));
		builder.add(createBuiltin("=", STRING, STRING, STRING));
		// boolean
		builder.add(createBuiltin("==", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("!=", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("and", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("or", BOOL, BOOL, BOOL));
		builder.add(createFunctionId("!", ExampleType.BOOL, createVariableId("rhs", ExampleType.BOOL)));
		// int
		builder.add(createBuiltin("+", INT, INT, INT));
		builder.add(createBuiltin("-", INT, INT, INT));
		builder.add(createBuiltin("*", INT, INT, INT));
		builder.add(createBuiltin("/", INT, INT, INT));
		builder.add(createBuiltin("%", INT, INT, INT));
		// boolean + int
		builder.add(createBuiltin("==", BOOL, INT, INT));
		builder.add(createBuiltin("!=", BOOL, INT, INT));
		builder.add(createBuiltin("<", BOOL, INT, INT));
		builder.add(createBuiltin("<=", BOOL, INT, INT));
		builder.add(createBuiltin(">", BOOL, INT, INT));
		builder.add(createBuiltin(">=", BOOL, INT, INT));
		// string -> tijdelijk met +..
		builder.add(createBuiltin("+", STRING, STRING, STRING));
		builder.add(createBuiltin("+", STRING, STRING, CHAR));
		builder.add(createBuiltin("+", STRING, STRING, INT));
		builder.add(createBuiltin("+", STRING, STRING, BOOL));
		builder.add(createBuiltin("==", BOOL, STRING, STRING));
		builder.add(createBuiltin("!=", BOOL, STRING, STRING));
		// char
		builder.add(createBuiltin("==", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("!=", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("<", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("<=", BOOL, CHAR, CHAR));
		builder.add(createBuiltin(">", BOOL, CHAR, CHAR));
		builder.add(createBuiltin(">=", BOOL, CHAR, CHAR));
		//Builting functions
		builder.add(createFunctionId("print", ExampleType.STRING, 	createVariableId("str", ExampleType.STRING)));
		builder.add(createFunctionId("print", ExampleType.BOOL, 	createVariableId("str", ExampleType.BOOL)));
		builder.add(createFunctionId("print", ExampleType.INT, 	createVariableId("str", ExampleType.INT)));
		builder.add(createFunctionId("print", ExampleType.CHAR, 	createVariableId("str", ExampleType.CHAR)));
		
		builder.add(createFunctionId("getInt", ExampleType.INT, 	createVariableId("list", ExampleType.INTLIST), createVariableId("i", ExampleType.INT)));
		builder.add(createFunctionId("putInt", ExampleType.VOID, createVariableId("list", ExampleType.INTLIST), createVariableId("i", ExampleType.INT)));
		builder.add(createFunctionId("length", ExampleType.INT, createVariableId("list", ExampleType.INTLIST)));
		builder.add(createFunctionId("newList", ExampleType.INTLIST));
		
		// RandomInt
		builder.add(createFunctionId("random", ExampleType.INT, createVariableId("max", ExampleType.INT)));
		
		for(ExampleType type : new ExampleType[]{INT, CHAR, STRING}){
		    FunctionId<TypedNode> r = createFunctionId("read", type, 	createVariableId("str", type));
		    r.setIdType(IdType.VARARGS);
		    builder.add(r);
		}
		
		FunctionNode readNode = byToken("read", ExampleType.VOID);
		VarArgsFunctionId<TypedNode> varArgs = new VarArgsFunctionId<TypedNode>(readNode, ExampleType.VOID, 2, ExampleType.CHAR, ExampleType.INT, ExampleType.STRING);
		readNode.setBoundMethod(varArgs);
		builder.add(varArgs);
		
		builder.add(createFunctionId("ensure", ExampleType.VOID, createVariableId("expr", ExampleType.BOOL)));
		//Sla op
		builtins = builder.build();
	}
	
	public void inject(SymbolTable<TypedNode> t) throws IllegalFunctionDefinitionException{
		for(FunctionId<TypedNode> node : builtins){
			t.put(node);
		}
	}
}
