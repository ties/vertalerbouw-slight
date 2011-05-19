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
  tokenVocab=ExampleLexer;
}

@header{ 
  package edu.utwente.vb.example;
  import edu.utwente.vb.example.*;
}


/**
 * A program consists of several functions
 */
program 
  : (compoundExpression | functionDef)*
  ;
  
declaration
  : VAR IDENTIFIER
  | CONST IDENTIFIER
  ;
  
functionDef
  : FUNCTION IDENTIFIER LPAREN! (parameterDef (COMMA! parameterDef)*)? RPAREN! COLON! closedCompoundExpression
  ;
  
parameterDef
  : (CONST)? primitive parameterVar
  ;

//TODO: Volgens mij niet goed, naar kijken.
parameterVar
  : variable 
  | STRING_LITERAL
  | INT_LITERAL
  | SQUOT CHAR_LITERAL SQUOT 
  ;
  
closedCompoundExpression
  : INDENT compoundExpression DEDENT
  ;

compoundExpression
  : expression compoundExpression? SEMI!
  | declaration compoundExpression? SEMI!
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
                          ( PLUS^ multiplyExpression)=>(PLUS^ multiplyExpression)
                          ( MINUS^ multiplyExpression)=>(MINUS^ multiplyExpression)
                        )*
  ;

multiplyExpression
  : unaryExpression ((MULT^ | DIV^ | MOD^) unaryExpression)*
  ;

unaryExpression
  : (NOT^ | PLUS^ | MINUS^)? simpleExpression
  ;
  
simpleExpression
  : INT
  | CHAR
  | TRUE | FALSE
  | variable
  | paren
  | closedCompoundExpression
  | statements
  
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | (functionCall)=> functionCall
  ;
  
statements
  : ifStatement
  | whileStatement
  ;

ifStatement
  : IF^ LPAREN! expression RPAREN! closedCompoundExpression (ELSE! closedCompoundExpression)?
  ;  
    
whileStatement
  : WHILE^ LPAREN! expression RPAREN! closedCompoundExpression
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

paren
  : LPAREN! expression RPAREN!
  ;
  
//TODO: Werkt wss niet, nog naar kijken.
variable
  : IDENTIFIER 
  ;
  
functionCall
  : IDENTIFIER LPAREN! (parameterVar (COMMA! parameterVar)*)? RPAREN!
  ;