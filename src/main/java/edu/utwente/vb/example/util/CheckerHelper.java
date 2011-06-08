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
	
	
	/**
	 * Set node type by type name
	 * @param node node to set type on
	 * @param type type to set/locate
	 * @throws IllegalArgumentException when type is unknown
	 */	
	public void tbn(TypedNode node, String type) {
		checkNotNull(node); checkNotNull(type);
		log.debug("tbn: " + node + " as " + type);
		((TypedNode) node).setType(Type.byName(type));
	}
	
	/**
	 * Directly set the node type by Type refernce
	 * @param node node to set the type on
	 * @param type type to set
	 */
	public Type st(TypedNode node, Type type) {
		checkNotNull(node); 
		checkNotNull(type);
		log.debug("st: " + node + " as " + type);
		((TypedNode) node).setType(type);
		return type;
	}
	
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table
	 * @param node
	 * @param type
	 * @throws IllegalVariableDefinitionException 
	 */
	public void declareVar(TypedNode node, Type type) throws IllegalVariableDefinitionException{
		checkNotNull(node); checkNotNull(type);
		VariableId varId = new VariableId(node, type);
		
		log.debug("declareVar: " + node + " as " + type);
		
		symbolTable.put(varId);
	}
	
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table, this time type is entered by it's String representation
	 * @param node	the node holding the binding occurrence of this variable
	 * @param type	the type of this variable
	 * @throws IllegalVariableDefinitionException when variable conflicts with existing variable in symboltable
	 */
	public void declareVar(TypedNode node, String type) throws IllegalVariableDefinitionException{
		checkNotNull(node); checkNotNull(type);
		
		log.debug("declareVar: " + node + " as " + type);
		
		VariableId varId = new VariableId(node, Type.byName(type));
		symbolTable.put(varId);
	}
	
	//TODO: Iets met herkenning van constants, dat ze onaanpasbaar zijn enzo;
	/* Ideen voor bovenstaande todo:
	 * Wat mij betreft zijn er twee oplossingen,
	 * 1) de symboltable nog een aparte table geven voor constants. In checker kan bij becomes regel gekeken worden of lhs in constanttable staat, in dat geval exceptie gooien
	 * 2) variableId uitbreiden met interne variabele isConstant. Scheelt het maken van een aparte table, wel moet dan isConstant steeds op false gezet worden bij declaratie van variabele
	 */
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table, this time type is entered by it's String representation
	 * @param node	the node holding the binding occurrence of this constant
	 * @param type	the type of this constant
	 * @throws IllegalVariableDefinitionException when variable conflicts with existing constant in symboltable
	 */
	public void declareConst(TypedNode node, Type type) throws IllegalVariableDefinitionException{
		VariableId varId = new VariableId(node, type);
		
		log.debug("declareConst " + node + " as " + type);
		
		symbolTable.put(varId);
	}
	
	public void declareConst(TypedNode node, String type) throws IllegalVariableDefinitionException{
		VariableId varId = new VariableId(node, Type.byName(type));
		
		log.debug("declareConst " + node + " as " + type);
		
		symbolTable.put(varId);
	}
	
	/**
	 * Method to declare a function
	 * @param node 			the node holding the binding occurrence of this function
	 * @param returnType 	the return-type of this function
	 * @param params		a list of parameters that this function needs
	 * @throws IllegalFunctionDefinitionException when function conflicts with existing function in symboltable
	 * @throws IllegalFunctionDefinitionException when one of the parameters has type VOID
	 */
	public void declareFunction(TypedNode node, Type returnType, List<TypedNode> params) throws IllegalFunctionDefinitionException{
		List<VariableId> ids = new ArrayList<VariableId>();
		for(TypedNode param : params)
			ids.add(new VariableId(param, param.getNodeType()));
		
		FunctionId funcId = new FunctionId(node, returnType, ids);
		
		log.debug("declareFunction " + node + " (" + params.toString() +  ") -> "  + returnType);
		
		symbolTable.put(funcId);
	}
	
	
	public void inferBecomes(TypedNode root, TypedNode lhs, TypedNode rhs) throws IncompatibleTypesException {
		log.debug("inferBecomes " + root + " lhs: " + lhs + " rhs: " + rhs);
		if(lhs.getNodeType() != Type.UNKNOWN){
			lhs.setNodeType(rhs.getNodeType());
		} else if(lhs.getNodeType().equals(rhs.getNodeType())){
			throw new IncompatibleTypesException(root, "type " + rhs.getNodeType() + " cannot be assigned to variable of type " + lhs.getNodeType());
		}
		
		root.setType(lhs.getNodeType());
	}
	
	/**
	 * Apply a function/operator (given its arguments) and return the type
	 * @param operator
	 * @param applied
	 * @return
	 * @throws SymbolTableException
	 */
	public Type apply(TypedNode operator, TypedNode... applied) throws SymbolTableException{
		return symbolTable.apply(operator.getText(), TypedNode.extractTypes(applied)).getType();
	}
	
	public Type apply(TypedNode operator, List<Type> applied) throws SymbolTableException{
		return symbolTable.apply(operator.getText(), applied).getType();
	}
	
	public Type apply(TypedNode operator, Type... applied) throws SymbolTableException{
		return symbolTable.apply(operator.getText(), applied).getType();
	}
	
	public Type apply(String op, List<Type> applied) throws SymbolTableException{
		return symbolTable.apply(op, applied).getType();
	}
	
	public Type apply(String op, Type... applied) throws SymbolTableException{
		return symbolTable.apply(op, applied).getType();
	}
	
	/**
	 * Method to identify the type of a variable. Throws exception if variable is not found within reachable scopes.
	 * @param identifier
	 * @return the type of the variable which name matches identifier
	 * @throws SymbolTableException 
	 */
	public Type apply(String identifier) throws SymbolTableException{
		log.debug("getVarType " + identifier);	
		return symbolTable.apply(identifier).getType();
	}
	
	/**
	 * Throws exception if two given types are not equal
	 * @param type1
	 * @param type2
	 */
	public Type testTypes(Type... types) throws IncompatibleTypesException{
		for(int i = 0; i < checkNotNull(types).length; i++){
			if(!checkNotNull(types[i]).equals(checkNotNull(types[(i + 1) % types.length])))
				throw new IncompatibleTypesException("type " + types[i] + " ander type dan RHS " + types[i+1]);
		}
		return types[0];
	}
	
	public Type testTypes(List<TypedNode> nodes) throws IncompatibleTypesException{
		return testTypes(TypedNode.extractTypes(nodes).toArray(new Type[0]));
	}
	
	/**
	 * Throws exception if two given types are equal
	 * @param type1
	 * @param type2
	 */
	public Type testNotType(Type... types) throws IncompatibleTypesException{
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
