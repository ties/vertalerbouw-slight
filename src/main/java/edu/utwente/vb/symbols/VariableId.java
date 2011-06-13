package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.BaseTree;

import com.google.common.base.Objects;

public class VariableId<T extends BaseTree> implements Id<T>{
	private Type type;
	private final T node;
	/** Variable is a constant (ie: not assignable) */
	private boolean constant = false;
	
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
	public String toString() {
		return Objects.toStringHelper(VariableId.class).add("name", getText()).add("type",type).add("constant", constant).toString();
	}
	
	@Override
	public boolean equalsSignature(String name, Type... params) {
		return Objects.equal(this.node.getText(), name) && (params == null || params.length == 0);
	}
	
	public boolean isConstant() {
		return constant;
	}
	
	public void setConstant(boolean c){
		constant = c;
	}
	
	@Override
	public void updateType(Type t) {
		checkArgument(!constant, "Trying to update the type of a constant");
		checkArgument(Type.UNKNOWN.equals(this.type), "Trying to update the type of a variable which is not Type.UNKNOWN");
		checkArgument(!Type.UNKNOWN.equals(t), "Can not update to Type.UNKNOWN");
		this.type = t;
	}
}
