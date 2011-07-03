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
    
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.exceptions.*;
  import edu.utwente.vb.example.util.Utils;

  
  /** Logger */
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  
  
  import static com.google.common.base.Preconditions.checkNotNull;
}

@rulecatch { 
    catch (RecognitionException e) { 
       throw e; 
    } 
    
}

@members{
  private Logger log = LoggerFactory.getLogger(CodegenPreparation.class);
  
  public enum Usage{READ, WRITE, RW}
  
  private Usage mode = Usage.READ;
}

program 
  : ^(PROGRAM content)
  ;

content
  : (declaration | functionDef)*
  ;
  
declaration
  : ^(VAR primitive IDENTIFIER vd=valueDeclaration? { if($vd.tree != null){ Utils.updateUsage($IDENTIFIER, Usage.WRITE); } }) 
  | ^(CONST primitive IDENTIFIER valueDeclaration { Utils.updateUsage($IDENTIFIER, Usage.WRITE); }) 
  | ^(INFERCONST IDENTIFIER valueDeclaration { Utils.updateUsage($IDENTIFIER, Usage.WRITE); }) 
  | ^(INFERVAR IDENTIFIER vd=valueDeclaration? { if($vd.tree != null){ Utils.updateUsage($IDENTIFIER, Usage.WRITE); } })
  //ANTLR bug, delete van node (door transformatie van ^(...) -> \n is stuk, zie http://www.antlr.org/wiki/display/ANTLR3/Tree+construction en http://www.antlr.org/pipermail/antlr-interest/2009-November/036711.html
  ;
  
valueDeclaration
  : BECOMES compoundExpression
  ;
 
functionDef
  : ^(FUNCTION (t=primitive?) IDENTIFIER (p=parameterDef)* closedCompoundExpression) 
  ;
  
parameterDef
  : ^(FORMAL primitive IDENTIFIER)
  ; 

closedCompoundExpression
  : ^(SCOPE (compoundExpression)*)
  ;

compoundExpression
  : expression
  | declaration
  ;
  
expression
  : ^(BECOMES {mode = Usage.WRITE;} variable {mode = Usage.READ;} expression) 
  | ^(OR expression expression) 
  | ^(AND expression expression) 
  | ^((LTEQ | GTEQ | GT | LT | EQ | NOTEQ) expression expression)
  | ^((PLUS|MINUS) expression expression) 
  | ^((MULT | DIV | MOD) expression expression) 
  | ^(NOT expression)
  | ^(RETURN expression)
  | simpleExpression
  ;
  
simpleExpression
  : atom                                     
  | functionCall                          
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
  : INT_LITERAL               { $INT_LITERAL.setConstantExpression(true); }
  | CHAR_LITERAL              { $CHAR_LITERAL.setConstantExpression(true); }
  | STRING_LITERAL            { $STRING_LITERAL.setConstantExpression(true); }
  | TRUE                      { $TRUE.setConstantExpression(true); }
  | FALSE                     { $FALSE.setConstantExpression(true); }
  ;
  
paren
  : ^(PAREN expression)
  ;
  
variable
  : IDENTIFIER { Utils.updateUsage($IDENTIFIER, mode); }
  ;
  
functionCall
  : ^(CALL IDENTIFIER { if(Utils.isRead($IDENTIFIER)){ mode = Usage.RW; } }(expression)* { mode = Usage.READ; } )
  ; 