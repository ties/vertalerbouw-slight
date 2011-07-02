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
  
  import org.objectweb.asm.Opcodes;
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
  : (declaration | { aa.setInFunction(true); } functionDef { aa.setInFunction(false); } )*
  ;
  
declaration
  : ^(VAR primitive IDENTIFIER { aa.visitDecl($IDENTIFIER); } rvd=valueDeclaration? {aa.visitDeclEnd($rvd.tree);})
  | ^(CONST primitive IDENTIFIER { aa.visitDecl($IDENTIFIER); } cvd=valueDeclaration {aa.visitDeclEnd($cvd.tree);}) 
  | ^(INFERVAR IDENTIFIER { aa.visitDecl($IDENTIFIER); } run=valueDeclaration? {aa.visitDeclEnd($run.tree);}) 
  | ^(INFERCONST IDENTIFIER { aa.visitDecl($IDENTIFIER); } cons=valueDeclaration {aa.visitDeclEnd($cons.tree);})
  ;
  
valueDeclaration 
  : BECOMES compoundExpression
  ;
 
functionDef
  @init{
    List<TypedNode> params = Lists.newArrayList();
  }
  : ^(FUNCTION (primitive?) IDENTIFIER 
      (p=parameterDef {params.add($p.id_node);})* 
      { 
        aa.visitFuncDef($IDENTIFIER, params);
      } 
      closedCompoundExpression 
      { aa.visitEndFuncDef(); })
  ;
  
parameterDef returns [TypedNode id_node]
	@init{
		int parameterNumber = 1;// parameter 0 = this bij func call van non-static
	}
  : ^(FORMAL primitive IDENTIFIER){ aa.visitArgument($IDENTIFIER, parameterNumber++); $id_node = $IDENTIFIER; }
  ; 

closedCompoundExpression
  : ^(SCOPE (compoundExpression)*)
  ;

compoundExpression
  : expression
  | declaration
  ;
  
expression
  //Assignment
  : ^(BECOMES base=expression expression { aa.visitBecomes($base.tree); })
  //Comparisons
  | ^(LTEQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPLE, $base.tree,$sec.tree); })
  | ^(GTEQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPGE, $base.tree,$sec.tree); })
  | ^(GT base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPGT, $base.tree,$sec.tree); })
  | ^(LT base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPLT, $base.tree,$sec.tree); })
  | ^(EQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPEQ, $base.tree,$sec.tree); })
  | ^(NOTEQ base=expression sec=expression { aa.visitCompareOperator(Opcodes.IF_ICMPNE, $base.tree,$sec.tree); })
  //Binary operators
  | ^(OR base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IOR, $base.tree, $sec.tree); }) 
  | ^(AND base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IAND, $base.tree, $sec.tree); }) 
  | ^(PLUS base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IADD, $base.tree,$sec.tree); })
  | ^(MINUS base=expression sec=expression { aa.visitBinaryOperator(Opcodes.ISUB, $base.tree,$sec.tree); })
  | ^(MULT base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IMUL, $base.tree,$sec.tree); }) 
  | ^(DIV base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IDIV, $base.tree,$sec.tree); })
  | ^(MOD base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IREM, $base.tree,$sec.tree); }) 
  //Unary operators
  | ^(NOT base=expression { aa.visitNot(); })
  | ^(RETURN expr=expression {aa.visitReturn($expr.tree); })
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
  @init{
    Label ifEnd = new Label();
    Label elseEnd = new Label();
  }
  : ^(IF cond=expression { aa.visitIfBegin($cond.tree, ifEnd); } closedCompoundExpression { aa.visitIfHalf($cond.tree, ifEnd, elseEnd); } (elseExpr=closedCompoundExpression)? ){ aa.visitIfEnd($elseExpr.tree, elseEnd); }
  ;  
    
whileStatement
  @init{
    Label loopBegin = new Label();
    Label loopEnd   = new Label();
  }
  : ^(WHILE cond=expression { aa.visitWhileBegin($cond.tree, loopBegin, loopEnd); } closedCompoundExpression { aa.visitWhileBegin($cond.tree, loopBegin, loopEnd); })
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  : INT_LITERAL { aa.visitIntegerAtom($INT_LITERAL, false); }
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
  : id=IDENTIFIER { aa.visitVariable($id); }
  ;
  
functionCall
  @init{
    List<TypedNode> params = Lists.newArrayList();
  }
  : ^(CALL id=IDENTIFIER 
    {
      aa.visitFuncCallBegin($id, params);
    }
      (ex=expression {params.add($ex.tree);})*
    { 
      aa.visitFuncCallEnd($id, params);
    }
    )
  ; 