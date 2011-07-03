package edu.utwente.vb.asm;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import edu.utwente.vb.example.asm.ASMAdapter;

public class TestASMAdapter {
	@Test
	public void testEmptyClass(){
		ASMAdapter a = new ASMAdapter("EmptyClass");
		a.visitEnd();
	}
	
	@Test
	public void testWriteClass() throws IOException{
		ASMAdapter a = new ASMAdapter("EmptyClass");
		File f = File.createTempFile("EmptyClass", ".class");
		
		a.visitEnd(f);
		
		assertTrue(f.length() > 0);
	}

}
