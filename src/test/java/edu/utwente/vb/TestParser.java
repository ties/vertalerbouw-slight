package edu.utwente.vb;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import edu.utwente.vb.example.Parser;

public class TestParser extends AbstractGrammarTest{
	@Test
	public void testComments() throws IOException{
		Parser parser = createParser("# Comment");
		assert parser.getTokenStream().get(0).getType() == Parser.SINGLELINE_COMMENT;
	}
	
	@Test
	public void testMultilineComments() throws IOException{
		Parser parser = createParser("/#Dit is een string die over meerdere \n" +
											"regels heen wrapped #/");
		assert parser.getTokenStream().get(0).getType() == Parser.MULTILINE_COMMENT;
	}
	
	@Test
	public void testMath() throws IOException, RecognitionException{
		Parser parser = createParser("5 - 5 \n"
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
}
