package edu.utwente.vb.example.util;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import static com.google.common.base.Preconditions.checkNotNull;

import edu.utwente.vb.symbols.FunctionId;
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
	
	public void declareVar(TypedNode node, Type type){
		checkNotNull(node); checkNotNull(type);
		VariableId varId = new VariableId(node, type);
		symbolTable.put(varId);
	}
	
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
	
	public void declareFunction(TypedNode node, Object returnTypeNode, List<Type> paramTypes){
		assert returnTypeNode instanceof TypedNode;
		returnTypeNode = (TypedNode) returnTypeNode;
		Type returnType = returnTypeNode.getNodeType();
		
		FunctionId funcId = new FunctionId(node, returnType, paramTypes);
		symbolTable.put(funcId);
	}
	
	public void setVar(AppliedOccurrenceNode node){
		
	}
	
}
