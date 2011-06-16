package edu.utwente.vb.example.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utwente.vb.exceptions.IllegalFunctionDefinitionException;
import edu.utwente.vb.exceptions.IllegalVariableDefinitionException;
import edu.utwente.vb.exceptions.IncompatibleTypesException;
import edu.utwente.vb.exceptions.SymbolTableException;
import edu.utwente.vb.symbols.FunctionId;
import edu.utwente.vb.symbols.Id;
import edu.utwente.vb.symbols.SymbolTable;
import edu.utwente.vb.symbols.Type;
import edu.utwente.vb.symbols.VariableId;
import edu.utwente.vb.tree.TypedNode;

public class CheckerHelper {
	private Logger log = LoggerFactory.getLogger(CheckerHelper.class);
	private final SymbolTable<TypedNode> symbolTable;
	
	/**
	 * Constructor of CheckerHelper
	 */
	public CheckerHelper() {
		symbolTable = new SymbolTable<TypedNode>();
	}
	
	/**
	 * Constructor of CheckerHelper, sets symboltable
	 * @param tab the symboltable of from now on used in this CheckerHelper
	 */
	public CheckerHelper(SymbolTable<TypedNode> tab){
		checkNotNull(tab);
		this.symbolTable = tab;
	}
	
	/**
	 * Returns symboltable of this CheckerHelper
	 * @return symboltable
	 */
	public SymbolTable<TypedNode> getSymbolTable() {
		return symbolTable;
	}
	
	/**
	 * Open symboltable scope 
	 */
	public void openScope(){
		symbolTable.openScope();
	}
	
	/**
	 * Closes current symboltable scope
	 * @throws SymbolTableException when closing is not possible
	 */
	public void closeScope() throws SymbolTableException{
		symbolTable.closeScope();
	}
	
	public TypedNode applyVariable(TypedNode id) throws SymbolTableException{
		return symbolTable.apply(id.getText()).getNode();
	}
	
	public TypedNode applyFunction(TypedNode id, List<Type> types) throws SymbolTableException{
		return symbolTable.apply(id.getText(), types).getNode();
	}
	
	/**
	 * Use of an assignment
	 * @param id meestal de '=' operator - maar handig omdat je meteen het type set
	 * @param lhs
	 * @param rhs
	 * @return
	 * @throws SymbolTableException
	 */
	public TypedNode applyBecomesAndSetType(TypedNode id, TypedNode lhs, TypedNode rhs) throws SymbolTableException{
		// variabele:
		VariableId<TypedNode> lhvar = symbolTable.apply(lhs.getText()); 
		if(lhvar.isConstant()){
			throw new IllegalFunctionDefinitionException("Trying to assign to the constant " + lhs.toString());
		}
		return applyFunctionAndSetType(id, lhs, rhs);
	}
	
	/**
	 * Apply a function/operator (given its arguments) and set the type
	 * @param operator
	 * @param applied
	 * @return
	 * @throws SymbolTableException
	 */
	public TypedNode applyFunctionAndSetType(TypedNode id, TypedNode... args) throws SymbolTableException{
		FunctionId<TypedNode> funcId = symbolTable.apply(id.getText(), TypedNode.extractTypes(args));
		id.setNodeType(funcId.getType());
		return id;
	}
	
	public Type copyNodeType(TypedNode rhs, TypedNode... targets){
		checkNotNull(rhs);
		log.debug("src:" + rhs);
		for(TypedNode target : targets){
			log.debug("Target: " + target.toString());
			target.setNodeType(rhs.getNodeType());//impliciet checkNotNull
		}
		return rhs.getNodeType();
	}
	
	public Type setNodeType(final Type type, TypedNode... nodes){
		for(TypedNode n : nodes){
			n.setNodeType(type);//impliciete null check door de-reference van object
		}
		return type;
	}
	
	public Type setNodeType(final String type, TypedNode... nodes){
		return setNodeType(Type.byName(type), nodes);
	}
	
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table
	 * @param node
	 * @param type
	 * @throws IllegalVariableDefinitionException 
	 * @throws IncompatibleTypesException 
	 */
	public VariableId<TypedNode> declareVar(TypedNode node) throws IllegalVariableDefinitionException{
		checkNotNull(node);
		
		Type type = checkNotNull(node.getNodeType());
		
		if(type==Type.VOID)
			throw new IllegalVariableDefinitionException("variale cannot be declared as type void");
		
		VariableId varId = new VariableId(node, type);
		
		log.debug("declareVar: {} as {}", node, type);
		
		symbolTable.put(varId);
		return varId;
	}
	
