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
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
  ;
  
valueDeclaration
  : BECOMES ce=compoundExpression
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
  | ^(ret=RETURN expr=expression)
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression
  : ^(op=BECOMES base=expression sec=expression)
  | ^(op=OR base=expression sec=expression)
  | ^(op=AND base=expression sec=expression)
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression)
  | ^(op=(PLUS|MINUS) base=expression sec=expression)
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression)
  | ^(op=NOT base=expression)
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
  : INT_LITERAL           { $INT_LITERAL.tree.setNodeType(Type.INT);}
  | NEGATIVE INT_LITERAL  { $NEGATIVE.tree.setNodeType(Type.INT); $INT_LITERAL.tree.setNodeType(Type.INT);}
  | CHAR_LITERAL          { $CHAR_LITERAL.tree.setNodeType(Type.CHAR);}
  | STRING_LITERAL        { $STRING_LITERAL.tree.setNodeType(Type.STRING);}
  | TRUE                  { $TRUE.tree.setNodeType(Type.BOOL);}
  | FALSE                 { $FALSE.tree.setNodeType(Type.BOOL);}
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren
  : ^(PAREN expression)           { $PAREN.tree.setNodeType($expression.tree.getNodeType()); }
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