package edu.utwente.vb.example.util;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class Utils {

	public static String testName(File f){
		List<String> words = Lists.newArrayList();
		StringBuffer word = new StringBuffer();
		
		for(Character c : Lists.charactersOf(f.getName())){
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
