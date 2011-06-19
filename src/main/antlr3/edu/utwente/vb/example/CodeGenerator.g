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
    
  import edu.utwente.vb.example.asm.ASMAdapter;
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.exceptions.*;
  import java.io.File;
  
  /** Logger */
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  
  
  import static com.google.common.base.Preconditions.*;
}

// Alter code generation so catch-clauses get replaced with this action. 
// This disables ANTLR error handling;
@rulecatch { 
    catch (RecognitionException e) { 
       log.error("RecognitionException in codegen; Illegal internal state?", e);
       throw e; 
    } 
    
}

@members{
  public enum OutputMode{ FILE, LOG}

  private ASMAdapter aa;
  private Logger log = LoggerFactory.getLogger(CodeGenerator.class);
  private OutputMode mode = OutputMode.LOG;
  private File target;
  
  public void setOutputMode(OutputMode mode){
    log.debug("setOutputMode {}", mode);
    this.mode = checkNotNull(mode);
  }
  
  public void setASMAdapter(ASMAdapter adap){
    log.debug("setASMAdapter {}", adap);
    this.aa = checkNotNull(adap);
  }
  
  public void setFile(File tgt){
    log.debug("setFile {}", tgt);
    this.target = checkNotNull(tgt);
  }
}

//Program regel w/ check van precondities, uitvoeren van goede visitEnd regel
program 
  : { 
      log.debug("Program: {} {} target:{}", new Object[]{aa, mode, target});
      checkNotNull(aa); checkNotNull(mode); checkArgument(mode == OutputMode.LOG || target != null); 
    } 
      ^(PROGRAM content) 
    { if(mode == OutputMode.FILE){ aa.visitEnd(target); } else {aa.visitEnd(); }}
  ;

content
  : (declaration | functionDef)*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER { aa.declVar($primitive.tree, $IDENTIFIER.text); } rvd=valueDeclaration?) { aa.varBody($rvd.tree); aa.endVar(); }  
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
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
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