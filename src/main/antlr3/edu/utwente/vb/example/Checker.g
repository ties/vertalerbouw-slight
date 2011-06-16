/**
 * Checker for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

tree grammar Checker;

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
  
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.exceptions.*;
  
  import java.util.List;
  import com.google.common.collect.Lists;
  
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
  private CheckerHelper ch;
  private Logger log = LoggerFactory.getLogger(Checker.class);

  /** 
  * Compositie met hulp van een CheckerHelper. In members stoppen is onhandig;
  * inheritance kan niet omdat hij wisselt tussen DebugTreeParser en TreeParser als super type
  */ 
  public void setCheckerHelper(CheckerHelper h){
    checkNotNull(h);
    this.ch = h;
  }
  
}

/**
 * A program consists of several functions
 */
program 
  : { checkNotNull(this.ch); } ^(PROGRAM content)
  ;

content
  : (declaration | functionDef)*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
    { ch.setNodeType($prim.text, $VAR, $IDENTIFIER); //type op VAR, IDENTIFIER 
      ch.declareVar($IDENTIFIER); //declare var met zijn huidige type
      if($rvd.tree != null){
        ch.checkTypes($IDENTIFIER, $rvd.tree); //kijk of typen overeen komen
      }
    }
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
    { ch.setNodeType($prim.text, $CONST, $IDENTIFIER); 
      ch.declareConst($IDENTIFIER); 
      ch.checkTypes($IDENTIFIER, $cvd.tree);
    } 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
    {   Type inferredType = Type.UNKNOWN; 
        if($run.tree != null){
          inferredType = $run.tree.getNodeType();
        } 
        ch.setNodeType(inferredType, $INFERVAR, $IDENTIFIER);
        ch.declareVar($IDENTIFIER); 
    }
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
    {   ch.copyNodeType($cons.tree, $INFERCONST, $IDENTIFIER); 
        ch.declareConst($IDENTIFIER);
    }
  ;
  
valueDeclaration //becomes bij het declareren van een variable
  : BECOMES ce=compoundExpression { ch.copyNodeType($BECOMES, $ce.tree); }
  ;
 
functionDef
  //Hack, voor closedcompoundexpression function declareren, anders wordt de function niet herkend in geval van recursion
  @init{
    List<TypedNode> formalArguments = Lists.newArrayList();
    FunctionId<TypedNode> functionId;
  }
  : { ch.openScope(); } ^(FUNCTION (t=primitive?) IDENTIFIER 
          (p=parameterDef { log.debug("Formal argument " + $p.id_node); formalArguments.add($p.id_node); })*  
          returnTypeNode=closedCompoundExpression) 
    { ch.closeScope();} 
  ;
  
parameterDef returns [TypedNode id_node]
  : ^(FORMAL primitive IDENTIFIER) { ch.copyNodeType($primitive.tree, $IDENTIFIER, $FORMAL); $id_node = $IDENTIFIER; }
  ; 

closedCompoundExpression returns [Type return_type = null;]
  : { ch.openScope(); } ^(SCOPE
                             (ce=compoundExpression { if($return_type != null && $ce.return_type != Type.UNKNOWN){//Check of er een return type is; Check of dit overeen komt met wat al gezien is
                                                        log.debug("Detected return type {}", $ce.return_type);
                                                        if($ce.return_type != $return_type)
                                                          throw new IllegalFunctionDefinitionException("Incompatible return types; " + $return_type + " and " + $ce.return_type);
                                                          $return_type = $ce.return_type;
                                                      }
                                                    })*) 
    { $return_type = $return_type == null ? Type.VOID : $return_type; //Als er nog geen return type is returnen we void
      log.debug("Returning type {}", $return_type);
      ch.closeScope(); }
  ;

compoundExpression returns [Type return_type = Type.UNKNOWN;]
  : expr=expression 
  | ^(RETURN expr=expression) { $return_type = ch.copyNodeType($expr.start, $RETURN); }
  | dec=declaration 
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression
  : ^(op=BECOMES base=expression sec=expression) { ch.applyBecomesAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=OR base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=AND base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(PLUS|MINUS) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=NOT base=expression) { ch.applyFunctionAndSetType($op, $base.tree); }
  | sim=simpleExpression
  ;
  
simpleExpression
  : atom
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
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
  : VOID      { $VOID.setNodeType(Type.VOID); }
  | BOOLEAN   { $BOOLEAN.setNodeType(Type.BOOL); }
  | CHAR      { $CHAR.setNodeType(Type.CHAR); }
  | INT       { $INT.setNodeType(Type.INT); }
  | STRING    { $STRING.setNodeType(Type.STRING); }
  ;

atom
  : INT_LITERAL           { ch.setNodeType(Type.INT, $INT_LITERAL);}
  | NEGATIVE INT_LITERAL  { ch.setNodeType(Type.INT, $NEGATIVE, $INT_LITERAL);}
  | CHAR_LITERAL          { ch.setNodeType(Type.CHAR, $CHAR_LITERAL);}
  | STRING_LITERAL        { ch.setNodeType(Type.STRING, $STRING_LITERAL);}
  | TRUE                  { ch.setNodeType(Type.BOOL, $TRUE);}
  | FALSE                 { ch.setNodeType(Type.BOOL, $FALSE);}
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren
  : ^(PAREN expression)           { ch.copyNodeType($expression.tree, $PAREN); }
  ;
  
variable
  : id=IDENTIFIER
    { /* get the type of variable and set it on the node */
      ((AppliedOccurrenceNode)$id).setBindingNode(ch.applyVariable($id));
    }
  ;
  
functionCall
  @init{
    List<Type> args = new ArrayList<Type>();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {args.add($ex.tree.getNodeType());})*)
    { ((AppliedOccurrenceNode)$id).setBindingNode(ch.applyFunction($id, args));
    }
  ; 