	public VariableId<TypedNode> declareConst(TypedNode node) throws IllegalVariableDefinitionException{
		VariableId<TypedNode> var = declareVar(node);
		var.setConstant(true);
		return var;
	}
	
	/**
	 * Method to declare a function
	 * @param node 			the node holding the binding occurrence of this function
	 * @param returnType 	the return-type of this function
	 * @param params		a list of parameters that this function needs
	 * @throws IllegalFunctionDefinitionException when function conflicts with existing function in symboltable
	 * @throws IllegalFunctionDefinitionException when one of the parameters has type VOID
	 */
	public FunctionId<TypedNode> declareFunction(TypedNode node, Type returnType, List<TypedNode> params) throws IllegalFunctionDefinitionException{
		List<VariableId> ids = new ArrayList<VariableId>();
		for(TypedNode param : params)
			ids.add(new VariableId(param, param.getNodeType()));
		
		FunctionId funcId = new FunctionId(node, returnType, ids);
		
		log.debug("declareFunction {} ({}) -> {}", new Object[]{node, params.toString(), returnType});
		
		symbolTable.put(funcId);
		
		return funcId;
	}
	
//	/**
//	 * Set node type by type name
//	 * @param node node to set type on
//	 * @param type type to set/locate
//	 * @throws IllegalArgumentException when type is unknown
//	 */	
//	public void tbn(TypedNode node, String type) {
//		checkNotNull(node); checkNotNull(type);
//		log.debug("tbn: {} as {}", node, type);
//		((TypedNode) node).setType(Type.byName(type));
//	}
//	
//	/**
//	 * Directly set the node type by Type refernce
//	 * @param node node to set the type on
//	 * @param type type to set
//	 */
//	public Type st(TypedNode node, Type type) {
//		checkNotNull(node); 
//		checkNotNull(type);
//		log.debug("st: {} as {}", node, type);
//		((TypedNode) node).setType(type);
//		return type;
//	}
//	
//	/**
//	 * Puts node node of type type in the currently opened scope of the symbol table
//	 * @param node
//	 * @param type
//	 * @throws IllegalVariableDefinitionException 
//	 * @throws IncompatibleTypesException 
//	 */
//	public void declareVar(TypedNode node, Type type) throws IllegalVariableDefinitionException{
//		checkNotNull(node); checkNotNull(type);
//		
//		if(type==Type.VOID)
//			throw new IllegalVariableDefinitionException("variale cannot be declared as type void");
//		
//		VariableId varId = new VariableId(node, type);
//		
//		log.debug("declareVar: {} as {}", node, type);
//		
//		symbolTable.put(varId);
//	}
//	
//	/**
//	 * Puts node node of type type in the currently opened scope of the symbol table, this time type is entered by it's String representation
//	 * @param node	the node holding the binding occurrence of this variable
//	 * @param type	the type of this variable
//	 * @throws IllegalVariableDefinitionException when variable conflicts with existing variable in symboltable
//	 */
//	public void declareVar(TypedNode node, String type) throws IllegalVariableDefinitionException{
//		Type varType = Type.byName(type);
//		declareVar(node, varType);
//	}
//	
//	/* Ideen voor bovenstaande todo:
//	 * Wat mij betreft zijn er twee oplossingen,
//	 * 1) de symboltable nog een aparte table geven voor constants. In checker kan bij becomes regel gekeken worden of lhs in constanttable staat, in dat geval exceptie gooien
//	 * 2) variableId uitbreiden met interne variabele isConstant. Scheelt het maken van een aparte table, wel moet dan isConstant steeds op false gezet worden bij declaratie van variabele
//	 */
//	/**
//	 * Puts node node of type type in the currently opened scope of the symbol table, this time type is entered by it's String representation
//	 * @param node	the node holding the binding occurrence of this constant
//	 * @param type	the type of this constant
//	 * @throws IllegalVariableDefinitionException when variable conflicts with existing constant in symboltable
//	 */
//	public void declareConst(TypedNode node, Type type) throws IllegalVariableDefinitionException{
//		if(type==Type.VOID)
//			throw new IllegalVariableDefinitionException("constant cannot be declared as type void");
//			
//		VariableId varId = new VariableId(node, type);
//		varId.setConstant(true);
//		
//		log.debug("declareConst {} as {}", node, type);
//		
//		symbolTable.put(varId);
//	}
//	
//	public void declareConst(TypedNode node, String type) throws IllegalVariableDefinitionException{
//		Type constType = Type.byName(type);
//		declareVar(node, constType);
//	}
//	
//	
//	/**
//	 * Changes the type of the given variable to the given type
//	 * @param node
//	 * @param type
//	 * @throws SymbolTableException
//	 */
//	public void changeType(TypedNode node, Type type) throws SymbolTableException{
//		String varName = node.getText();
//		VariableId<TypedNode> var = symbolTable.apply(varName);
//		
//		try{
//			var.updateType(type);
//		}catch(IllegalArgumentException e){
//			throw new SymbolTableException("Cannot assign the type " + type + "to variable " + node.getText());
//		}
//	}
//	
//	public Type apply(TypedNode operator, TypedNode... applied) throws SymbolTableException{
//		return symbolTable.apply(operator.getText(), TypedNode.extractTypes(applied)).getType();
//	}
//	
//	public Type apply(TypedNode operator, List<Type> applied) throws SymbolTableException{
//		return symbolTable.apply(operator.getText(), applied).getType();
//	}
//	
//	public Type apply(TypedNode operator, Type... applied) throws SymbolTableException{
//		return symbolTable.apply(operator.getText(), applied).getType();
//	}
//	
//	public Type apply(String operator, TypedNode... applied) throws SymbolTableException{
//		return symbolTable.apply(operator, TypedNode.extractTypes(applied)).getType();
//	}
//	
//	public Type apply(String op, List<Type> applied) throws SymbolTableException{
//		return symbolTable.apply(op, applied).getType();
//	}
//	
//	public Type apply(String op, Type... applied) throws SymbolTableException{
//		return symbolTable.apply(op, applied).getType();
//	}
//	
//	
//	/**
//	 * Method to identify the type of a variable. Throws exception if variable is not found within reachable scopes.
//	 * @param identifier
//	 * @return the type of the variable which name matches identifier
//	 * @throws SymbolTableException 
//	 */
//	public Type apply(String identifier) throws SymbolTableException{
//		log.debug("getVarType {}", identifier);	
//		return symbolTable.apply(identifier).getType();
//	}
//	
	/**
	 * Throws exception if two given types are not equal
	 * @param type1
	 * @param type2
	 */
	public Type checkTypes(Type... types) throws IncompatibleTypesException{
		for(int i = 0; i < checkNotNull(types).length; i++){
			if(!checkNotNull(types[i]).equals(checkNotNull(types[(i + 1) % types.length])))
				throw new IncompatibleTypesException("type " + types[i] + " ander type dan RHS " + types[i+1]);
		}
		return types[0];
	}
	
