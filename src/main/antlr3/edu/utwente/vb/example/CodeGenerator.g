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

@rulecatch { 
    catch (RecognitionException e) { 
       log.error("RecognitionException in codegen; Illegal internal state?", e);
       throw e; 
    } 
    
}

@members{
  /** Printen JVM bytecode kan naar classfile of naar logfile **/
  public enum OutputMode{ FILE, LOG}
  private OutputMode mode = OutputMode.LOG;
  private File target;

  /** ASMAdapter bevat alle methoden om JVM bytecode te genereren **/
  private ASMAdapter aa;
  
  /** 
   * Zorgt voor nette logfiles welke informatie geven over alle handelingen doorlopen in de checker (en indirect CheckerHelper). 
   * Deze logging is zeer handig gedurende het debuggen van de Example Compiler
   **/
  private Logger log = LoggerFactory.getLogger(CodeGenerator.class);
  
  /**
   * Zet de output mode naar FILE of LOG.
   * Compiler.java gebruikt setOutputMode(FILE)
   **/
  public void setOutputMode(OutputMode mode){
    log.debug("setOutputMode {}", mode);
    this.mode = checkNotNull(mode);
  }
  
  /**
   * Kent aan Codegenerator een ASMAdatper toe die Java-methoden bevat voor emitten JVM-code
   * Constructie identiek aan Checker en CheckerHelper
   **/
  public void setASMAdapter(ASMAdapter adap){
    log.debug("setASMAdapter {}", adap);
    this.aa = checkNotNull(adap);
  }
  
   /**
   * Stelt outputbestand in voor geval outputMode==FILE
   **/
  public void setFile(File tgt){
    log.debug("setFile {}", tgt);
    this.target = checkNotNull(tgt);
  }
}

//Program regel w/ check van precondities, uitvoeren van goede visitEnd regel
program 
	:                       { 
											      log.debug("Program: {} {} target:{}", new Object[]{aa, mode, target});
											      checkNotNull(aa); 
											      checkNotNull(mode); 
											      checkArgument(mode == OutputMode.LOG || target != null); 
											    } 
      ^(PROGRAM content) 
											    { if(mode == OutputMode.FILE){
											        aa.visitEnd(target); } 
											      else{
											        aa.visitEnd();
											      }
											    }
  ;

content
  : (declaration 
  |                       { aa.setInFunction(true); } 
    functionDef 
                          { aa.setInFunction(false); } 
    )*
  ;
  
declaration
  /* Let op: Geef $rvd etc mee om te kijken of er een value is */									
  : ^(VAR primitive IDENTIFIER 
                          { aa.visitDecl($IDENTIFIER); } 
     rvd=valueDeclaration? 
                          {aa.visitDeclEnd($rvd.tree);}
     )
  | ^(CONST primitive IDENTIFIER 
                          { aa.visitDecl($IDENTIFIER); } 
     cvd=valueDeclaration 
                          {aa.visitDeclEnd($cvd.tree);}
     ) 
  | ^(INFERVAR IDENTIFIER 
                          { aa.visitDecl($IDENTIFIER); } 
     run=valueDeclaration? 
                          {aa.visitDeclEnd($run.tree);}
     ) 
  | ^(INFERCONST IDENTIFIER 
                          { aa.visitDecl($IDENTIFIER); } 
      cons=valueDeclaration 
                          {aa.visitDeclEnd($cons.tree);}
      )
  ;
  
valueDeclaration 
  : BECOMES compoundExpression
  ;
 
functionDef
  @init{
    List<TypedNode> params = Lists.newArrayList();
    int parameterNumber = 0;// parameter 0 = this bij func call van non-static
  }
  : ^(FUNCTION (primitive?) IDENTIFIER 
      (p=parameterDef[parameterNumber] 
                            {params.add($p.id_node); parameterNumber++;}
      )* 
                            { aa.visitFuncDef($IDENTIFIER, params);} 
      closedCompoundExpression 
                            { aa.visitEndFuncDef(); })
  ;
  
parameterDef[int parameterNumber] returns [TypedNode id_node]
  : ^(FORMAL primitive IDENTIFIER){ aa.visitArgument($IDENTIFIER, parameterNumber); $id_node = $IDENTIFIER; }
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
  : ^(BECOMES { aa.loadVars(false); } base=expression { aa.loadVars(true); } expression { aa.visitBecomes($BECOMES, $base.tree); })
  //Comparisons
  | ^(LTEQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPLE, $LTEQ, $base.tree,$sec.tree); })
  | ^(GTEQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPGE, $GTEQ, $base.tree,$sec.tree); })
  | ^(GT base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPGT, $GT, $base.tree,$sec.tree); })
  | ^(LT base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPLT, $LT, $base.tree,$sec.tree); })
  | ^(EQ base=expression sec=expression {aa.visitCompareOperator(Opcodes.IF_ICMPEQ, $EQ, $base.tree,$sec.tree); })
  | ^(NOTEQ base=expression sec=expression { aa.visitCompareOperator(Opcodes.IF_ICMPNE, $NOTEQ, $base.tree,$sec.tree); })
  //Binary operators
  | ^(OR base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IOR, $OR, $base.tree, $sec.tree); }) 
  | ^(AND base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IAND, $AND, $base.tree, $sec.tree); }) 
  | ^(PLUS base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IADD, $PLUS, $base.tree,$sec.tree); })
  | ^(MINUS base=expression sec=expression { aa.visitBinaryOperator(Opcodes.ISUB, $MINUS, $base.tree,$sec.tree); })
  | ^(MULT base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IMUL, $MULT, $base.tree,$sec.tree); }) 
  | ^(DIV base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IDIV, $DIV, $base.tree,$sec.tree); })
  | ^(MOD base=expression sec=expression { aa.visitBinaryOperator(Opcodes.IREM, $MOD, $base.tree,$sec.tree); }) 
  //Unary operators
  | ^(NOT base=expression { aa.visitNot($NOT); })
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
  	Label condBegin = new Label();
    Label loopEnd   = new Label();
  }
  : ^(WHILE { aa.visitWhileCond(condBegin); } cond=expression { aa.visitWhileBegin(loopEnd); } closedCompoundExpression { aa.visitWhileEnd(condBegin, loopEnd); })
  ;    
    
primitive
  : VOID
  | BOOLEAN
  | CHAR
  | INT
  | STRING
  ;

atom
  : INT_LITERAL { aa.visitIntegerAtom($INT_LITERAL); }
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
      aa.visitFuncCallEnd($CALL, $id, params);
    }
    )
  ; 