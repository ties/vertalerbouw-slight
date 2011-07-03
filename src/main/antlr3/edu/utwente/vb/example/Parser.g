/**
 * Parser for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */
parser grammar Parser;

options {
  k            = 1;
  language     = Java;
  output       = AST;
  tokenVocab   = Lexer;
  ASTLabelType = TypedNode;
}

@header {
package edu.utwente.vb.example;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.example.util.Utils;
}

@rulecatch {
	catch (RecognitionException e) {
	  if(debug_mode==true) 
	  throw e;
	}
	  
	catch (RuntimeException e){
	  nrErr += 1;
	  emitErrorMessage("[Example] error [" + nrErr + "] - " + e.getMessage());
	  if(debug_mode==true) 
	    throw e;
	}
	  
}

@members {
/**
 * debug_mode==true impliceert debug_mode==true in checker. 
 * Zorgt voor doorgooien excepties, zodat testsuite deze kan waarnemen. 
 */
private static boolean debug_mode = false;

/** Binnen een assignment mag geen return meer staan aan de RHS */
private boolean inAssignment = false;

/**
 * BEGIN SECTIE VOOR NETTE AFHANDELING EXCEPTIES
 */

public String getTokenErrorDisplay(Token t) {
	return t.toString();
}

/** Teller voor het aantal waargenomen fouten in het Example-programma **/
protected int nrErr = 0;

/**
 * getmethode voor aantal fouten in testprogramma
 * @return aantal fouten in ingevoerde programma
 */
public int nrErrors() {
	return nrErr;
}


public void displayRuntimeError(RuntimeException e){
  nrErr += 1;
  emitErrorMessage("[Example] error [" + nrErr + "] - char " + e.getMessage());
}

/**
 * @override
 * Eigen implementatie van de methode die ANTLR standaard aanroept bij waarneming van een RecognitionException
 * of een van haar subklassen.
 * Bij elke veelvoorkomende subklasse van RecognitionException wordt een zo nuttig mogelijke foutmelding geprint.
 * @ ensure nrErrors() == \old(nrErrors()) + 1 
 */
public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	nrErr += 1;
	if (e instanceof RecognitionException) {
		String msg = null;
		if (e instanceof NoViableAltException) {
			NoViableAltException nvae = (NoViableAltException) e;
			msg = tokenNames[nvae.getUnexpectedType()] + " was unexpected";
		} else if (e instanceof MismatchedTokenException) {
			MismatchedTokenException mte = (MismatchedTokenException) e;
			String expectedToken = (mte.expecting > 0 && mte.expecting < tokenNames.length) ? tokenNames[mte.expecting]
					: "unknown";
			String foundToken = (mte.getUnexpectedType() > 0 && mte
					.getUnexpectedType() < tokenNames.length) ? tokenNames[mte
					.getUnexpectedType()] : "unknown";
			msg = " Expected:" + expectedToken + ", but found: " + foundToken;
		} else if (e instanceof FailedPredicateException) {
			FailedPredicateException fpe = (FailedPredicateException) e;
			String foundToken = (fpe.getUnexpectedType() > 0 && fpe
					.getUnexpectedType() < tokenNames.length) ? tokenNames[fpe
					.getUnexpectedType()] : "unknown";
			String predicate = fpe.predicateText;
			msg = " Did not match predicate: " + predicate + ", token found: "
					+ foundToken;
		} else {
			msg = super.getErrorMessage(e, tokenNames);
		}
		emitErrorMessage("[Example] error [" + nrErr + "] - char "
				+ e.charPositionInLine + " on line " + e.line + " | " + msg);
	} else
		super.displayRecognitionError(tokenNames, e);
}
  
  /**
   * Zet debug_mode aan. Dit heeft tot gevolg excepties worden doorgegooid zodat de testsuite deze kan detecteren.
   * Deze methode wordt in Compiler.java aangeroepen na het aanmaken van de Parser 
   */
  public void setDebug(){
    debug_mode = true;
  }

}

/**
 * A program consists of several functions
 */
program
  :
  content EOF -> ^(PROGRAM content)
  | EOF -> ^(PROGRAM)
  ;

content
  :
  (
    declaration
    | functionDef
  )+
  ;

