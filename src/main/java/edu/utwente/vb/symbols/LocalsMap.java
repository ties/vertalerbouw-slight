package edu.utwente.vb.symbols;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.google.common.collect.Maps;

import edu.utwente.vb.tree.TypedNode;

public class LocalsMap{
	private Map<TypedNode, LocalVariable> indexMap = Maps.newHashMap();
	private int i = 0;
	
	public LocalVariable put(MethodVisitor mv, TypedNode node){
		Label before = new Label();
		mv.visitLabel(before);
		Label after = new Label();
		
		LocalVariable res = new LocalVariable(before, after, node, ++i);
		indexMap.put(node, res);
		
		return res;
	}
	
	public LocalVariable get(TypedNode node){
		return indexMap.get(node);
	}
	
	public void reset(){
		indexMap = Maps.newHashMap();
		i = 0;
	}
	
	public class LocalVariable{
		private Label start;
		private Label end;
		private TypedNode node;
		private int index;
		
		public LocalVariable(Label start, Label end, TypedNode node, int index) {
			this.start = start;
			this.end = end;
			this.node = node;
			this.index = index;
		}
		
		public Label getStart() {
			return start;
		}
		
		public Label getEnd() {
			return end;
		}
		
		public TypedNode getNode() {
			return node;
		}
		
		public int getIndex() {
			return index;
		}
	}
}
