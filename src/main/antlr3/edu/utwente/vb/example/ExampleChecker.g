/**
 * Checker for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

parser grammar ExampleChecker;

options {
  k=1;
  language = Java;
  output = AST;
  tokenVocab = ExampleLexer;
}

@header{ 
  package edu.utwente.vb.example;
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.symbols.SymbolTable;
  import edu.utwente.vb.tree.TypeTree;
}

// Alter code generation so catch-clauses get replaced with this action. 
// This disables ANTLR error handling;
@rulecatch { 
    catch (RecognitionException e) { 
        throw e; 
    } 
}

@members{
  private SymbolTable st;
  
  public void setSymbolTable(SymbolTable t){
    st = t;
  }

}

/**
 * A program consists of several functions
 */
program 
  :  ^(PROGRAM content)
  ;

content	
  : (compoundExpression | functionDef)* 
  ;
  
declaration
  @init{
    TypeTree decl = null;
  }

  : primitive ident=IDENTIFIER runtimeValueDeclaration? //{st.put(ident)}
  //Constanten kunnen alleen een simpele waarde krijgen
  | (CONST (BOOLEAN | CHAR | INT | STRING)) => CONST primitive IDENTIFIER constantValueDeclaration
  | VAR IDENTIFIER runtimeValueDeclaration?
  | CONST IDENTIFIER constantValueDeclaration
  ;
  
runtimeValueDeclaration
  : BECOMES compoundExpression
  ;
 
constantValueDeclaration
  : BECOMES atom;
  
functionDef
  : ^(FUNCTION IDENTIFIER (parameterDef (parameterDef)*)? closedCompoundExpression)
  ;
  
parameterDef
  : ^(FORMAL primitive parameterVar)
  ; 

parameterVar
  : variable 
  | STRING_LITERAL
  | INT_LITERAL
  | SQUOT CHAR_LITERAL SQUOT
  | (IDENTIFIER LPAREN)=> functionCall 
  ;
  
closedCompoundExpression
  : {st.openScope();} ^(SCOPE compoundExpression*) {st.closeScope();}
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
  : ^(IF expression closedCompoundExpression (closedCompoundExpression)?)
  ;  
    
whileStatement
  : ^(WHILE expression closedCompoundExpression)
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
  : IDENTIFIER 
  ;
  
functionCall
  : ^(CALL IDENTIFIER expression*)
  ;