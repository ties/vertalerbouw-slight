/**
 * Code generator for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

tree grammar CodeGenerator;

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
  public enum OutputMode{ FILE, LOG}

  private ASMAdapter aa;
  private Logger log = LoggerFactory.getLogger(CodeGenerator.class);
  private OutputMode mode;
  
  public void setOutputMode(OutputMode mode){
    this.mode = checkNotNull(mode);
  }
  
  public void setASMAdapter(ASMAdapter adap){
    this.aa = checkNotNull(adap);
  }
}

program 
  : ^(PROGRAM content)
  ;

content
  : (declaration | functionDef)*
  ;
  
declaration returns [Type type]
  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
  ;
  
valueDeclaration returns [Type type]
  : BECOMES ce=compoundExpression
  ;
 
functionDef returns [Type type]
  : ^(FUNCTION (t=primitive?) IDENTIFIER (p=parameterDef)* returnTypeNode=closedCompoundExpression) 
  ;
  
parameterDef returns[Type type, TypedNode node]
  : ^(FORMAL primitive IDENTIFIER)
  ; 

closedCompoundExpression returns[Type type, Boolean hasReturn]
  : ^(SCOPE (ce=compoundExpression)*)
  ;

compoundExpression returns [Type type, Boolean isReturn, Boolean hasReturn]
  : expr=expression
  | dec=declaration
  | ^(ret=RETURN expr=expression)
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type, Boolean hasReturn]
  : ^(op=BECOMES base=expression sec=expression) 
  | ^(op=OR base=expression sec=expression) 
  | ^(op=AND base=expression sec=expression) 
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression)
  | ^(op=(PLUS|MINUS) base=expression sec=expression) 
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) 
  | ^(op=NOT base=expression)
  | sim=simpleExpression
  ;
  
simpleExpression returns [Type type, Boolean hasReturn]
  : atom                                     
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | fc=functionCall                          
  | variable                                 
  | paren                                    
  | cce=closedCompoundExpression             
  | s=statements                             
  ;
  
statements returns [Type type, Boolean hasReturn]
  : ifState=ifStatement
  | whileState=whileStatement    
  ;

ifStatement returns [Type type, Boolean hasReturn]
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
  ;  
    
whileStatement returns [Type type, Boolean hasReturn]
  : ^(WHILE cond=expression loop=closedCompoundExpression)
  ;    
    
primitive returns [Type type]
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom returns [Type type]
  : INT_LITERAL
  | NEGATIVE INT_LITERAL
  | CHAR_LITERAL
  | STRING_LITERAL
  | TRUE
  | FALSE
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren returns [Type type]
  : ^(PAREN expression)
  ;
  
variable returns [Type type]
  : id=IDENTIFIER
  ;
  
functionCall returns [Type type]
  : ^(CALL id=IDENTIFIER (ex=expression)*)
  ; 