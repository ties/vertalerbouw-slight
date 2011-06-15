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
  
declaration returns [Type type]
  @init{
    TypedNode decl = null;
  }

  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
      { 
        if(rvd==null){
	        ch.st($prim.tree, $prim.type);
	        ch.declareVar($IDENTIFIER, $prim.type);
	        $type = $prim.type;
	      }else{
	        ch.st($prim.tree, $prim.type);
	        ch.st($rvd.tree, $rvd.type); 
          ch.declareVar($IDENTIFIER, $prim.type);
          ch.apply("=", $prim.type, $rvd.type);
          $type = $prim.type;      
        }
      }
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
      { ch.st($prim.tree, $prim.type); 
        ch.st($cvd.tree, $cvd.type); 
        ch.declareConst($IDENTIFIER, $prim.type); 
        ch.apply("=", $prim.type, $cvd.type);
        $type = $prim.type;
      }
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
      { if(run==null){
          log.debug("Non-inferrable");
          ch.declareVar($IDENTIFIER, Type.UNKNOWN);
          log.debug($IDENTIFIER + " declared as " + Type.UNKNOWN);
          $type = ch.st($INFERVAR, Type.UNKNOWN);
        }else{
          log.debug("Inferrable");
          Type type = $run.type;
          ch.declareVar($IDENTIFIER, type);
          $type = ch.st($INFERVAR, type);
        }
      }
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
      { Type type = $cons.type;
        ch.declareConst($IDENTIFIER, type);
        $type = ch.st($INFERCONST, type);
      }
  ;
  
valueDeclaration returns [Type type]
  : BECOMES ce=compoundExpression
    { ch.st($ce.tree, $ce.type); $type = $ce.type; }
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

closedCompoundExpression returns[Type type, Boolean hasReturn]
  @init{
    List<compoundExpression_return> coex = new ArrayList<compoundExpression_return>();
  }
  : { ch.openScope(); } ^(SCOPE (ce=compoundExpression { coex.add(ce); })*) { ch.closeScope(); }
    {
    //Standaard return type
    $type = Type.UNKNOWN;
    $hasReturn = false;
    //Nu alle 
    for(compoundExpression_return cer: coex){//alle compound expressies
        log.debug("checking " + cer + " type: " + cer.type + " return? " + cer.isReturn);
        if(cer.isReturn || cer.hasReturn){//het is een return of heeft een return dieper in zijn boomstructuur liggen
          $hasReturn = true;
          if($type == Type.UNKNOWN){//1e return
            $type = cer.type;
            log.debug("setting type to " + cer.type);
          } else {//Alle 2..ne returns moeten zelfde type hebben als 1e
            if(cer.type != $type){
              throw new IllegalFunctionDefinitionException("Multiple return types");
            }
          }
        }
      }
      //Als type nog unknown is: Er is geen return geweest, effectieve type is dan VOID
      $type = $type == Type.UNKNOWN ? Type.VOID : $type;
      log.debug("end of function analysis, effective type is "  + $type);
    }
  ;

