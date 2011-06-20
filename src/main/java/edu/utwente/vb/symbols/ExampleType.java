package edu.utwente.vb.symbols;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.objectweb.asm.Type;

/**
 * Type names. Enum for iterateability/they are not dynamically created anyway.
 * @author Ties
 *
 */
public enum ExampleType{
	VOID(Type.VOID_TYPE), INT(Type.INT_TYPE), CHAR(Type.CHAR_TYPE), STRING(Type.getType(String.class)), BOOL(Type.BOOLEAN_TYPE), UNKNOWN;
	
	private final Type asmType;
	
	/**
	 * Create a type mapped to a ASM Equivalent
	 * @param asm ASM Equivalent type
	 */
	private ExampleType(Type asm){
		this.asmType = checkNotNull(asm);
	}
	
	/**
	 * Create a type with no ASM Equivalent
	 */
	private ExampleType(){
		asmType = null;
	}
	
	public static boolean isNumeric(ExampleType t){
		return ExampleType.INT.equals(t);
	}
	
	/**
	 * Return het type met de goede naam
	 * @param n naam of het type
	 * @return [type] wat bij n[aam] hoort
	 * @throws IllegalArgumentException when n is not a valid type name.
	 */
	public static ExampleType byName(String n){
		checkNotNull(n);
		for(ExampleType t : ExampleType.values())
			if(t.name().equals(n.toUpperCase()))
				return t;
		throw new IllegalArgumentException("Type " + n +" is unknown");
	}
	
	public static ExampleType[] asArray(ExampleType... types){
		return types;
	}
	
	public static ExampleType[] asArray(List<ExampleType> types){
		return types.toArray(new ExampleType[0]);
	}
	
	@Override
	public String toString() {
		return name();
	}
	
	public Type toASM(){
		if(asmType == null)
			throw new IllegalStateException("Can not convert " + this + " to ASM Type");
		return asmType;
	}
}
