package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.base.Objects;

public class VariableId<T extends BaseTree> implements Id<T>{
	private final Type type;
	private final T node;
	
	public VariableId(T n, Type t){
		this.type =	checkNotNull(t);
		this.node = checkNotNull(n);
	}
	
	@Override
	public String getText() {
		return node.getText();
	}
	
	@Override
	public T getNode() {
		return node;
	}
	
	public Type getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(node, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VariableId){
			VariableId that = (VariableId)obj;
			return Objects.equal(this.node, that.node) && Objects.equal(this.type, that.type);
		}
		return false;
	}
	
	@Override
	public boolean equalsSignature(String name, Type... params) {
		return Objects.equal(this.token.getText(), name) && params.length == 0;
	}
}
