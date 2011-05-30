//package edu.utwente.vb.symbols;
//
//import java.util.List;
//
//import org.antlr.runtime.CommonToken;
//
//import com.google.common.collect.Lists;
//import static edu.utwente.vb.symbols.Type.*;
//
//import edu.utwente.vb.tree.TypedNode;
//
//public class Prelude {
//	public final static FunctionId[] builtins = {
//		 createBuiltin("+", INT, INT, INT),
//		 createBuiltin("-", INT, INT, INT),
//		 createBuiltin("/", INT, INT, INT),
//		 createBuiltin("*", INT, INT, INT),
//		 createBuiltin("%", INT, INT, INT),
//		 
//	};
//	
//	/**
//	 * Create a builtin functipon's functionId
//	 * @param token token text
//	 * @param lhs left hand side type
//	 * @param rhs right hand side type
//	 * @return new functionId
//	 */
//	private static FunctionId<TypedNode> createBuiltin(String token, Type ret, Type lhs, Type rhs){
//		return createFunctionId(token, ret, createVariableId("lhs", lhs), createVariableId("rhs", rhs));
//	}
//	
//	private static FunctionId<TypedNode> createFunctionId(String token, Type type, VariableId<TypedNode>... p){
//		return new FunctionId<TypedNode>(byToken(token), type, Lists.newArrayList(p));
//	}
//	
//	private static VariableId<TypedNode> createVariableId(String token, Type type){
//		return new VariableId<TypedNode>(byToken(token), type);
//	}
//	
//	private static TypedNode byToken(String token){
//		return new TypedNode(new CommonToken(-1, token));
//	}
//}
