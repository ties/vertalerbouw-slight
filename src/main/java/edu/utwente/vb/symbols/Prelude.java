package edu.utwente.vb.symbols;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.CommonToken;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static edu.utwente.vb.symbols.Type.*;
import static edu.utwente.vb.example.util.CheckerHelper.*;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.tree.TypedNode;

public class Prelude {
	public final Set<FunctionId<TypedNode>> builtins;
	
	public Prelude() throws IllegalFunctionDefinitionException{
		ImmutableSet.Builder<FunctionId<TypedNode>> builder = ImmutableSet.builder();
		//Voeg operatoren toe
		// boolean
		builder.add(createBuiltin("==", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("!=", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("and", BOOL, BOOL, BOOL));
		builder.add(createBuiltin("or", BOOL, BOOL, BOOL));
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
		builder.add(createBuiltin("==", BOOL, STRING, STRING));
		builder.add(createBuiltin("!=", BOOL, STRING, STRING));
		// char
		builder.add(createBuiltin("==", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("!=", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("<", BOOL, CHAR, CHAR));
		builder.add(createBuiltin("<=", BOOL, CHAR, CHAR));
		builder.add(createBuiltin(">", BOOL, CHAR, CHAR));
		builder.add(createBuiltin(">=", BOOL, CHAR, CHAR));
		//Sla op
		builtins = builder.build();
	}
	
	public void inject(SymbolTable<TypedNode> t) throws IllegalFunctionDefinitionException{
		for(FunctionId<TypedNode> node : builtins){
			t.put(node);
		}
	}
}