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
  
declaration returns [Type type]
  @init{
    TypedNode decl = null;
  }

  : ^(VAR prim=primitive IDENTIFIER runtimeValueDeclaration) { ch.declareVar($IDENTIFIER, $prim.text); ch.tbn($VAR, $prim.text); }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST prim=primitive IDENTIFIER cvd=constantValueDeclaration) { ch.testTypes(Type.byName($prim.text), $cvd.type); ch.declareConst($IDENTIFIER, $prim.text); ch.tbn($CONST, $prim.text); }
  | ^(INFERVAR IDENTIFIER run=runtimeValueDeclaration?) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERVAR, Type.UNKNOWN); }
  | ^(INFERCONST IDENTIFIER cons=constantValueDeclaration?) { ch.declareVar($IDENTIFIER, Type.UNKNOWN); ch.st($INFERCONST, Type.UNKNOWN); }
  ;
  
runtimeValueDeclaration returns [Type type]
  : BECOMES ce=compoundExpression
      {ch.st($ce.tree, $ce.type); }
  ;
 
constantValueDeclaration returns [Type type]
  : BECOMES atom { ch.st($atom.tree, $atom.type); $type = $atom.type; }
  ;
  
functionDef returns [Type type]
  @init{
    List<TypedNode> pl = new ArrayList<TypedNode>();
  }
  : { ch.openScope(); } ^(FUNCTION IDENTIFIER (p=parameterDef { pl.add($p.node); }(p=parameterDef { pl.add($p.node); })*)? returnTypeNode=closedCompoundExpression) { ch.declareFunction($IDENTIFIER, returnTypeNode.type, pl); ch.closeScope(); }
  ;
  
parameterDef returns[Type type, TypedNode node]
  : ^(FORMAL primitive IDENTIFIER) { ch.declareVar($IDENTIFIER, $primitive.type); ch.st($FORMAL, $primitive.type); $node=$FORMAL; $type=$primitive.type; }
  ; 

// TODO: Nog naar kijken
closedCompoundExpression returns[Type type, Boolean hasReturn]
  @init{
    List<Type> coex = new ArrayList<Type>();
    
  }
  : {ch.openScope();} ^(SCOPE (ce=compoundExpression { coex.add($ce.type); })*) {ch.closeScope(); $hasReturn=coex.contains(true);}
  ;

compoundExpression returns [Type type, Boolean isReturn]
  : expr=expression { ch.st($expr.tree, $expr.type); $type = $expr.type; $isReturn=false; }
  | dec=declaration { ch.st($dec.tree, $dec.type); $type = $dec.type; $isReturn=false; }
  | ^(ret=RETURN expr=expression) {ch.st($ret.tree, $expr.type); $type = $expr.type; $isReturn=true; }
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type]
  : base=orExpression (BECOMES^ opt=expression)?
    { ch.testTypes($base.type, $opt.type);
      ch.st($opt.tree, $opt.type);
      $type = $opt.type;
    }
  ;

orExpression returns [Type type]
  @init{
    List<andExpression_return> anex = new ArrayList<andExpression_return>();
    
  }
  : base=andExpression (OR^ an=andExpression { anex.add(an); })*
    { if(anex!=null && anex.size()>0){
        ch.testTypes($base.type, Type.BOOL);
        for(andExpression_return o : anex){
          ch.testTypes(o.type, Type.BOOL);
          ch.st(o.tree, o.type);
        }
        ch.st($base.tree, $base.type);
        $type = Type.BOOL;
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type;
      }
    }
  ;
  
andExpression returns [Type type]
  @init{
    List<equationExpression_return> eqex = new ArrayList<equationExpression_return>();
    
  }
  : base=equationExpression (AND^ eq=equationExpression { eqex.add(eq); } )*
    { if(eqex!=null && eqex.size()>0){
        ch.testTypes($base.type, Type.BOOL);
        for(equationExpression_return o : eqex){
          ch.testTypes(o.type, Type.BOOL);
          ch.st(o.tree, o.type);
        }
        ch.st($base.tree, $base.type);
        $type = Type.BOOL;
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type;
      }
    }
  ;

equationExpression returns [Type type]
  : plusExpression ((LTEQ^ | GTEQ^ | GT^ | LT^ | EQ^ | NOTEQ^) plusExpression)*
  ;

plusExpression returns [Type type]
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  : multiplyExpression (
                          (PLUS)=>(PLUS^ multiplyExpression)
                          |(MINUS)=>(MINUS^ multiplyExpression)
                        )*
  ;

multiplyExpression returns [Type type]
  : unaryExpression ((MULT^ | DIV^ | MOD^) unaryExpression)*
  ;

unaryExpression returns [Type type]
  : (NOT^)? simpleExpression
  ;
  
simpleExpression returns [Type type]
  : atom { ch.st($atom.tree, $atom.type); $type = $atom.type; }
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
    
primitive returns [Type type]
  : VOID      { $type = Type.VOID; }
  | BOOLEAN   { $type = Type.BOOL; }
  | CHAR      { $type = Type.CHAR; }
  | INT       { $type = Type.INT; }
  | STRING    { $type = Type.STRING; }
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