declaration
  // Regels herschrijven naar consistente vorm. BindingOccurrenceNode maken voor binding occurrence IDENTIFIERS.
  :
  primitive IDENTIFIER valueDeclaration?
    ->
      ^(VAR primitive IDENTIFIER<BindingOccurrenceNode> valueDeclaration?)
  | (CONST primitive) => CONST primitive IDENTIFIER valueDeclaration
    ->
      ^(CONST primitive IDENTIFIER<BindingOccurrenceNode> valueDeclaration)
  | VAR IDENTIFIER valueDeclaration?
    ->
      ^(INFERVAR IDENTIFIER<BindingOccurrenceNode> valueDeclaration?)
  | CONST IDENTIFIER valueDeclaration
    ->
      ^(INFERCONST IDENTIFIER<BindingOccurrenceNode> valueDeclaration)
  ;

valueDeclaration
  :
  { inAssignment = true; }
  BECOMES compoundExpression 
  { inAssignment = false; }
  ;

functionDef
  :
  FUNCTION IDENTIFIER LPAREN (parameterDef (COMMA parameterDef)*)? RPAREN (ARROW primitive)? COLON closedCompoundExpression
    ->
      ^(FUNCTION primitive? IDENTIFIER<FunctionNode> parameterDef* closedCompoundExpression)
  ;

parameterDef
  :
  primitive IDENTIFIER
    ->
      ^(FORMAL primitive IDENTIFIER<BindingOccurrenceNode>)
  ;

closedCompoundExpression
  :
  INDENT compoundExpression* DEDENT
    ->
      ^(SCOPE compoundExpression*)
  ;

compoundExpression
  :
  expression
  | {!inAssignment}? RETURN expression
    ->
      ^(RETURN expression)
  | declaration
  ;

expression
  :
  orExpression (BECOMES^ { inAssignment = true; }
    expression { inAssignment = false; })?
  ;

orExpression
  :
  andExpression (OR^ andExpression)*
  ;

andExpression
  :
  equationExpression (AND^ equationExpression)*
  ;

equationExpression
  :
  plusExpression
  (
    (
      LTEQ^
      | GTEQ^
      | GT^
      | LT^
      | EQ^
      | NOTEQ^
    )
    plusExpression
  )*
  ;

plusExpression
  :
  multiplyExpression
  (
    (PLUS) => (PLUS^ multiplyExpression)
    | (MINUS) => (MINUS^ multiplyExpression)
  )*
  ;

multiplyExpression
  :
  unaryExpression
  (
    (
      MULT^
      | DIV^
      | MOD^
    )
    unaryExpression
  )*
  ;

unaryExpression
  :
  (NOT^)? simpleExpression
  ;

simpleExpression
  :
  atom
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  | (IDENTIFIER LPAREN) => functionCall
  | variable
  | paren
  | closedCompoundExpression
  | statements
  ;

statements
  :
  ifStatement
  | whileStatement
  ;

ifStatement
  :
  (IF LPAREN) => IF LPAREN expression RPAREN COLON closedCompoundExpression (ELSE COLON closedCompoundExpression)?
    ->
      ^(
        IF expression closedCompoundExpression (closedCompoundExpression)?
       )
  | IF expression COLON closedCompoundExpression (ELSE COLON closedCompoundExpression)?
    ->
      ^(
        IF expression closedCompoundExpression (closedCompoundExpression)?
       )
  ;

whileStatement
  :
  (WHILE LPAREN) => WHILE LPAREN expression RPAREN COLON closedCompoundExpression
    ->
      ^(WHILE expression closedCompoundExpression)
  | WHILE expression COLON closedCompoundExpression
    ->
      ^(WHILE expression closedCompoundExpression)
  ;

primitive
  :
  VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  :
  PLUS! INT_LITERAL
  | MINUS INT_LITERAL { $INT_LITERAL.setText("-" + $INT_LITERAL.getText()); }
    -> INT_LITERAL
  | INT_LITERAL
  | CHAR_LITERAL { Utils.stripQuotes($CHAR_LITERAL); }
  | STRING_LITERAL { Utils.stripQuotes($STRING_LITERAL); }
  | TRUE
  | FALSE
  ;

paren
  :
  LPAREN expression RPAREN
    ->
      ^(PAREN expression)
  ;

variable
  :
  IDENTIFIER
    -> IDENTIFIER<AppliedOccurrenceNode>[$IDENTIFIER]
  ;

functionCall
  :
  IDENTIFIER LPAREN (expression (COMMA expression)*)? RPAREN
    ->
      ^(CALL IDENTIFIER<AppliedOccurrenceNode>[$IDENTIFIER] expression*)
  ;
