package edu.utwente.vb.example;

import static edu.utwente.vb.example.util.CheckerHelper.createFunctionId;
import static edu.utwente.vb.example.util.CheckerHelper.createVariableId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import edu.utwente.vb.symbols.ExampleType;

public class Builtins {
	BufferedReader reader;
	
	public Builtins(){
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	protected int read(int i){
		return Integer.valueOf(read(""));
	}
	
	protected char read(char c){
		return (char)read(0);
	}
	
	protected String read(String s){
		try{
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Internal error while reading a int", e);
		}
	}
	
	protected int print(int i){
		System.out.println(i);
		return i;
	}
	
	protected char print(char c){
		System.out.println(c);
		return c;
	}
	
	protected String print(String line){
		System.out.println(line);
		return line;
	}
	
	protected boolean print(boolean b){
		System.out.println(b);
		return b;
	}
	
	protected String stringAppend(String lhs, String rhs){
		return lhs + rhs;
	}
	
	protected String stringAppend(String lhs, char rhs){
		return lhs + rhs;
	}
	
	protected String stringAppend(String lhs, int rhs){
		return lhs + rhs;
	}
	
	protected String stringAppend(String lhs, boolean rhs){
		return lhs + rhs;
	}
	
	protected boolean stringEQ(String lhs, String rhs){
		return lhs.equals(rhs);
	}
	
	protected boolean stringNE(String lhs, String rhs){
		return !lhs.equals(rhs);
	}
	
	protected void ensure(boolean expr){
		if(!expr)
			throw new Error("[Example] runtime error - ensure failed");
	}
	
	protected int random(int max){
	    Random rand = new Random();
	    return rand.nextInt(max + 1);
	}
	
	// builder.add(createFunctionId("getInt", ExampleType.VOID, 	createVariableId("list", ExampleType.INTLIST), createVariableId("i", ExampleType.INT)));
	protected int getInt(ArrayList list, int index){
	    return (Integer)list.get(index);
	}
	// builder.add(createFunctionId("putInt", ExampleType.VOID, createVariableId("list", ExampleType.INTLIST), createVariableId("i", ExampleType.INT));
	protected void putInt(ArrayList list, int val){
	    list.add(val);
	}
	// builder.add(createFunctionId("length", ExampleType.INT, createVariableId("list", ExampleType.INTLIST));
	protected int length(ArrayList list){
	    return list.size();
	}
	// builder.add(createFunctionId("newList", ExampleType.INTLIST);
	protected ArrayList newList(){
	    return new ArrayList();
	}
}
