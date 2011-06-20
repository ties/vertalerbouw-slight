package edu.utwente.vb.example.util;

import java.io.File;
import java.util.List;

import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.utwente.vb.symbols.ExampleType;
import edu.utwente.vb.tree.TypedNode;

public class Utils {

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
}
