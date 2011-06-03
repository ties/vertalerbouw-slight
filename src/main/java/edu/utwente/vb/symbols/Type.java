package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/**
 * Type names. Enum for iterateability/they are not dynamically created anyway.
 * @author Ties
 *
 */
public enum Type{
	VOID, INT, CHAR, STRING, BOOL, UNKNOWN;
	
	public static boolean isNumeric(Type t){
		return Type.INT.equals(t);
	}
	
	/**
	 * Return het type met de goede naam
	 * @param n naam of het type
	 * @return [type] wat bij n[aam] hoort
	 * @throws IllegalArgumentException when n is not a valid type name.
	 */
	public static Type byName(String n){
		checkNotNull(n);
		for(Type t : Type.values())
			if(t.name().equals(n.toUpperCase()))
				return t;
		throw new IllegalArgumentException("Type " + n +" is unknown");
	}
	
	public static Type[] asArray(Type... types){
		return types;
	}
	
	public static Type[] asArray(List<Type> types){
		return types.toArray(new Type[0]);
	}
}
