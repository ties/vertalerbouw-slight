package edu.utwente.vb.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.ExampleType;

/**
 * A tree node that saves it's type
 * @author ties
 *
 */
public class TypedNode extends CommonTree {
	protected ExampleType type;
	private boolean constantExpression = false;
	
	public TypedNode() { }
	
	public TypedNode(TypedNode node) {
		super(node);
		this.type = node.getNodeType();
		this.constantExpression = node.constantExpression;
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
	public ExampleType getNodeType(){
		return type;
	}
	
	public boolean isConstantExpression() {
		return constantExpression;
	}
	
	public void setConstantExpression(boolean constantExpression) {
		this.constantExpression = constantExpression;
	}
	
	/**
	 * Set the node type
	 * @param type type of the node
	 * @require type != null
	 */
	public void setNodeType(ExampleType type) {
		checkNotNull(type);
		this.type = type;
	}
	
	/**
	 * Transforms a list of TypeNodes into a list of Types
	 * @param <Q>
	 * @param params
	 * @return
	 */
	public static List<ExampleType> extractTypes(List<TypedNode> params){
		return Lists.transform(params, new Function<TypedNode, ExampleType>() {
			@Override
			public ExampleType apply(TypedNode input) {
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
	public static List<ExampleType> extractTypes(TypedNode... params){
		return extractTypes(ImmutableList.copyOf(params));
	}
}
