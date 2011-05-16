/**
 * Tokenizer for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

grammar Example;

options {
  superClass = BaseOffSideLexer;
}

tokens {
  DECLARATION;
  CLOSEDCOMPOUND;
  COMPOUND;
  PAREN;
  
  PROGRAM;
  FUNCTION;
  PARAMETER;
  DEDENT;
  INDENT;  
}

@header{
  package edu.utwente.vb.example;
  import edu.utwente.vb.example.tree.*;
}

@members{
  protected int indentToken(){
    return INDENT;
  }
  
  protected int dedentToken(){
    return DEDENT;
  }
}

//Interpunction
COLON   : ':';
SEMI    : ';';
COMMA   : ',';
//DOT     : '.';
LPAREN  : '(';
RPAREN  : ')';
SQUOT   : '\'';
DQUOT   : '"';

//Operators
BECOMES : '=';
PLUS    : '+';
MINUS   : '-';
MULT    : '*';
DIV     : '/';
MOD     : '%';

//Boolean operators
LTEQ    : '<=';
GTEQ    : '>=';
GT      : '>';
LT      : '<';
EQ      : '==';
NOTEQ   : '!=';
NOT     : '!';

AND     : 'and';
OR      : 'or';

//Conditional statements
IF      : 'if';
ELSE    : 'else';
WHILE   : 'while';
//Function declaration
//YIELDS    : '->';
FUNCTION  : 'def';

//Variable & Const definition
CONST   : 'const';
VAR     : 'var';  
//True/False
TRUE    : 'True';
FALSE   : 'False';

WS  
	@init{
	  initWhiteSpace();
	}     
	@after {
	  afterWhiteSpace();
	}:
	(' '|'\r'|'\u000C'
	|{!atLineStart() }? => '\t'
	| (t='\n' {line();} (('    '| '\t') {indent();})*) /* newline + tabs -> take them all. Omdat ik hier in de regel geen emit doe, worden tokens gewoon doorgegeven. */
	//Parr, The Definite Antlr Reference, 2007 (pg. 110)
	//turn on only if at left edge
	| {atLineStart()}? => (('    '| '\t') {lineIndent();})+ // match whitespace
	){$channel=HIDDEN;}
	;
	
MULTILINE_COMMENT   : '/#' .* '#/' {$channel=HIDDEN;};

SINGLELINE_COMMENT  : '#' ~('\n'|'\r')* '\r'? {$channel=HIDDEN;};

LIT_INT     : DIGIT+;
LIT_CHAR    : SQUOT LETTER SQUOT;
IDENTIFIER  : LETTER (LETTER | DIGIT)*;

STRING_LITERAL  : '"' (options{greedy=true;}) : (~'"'|'\n'|'\r'))*) '"';

fragment DIGIT  : ('0'..'9');
fragment LOWER  : ('a'..'z');
fragment UPPER  : ('A'..'Z');
fragment LETTER : LOWER | UPPER; 