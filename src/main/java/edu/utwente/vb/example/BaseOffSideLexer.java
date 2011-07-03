package edu.utwente.vb.example;

import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;

public abstract class BaseOffSideLexer extends Lexer {
    private Deque<Token> queue 	= new ArrayDeque<Token>();
	private int indentLevel		= 0;
	private int lineIndent;
	private boolean newLine;
	
    public BaseOffSideLexer() {
    	//default constructor - because alternatives exist we need to explicitly specify this
    }
    
    public BaseOffSideLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public BaseOffSideLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
	
	/**
	 * emit method that is capable of multiple emits per rule.
	 * - set the "real" token.
	 * - if there is a second emit, queue the second token.
	 */
	@Override
	public void emit(Token token) {
		if(state.token == null){
			super.emit(token);//== state.token = token
		} else {
			queue.addLast(state.token);
			state.token = token;
		}
	}
	
	/** Returns the next token. The next token is, the head of the queue (if non-empty).
	 *  Else the next token is the "regular" next token.
	 */
	@Override
	public Token nextToken() {
		return queue.isEmpty() ? super.nextToken() : queue.pollFirst();
	}
	
	public void trackIndent(){
		indentLevel++;
	}
	
	public void trackDedent(){
		indentLevel--;
	}
	
	public int getIndentLevel() {
		return indentLevel;
	}
	
	protected void initWhiteSpace(){
		lineIndent = 0;
		newLine = false;
	}
	
	protected void afterWhiteSpace(){
		if(lineIndent > getIndentLevel() && newLine){
			while(lineIndent > getIndentLevel()){
				trackIndent();
				emit(new CommonToken(indentToken(), "INDENT"));
			}
			
		}  else if (lineIndent < getIndentLevel() && newLine){
			while(lineIndent < getIndentLevel()){/* waarom die code hier? Unit testing  + dat het maar 1 keer gebeurt */
				trackDedent();
				emit(new CommonToken(dedentToken(), "DEDENT"));
			}
			assert getIndentLevel() == lineIndent;
		}
	}
	
	protected boolean atLineStart(){
		return getCharPositionInLine() == 0;
	}
	
	protected void line(){
		newLine = true;
	}
	
	protected void indent(){
		lineIndent++;
	}
	
	protected void lineIndent(){
		line();
		indent();
	}
	
	protected abstract int indentToken();
	protected abstract int dedentToken();
}
