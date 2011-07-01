/**
 * Tokenizer for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

lexer grammar Lexer;

options {
  superClass = BaseOffSideLexer;
}

tokens {
  DEDENT;
  INDENT;
  PROGRAM;
  SCOPE;
  CALL;
  FORMAL;
  EMPTY;
  INFER;
  INFERVAR;
  INFERCONST;
  NEGATIVE;
  PAREN;
  GLOBAL;
  SYNTHETIC;
}

@header{
  package edu.utwente.vb.example;
  import edu.utwente.vb.example.*;
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
//SEMI    : ';';
COMMA   : ',';
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
FUNCTION	: 'def';
ARROW		: '->';

//Return statement
RETURN    : 'return';

//Variable & Const definition
CONST   : 'const';
VAR     : 'var';  
//True/False
TRUE    : 'True';
FALSE   : 'False';

VOID    : 'void';
BOOLEAN : 'bool';
CHAR    : 'char';
INT     : 'int';
STRING  : 'string';

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

INT_LITERAL     : DIGIT+;
CHAR_LITERAL    : SQUOT LETTER SQUOT;
IDENTIFIER      : LETTER (LETTER | DIGIT)*;

STRING_LITERAL    : ('\"' (options{greedy=true;} : (~('"'|'\n'|'\r'))*) '\"');

fragment DIGIT  : ('0'..'9');
fragment LOWER  : ('a'..'z');
fragment UPPER  : ('A'..'Z');
fragment LETTER : LOWER | UPPER; 
