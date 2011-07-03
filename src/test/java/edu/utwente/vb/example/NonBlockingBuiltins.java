package edu.utwente.vb.example;

public class NonBlockingBuiltins extends Builtins{
    @Override
    protected char read(char c) {
        return 'a';
    }
    
    @Override
    protected int read(int i) {
        return 1;
    }
    
    @Override
    protected String read(String s) {
        return "string";
    }
}
