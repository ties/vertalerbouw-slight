package edu.utwente.vb;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.debug.BlankDebugEventListener;

import edu.utwente.vb.example.ExampleLexer;
import edu.utwente.vb.example.ExampleParser;
import junit.framework.TestCase;

public abstract class AbstractGrammarTest extends TestCase{
	protected ExampleParser createParser(String testString) throws IOException {
    	CharStream stream = new ANTLRStringStream(testString);
    	ExampleLexer lexer = new ExampleLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExampleParser parser = new ExampleParser(tokens,new BlankDebugEventListener());
        return parser;
    }
}
