/**
 * Parser for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

grammar ExampleParser;

options {
  k=1;
  language = Java;
  output = AST;
}

tokens
{
  MAIN = 'main';
  RETURN = 'return';

  IF = 'if';
  ELSE = 'else';
  WHILE = 'while';

  CONST = 'const';
  
  // primitives
  VOID = 'void';
  BOOLEAN = 'bool';
  CHAR = 'char';
  INT = 'int';
  STRING = 'string';

  TRUE    : 'True';
  FALSE   : 'False';
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
  
/**parameterVar
  : I*/

closedCompountExpression
  : INDENT compoundExpression DEDENT
  ;

compoundExpression
  : expression SEMI!
  | declaration SEMI!
  | expression compoundExpression SEMI!
  | declaration compoundExpression SEMI!
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
  
