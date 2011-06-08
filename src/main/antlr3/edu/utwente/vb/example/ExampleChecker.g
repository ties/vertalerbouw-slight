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
  private Logger log = LoggerFactory.getLogger(ExampleChecker.class);

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
  : functionDef* compoundExpression* 
  ;
  
declaration returns [Type type]
  @init{
    TypedNode decl = null;
  }

  : ^(VAR prim=primitive IDENTIFIER rvd=runtimeValueDeclaration) { ch.st($prim.tree, $prim.type); ch.st($rvd.tree, $rvd.type); ch.declareVar($IDENTIFIER, $prim.type); ch.inferBecomes($VAR, $prim.tree, $rvd.tree); $type = $prim.type; }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST prim=primitive IDENTIFIER cvd=constantValueDeclaration) { ch.st($prim.tree, $prim.type); ch.st($cvd.tree, $cvd.type); ch.declareConst($IDENTIFIER, $prim.type); ch.inferBecomes($CONST.tree, $prim.tree, $cvd.tree); $type = $prim.type; }
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
    { ch.st($ce.tree, $ce.type); $type = $ce.type; }
  ;
 
constantValueDeclaration returns [Type type]
  : BECOMES atom 
    { ch.st($atom.tree, $atom.type); $type = $atom.type; }
  ;
  
functionDef returns [Type type]
  @init{
    List<parameterDef_return> pl = new ArrayList<parameterDef_return>();
  }
  //Hack, voor closedcompoundexpression function declareren, anders wordt de function niet herkend in geval van recursion
  : { ch.openScope(); } ^(FUNCTION (t=primitive?) IDENTIFIER (p=parameterDef { pl.add(p); })* 
     { List<TypedNode> nodes = new ArrayList<TypedNode>();
      
        for(parameterDef_return ret : pl)
          nodes.add(ret.node);
        
        if(t!=null){
          $type = t.type;
          ch.declareFunction($IDENTIFIER, $type, nodes);
        }else{
          ch.declareFunction($IDENTIFIER, Type.UNKNOWN, nodes);
        }

     }  
  returnTypeNode=closedCompoundExpression) 
    {
      List<TypedNode> nodes = new ArrayList<TypedNode>();
      
       for(parameterDef_return ret : pl)
         nodes.add(ret.node);
      
      if($type != null && !$type.equals(returnTypeNode.type)){
        throw new IllegalFunctionDefinitionException("Return type in function body (" +returnTypeNode.type + ") does not match defined return type (" + $type +")");
      } else {
        $type = returnTypeNode.type;
      }
      
      ch.st($FUNCTION, $type);
      
      ch.closeScope();
      ch.declareFunction($IDENTIFIER, $type, nodes);
    }
  ;
  
parameterDef returns[Type type, TypedNode node]
  : ^(FORMAL primitive IDENTIFIER) { ch.declareVar($IDENTIFIER, $primitive.type); ch.st($FORMAL, $primitive.type); $node=$FORMAL; $type=$primitive.type; }
  ; 

closedCompoundExpression returns[Type type, Type returns]
  @init{
    List<compoundExpression_return> coex = new ArrayList<compoundExpression_return>();
  }
  : { ch.openScope(); } ^(SCOPE (ce=compoundExpression { coex.add(ce); })*) { ch.closeScope(); }
    {
    //Standaard return type
    $type = Type.VOID;
    $returns = Type.UNKNOWN;
    //Nu alle 
    for(compoundExpression_return cer: coex){//alle compound expressies
        log.debug("checking " + cer + " type: " + cer.type + " return: " + cer.returns);

          if($returns == Type.UNKNOWN && cer.returns!=null){//1e return
            $returns = cer.returns;
            log.debug("setting type to " + cer.returns);
          } else if (cer.returns != null){//Alle 2..ne returns moeten zelfde type hebben als 1e
            if(cer.returns != $returns){
              throw new IllegalFunctionDefinitionException("Multiple return types");
            }
          }
        }
      
      //Als type nog unknown is: Er is geen return geweest, effectieve type is dan VOID
      $returns = $returns==Type.UNKNOWN ? Type.VOID : $returns;
      log.debug("end of function analysis, effective type is "  + $returns);
    }
  ;

compoundExpression returns [Type type, Type returns]
  : expr=expression { ch.st($expr.tree, $expr.type); $type = $expr.type; }
  | dec=declaration { ch.st($dec.tree, $dec.type); $type = $dec.type; }
  | ^(ret=RETURN expr=expression) {$returns = $type = $expr.type; }
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type, Type returns]
  : ^(op=BECOMES lhs=orExpression rhs=expression)
    { ch.testTypes($lhs.type, $rhs.type);
      ch.st($rhs.tree, $rhs.type);
      $type = ch.apply($op, $lhs.tree, $rhs.tree);
    }
  | base=orExpression
    { ch.st($base.tree, $base.type);
      $returns = $type = $base.type;
    }    
  ;

