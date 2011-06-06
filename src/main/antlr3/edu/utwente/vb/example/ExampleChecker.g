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
  tokenVocab = ExampleParser;
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
  
declaration returns [Type type]
  @init{
    TypedNode decl = null;
  }

  : ^(VAR prim=primitive IDENTIFIER rvd=runtimeValueDeclaration) { ch.st($prim.tree, $prim.type); ch.st($rvd.tree, $rvd.type); ch.declareVar($IDENTIFIER, $prim.type); ch.inferBecomes($VAR.tree, $prim.tree, $rvd.tree);  }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST prim=primitive IDENTIFIER cvd=constantValueDeclaration) { ch.st($prim.tree, $prim.type); ch.st($cvd.tree, $cvd.type); ch.declareConst($IDENTIFIER, $prim.type); ch.inferBecomes($CONST.tree, $prim.tree, $cvd.tree);  }
  | ^(INFERVAR IDENTIFIER run=runtimeValueDeclaration?) 
      { if(run==null){
          ch.declareVar($IDENTIFIER, Type.UNKNOWN);
          ch.st($INFERVAR, Type.UNKNOWN);
        }else{
          Type type = $run.type;
          ch.declareVar($IDENTIFIER, type);
          ch.st($INFERVAR, type);
        }
      }
  | ^(INFERCONST IDENTIFIER cons=constantValueDeclaration?) 
      { if(cons==null){
          ch.declareVar($IDENTIFIER, Type.UNKNOWN); 
          ch.st($INFERCONST, Type.UNKNOWN); 
        }else{
          Type type = $cons.type;
          ch.declareVar($IDENTIFIER, type);
          ch.st($INFERCONST, type);
        }
      }
  ;
  
runtimeValueDeclaration returns [Type type]
  : BECOMES ce=compoundExpression
    { ch.st($ce.tree, $ce.type); }
  ;
 
constantValueDeclaration returns [Type type]
  : BECOMES atom 
    { ch.st($atom.tree, $atom.type); $type = $atom.type; }
  ;
  
functionDef returns [Type type]
  @init{
    List<TypedNode> pl = new ArrayList<TypedNode>();
  }
  : { ch.openScope(); } ^(FUNCTION IDENTIFIER (p=parameterDef { pl.add($p.node); }(p=parameterDef { pl.add($p.node); })*)? returnTypeNode=closedCompoundExpression) 
    { ch.declareFunction($IDENTIFIER, returnTypeNode.type, pl);
      ch.st($FUNCTION, returnTypeNode.type);
      $type = returnTypeNode.type;
      ch.closeScope();
    }
  ;
  
parameterDef returns[Type type, TypedNode node]
  : ^(FORMAL primitive IDENTIFIER) { ch.declareVar($IDENTIFIER, $primitive.type); ch.st($FORMAL, $primitive.type); $node=$FORMAL; $type=$primitive.type; }
  ; 

closedCompoundExpression returns[Type type]
  @init{
    List<compoundExpression_return> coex = new ArrayList<compoundExpression_return>();
  }
  : { ch.openScope(); } ^(SCOPE (ce=compoundExpression { coex.add(ce); })*) { ch.closeScope(); }
    { List<Boolean> retList = new ArrayList<Boolean>();
      for(compoundExpression_return cer: coex){
        retList.add(cer.isReturn);
      }
      Type returnType = Type.VOID;
      if(retList.contains(true)){
        returnType = coex.get(retList.indexOf(true)).type;
      }
      $type=returnType;
    }
  ;

compoundExpression returns [Type type, Boolean isReturn]
  : expr=expression { ch.st($expr.tree, $expr.type); $type = $expr.type; $isReturn=false; }
  | dec=declaration { ch.st($dec.tree, $dec.type); $type = $dec.type; $isReturn=false; }
  | ^(ret=RETURN expr=expression) {ch.st($ret.tree, $expr.type); $type = $expr.type; $isReturn=true; }
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type]
  : ^(BECOMES lhs=orExpression rhs=expression)
    { ch.testTypes($lhs.type, $rhs.type);
      ch.st($rhs.tree, $rhs.type);
      $type = $rhs.type;
    }
  | base=orExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }    
  ;

