package edu.utwente.vb.tree;

import edu.utwente.vb.symbols.ExampleType;

public class ReturnData {
    public final static ReturnData UNKNOWN = new ReturnData(ExampleType.UNKNOWN);
    public final static ReturnData VOID = new ReturnData(ExampleType.VOID);
    
    public final boolean isConditional;
    public final ExampleType type;
    
    public ReturnData(ExampleType type){
	this.type = type;	
	this.isConditional = false;
    }
    
    public ReturnData(ExampleType type, boolean cond){
	this.type = type;	
	this.isConditional = cond;
    }
    
    public ReturnData(ReturnData old, boolean newCond){
	this(old.type, newCond);
    }
    
    public ReturnData(ReturnData old){
	this(old.type, false);
    }
    
    @Override
    public String toString() {
        return (isConditional ? "conditional " : "") + type;
    }
}
