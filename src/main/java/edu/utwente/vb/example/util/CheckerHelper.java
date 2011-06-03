package edu.utwente.vb.example.util;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utwente.vb.exceptions.IncompatibleTypesException;
import edu.utwente.vb.symbols.FunctionId;
import edu.utwente.vb.symbols.Id;
import edu.utwente.vb.symbols.SymbolTable;
import edu.utwente.vb.symbols.Type;
import edu.utwente.vb.symbols.VariableId;
import edu.utwente.vb.tree.AppliedOccurrenceNode;
import edu.utwente.vb.tree.TypedNode;

public class CheckerHelper {
	private final SymbolTable<TypedNode> symbolTable;
	
	public CheckerHelper() {
		symbolTable = new SymbolTable<TypedNode>();
	}
	
	public CheckerHelper(SymbolTable<TypedNode> tab){
		checkNotNull(tab);
		this.symbolTable = tab;
	}
	
	public SymbolTable<TypedNode> getSymbolTable() {
		return symbolTable;
	}
	
	/**
	 * Open symbol table scope 
	 */
	public void openScope(){
		symbolTable.openScope();
	}
	
	/**
	 * Sluit de scope
	 */
	public void closeScope(){
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
		((TypedNode) node).setType(Type.byName(type));
	}
	
	/**
	 * Directly set the node type by Type refernce
	 * @param node node to set the type on
	 * @param type type to set
	 */
	public void st(TypedNode node, Type type) {
		checkNotNull(node); checkNotNull(type);
		((TypedNode) node).setType(type);
	}
	
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table
	 * @param node
	 * @param type
	 */
	public void declareVar(TypedNode node, Type type){
		checkNotNull(node); checkNotNull(type);
		VariableId varId = new VariableId(node, type);
		symbolTable.put(varId);
	}
	
	/**
	 * Puts node node of type type in the currently opened scope of the symbol table, this time type is entered by it's String representation
	 * @param node
	 * @param type
	 */
	public void declareVar(TypedNode node, String type){
		checkNotNull(node); checkNotNull(type);
		VariableId varId = new VariableId(node, Type.byName(type));
		symbolTable.put(varId);
	}
	
	//TODO: Iets met herkenning van constants, dat ze onaanpasbaar zijn enzo;
	public void declareConst(TypedNode node, Type type){
		VariableId varId = new VariableId(node, type);
		symbolTable.put(varId);
	}
	
	public void declareConst(TypedNode node, String type){
		VariableId varId = new VariableId(node, Type.byName(type));
		symbolTable.put(varId);
	}
	
	public void declareFunction(TypedNode node, Type returnType, List<TypedNode> params){
		List<VariableId> ids = new ArrayList<VariableId>();
		for(TypedNode param : params)
			ids.add(new VariableId(param, param.getNodeType()));
		
		FunctionId funcId = new FunctionId(node, returnType, ids);
		symbolTable.put(funcId);
	}
	
	/**
	 * Method to identify the type of a variable. Throws exception if variable is not found within reachable scopes.
	 * @param identifier
	 * @return the type of the variable which name matches identifier
	 */
	public Type getVarType(String identifier){
		List<Id<TypedNode>> matches = symbolTable.get(identifier);
		//TODO: Herkennen welke matchende variabele gereturned moet worden. Hiervoor moet de get van symbolTable waarschijnlijk informatie over de scope van elk ID gaan bevatten.
		return matches.get(0).getType();
	}
	
	public void inferBecomes(TypedNode root, TypedNode lhs, TypedNode rhs) {
		if(!lhs.getNodeType().equals(rhs.getNodeType())){
			//throw new IncompatibleTypesException("LHS " + lhs.getNodeType() + " ander type dan RHS " + rhs.getNodeType());
		}
		root.setNodeType(lhs.getNodeType());
	}
	
	/**
	 * Throws exception if two given types are not equal
	 * @param type1
	 * @param type2
	 */
	public void testTypes(Type type1, Type type2) {
		if(!type1.equals(type2)){
			//TODO: Een exceptie gooien 
			
			//throw new IncompatibleTypesException("LHS " + type1 + " ander type dan RHS " + type2);
		}
	}
	
	/**
	 * Throws exception if two given types are equal
	 * @param type1
	 * @param type2
	 */
	public void testNotType(Type type1, Type type2) {
		if(!type1.equals(type2)){
			//TODO: Een exceptie gooien 
			
			//throw new IncompatibleTypesException("LHS " + type1 + " ander type dan RHS " + type2);
		}
	}
	
	/**
	 * Create a builtin function's functionId
	 * @param token token text
	 * @param lhs left hand side type
	 * @param rhs right hand side type
	 * @return new functionId
	 */
	public static FunctionId<TypedNode> createBuiltin(String token, Type ret, Type lhs, Type rhs){
		return createFunctionId(token, ret, createVariableId("lhs", lhs), createVariableId("rhs", rhs));
	}
	
	public static FunctionId<TypedNode> createFunctionId(String token, Type type, VariableId<TypedNode>... p){
		return new FunctionId<TypedNode>(byToken(token), type, Lists.newArrayList(p));
	}
	
	public static VariableId<TypedNode> createVariableId(String token, Type type){
		return new VariableId<TypedNode>(byToken(token), type);
	}
	
	public static TypedNode byToken(String token){
		return new TypedNode(new CommonToken(-1, token));
	}
}
