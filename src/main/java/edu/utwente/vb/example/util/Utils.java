package edu.utwente.vb.example.util;

import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.ExampleType;
import edu.utwente.vb.tree.BindingOccurrenceNode;

public class Utils {
	private static Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static String camelCaseName(String fileName){
		List<String> words = Lists.newArrayList();
		StringBuffer word = new StringBuffer();
		
		for(Character c : Lists.charactersOf(fileName)){
			if(c.equals('_') || c.equals('-')){
				words.add(word.toString());
				word = new StringBuffer();
			} else if(c.equals('.')){
				break;//extensie begint
			} else {
				word.append(c);
			} 
		}
		words.add(word.toString());
				
		StringBuffer camelCase = new StringBuffer();
		for(String w : words){
			if(w.length() >=  2){
				camelCase.append(w.substring(0,1).toUpperCase());
				camelCase.append(w.substring(1));
			} else {
				camelCase.append(System.currentTimeMillis());
			}
		}
		
		return camelCase.toString();
	}
	
	public static void stripQuotes(Token token){
    	CommonToken t = ((CommonToken)token);
        t.setStartIndex(t.getStartIndex() + 1);
        t.setStopIndex(t.getStopIndex() - 1);
	}
	
	public static boolean isUnknownVarNode(Object node){
		if(!(node instanceof BindingOccurrenceNode))
			return false;
		
		log.debug("Is unknown? {}", ((BindingOccurrenceNode)node).getNodeType());
		
		return ExampleType.UNKNOWN.equals(((BindingOccurrenceNode)node).getNodeType());	
	}
}
