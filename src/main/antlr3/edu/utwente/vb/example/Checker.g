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
  
  import java.util.List;
  
  /** imports van gebruikte Java-hulpklassen **/
  import edu.utwente.vb.example.util.*;
  import edu.utwente.vb.symbols.*;
  import edu.utwente.vb.tree.*;
  import edu.utwente.vb.exceptions.*;
    
    /** import van gebruikte Guava-library **/
  import com.google.common.collect.Lists;
  import static com.google.common.base.Preconditions.checkNotNull;
  
  /** Logger */
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
}

@rulecatch { 
    catch (RecognitionException e) {
      nrErr += 1;
      emitErrorMessage("[Example] checker error [" + nrErr + "] - " + e.getMessage());
      if(debug_mode==true) 
        throw e;
    }
    catch (RuntimeException e){
      if(debug_mode==true){ 
        throw e;
      }
  }
}

@members{
  /**
   * Een instantie van CheckerHelper, welke alle hulpmethoden bevat voor Checker.g.
   * De beoogde architectuur is er een met zo min mogelijk Java-code geintegreerd in de ANTLR-specificaties,
   * het is hierbij van belang Java-code zoveel mogelijk in Java-klassen te plaatsen.
   **/
  private CheckerHelper ch;
  
  /** 
   * Zorgt voor nette logfiles welke informatie geven over alle handelingen doorlopen in de checker (en indirect CheckerHelper). 
   * Deze logging is zeer handig gedurende het debuggen van de Example Compiler
   **/
  private Logger log = LoggerFactory.getLogger(Checker.class);
  
  /** Zorgt voor doorgooien excepties, zodat testsuite deze kan waarnemen **/
  private static boolean debug_mode = false;
  
  /** Teller voor het aantal waargenomen fouten in het Example-programma **/
  protected int nrErr = 0;

  /** Teller voor het aantal waargenomen fouten in het Example-programma **/
  public int nrErrors() {
    return nrErr;
  } 
  
  /**
   * Zet debug_mode aan. Dit heeft tot gevolg excepties worden doorgegooid zodat de testsuite deze kan detecteren.
   * Deze methode wordt in Compiler.java aangeroepen na het aanmaken van de Parser 
   */
  public void setDebug(){
    debug_mode = true;
  }
  
  /** 
  * Compositie met hulp van een CheckerHelper. Alle hulpfuncties in members stoppen is een slechte gewoonte.
  * Inheritance is niet mogelijk omdat hij wisselt tussen DebugTreeParser en TreeParser als super type
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
  : 
                        { checkNotNull(this.ch); } 
    ^(PROGRAM content)
  ;

content
  : (declaration | functionDef)* -> declaration* functionDef*
  ;
  
declaration
  : ^(VAR prim=primitive IDENTIFIER rvd=valueDeclaration?) 
										    { //type op VAR, IDENTIFIER 
										      ch.copyNodeType($prim.tree, $VAR, $IDENTIFIER);
										      //declare var met zijn huidige type 
										      ch.declareVar($IDENTIFIER); 
										      if($rvd.tree != null){
										        //kijk of typen overeen komen
										        ch.checkTypes($IDENTIFIER, $rvd.tree); 
										      }
										    }
  | ^(CONST prim=primitive IDENTIFIER cvd=valueDeclaration) 
										    { ch.copyNodeType($prim.tree, $CONST, $IDENTIFIER); 
										      ch.declareConst($IDENTIFIER); 
										      ch.checkTypes($IDENTIFIER, $cvd.tree); } 
  | ^(INFERVAR IDENTIFIER run=valueDeclaration?) 
										    { ExampleType inferredType = $run.tree != null ? $run.tree.getNodeType() : ExampleType.UNKNOWN;//Als run niet leeg is, type van de run, anders UNKNOWN. Bij ExampleType.UNKNOWN kan hij later bij BECOMES geinferred worden 
										      ch.setNodeType(inferredType, $INFERVAR, $IDENTIFIER);
										      ch.declareVar($IDENTIFIER); }
  | ^(INFERCONST IDENTIFIER cons=valueDeclaration) 
										    {   ch.copyNodeType($cons.tree, $INFERCONST, $IDENTIFIER); 
										        ch.declareConst($IDENTIFIER); }
  ;
  
valueDeclaration //becomes bij het declareren van een variable
  : BECOMES ce=compoundExpression 
                        { ch.copyNodeType($ce.tree, $BECOMES); }
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
  : ^(FUNCTION (t=primitive 
                        { returnType = $t.tree.getNodeType(); }
     )? IDENTIFIER (p=parameterDef    
                        { formalArguments.add($p.id_node); }
     )*  
							          { functionId = ch.declareFunction($IDENTIFIER, returnType, formalArguments);
							            ch.openScope();
							            //Nu de parameters definieren in de scope van de functie 
							            ch.declareVariables(formalArguments); }
    returnTypeNode=closedCompoundExpression) 
										    { //Na functie body -> close scope
										      ch.closeScope();
										      if(returnType == ExampleType.UNKNOWN){
										        //return type is unknown -> niet ingevuld. geen geldige content voor primitive namelijk
										        functionId.updateType(ch.setNodeType($returnTypeNode.return_type.type, $IDENTIFIER));
										      } else if(!returnType.equals($returnTypeNode.return_type.type)){
										        //typen ongelijk van definitie & body -> error.
										        throw new IllegalFunctionDefinitionException("Return types do not match; " 
										            + returnType + " and " + $returnTypeNode.return_type);
										      } 
										      
										      if($returnTypeNode.return_type.type != ExampleType.VOID && $returnTypeNode.return_type.isConditional){
										      	throw new IllegalFunctionDefinitionException("Function might not return; Please add a explicit return");
										      }
										    } 
  ;
  
/**
 * Een applied occurrence
 */
parameterDef returns [TypedNode id_node]//kopieer de node van de applied occurrence
  : ^(FORMAL primitive IDENTIFIER) 
                        { ch.copyNodeType($primitive.tree, $IDENTIFIER, $FORMAL); $id_node = $IDENTIFIER; }
  ; 

closedCompoundExpression returns [ReturnData return_type = null;]
  :                     { ch.openScope(); }
    ^(SCOPE (ce=compoundExpression 
									      { 
									          if($ce.return_type != ReturnData.UNKNOWN){
									            log.debug("Detected return type {}", $ce.return_type);
									            if($return_type != null && $ce.return_type.type != $return_type.type){
									              throw new IllegalFunctionDefinitionException("Incompatible return types; " 
									                 + $return_type + " and " + $ce.return_type);
									            }
									            // Kopieer het type als het conditional is of als het nog niet geset is
									            // gevolg:
									            // - unconditional gezien -> die staat er
									            // else: andere
									            if($return_type != null){ 
										            if(!$return_type.isConditional && !($return_type.type == ExampleType.VOID)){
										            	throw new IllegalFunctionDefinitionException("Unreachable code");
										            } 
										            
										            if(!$ce.return_type.isConditional){
										            	$return_type = $ce.return_type;
										            } 						
									            } else {
									            	$return_type = $ce.return_type;
									            }	            
									          }
									      }
      )*)
										    { 
										      if($return_type == null){
										        //Als je geen return type ziet is het return type van de close compound void.
										        $return_type = ReturnData.VOID;
										      }
										      log.debug("Returning type {}", $return_type);
										      ch.closeScope(); 
										    }
  ;

compoundExpression returns [ReturnData return_type = ReturnData.UNKNOWN; ]
  : expr=expression       { $return_type = $expr.return_type; }
  | ^(RETURN expr=expression) 
                          { $return_type = new ReturnData(ch.copyNodeType($expr.tree, $RETURN)); }
  | declaration 
  ;
 
//TODO: Constraint toevoegen, BECOMES mag alleen plaatsvinden wanneer orExpression een variable is
// => misschien met INFERVAR/VARIABLE als LHS + een predicate? 
expression  returns [ReturnData return_type = ReturnData.UNKNOWN;]
  : ^(op=BECOMES lhvar=variable sec=expression) 
                          { ch.applyBecomesAndSetType($op, $lhvar.tree, $sec.tree); }//infer van type zit in CheckerHelper 
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
  
simpleExpression returns [ReturnData return_type = ReturnData.UNKNOWN;] 
  : atom
  | functionCall
  | variable
  | paren                 { $return_type = $paren.return_type; }
  | cce=closedCompoundExpression 
                          { $return_type = cce.return_type; }
  | s=statements          { $return_type = $s.return_type; }
  ;
  
statements returns [ReturnData return_type; ]
  : ifState=ifStatement   { $return_type = $ifState.return_type; }
  | whileState=whileStatement 
                          { $return_type = $whileState.return_type; }
  ;

ifStatement returns [ ReturnData return_type;]
  : ^(IF cond=expression ifExpr=closedCompoundExpression (elseExpr=closedCompoundExpression)?)
												  { 	ch.setNodeType(ExampleType.VOID, $IF);
													    ch.checkTypes(ExampleType.BOOL, $cond.start.getNodeType());
													    if($elseExpr.tree != null){
													    	if($elseExpr.return_type.type == ExampleType.VOID){
													    		$return_type = new ReturnData($ifExpr.return_type, true); 
													    	} else{
														    	ch.checkTypes($ifExpr.return_type.type, $elseExpr.return_type.type);
														    	$return_type = new ReturnData($ifExpr.return_type);
													    	}
													    } else {
													    	$return_type = new ReturnData($ifExpr.return_type, true);
													    }
													    log.debug("Detected IF return type of {} with IF {} and ELSE {}", new Object[]{$return_type, $ifExpr.return_type, $elseExpr.return_type}); 
												    }
  ;  
    
whileStatement returns [ReturnData return_type; ]
  : ^(WHILE cond=expression loop=closedCompoundExpression)
												  { ch.checkTypes(ExampleType.BOOL, $cond.start.getNodeType());
												    ch.setNodeType(ExampleType.VOID, $WHILE); 
												    $return_type = new ReturnData($loop.return_type, true); }
  ;    
    
primitive
  : VOID                  { $VOID.setNodeType(ExampleType.VOID); }
  | BOOLEAN               { $BOOLEAN.setNodeType(ExampleType.BOOL); }
  | CHAR                  { $CHAR.setNodeType(ExampleType.CHAR); }
  | INT                   { $INT.setNodeType(ExampleType.INT); }
  | STRING                { $STRING.setNodeType(ExampleType.STRING); }
  ;

atom
  : INT_LITERAL           { ch.setNodeType(ExampleType.INT, $INT_LITERAL); }
  | CHAR_LITERAL          { ch.setNodeType(ExampleType.CHAR, $CHAR_LITERAL); }
  | STRING_LITERAL        { ch.setNodeType(ExampleType.STRING, $STRING_LITERAL); }
  | TRUE                  { ch.setNodeType(ExampleType.BOOL, $TRUE); }
  | FALSE                 { ch.setNodeType(ExampleType.BOOL, $FALSE); }
  ;
  
paren returns [ReturnData return_type; ]
  : ^(PAREN expression)   { ch.copyNodeType($expression.tree, $PAREN); $return_type = $expression.return_type; }
  ;
  
variable
  : id=IDENTIFIER         { ch.setBindingNode($id, ch.applyVariable($id)); }
  ;
  
functionCall
  @init{
    List<ExampleType> args = Lists.newArrayList();
  }
  : ^(CALL id=IDENTIFIER (ex=expression {args.add($ex.tree.getNodeType());})*)
											    { ch.setBindingNodeFunction($id, ch.applyFunction($id, args));
											      ch.copyNodeType($id, $CALL); }
  ; 