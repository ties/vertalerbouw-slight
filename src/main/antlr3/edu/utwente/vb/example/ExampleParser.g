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
  : (compoundExpression | functionDef)* -> functionDef* compoundExpression* 
  ;
  
declaration
  // Regels herschrijven naar consistente vorm
  : primitive id=IDENTIFIER valueDeclaration? -> ^(VAR primitive IDENTIFIER valueDeclaration?)
  // Constanten kunnen alleen van primitive typen zijn
  | (CONST primitive) => CONST primitive IDENTIFIER valueDeclaration -> ^(CONST primitive IDENTIFIER valueDeclaration) 
  | VAR IDENTIFIER valueDeclaration? -> ^(INFERVAR IDENTIFIER valueDeclaration?)
  | CONST IDENTIFIER valueDeclaration -> ^(INFERCONST IDENTIFIER valueDeclaration)
  ;
  
valueDeclaration
  : BECOMES compoundExpression
  ;
 
functionDef
  : FUNCTION IDENTIFIER LPAREN (parameterDef (COMMA parameterDef)*)? RPAREN (ARROW primitive)? COLON closedCompoundExpression -> ^(FUNCTION primitive? IDENTIFIER parameterDef*  closedCompoundExpression)
  ;
  
parameterDef
  : primitive IDENTIFIER -> ^(FORMAL primitive IDENTIFIER)
  ; 

closedCompoundExpression
  : INDENT compoundExpression* DEDENT -> ^(SCOPE compoundExpression*)
  ;

compoundExpression
  : expression
  | RETURN expression -> ^(RETURN expression)
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
  | IF expression COLON closedCompoundExpression (ELSE COLON closedCompoundExpression)? -> ^(IF expression closedCompoundExpression (closedCompoundExpression)? )
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
  //Negative wordt gebruikt om onderscheid te maken tussen MINUS bij een negatief getal en MINUS bij aftrekken
  : PLUS! INT_LITERAL
  | MINUS INT_LITERAL -> NEGATIVE INT_LITERAL
  | INT_LITERAL
  | CHAR_LITERAL
  | STRING_LITERAL
  | TRUE 
  | FALSE;

paren
  : LPAREN expression RPAREN -> ^(PAREN expression)
  ;
  
variable
  : IDENTIFIER
  ;
  
functionCall
  : IDENTIFIER LPAREN (expression (COMMA expression)*)? RPAREN -> ^(CALL IDENTIFIER expression*)
  ;