orExpression returns [Type type]
  : ^(OR base=andExpression sec=orExpression)
	    { ch.testTypes($base.type, Type.BOOL);
	      ch.testTypes($sec.type, Type.BOOL);
	      ch.st($base.tree, $base.type);
	      ch.st($sec.tree, $sec.type);
	      $type = Type.BOOL;
	    }
  | base=andExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }      
  ;
  
andExpression returns [Type type]
  : ^(AND base=equationExpression sec=andExpression)
    { ch.testTypes($base.type, Type.BOOL);
      ch.testTypes($sec.type, Type.BOOL);
      ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = Type.BOOL;
    }
  | base=equationExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }          
  ;

equationExpression returns [Type type]
  : ^((LTEQ | GTEQ | GT | LT) base=plusExpression sec=equationExpression)
    { ch.testTypes($base.type, Type.INT);
      ch.testTypes($base.type, $sec.type);
      ch.st($base.tree, $base.type);
      $type = $base.type;
    }
  | ^((EQ | NOTEQ) base=plusExpression sec=equationExpression)
    { ch.testNotType($base.type, Type.VOID);
      ch.testTypes($base.type, $sec.type);
      ch.st($base.tree, $base.type);
      $type = $base.type;
    }
  | base=plusExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }          
  ;

plusExpression returns [Type type]
  : ^((PLUS|MINUS) base=multiplyExpression sec=plusExpression)
    { ch.testTypes($base.type, Type.INT);
      ch.testTypes($sec.type, Type.INT);
      ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = $base.type;
    }
  | base=multiplyExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }     
  ;

multiplyExpression returns [Type type]
  : ^((MULT | DIV | MOD) base=unaryExpression sec=multiplyExpression)
    { ch.testTypes($base.type, Type.INT);
      ch.testTypes($sec.type, Type.INT);
      ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = $base.type;
    }
  | base=unaryExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }      
  ;

unaryExpression returns [Type type]
  : ^(op=NOT base=simpleExpression)
    //TODO: Hieronder lelijke hack. Manier bedenken waar 'op' vergeleken kan worden met de NOT-token zonder deze hierin te hardcoden.
    { ch.testTypes($base.type, Type.BOOL); }
  
  | base=simpleExpression
    {ch.st($base.tree, $base.type);
      $type = $base.type;
    }
  ;
  
simpleExpression returns [Type type]
  : atom                                     { ch.st($atom.tree, $atom.type); $type = $atom.type; }
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | (IDENTIFIER LPAREN)=> fc=functionCall    { ch.st($fc.tree, $fc.type); $type = $fc.type; }
  | variable                                 { ch.st($variable.tree, $variable.type); $type = $variable.type; }
  | paren                                    { ch.st($paren.tree, $paren.type); $type = $paren.type; }
  | cce=closedCompoundExpression             { ch.st($cce.tree, $cce.type); $type = $cce.type; }
  | statements                               { //TODO: Wat gaan we hier doen met typen? 
                                               ch.st($statements.tree, Type.BOOL); $type = Type.BOOL; }
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
    
primitive returns [Type type]
  : VOID      { $type = Type.VOID; }
  | BOOLEAN   { $type = Type.BOOL; }
  | CHAR      { $type = Type.CHAR; }
  | INT       { $type = Type.INT; }
  | STRING    { $type = Type.STRING; }
  ;

atom returns [Type type]
  : INT_LITERAL                   { ch.st($INT_LITERAL,Type.INT);       $type = Type.INT;    }
  | NEGATIVE INT_LITERAL          { ch.st($INT_LITERAL,Type.INT);       $type = Type.INT;    }
  | CHAR_LITERAL                  { ch.st($CHAR_LITERAL,Type.CHAR);     $type = Type.CHAR;   }
  | STRING_LITERAL                { ch.st($STRING_LITERAL,Type.STRING); $type = Type.STRING; }
  | TRUE                          { ch.st($TRUE,Type.BOOL);             $type = Type.BOOL;   }
  | FALSE                         { ch.st($FALSE,Type.BOOL);            $type = Type.BOOL;   }
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren returns [Type type]
  : LPAREN! expression RPAREN!    { ch.st($expression.tree, $expression.type); $type = $expression.type; }
  ;
  
variable returns [Type type]
  : id=IDENTIFIER
    { Type varType = ch.getVarType(id.getText());
      ch.st($id, varType);
      $type = varType;
    }
  ;
  
functionCall returns [Type type]
  : ^(CALL IDENTIFIER expression*)
  ; 