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
  : (declaration | functionDef)* -> declaration* functionDef*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
    { 
      ch.copyNodeType($prim.tree, $VAR, $IDENTIFIER); //type op VAR, IDENTIFIER 
      ch.declareVar($IDENTIFIER); //declare var met zijn huidige type
      if($rvd.tree != null){
        ch.checkTypes($IDENTIFIER, $rvd.tree); //kijk of typen overeen komen
      }
    }
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
    { 
      ch.copyNodeType($prim.tree, $CONST, $IDENTIFIER); 
      ch.declareConst($IDENTIFIER); 
      ch.checkTypes($IDENTIFIER, $cvd.tree);
    } 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
    {   
      ExampleType inferredType = $run.tree != null ? $run.tree.getNodeType() : ExampleType.UNKNOWN;//Als run niet leeg is, type van de run, anders UNKNOWN. Bij ExampleType.UNKNOWN kan hij later bij BECOMES geinferred worden 
      ch.setNodeType(inferredType, $INFERVAR, $IDENTIFIER);
      ch.declareVar($IDENTIFIER); 
    }
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
    {   
        ch.copyNodeType($cons.tree, $INFERCONST, $IDENTIFIER); 
        ch.declareConst($IDENTIFIER);
    }
  ;
  
valueDeclaration //becomes bij het declareren van een variable
  : BECOMES ce=compoundExpression { ch.copyNodeType($ce.tree, $BECOMES); }
  ;

 /**
 * Definieer een functie. 
 * Eerste stap:
 * - verzamel type van argumenten
 * - declarareer functie met return type (als dat expliciet er staat)
 *
 * [scope]
 * declareer de formele parameters
 *
 * ... parse body ...
 * [/scope]
 *
 *
 * Tweede:
 * - check formele return type tegen effectieve return type
 * - set return type als het geinferred moest worden. 
 */
functionDef
  @init{
    List<TypedNode> formalArguments = Lists.newArrayList();
    FunctionId<TypedNode> functionId;
    ExampleType returnType = ExampleType.UNKNOWN;
  }
  : ^(FUNCTION (t=primitive {returnType = $t.tree.getNodeType(); })? IDENTIFIER 
          (p=parameterDef { formalArguments.add($p.id_node); })*  
          {
            functionId = ch.declareFunction($IDENTIFIER, returnType, formalArguments);
            ch.openScope();
            ch.declareVariables(formalArguments);//Nu de parameters definieren in de scope van de functie
          }
    returnTypeNode=closedCompoundExpression) 
    { //Na functie body -> close scope
      ch.closeScope();
      if(returnType == ExampleType.UNKNOWN){//return type is unknown -> niet ingevuld. geen geldige content voor primitive namelijk
        functionId.updateType(ch.setNodeType($returnTypeNode.return_type, $IDENTIFIER));
      } else if(!returnType.equals($returnTypeNode.return_type)){//typen ongelijk van definitie & body -> error.
        throw new IllegalFunctionDefinitionException("Return types do not match; " + returnType + " and " + $returnTypeNode.return_type);
      }
    } 
  ;
  
/**
 * Een applied occurrence
 */
parameterDef returns [TypedNode id_node]//kopieer de node van de applied occurrence
  : ^(FORMAL primitive IDENTIFIER) { ch.copyNodeType($primitive.tree, $IDENTIFIER, $FORMAL); $id_node = $IDENTIFIER; }
  ; 

closedCompoundExpression returns [ExampleType return_type = null;]
  : { ch.openScope(); } ^(SCOPE (ce=compoundExpression { 
          if($ce.return_type != ExampleType.UNKNOWN){
            log.debug("Detected return type {}", $ce.return_type);
            if($return_type != null && $ce.return_type != $return_type){
              throw new IllegalFunctionDefinitionException("Incompatible return types; " + $return_type + " and " + $ce.return_type);
            }
            $return_type = $ce.return_type;
          }
      })*)//voor alle compound expressies: Kijk of er al een return type is. Is dit er, dan kijk je of dit overeen komt met het nieuwe type. Anders wordt het geziene type het return type
    { 
      if($return_type == null){//Als je geen return type ziet is het return type van de close compound void.
        $return_type = ExampleType.VOID;
      }
      log.debug("Returning type {}", $return_type);
      ch.closeScope(); 
    }
  ;

