/**
 * Checker for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

tree grammar CodegenPreparation;

options {
  k=1;
  language = Java;
  output = AST;
  ASTLabelType = TypedNode;
  rewrite = true;
  tokenVocab = Lexer;
}

@header{ 
  package edu.utwente.vb.example;

  import edu.utwente.vb.example.asm.*;
    
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.exceptions.*;

  
  /** Logger */
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  
  
  import static com.google.common.base.Preconditions.checkNotNull;
}

// Alter code generation so catch-clauses get replaced with this action. 
// This disables ANTLR error handling;
@rulecatch { 
    catch (RecognitionException e) { 
       throw e; 
    } 
    
}

@members{
  private Logger log = LoggerFactory.getLogger(CodegenPreparation.class);
}

program 
  : ^(PROGRAM content)
  ;

content
  : (declaration | functionDef)*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
  ;
  
valueDeclaration
  : BECOMES ce=compoundExpression
  ;
 
functionDef
  : ^(FUNCTION (t=primitive?) IDENTIFIER (p=parameterDef)* returnTypeNode=closedCompoundExpression) 
  ;
  
parameterDef
  : ^(FORMAL primitive IDENTIFIER)
  ; 

closedCompoundExpression
  : ^(SCOPE (ce=compoundExpression)*)
  ;

compoundExpression
  : expr=expression
  | dec=declaration
  ;
  
expression
  : ^(op=BECOMES base=expression sec=expression) 
  | ^(op=OR base=expression sec=expression) 
  | ^(op=AND base=expression sec=expression) 
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression)
  | ^(op=(PLUS|MINUS) base=expression sec=expression) 
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) 
  | ^(op=NOT base=expression)
  | ^(ret=RETURN expr=expression)
  | sim=simpleExpression
  ;
  
simpleExpression
  : atom                                     
  | fc=functionCall                          
  | variable                                 
  | paren                                    
  | cce=closedCompoundExpression             
  | s=statements                             
  ;
  
statements
  : ifState=ifStatement
  | whileState=whileStatement    
  ;

ifStatement
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
  ;  
    
whileStatement
  : ^(WHILE cond=expression loop=closedCompoundExpression)
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  : INT_LITERAL
  | NEGATIVE INT_LITERAL
  | CHAR_LITERAL
  | STRING_LITERAL
  | TRUE
  | FALSE
  ;
  
paren
  : ^(PAREN expression)
  ;
  
variable
  : id=IDENTIFIER
  ;
  
functionCall
  : ^(CALL id=IDENTIFIER (ex=expression)*)
  ; 