tree grammar ExampleGenerator;

options {
  language = Java;
  output = template;
  ASTLabelType = TypedNode;
}

@members { 
    // The Map dict maps each declared variable upon its address (an Integer)
    // in the global stack base frame, i.e. relative to [SB]. 
    private Map<String,Integer> dict = new HashMap<String,Integer>();
    
    // The variable nextVarAddress holds the address (displacement) 
    // within [SB] for the "next" variable that will be declared.
    private int nextVarAddress = 0;
}

program 
  : ^(PROGRAM c=content)
        -> tamFile(instructions={$c})
  ;

content 
  : (compoundExpression | functionDef)* 
  ;
  
declaration
  : ^(VAR type=primitive IDENTIFIER runtimeValueDeclaration)
  //Constanten kunnen alleen een simpele waarde krijgen
  | ^(CONST type=primitive IDENTIFIER rhs=constantValueDeclaration)
  | ^(INFERVAR IDENTIFIER runtimeValueDeclaration?) 
  | ^(INFERCONST IDENTIFIER constantValueDeclaration) 
  ;
  
runtimeValueDeclaration
  : BECOMES compoundExpression  // {$type = $compountExpression.getNodeType(); }
  ;
 
constantValueDeclaration
  : BECOMES rhs=atom 
  ;
  
functionDef
  : ^(FUNCTION IDENTIFIER (p=parameterDef (p=parameterDef)*)? returnTypeNode=closedCompoundExpression)
  ;
  
parameterDef
  : ^(FORMAL type=primitive IDENTIFIER)
  ; 

closedCompoundExpression
  : ^(SCOPE compoundExpression*)
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
  : (PLUS^ | MINUS^)? INT_LITERAL
  | CHAR_LITERAL                 
  | STRING_LITERAL            
  | TRUE                      
  | FALSE                         
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