compoundExpression returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : expr=expression { $return_type = $expr.return_type; }
  | ^(RETURN expr=expression) { $return_type = ch.copyNodeType($expr.tree, $RETURN); }
  | declaration 
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression  returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : ^(op=BECOMES base=expression sec=expression) 
          { ch.applyBecomesAndSetType($op, $base.tree, $sec.tree); }//infer van type zit in CheckerHelper 
  | ^(op=OR base=expression sec=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=AND base=expression sec=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(LTEQ | GTEQ | GT | LT | EQ | NOTEQ) base=expression sec=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(PLUS|MINUS) base=expression sec=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=(MULT | DIV | MOD) base=expression sec=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree, $sec.tree); }
  | ^(op=NOT base=expression) 
          { ch.applyFunctionAndSetType($op, $base.tree); }
  | sim=simpleExpression 
          { $return_type = sim.return_type; }
  ;
  
simpleExpression returns [ExampleType return_type = ExampleType.UNKNOWN;] 
  : atom
  //Voorrangsregel, bij dubbelzinnigheid voor functionCall kiezen. Zie ANTLR reference paginga 58.
  //Functioncall zou gevoelsmatig meer onder 'statements' thuishoren. In dat geval werkt de voorrangsregel echter niet meer.
  | functionCall
  | variable
  | paren { $return_type = $paren.return_type; }
  | cce=closedCompoundExpression { $return_type = cce.return_type; }
  | s=statements { $return_type = $s.return_type; }
  ;
  
statements returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : ifState=ifStatement { $return_type = $ifState.return_type; }
  | whileState=whileStatement { $return_type = $whileState.return_type; }
  ;

ifStatement returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
  {
  ch.setNodeType(ExampleType.VOID, $IF);
  ch.checkTypes(ExampleType.BOOL, $cond.start.getNodeType());
  if($elseExpr.tree != null)
    ch.checkTypes($ifExpr.return_type, $elseExpr.return_type); 
  $return_type = $ifExpr.return_type; 
  }
  ;  
    
whileStatement returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : ^(WHILE cond=expression loop=closedCompoundExpression)
  {
  ch.checkTypes(ExampleType.BOOL, $cond.start.getNodeType());
  ch.setNodeType(ExampleType.VOID, $WHILE); 
  $return_type = $loop.return_type; }
  ;    
    
primitive
  : VOID      { $VOID.setNodeType(ExampleType.VOID); }
  | BOOLEAN   { $BOOLEAN.setNodeType(ExampleType.BOOL); }
  | CHAR      { $CHAR.setNodeType(ExampleType.CHAR); }
  | INT       { $INT.setNodeType(ExampleType.INT); }
  | STRING    { $STRING.setNodeType(ExampleType.STRING); }
  ;

atom
  : INT_LITERAL           { ch.setNodeType(ExampleType.INT, $INT_LITERAL);}
  | NEGATIVE INT_LITERAL  { ch.setNodeType(ExampleType.INT, $NEGATIVE, $INT_LITERAL);}
  | CHAR_LITERAL          { ch.setNodeType(ExampleType.CHAR, $CHAR_LITERAL);}
  | STRING_LITERAL        { ch.setNodeType(ExampleType.STRING, $STRING_LITERAL);}
  | TRUE                  { ch.setNodeType(ExampleType.BOOL, $TRUE);}
  | FALSE                 { ch.setNodeType(ExampleType.BOOL, $FALSE);}
  ;
  
paren returns [ExampleType return_type = ExampleType.UNKNOWN;]
  : ^(PAREN expression)           { ch.copyNodeType($expression.tree, $PAREN); $return_type = $expression.return_type; }
  ;
  
variable
  : id=IDENTIFIER { ch.setBindingNode($id, ch.applyVariable($id)); }
  ;
  
functionCall
  @init{
    List<ExampleType> args = Lists.newArrayList();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {args.add($ex.tree.getNodeType());})*)
    { ch.setBindingNodeFunction($id, ch.applyFunction($id, args));
      ch.copyNodeType($id, $CALL);
    }
  ; 