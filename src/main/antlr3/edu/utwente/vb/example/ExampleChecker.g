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
  ASTLabelType = TypedNode;
  tokenVocab = ExampleLexer;
  rewrite = true;
}

@header{ 
  package edu.utwente.vb.example;
  
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
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
  
  private void tbn(Token node, String type){
    ((TypedNode)node).setType(Type.byName(type));
  }
  
  private void tbn(Token token){
  }
  
  private void st(Token token, Type type){
    ((TypedNode)token).setType(type);
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
    TypedNode decl = null;
  }

  : ^(VAR type=primitive IDENTIFIER runtimeValueDeclaration) { tbn($VAR, $type.text); }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST type=primitive IDENTIFIER constantValueDeclaration) { tbn($CONST, $type.text); }
  | ^(INFERVAR IDENTIFIER runtimeValueDeclaration?) { tbn($INFERVAR); }
  | ^(INFERCONST IDENTIFIER constantValueDeclaration) { tbn($INFERCONST); }
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
  : ^(FORMAL primitive variable)
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
  : (PLUS^ | MINUS^)? INT_LITERAL { st($INT_LITERAL,Type.INT); }
  | CHAR_LITERAL    { st($CHAR_LITERAL,Type.CHAR); }
  | STRING_LITERAL  { st($STRING_LITERAL,Type.STRING); }
  | TRUE            { st($TRUE,Type.BOOL); }
  | FALSE          { st($FALSE,Type.BOOL); }
  ;
  
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