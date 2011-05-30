/**
 * Checker for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

tree grammar ExampleChecker;

options {
  k=1;
  language = Java;
  output = AST;
  ASTLabelType = TypedNode;
  rewrite = true;
}

@header{ 
  package edu.utwente.vb.example;
  
  import edu.utwente.vb.example.*;
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  
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
  : (compoundExpression | functionDef)* 
  ;
  
declaration
  @init{
    TypedNode decl = null;
  }

  : ^(VAR type=primitive IDENTIFIER runtimeValueDeclaration) { ch.declareVar($IDENTIFIER, $type.text); ch.tbn($VAR, $type.text); }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST type=primitive IDENTIFIER constantValueDeclaration) { ch.declareConst($IDENTIFIER, $type.text); ch.tbn($CONST, $type.text); }
  | ^(INFERVAR IDENTIFIER runtimeValueDeclaration?) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERVAR, Type.UNKNOWN); }
  | ^(INFERCONST IDENTIFIER constantValueDeclaration) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERCONST, Type.UNKNOWN); }
  ;
  
runtimeValueDeclaration returns[Type type]
  : BECOMES compoundExpression { //$type = $compountExpression.getNodeType(); }
  ;
 
constantValueDeclaration returns[Type type]
  : BECOMES atom
  ;
  
functionDef
  @init{
    List<TypedNode> pl = new ArrayList<TypedNode>();
  }
  : { ch.openScope(); }^(FUNCTION IDENTIFIER (p=parameterDef { pl.add((TypedNode) p.getTree()); }(p=parameterDef { pl.add((TypedNode) p.getTree()); })*)? returnTypeNode=closedCompoundExpression) { ch.declareFunction($IDENTIFIER, returnTypeNode.getTree(), pl); ch.closeScope(); }
  ;
  
parameterDef returns[TypedNode node]
  : ^(FORMAL type=primitive IDENTIFIER) { ch.declareVar($IDENTIFIER, $type.text); ch.tbn($FORMAL, $type.text); $node=new TypedNode($FORMAL); }
  ; 

closedCompoundExpression
  : {ch.openScope();} ^(SCOPE compoundExpression*) {ch.closeScope();}
  ;

compoundExpression
  : expression
  | declaration
  ;
 
expression
  : orExpression (BECOMES^ expression)?
  ;
  
orExpression 
  : andExpression (OR^ andExpression)*
  ;
  
andExpression
  : equationExpression (AND^ equationExpression)*
  ;

equationExpression
  : plusExpression ((LTEQ^ | GTEQ^ | GT^ | LT^ | EQ^ | NOTEQ^) plusExpression)*
  ;

plusExpression
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  : multiplyExpression (
                          (PLUS)=>(PLUS^ multiplyExpression)
                          |(MINUS)=>(MINUS^ multiplyExpression)
                        )*
  ;

multiplyExpression
  : unaryExpression ((MULT^ | DIV^ | MOD^) unaryExpression)*
  ;

unaryExpression
  : (NOT^)? simpleExpression
  ;
  
simpleExpression
  : atom
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | (IDENTIFIER LPAREN)=> functionCall
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
  : (PLUS^ | MINUS^)? INT_LITERAL { ch.st($INT_LITERAL,Type.INT); }
  | CHAR_LITERAL    { ch.st($CHAR_LITERAL,Type.CHAR); }
  | STRING_LITERAL  { ch.st($STRING_LITERAL,Type.STRING); }
  | TRUE            { ch.st($TRUE,Type.BOOL); }
  | FALSE          { ch.st($FALSE,Type.BOOL); }
  ;
  
paren
  : LPAREN! expression RPAREN!
  ;
  
variable
  : IDENTIFIER
  ;
  
functionCall
  : ^(CALL IDENTIFIER expression*)
  ;