	public Type checkTypes(List<TypedNode> nodes) throws IncompatibleTypesException{
		return checkTypes(TypedNode.extractTypes(nodes).toArray(new Type[0]));
	}
	
	public Type checkTypes(TypedNode... nodes) throws IncompatibleTypesException{
		return checkTypes(TypedNode.extractTypes(nodes).toArray(new Type[0]));
	}
	
	/**
	 * Throws exception if two given types are equal
	 * @param type1
	 * @param type2
	 */
	public Type checkNotType(Type... types) throws IncompatibleTypesException{
		for(int i = 0; i < checkNotNull(types).length; i++){
			if(checkNotNull(types[i]).equals(types[(i + 1) % types.length]))
				throw new IncompatibleTypesException("type " + types[i] + " is gelijk aan " + types[i+1]);
		}
		return types[0];
	}
	
	/**
	 * Create a builtin function's functionId
	 * @param token token text
	 * @param lhs left hand side type
	 * @param rhs right hand side type
	 * @return new functionId
	 * @throws IllegalFunctionDefinitionException 
	 */
	public static FunctionId<TypedNode> createBuiltin(String token, Type ret, Type lhs, Type rhs) throws IllegalFunctionDefinitionException{
		return createFunctionId(token, ret, createVariableId("lhs", lhs), createVariableId("rhs", rhs));
	}
	
	public static FunctionId<TypedNode> createFunctionId(String token, Type type, VariableId<TypedNode>... p) throws IllegalFunctionDefinitionException{
		return new FunctionId<TypedNode>(byToken(token), type, Lists.newArrayList(p));
	}
	
	public static VariableId<TypedNode> createVariableId(String token, Type type){
		return new VariableId<TypedNode>(byToken(token), type);
	}
	
	public static TypedNode byToken(String token){
		return new TypedNode(new CommonToken(-1, token));
	}
}
