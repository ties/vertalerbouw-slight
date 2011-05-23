package edu.utwente.vb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.junit.Test;

import edu.utwente.vb.example.ExampleLexer;
import edu.utwente.vb.example.ExampleParser;
import edu.utwente.vb.example.ExampleParser.program_return;

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
		parser.plusExpression();
		
		boolean caught = false;
		parser = createParser("5 -- 3");
		parser.plusExpression();
		try{
			parser.multiplyExpression();
		}catch(RecognitionException e){
			caught = true;
		}
		assertTrue(caught);
	}
	
	@Test
	public void testParseExamplePrograms() throws Exception{
			for(CharStream s : getTestFiles()){
				ExampleParser ep = createParser(s);
				//ep.program();
			}
	}
}
