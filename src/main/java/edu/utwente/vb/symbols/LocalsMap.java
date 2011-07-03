package edu.utwente.vb.symbols;

import java.util.Map;

import com.google.common.collect.Maps;

import edu.utwente.vb.tree.TypedNode;

public class LocalsMap{
	private Map<String, LocalVariable> indexMap = Maps.newHashMap();
	private int i = 0;
	
	public LocalVariable put(TypedNode node, int index){
		LocalVariable res = new LocalVariable(node, index);
		indexMap.put(node.getText(), res);
		
		return res;
	}
	
	public LocalVariable get(TypedNode node){
		return indexMap.get(node.getText());
	}
	
	public int getIndex(String name){
		return indexMap.get(name).index;
	}
	
	
	
	public void reset(){
		indexMap = Maps.newHashMap();
		i = 0;
	}
	
	public class LocalVariable{
		private final TypedNode node;
		private final int index;
		
		public LocalVariable(TypedNode node, int index) {
			this.node = node;
			this.index = index;
		}
	}
}
