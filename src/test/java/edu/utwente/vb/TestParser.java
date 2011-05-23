package edu.utwente.vb;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.junit.Test;

import edu.utwente.vb.example.ExampleLexer;
import edu.utwente.vb.example.ExampleParser;

public class TestParser extends AbstractGrammarTest{
	@Test
	public void testComments() throws IOException{
		ExampleParser parser = createParser("# Comment");
		assert parser.getTokenStream().get(0).getType() == ExampleParser.SINGLELINE_COMMENT;
	}
	
	@Test
	public void testMultilineComments() throws IOException{
		ExampleParser parser = createParser("/#Dit is een string die over meerdere \n" +
											"regels heen wrapped #/");
		assert parser.getTokenStream().get(0).getType() == ExampleParser.MULTILINE_COMMENT;
	}
	
	@Test
	public void testMath() throws IOException, RecognitionException{
		ExampleParser parser = createParser("5 - 5 \n"
											+ "a = -5 - -5\n");
	}
}
