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
import edu.utwente.vb.tree.AppliedOccurrenceNode;
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
	
	public List<TypedNode> declareVariables(final List<TypedNode> variables) throws SymbolTableException{
		for(TypedNode var : variables)
			declareVar(var);
		return variables;
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
		// inferren van becomes
		if(lhs.getNodeType() == Type.UNKNOWN)
			lhs.setNodeType(rhs.getNodeType());
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
		for(TypedNode target : targets){
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
		
		//Update return type van Node
		node.setNodeType(returnType);
		
		FunctionId funcId = new FunctionId(node, returnType, ids);
		
		log.debug("declareFunction {} ({}) -> {}", new Object[]{node, params.toString(), returnType});
		
		symbolTable.put(funcId);
		
		return funcId;
	}
	
	public AppliedOccurrenceNode setBindingNode(TypedNode tgt, TypedNode src){
		AppliedOccurrenceNode target = (AppliedOccurrenceNode)tgt;
		target.setBindingNode(src);
		return target;
	}
	
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
		return new FunctionId<TypedNode>(byToken(token, type), type, Lists.newArrayList(p));
	}
	
	public static VariableId<TypedNode> createVariableId(String token, Type type){
		return new VariableId<TypedNode>(byToken(token, type), type);
	}
	
	public static TypedNode byToken(String token, Type type){
		TypedNode tmp = new TypedNode(new CommonToken(-1, token));
		tmp.setNodeType(type);
		return tmp;
	}
}
