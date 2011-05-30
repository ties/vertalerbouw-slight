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
  import edu.utwente.vb.exceptions.*;
  
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
  
  /**
  * In de sectie hieronder word de afhandeling van excepties geregeld.
  *
  */
  
  protected int nrErr = 0;
  public    int nrErrors() { return nrErr; }
  
  public void displayRecognitionError(
              String[] tokenNames, RecognitionException e){
    nrErr += 1;
    if (e instanceof IncompatibleTypesException)
      emitErrorMessage("[Example] error: " + e.getMessage());
    else
      super.displayRecognitionError(tokenNames, e);
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
  | ^(CONST type=primitive IDENTIFIER rhs=constantValueDeclaration) { ch.testTypes(Type.byName($type.text), $rhs.type); ch.declareConst($IDENTIFIER, $type.text); ch.tbn($CONST, $type.text); }
  | ^(INFERVAR IDENTIFIER runtimeValueDeclaration?) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERVAR, Type.UNKNOWN); }
  | ^(INFERCONST IDENTIFIER constantValueDeclaration) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERCONST, Type.UNKNOWN); }
  ;
  
runtimeValueDeclaration
  : BECOMES compoundExpression  // {$type = $compountExpression.getNodeType(); }
  ;
 
constantValueDeclaration returns [Type type]
  : BECOMES rhs=atom { $type = $rhs.type; }
  ;
  
functionDef
  @init{
    List<TypedNode> pl = new ArrayList<TypedNode>();
  }
  : { ch.openScope(); }^(FUNCTION IDENTIFIER (p=parameterDef { pl.add($p.node); }(p=parameterDef { pl.add($p.node); })*)? returnTypeNode=closedCompoundExpression) { ch.declareFunction($IDENTIFIER, returnTypeNode.getTree(), pl); ch.closeScope(); }
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

atom returns [Type type]
  : (PLUS^ | MINUS^)? INT_LITERAL { ch.st($INT_LITERAL,Type.INT);       $type = Type.INT;    }
  | CHAR_LITERAL                  { ch.st($CHAR_LITERAL,Type.CHAR);     $type = Type.CHAR;   }
  | STRING_LITERAL                { ch.st($STRING_LITERAL,Type.STRING); $type = Type.STRING; }
  | TRUE                          { ch.st($TRUE,Type.BOOL);             $type = Type.BOOL;   }
  | FALSE                         { ch.st($FALSE,Type.BOOL);            $type = Type.BOOL;   }
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