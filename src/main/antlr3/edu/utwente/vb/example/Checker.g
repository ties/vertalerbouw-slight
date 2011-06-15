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
  : { ch.openScope(); } ^(FUNCTION (t=primitive?) IDENTIFIER (p=parameterDef)*  returnTypeNode=closedCompoundExpression) 
  ;
  
parameterDef
  : ^(FORMAL primitive IDENTIFIER)
  ; 

closedCompoundExpression
  : { ch.openScope(); } ^(SCOPE (ce=compoundExpression)*) { ch.closeScope(); }
  ;

compoundExpression
  : expr=expression
  | dec=declaration
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type = Type.UNKNOWN;]
  : ^(op=BECOMES base=expression sec=expression) { ch.applyBecomesAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=OR base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=AND base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(PLUS|MINUS) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=NOT base=expression) { ch.applyFunctionAndSetType($op, $base.tree); }
  | ^(RETURN expr=expression) { $type = ch.copyNodeType($expr.tree, $RETURN); }
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
  : INT_LITERAL           { ch.setNodeType(Type.INT, $INT_LITERAL.tree);}
  | NEGATIVE INT_LITERAL  { ch.setNodeType(Type.INT, $NEGATIVE.tree, $INT_LITERAL.tree);}
  | CHAR_LITERAL          { ch.setNodeType(Type.CHAR, $CHAR_LITERAL.tree);}
  | STRING_LITERAL        { ch.setNodeType(Type.STRING, $STRING_LITERAL.tree);}
  | TRUE                  { ch.setNodeType(Type.BOOL, $TRUE.tree);}
  | FALSE                 { ch.setNodeType(Type.BOOL, $FALSE.tree);}
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren
  : ^(PAREN expression)           { ch.copyNodeType($PAREN.tree,$expression.tree); }
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