package edu.utwente.vb;

import java.io.File;

import static junit.framework.Assert.*;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.debug.BlankDebugEventListener;
import org.antlr.runtime.tree.BufferedTreeNodeStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import edu.utwente.vb.example.CodegenPreparation;
import edu.utwente.vb.example.ExampleChecker;
import edu.utwente.vb.example.ExampleLexer;
import edu.utwente.vb.example.ExampleParser;
import edu.utwente.vb.example.util.CheckerHelper;
import edu.utwente.vb.symbols.Prelude;
import edu.utwente.vb.symbols.SymbolTable;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.tree.TypedNodeAdaptor;
import junit.framework.TestCase;

public abstract class AbstractGrammarTest{
	/**
	 * Maak een Parser instantie met de gegeven string als input.
	 * 
	 * @param testString
	 * @return
	 * @throws IOException
	 */
	protected ExampleParser createParser(String testString) throws IOException {
		CharStream stream = new ANTLRStringStream(testString);
		return createParser(stream);
	}
	
	protected ExampleParser createParser(CharStream stream) throws IOException{
		ExampleLexer lexer = new ExampleLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExampleParser parser = new ExampleParser(tokens,
				new BlankDebugEventListener());
		//*needed* :)
		parser.setTreeAdaptor(new TypedNodeAdaptor());
		return parser;
	}
	
	protected ExampleChecker createChecker(CharStream stream, ExampleParser parser) throws IOException, RecognitionException{
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(parser.program().getTree());
		
		ExampleChecker	checker = new ExampleChecker(nodes, new BlankDebugEventListener());
		/* Patch de symbol table met default functies */
		SymbolTable<TypedNode> symtab = new SymbolTable<TypedNode>();
		Prelude pre = new Prelude();
		pre.inject(symtab);
		symtab.openScope();
		CheckerHelper ch = new CheckerHelper(symtab);
		checker.setCheckerHelper(ch);
		
		checker.setTreeAdaptor(new TypedNodeAdaptor());

		return checker;
	}
	
	protected CodegenPreparation createCodegenPreparation(CharStream stream, ExampleParser parser) throws IOException, RecognitionException{
		ExampleChecker checker = createChecker(stream, parser);
		ExampleChecker.program_return checker_result = checker.program();
		
		BufferedTreeNodeStream checker_nodes = new BufferedTreeNodeStream((TypedNode)checker_result.getTree());
		CodegenPreparation prepare = new CodegenPreparation(checker_nodes, new BlankDebugEventListener());
		return prepare;
	}

	
	protected CharStream asCharStream(File f) throws IOException{
		return new ANTLRFileStream(f.toString());
	}
}
