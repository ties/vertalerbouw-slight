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
        ch.st($base.tree, $base.type);
        $type = Type.BOOL;
        for(equationExpression_return o : eqex){
          ch.testTypes(o.type, Type.BOOL);
          ch.st(o.tree, o.type);
        }
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type;
      }
    }
  ;

equationExpression returns [Type type]
  @init{
    List<plusExpression_return> plex = new ArrayList<plusExpression_return>();
    List<TypedNode> ops              = new ArrayList<TypedNode>();
  }
  : base=plusExpression (op=(LTEQ^ | GTEQ^ | GT^ | LT^ | EQ^ | NOTEQ^) pe=plusExpression { ops.add(op); plex.add(pe); })*
    { if(plex!=null && plex.size()>0){
        //Deze operators kunnen alleen bij integers worden toegepast
        if(ops.contains(LTEQ) || ops.contains(GTEQ) || ops.contains(GT) || ops.contains(LT)){
          ch.testTypes($base.type, Type.INT);
          ch.st($base.tree, $base.type);
          $type = $base.type;
          for(plusExpression_return o : plex){
            ch.testTypes(o.type, Type.INT);
            ch.st(o.tree, o.type);
          }          
        }else{
          //Bij operators EQ en NOTEQ mogen alle Typen behalve Void worden gebruikt
          ch.testNotType($base.type, Type.VOID);
          ch.st($base.tree, $base.type);
          for(plusExpression_return o : plex){
            ch.testNotType(o.type, Type.VOID);
            ch.st(o.tree, o.type);
          }
        }
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type;
      }
    }
  ;

plusExpression returns [Type type]
  @init{
    List<multiplyExpression_return> muex = new ArrayList<multiplyExpression_return>();
  }
  : base=multiplyExpression (
                          (PLUS)=>(PLUS^ me=multiplyExpression {muex.add(me);})
                          |(MINUS)=>(MINUS^ me=multiplyExpression {muex.add(me);})
                        )*
    { if(muex!=null && muex.size()>0){
        // Alles moet int zijn voor optellen/aftrekken.
        ch.testTypes($base.type, Type.INT);
        ch.st($base.tree, $base.type);
        $type = $base.type;
          for(multiplyExpression_return o : muex){
            ch.testTypes(o.type, Type.INT);
            ch.st(o.tree, o.type);
          }
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type; 
      }
    }        
  ;

multiplyExpression returns [Type type]
  @init{
    List<unaryExpression_return> unex = new ArrayList<unaryExpression_return>();
  }
  : base=unaryExpression ((MULT^ | DIV^ | MOD^) ue=unaryExpression {unex.add(ue);})*
    { if(unex!=null && unex.size()>0){
        // Alles moet int zijn voor vermenigvuldigen/delen/modulo
        ch.testTypes($base.type, Type.INT);
        ch.st($base.tree, $base.type);
          for(unaryExpression_return o : unex){
            ch.testTypes(o.type, Type.INT);
            ch.st(o.tree, o.type);
          }
      }else{
        ch.st($base.tree, $base.type);
        $type = $base.type; 
      }
    }        
  ;

unaryExpression returns [Type type]
  : (op=NOT^)? base=simpleExpression
    //TODO: Hieronder lelijke hack. Manier bedenken waar 'op' vergeleken kan worden met de NOT-token zonder deze hierin te hardcoden.
    { if(op.getText()=="!"){
        ch.testTypes($base.type, Type.BOOL);
      }
      ch.st($base.tree, $base.type);
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