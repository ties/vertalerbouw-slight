package edu.utwente.vb.asm;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import edu.utwente.vb.example.Builtins;
import edu.utwente.vb.example.asm.ASMAdapter;

import static junit.framework.Assert.*;

public class TestASMAdapter {
	@Test
	public void testEmptyClass(){
		ASMAdapter a = new ASMAdapter("EmptyClass", Builtins.class);
		a.visitEnd();
	}
	
	@Test
	public void testWriteClass() throws IOException{
		ASMAdapter a = new ASMAdapter("EmptyClass", Builtins.class);
		File f = File.createTempFile("EmptyClass", ".class");
		
		a.visitEnd(f);
		
		assertTrue(f.length() > 0);
	}

}
