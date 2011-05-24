package edu.utwente.vb;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.debug.BlankDebugEventListener;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import edu.utwente.vb.example.ExampleLexer;
import edu.utwente.vb.example.ExampleParser;
import junit.framework.TestCase;

public abstract class AbstractGrammarTest extends TestCase {
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
		return parser;
	}
	
	protected CharStream asCharStream(File f) throws IOException{
		return new ANTLRFileStream(f.toString());
	}
}
