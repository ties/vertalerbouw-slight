package edu.utwente.vb.example.util;

import org.antlr.runtime.tree.CommonTree;
import static com.google.common.base.Preconditions.checkNotNull;

import edu.utwente.vb.symbols.SymbolTable;
import edu.utwente.vb.symbols.Type;
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
	public void tbn(CommonTree node, String type) {
		checkNotNull(node); checkNotNull(type);
		((TypedNode) node).setType(Type.byName(type));
	}
	
	/**
	 * Directly set the node type by Type refernce
	 * @param node node to set the type on
	 * @param type type to set
	 */
	public void st(CommonTree node, Type type) {
		checkNotNull(node); checkNotNull(type);
		((TypedNode) node).setType(type);
	}
	
	
}