compoundExpression returns [Type type, Boolean isReturn, Boolean hasReturn]
  : expr=expression { ch.st($expr.tree, $expr.type); $type = $expr.type; $isReturn=false; $hasReturn=$expr.hasReturn; }
  | dec=declaration { ch.st($dec.tree, $dec.type); $type = $dec.type; $isReturn=false; $hasReturn=false; }
  | ^(ret=RETURN expr=expression) { $type = $expr.type; $isReturn=true; $hasReturn=$expr.hasReturn; }
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression returns [Type type, Boolean hasReturn]
  : ^(op=BECOMES base=expression sec=expression) { if($base.type==Type.UNKNOWN) ch.changeType($base.tree, $sec.type); ch.st($sec.tree, $sec.type); ch.st($base.tree, $base.type); $type = ch.applyBecomes($base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=OR base=expression sec=expression) { ch.st($base.tree, $base.type); ch.st($sec.tree, $sec.type); $type = ch.apply($op, $base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=AND base=expression sec=expression)  { ch.st($base.tree, $base.type); ch.st($sec.tree, $sec.type); $type = ch.apply($op, $base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression) { ch.st($base.tree, $base.type); ch.st($sec.tree, $sec.type); $type = ch.apply($op, $base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=(PLUS|MINUS) base=expression sec=expression) { ch.st($base.tree, $base.type); ch.st($sec.tree, $sec.type); $type = ch.apply($op, $base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) { ch.st($base.tree, $base.type); ch.st($sec.tree, $sec.type); $type = ch.apply($op, $base.tree, $sec.tree); $hasReturn=false; }
  | ^(op=NOT base=expression) { ch.st($base.tree, $base.type); $type = ch.apply($op, $base.tree); $hasReturn=false; }
  | sim=simpleExpression  { $type = ch.st($sim.tree, $sim.type); $hasReturn = $sim.hasReturn; }
  ;
  
simpleExpression returns [Type type, Boolean hasReturn]
  : atom                                     { $type = ch.st($atom.tree, $atom.type); $hasReturn=false;}
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | fc=functionCall                          { $type = ch.st($fc.tree, $fc.type); $hasReturn=false;}
  | variable                                 { $type = ch.st($variable.tree, $variable.type); $hasReturn=false;}
  | paren                                    { $type = ch.st($paren.tree, $paren.type); $hasReturn=false;}
  | cce=closedCompoundExpression             { $type = ch.st($cce.tree, $cce.type); $hasReturn=false;}
  | s=statements                             { $type = ch.st($s.tree, $s.type); $hasReturn=$s.hasReturn;}
  ;
  
statements returns [Type type, Boolean hasReturn]
  : ifState=ifStatement
    { $type = ch.st($ifState.tree, $ifState.type); 
      $hasReturn = $ifState.hasReturn;
    }
  | whileState=whileStatement
    { $type = ch.st($whileState.tree, $whileState.type);
      $hasReturn = $whileState.hasReturn; 
    }
    
  ;

ifStatement returns [Type type, Boolean hasReturn]
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
      { ch.testTypes($cond.type, Type.BOOL);
        if(elseExpr==null){
          $type = ch.st($ifExpr.tree, $ifExpr.type);
          $hasReturn = $ifExpr.hasReturn;
        }else{
          ch.st($ifExpr.tree, $ifExpr.type);
          $type = ch.testTypes($ifExpr.type, $elseExpr.type);
          $hasReturn = $ifExpr.hasReturn && $elseExpr.hasReturn;
        }
      }
  ;  
    
whileStatement returns [Type type, Boolean hasReturn]
  : ^(WHILE cond=expression loop=closedCompoundExpression)
      { ch.testTypes($cond.type, Type.BOOL);
        $type = $loop.type;
        $hasReturn = $loop.hasReturn;
      }
  ;    
    
primitive returns [Type type]
  : VOID      { $type = Type.VOID; }
  | BOOLEAN   { $type = Type.BOOL; }
  | CHAR      { $type = Type.CHAR; }
  | INT       { $type = Type.INT; }
  | STRING    { $type = Type.STRING; }
  ;

atom returns [Type type]
  : INT_LITERAL                   { $type = ch.st($INT_LITERAL,Type.INT); }
  | NEGATIVE INT_LITERAL          { $type = ch.st($INT_LITERAL,Type.INT); }
  | CHAR_LITERAL                  { $type = ch.st($CHAR_LITERAL,Type.CHAR); }
  | STRING_LITERAL                { $type = ch.st($STRING_LITERAL,Type.STRING); }
  | TRUE                          { $type = ch.st($TRUE,Type.BOOL); }
  | FALSE                         { $type = ch.st($FALSE,Type.BOOL); }
  //TODO: Hier exceptie gooien zodra iets anders dan deze tokens wordt gelezen
  ;
  
paren returns [Type type]
  : ^(PAREN expression)           { $type = ch.st($PAREN, $expression.type);
                                    ch.st($expression.tree, $expression.type);
                                  }
  ;
  
variable returns [Type type]
  : id=IDENTIFIER
    { /* get the type of variable and set it on the node */
      $type = ch.st($id, ch.apply(id.getText()));
    }
  ;
  
functionCall returns [Type type]
  @init{
    List<Type> args = new ArrayList<Type>();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {args.add($ex.type);})*)
    { $type = ch.apply($id.text, args);
    }
  ; 