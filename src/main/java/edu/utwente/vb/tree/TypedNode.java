package edu.utwente.vb.tree;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.Type;
import edu.utwente.vb.symbols.VariableId;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A tree node that saves it's type
 * @author ties
 *
 */
public class TypedNode extends CommonTree {
	private Type type;
	
	public TypedNode() { }
	
	public TypedNode(TypedNode node) {
		super(node);
		this.type = node.getNodeType();
	}

	public TypedNode(Token t) {
		super(t);
	}
	
	@Override
	public TypedNode dupNode() {
		return new TypedNode(this);
	}
	
	/**
	 * Get the node type
	 * @return Type of this node (or null)
	 */
	public Type getNodeType(){
		return type;
	}
	
	/**
	 * Set the node type
	 * @param type type of the node
	 * @require type != null
	 */
	public void setType(Type type) {
		checkNotNull(type);
		this.type = type;
	}

	public void setNodeType(Type nodeType) {
		this.type = checkNotNull(nodeType);
	}
	
	/**
	 * Transforms a list of TypeNodes into a list of Types
	 * @param <Q>
	 * @param params
	 * @return
	 */
	public static List<Type> extractTypes(List<TypedNode> params){
		return Lists.transform(params, new Function<TypedNode, Type>() {
			@Override
			public Type apply(TypedNode input) {
				return input.getNodeType();
			}
		});
	}
	
	/**
	 * Transforms a list of TypeNodes into a list of Types
	 * @param <Q>
	 * @param params
	 * @return
	 */
	public static List<Type> extractTypes(TypedNode... params){
		return extractTypes(ImmutableList.copyOf(params));
	}
}
