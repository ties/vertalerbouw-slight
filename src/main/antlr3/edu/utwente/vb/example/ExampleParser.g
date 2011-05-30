/**
 * Parser for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

parser grammar ExampleParser;

options {
  k=1;
  language = Java;
  output = AST;
  tokenVocab = ExampleLexer;
  ASTLabelType = TypedNode;
}

@header{ 
  package edu.utwente.vb.example;
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.tree.TypedNode;
  import edu.utwente.vb.tree.AppliedOccurrenceNode;
}

// Alter code generation so catch-clauses get replaced with this action. 
// This disables ANTLR error handling;
@rulecatch { 
    catch (RecognitionException e) { 
        throw e; 
    } 
}

/**
 * A program consists of several functions
 */
program 
  : content EOF -> ^(PROGRAM content)
  ;

content	
  : (compoundExpression | functionDef)* 
  ;
  
declaration
  // Regels herschrijven naar consistente vorm
  : primitive IDENTIFIER runtimeValueDeclaration? -> ^(VAR primitive IDENTIFIER runtimeValueDeclaration?)
  // Constanten kunnen alleen van primitive typen zijn
  | (CONST (BOOLEAN | CHAR | INT | STRING)) => CONST primitive IDENTIFIER constantValueDeclaration -> ^(CONST primitive IDENTIFIER constantValueDeclaration)
  | VAR IDENTIFIER runtimeValueDeclaration? -> ^(INFERVAR IDENTIFIER runtimeValueDeclaration?)
  | CONST IDENTIFIER constantValueDeclaration -> ^(INFERCONST IDENTIFIER constantValueDeclaration)
  ;
  
runtimeValueDeclaration
  : BECOMES compoundExpression
  ;
 
constantValueDeclaration
  : BECOMES atom;
  
functionDef
  : FUNCTION IDENTIFIER LPAREN (parameterDef (COMMA parameterDef)*)? RPAREN COLON closedCompoundExpression -> ^(FUNCTION IDENTIFIER (parameterDef (parameterDef)*)? closedCompoundExpression)
  ;
  
parameterDef
  : primitive variable -> ^(FORMAL primitive variable)
  ; 

closedCompoundExpression
  : INDENT compoundExpression* DEDENT -> ^(SCOPE compoundExpression*)
  ;

compoundExpression
  : expression
  | declaration
  ;
 
expression
  : orExpression (BECOMES^ expression)?
  ;
  
orExpression 
  : andExpression (OR^ andExpression)*
  ;
  
andExpression
  : equationExpression (AND^ equationExpression)*
  ;

equationExpression
  : plusExpression ((LTEQ^ | GTEQ^ | GT^ | LT^ | EQ^ | NOTEQ^) plusExpression)*
  ;

plusExpression
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  : multiplyExpression (
                          (PLUS)=>(PLUS^ multiplyExpression)
                          |(MINUS)=>(MINUS^ multiplyExpression)
                        )*
  ;

multiplyExpression
  : unaryExpression ((MULT^ | DIV^ | MOD^) unaryExpression)*
  ;

unaryExpression
  : (NOT^)? simpleExpression
  ;
  
simpleExpression
  : atom
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | (IDENTIFIER LPAREN)=> functionCall
  | variable
  | paren
  | closedCompoundExpression
  | statements
  ;
  
statements
  : ifStatement
  | whileStatement
  ;

ifStatement
  /* Geen COLON? omdat je colons wel moet matchen */
  : (IF LPAREN) => IF LPAREN expression RPAREN COLON closedCompoundExpression (ELSE COLON closedCompoundExpression)? -> ^(IF expression closedCompoundExpression (closedCompoundExpression)? )
  | IF expression COLON (ELSE COLON closedCompoundExpression)? -> ^(IF expression closedCompoundExpression (closedCompoundExpression)? )
  ;  
    
whileStatement
  : (WHILE LPAREN) => WHILE LPAREN expression RPAREN COLON closedCompoundExpression -> ^(WHILE expression closedCompoundExpression)
  | WHILE expression COLON closedCompoundExpression -> ^(WHILE expression closedCompoundExpression)
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  : (PLUS^ | MINUS^)? INT_LITERAL
  | CHAR_LITERAL
  | STRING_LITERAL
  | TRUE 
  | FALSE;

paren
  : LPAREN! expression RPAREN!
  ;
  
//TODO: Werkt wel! niet, nog naar kijken.
variable
  : IDENTIFIER<AppliedOccurrenceNode>
  ;
  
functionCall
  : IDENTIFIER LPAREN (expression (COMMA expression)*)? RPAREN -> ^(CALL IDENTIFIER<AppliedOccurrenceNode> expression*)
  ;