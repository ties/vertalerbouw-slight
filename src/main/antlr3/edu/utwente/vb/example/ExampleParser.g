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
  import org.utwente.vb.example.*;
}


/**
 * A program consists of several functions
 */
program 
  : declarations (function)*
  ;
  
declarations
  : declaration*
  ;

declaration
  : VAR IDENTIFIER
  | CONST IDENTIFIER
  ;
  
function
  : IDENTIFIER LPAREN! (parameterDef (COMMA! parameterDef)*)? RPAREN! closedCompoundExpression
  ;
  
parameterDef
  : (CONST)? primitive parameterVar
  ;

//TODO: Volgens mij niet goed, naar kijken.
parameterVar
  : IDENTIFIER
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
  : multiplyExpression ((PLUS^ | MINUS^) multiplyExpression)
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
  : IDENTIFIER LPAREN! (IDENTIFIER (COMMA! IDENTIFIER)*)? RPAREN!
  ;