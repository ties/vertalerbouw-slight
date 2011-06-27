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
  
  import org.objectweb.asm.Label;
  
  /** Logger */
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  
  import com.google.common.collect.Lists;
  
  
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
  : (declaration | {aa.setInFunction(true);}functionDef {aa.setInFunction(false);})*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER { aa.visitDecl($IDENTIFIER); } rvd=valueDeclaration? {aa.visitDeclEnd($rvd.tree);})
  | ^(CONST prim=primitive IDENTIFIER { aa.visitDecl($IDENTIFIER); } cvd=valueDeclaration {aa.visitDeclEnd($cvd.tree);}) 
  | ^(INFERVAR IDENTIFIER { aa.visitDecl($IDENTIFIER); } run=valueDeclaration? {aa.visitDeclEnd($run.tree);}) 
  | ^(INFERCONST IDENTIFIER { aa.visitDecl($IDENTIFIER); } cons=valueDeclaration {aa.visitDeclEnd($cons.tree);})
  ;
  
valueDeclaration 
  : BECOMES ce=compoundExpression
  ;
 
functionDef
  @init{
    List<TypedNode> params = Lists.newArrayList();
  }
  : ^(FUNCTION (t=primitive?) IDENTIFIER 
      (p=parameterDef {params.add($p.id_node);})* 
      { 
        aa.visitFuncDef($IDENTIFIER, params);
      } 
      returnTypeNode=closedCompoundExpression 
      { aa.visitEndFuncDef(); })
  ;
  
parameterDef returns [TypedNode id_node]
  : ^(FORMAL primitive IDENTIFIER){ $id_node = $IDENTIFIER; }
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
  @init{
    Label ifEnd = new Label();
    Label elseEnd = new Label();
  }
  : ^(IF cond=expression { aa.visitIfBegin($cond.tree, ifEnd); } ifExpr=closedCompoundExpression { aa.visitIfHalf($cond.tree, ifEnd, elseEnd); } (elseExpr=closedCompoundExpression)? ){ aa.visitIfEnd($elseExpr.tree, elseEnd); }
    {/* if($elseExpr.tree==null)
        aa.visitIf($cond.tree, $ifExpr.tree);
      else
        aa.visitIfElse($cond.tree, $ifExpr.tree, $elseExpr.tree); 
    */}
  
  ;  
    
whileStatement
  : ^(WHILE { aa.visitWhile(); } cond=expression {aa.visitWhile($cond.tree); } loop=closedCompoundExpression { aa.visitEndWhile(); })
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  : INT_LITERAL { aa.visitIntegerAtom($INT_LITERAL, true); }
  | NEGATIVE INT_LITERAL { aa.visitIntegerAtom($INT_LITERAL, true); }
  | CHAR_LITERAL { aa.visitCharAtom($CHAR_LITERAL); }
  | STRING_LITERAL { aa.visitStringAtom($STRING_LITERAL); }
  | TRUE { aa.visitBooleanAtom($TRUE); }
  | FALSE { aa.visitBooleanAtom($FALSE); }
  ;
  
paren
  : ^(PAREN expression)
  ;
  
variable
  : id=IDENTIFIER
  ;
  
functionCall
  @init{
    List<TypedNode> params = Lists.newArrayList();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {params.add($ex.tree);})*)
    { 
      aa.visitFuncCall($id, params);
    }
  ; 