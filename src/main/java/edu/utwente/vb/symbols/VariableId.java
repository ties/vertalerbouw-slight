package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.runtime.tree.BaseTree;

import com.google.common.base.Objects;

public class VariableId<T extends BaseTree> implements Id<T>{
	private ExampleType type;
	private final T node;
	/** Variable is a constant (ie: not assignable) */
	private boolean constant = false;
	
	public VariableId(T n, ExampleType t){
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
	
	public ExampleType getType() {
		return type;
	}
	
	@Override
	public edu.utwente.vb.symbols.Id.IdType getIdType() {
		return IdType.VARIABLE;
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
	public boolean equalsSignature(String name, ExampleType... params) {
		return Objects.equal(this.node.getText(), name) && (params == null || params.length == 0);
	}
	
	public boolean isConstant() {
		return constant;
	}
	
	public void setConstant(boolean c){
		constant = c;
	}
	
	@Override
	public void updateType(ExampleType t) {
		checkArgument(!constant, "Trying to update the type of a constant");
		checkArgument(ExampleType.UNKNOWN.equals(this.type), "Trying to update the type of a variable which is not Type.UNKNOWN");
		checkArgument(!ExampleType.UNKNOWN.equals(t), "Can not update to Type.UNKNOWN");
		this.type = t;
	}
}