orExpression returns [Type type, Type returns]
  : ^(op=OR base=andExpression sec=orExpression)
    { ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = ch.apply($op, $base.tree, $sec.tree);
    }
  | base=andExpression
    { ch.st($base.tree, $base.type);
      $returns = $type = $base.type;
    }      
  ;
  
andExpression returns [Type type, Type returns]
  : ^(op=AND base=equationExpression sec=andExpression)
    { ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = ch.apply($op, $base.tree, $sec.tree);
    }
  | base=equationExpression
    { ch.st($base.tree, $base.type);
      $returns = $type = $base.type;
    }          
  ;

equationExpression returns [Type type, Type returns]
  : ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=plusExpression sec=equationExpression)
    { ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = ch.apply($op, $base.tree, $sec.tree);
    }
  | base=plusExpression
    { ch.st($base.tree, $base.type);
      $returns = $type = $base.type;
    }          
  ;

plusExpression returns [Type type, Type returns]
  : ^(op=(PLUS|MINUS) base=multiplyExpression sec=plusExpression)
    { ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = ch.apply($op, $base.tree, $sec.tree);
    }
  | base=multiplyExpression
    { 
      $returns = $type = ch.st($base.tree, $base.type);
    }     
  ;

multiplyExpression returns [Type type, Type returns]
  : ^(op=(MULT | DIV | MOD) base=unaryExpression sec=multiplyExpression)
    { ch.st($base.tree, $base.type);
      ch.st($sec.tree, $sec.type);
      $type = ch.apply($op, $base.tree, $sec.tree);
    }
  | base=unaryExpression
    {
      $returns = $type = ch.st($base.tree, $base.type);
    }      
  ;

unaryExpression returns [Type type, Type returns]
  : ^(op=NOT base=simpleExpression)
    //TODO: Hieronder lelijke hack. Manier bedenken waar 'op' vergeleken kan worden met de NOT-token zonder deze hierin te hardcoden.
    { 
      $type = ch.apply($op, ch.st($base.tree, $base.type));
     }
  
  | base=simpleExpression
    {
      $returns = $type = ch.st($base.tree, $base.type);
    }
  ;
  
simpleExpression returns [Type type, Type returns]
  : atom                                     { $returns = $type = ch.st($atom.tree, $atom.type); }
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | fc=functionCall                          { $returns = $type = ch.st($fc.tree, $fc.type); }
  | variable                                 { $returns = $type = ch.st($variable.tree, $variable.type); }
  | paren                                    { $returns = $type = ch.st($paren.tree, $paren.type); }
  | cce=closedCompoundExpression             { $returns = $type = ch.st($cce.tree, $cce.type); }
  | statements                               { $returns = $type = ch.st($statements.tree, $statements.type);}
  ;
  
statements returns [Type type, Type returns]
  : ifState=ifStatement
    { $type = Type.VOID;
      $returns = $ifState.type;
    }
  | whileState=whileStatement
    { $type = Type.VOID;
      $returns = $whileState.type;  
    }
  ;

ifStatement returns [Type type, Type returns]
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
      { ch.testTypes($cond.type, Type.BOOL); 
        $type = Type.VOID;
        $returns = ch.testTypes($ifExpr.returns, $elseExpr.returns);
      }
  ;  
    
whileStatement returns [Type type, Type returns]
  : ^(WHILE cond=expression loop=closedCompoundExpression)
      { ch.testTypes($cond.type, Type.BOOL);
        $returns = $loop.returns;
        $type = Type.VOID;
      }
  ;    
    
primitive returns [Type type, Type returns]
  : VOID      { $type = Type.VOID; }
  | BOOLEAN   { $type = Type.BOOL; }
  | CHAR      { $type = Type.CHAR; }
  | INT       { $type = Type.INT; }
  | STRING    { $type = Type.STRING; }
  ;

atom returns [Type type, Type returns]
  : INT_LITERAL                   { $type = ch.st($INT_LITERAL,Type.INT); }
  | NEGATIVE INT_LITERAL          { $type = ch.st($INT_LITERAL,Type.INT); }
  | CHAR_LITERAL                  { $type = ch.st($CHAR_LITERAL,Type.CHAR); }
  | STRING_LITERAL                { $type = ch.st($STRING_LITERAL,Type.STRING); }
  | TRUE                          { $type = ch.st($TRUE,Type.BOOL); }
  | FALSE                         { $type = ch.st($FALSE,Type.BOOL); }
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren returns [Type type, Type returns]
  : ^(PAREN expression)           { $type = ch.st($expression.tree, $expression.type); }
  ;
  
variable returns [Type type, Type returns]
  : id=IDENTIFIER
    { /* get the type of variable and set it on the node */
      $type = ch.st($id, ch.apply(id.getText()));
    }
  ;
  
functionCall returns [Type type, Type returns]
  @init{
    List<Type> args = new ArrayList<Type>();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {args.add($ex.type);})*)
    { $type = ch.apply($id.text, args);
    }
  ; 