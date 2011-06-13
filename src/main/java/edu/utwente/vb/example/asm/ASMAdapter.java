package edu.utwente.vb.example.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Adapter voor de ASM library, scheelt code in de grammar & testbaarheid
 * 
 * let op: implements Opcodes ipv import static org.objectweb.asm.Opcodes.*;
 * 
 * Om dit te schrijven is gebruik gemaakt van de code in
 * http://stackoverflow.com
 * /questions/5346908/generating-a-hello-world-class-with-the-java-asm-library
 * en de ASM guide.
 */
public class ASMAdapter implements Opcodes {
	/** ClassWriter. Nodig voor toByteArray, maar liever niet aan zitten */
	private ClassWriter $__cw;
	/**
	 * De classVisitor, hier roep je alles op aan. Delegeert het door
	 * TraceClassVisitor en CheckClassAdaptor naar ClassWriter
	 */
	private ClassVisitor cv;
	/**
	 * Buffer voor de ClassTracer, hierdoor kunnen we de tekst via log.debug
	 * gooien ipv direct naar console
	 */
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	/** De logger */
	private final Logger log = LoggerFactory.getLogger(ASMAdapter.class);

	public ASMAdapter(String className) {
		log.debug("instantiating ASMAdapter for {}", className);
		// Instantieer een ClassWriter
		$__cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// Instantieer de classVisitor die delegeert naar de classwriter
		TraceClassVisitor tcv = new TraceClassVisitor($__cw, new PrintWriter(
				buffer));
		// En een CheckClassAdapter die controleert of de bytecode geldig is.
		cv = new CheckClassAdapter(tcv);

		/*
		 * version - the class version. access - the class's access flags (see
		 * Opcodes). This parameter also indicates if the class is deprecated.
		 * name - the internal name of the class (see getInternalName).
		 * signature - the signature of this class. May be null if the class is
		 * not a generic one, and does not extend or implement generic classes
		 * or interfaces. superName - the internal of name of the super class
		 * (see getInternalName). For interfaces, the super class is Object. May
		 * be null, but only for the Object class. interfaces - the internal
		 * names of the class's interfaces (see getInternalName). May be null.
		 */
		cv.visit(V1_5, ACC_PUBLIC, className.replace('.', '/'), null,
				"java/lang/Object", null);
		// Constructor stub
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
				null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	/**
	 * Einde van de visit
	 */
	public void visitEnd() {
		log.debug("visitEnd(), bytecode:");
		cv.visitEnd();
		//
		log.debug(buffer.toString());
	}

	public void visitEnd(File file) throws IOException {
		log.debug("visitEnd({})", file.getName());
		cv.visitEnd();
		//
		log.debug(buffer.toString());
		Files.write($__cw.toByteArray(), file);
	}
}
