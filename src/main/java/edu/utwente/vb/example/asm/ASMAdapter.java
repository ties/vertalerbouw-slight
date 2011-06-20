package edu.utwente.vb.example.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.Utils;

import com.google.common.io.Files;

import edu.utwente.vb.tree.AppliedOccurrenceNode;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.symbols.ExampleType;

/**
 * Adapter voor de ASM library, scheelt code in de grammar & testbaarheid
 * 
 * let op: implements Opcodes ipv import static org.objectweb.asm.Opcodes.*;
 * 
 * Om dit te schrijven is gebruik gemaakt van de code in
 * http://stackoverflow.com/questions/5346908/generating-a-hello-world-class-with-the-java-asm-library
 * en de ASM guide.
 */
public class ASMAdapter implements Opcodes {	
	/** ClassWriter. Nodig voor toByteArray, maar liever niet aan zitten */
	private ClassWriter $__cw;
	
	// Nodig om methodes en instantievariabelen te passeren 
	private FieldVisitor fv;
	private MethodVisitor mv;
	private AnnotationVisitor av0;
	
	// Label voor return van functie
	private Label 	funcDefReturn;
	// Type voor return van functie
	private Type 	funcDefReturnType;
	
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

	private List<String> toInstantiate = new ArrayList<String>();
	
	
	public ASMAdapter(String className, String sourceName){
		this(className);
		cv.visitSource(sourceName, null);
	}
	
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
		mv.visitCode();
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

	public void visitEnd(File file){
		log.debug("visitEnd({})", file.getName());
		cv.visitEnd();
		//
		log.debug(buffer.toString());
		try{
			Files.write($__cw.toByteArray(), file);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public void varBody(TypedNode node){
	}
	
	public void endVar(){
	}
	
	/////////////////////////////////////////////////////////
	//            Visit methods for codeGenerator.g        //
	/////////////////////////////////////////////////////////
	
	public void visitFuncDef(TypedNode node, List<TypedNode> params){
		String name = node.getText();
		
		log.debug("visitFuncDef {} {}", name, Type.getMethodDescriptor(node.getNodeType().toASM(), ExampleType.nodeListToASM(params)));
		
		mv = cv.visitMethod(ACC_PUBLIC, name, Type.getMethodDescriptor(node.getNodeType().toASM(), ExampleType.nodeListToASM(params)), null, null);
		//Label voor einde methode-jump
		funcDefReturn = new Label();
		funcDefReturnType = node.getNodeType().toASM();
		//begin code
		mv.visitCode();
		if("main".equals(name)){
			instantiate();
		}
	}
	
	public void visitEndFuncDef(){
		log.debug("visitEndFuncDef");
		mv.visitLabel(funcDefReturn);
		if(Type.VOID_TYPE.equals(funcDefReturnType)){
			mv.visitInsn(RETURN);
		} else {
			//Goede type return
			mv.visitInsn(funcDefReturnType.getOpcode(IRETURN));
		}
		//Max stack -> wordt uitgerekend
		mv.visitMaxs(1,1);
		//End van method body
		mv.visitEnd();
		mv = null;
	}
	
	public void visitBecomes(TypedNode node){
		
		//fv.visitEnd();
	}
	
	public void declVar(TypedNode node){
		log.debug("declVar {} {}", node.getText(), node.getNodeType().toASM().getDescriptor());
		cv.visitField(ACC_PUBLIC, node.getText(), node.getNodeType().toASM().getDescriptor(), null, null).visitEnd();
	}
	
	public void declConst(TypedNode node){
		String name = node.getText();
		
		log.debug("declConst {} {}", node.getText(), node.getNodeType().toASM().getDescriptor());
		
		fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, name, node.getNodeType().toASM().getDescriptor(), null, null);
		
		toInstantiate.add(name);
		
		fv.visitEnd();
	}
	
	public void instantiate(){
		log.debug("instantiate()");
		for (String id: toInstantiate){
			//variabele setten.
		}
	}
	
	public void visitFuncCall(TypedNode node, List<TypedNode> params){
		String name = node.getText();
		
		String descriptor = "(";
				for(TypedNode param : params){
			ExampleType type = param.getNodeType();
			descriptor += type.toASM();
		}		
		descriptor += ")";
		
		ExampleType returnType = node.getNodeType();
		descriptor += returnType.toASM();
		
		mv.visitMethodInsn(INVOKESPECIAL, null, name, descriptor);
	}
	
	public void visitIf(){
		
	}
	
	public void visitIfElse(){
		
	}
	
	public void visitWhile(){
		
	}
	
	public void visitWhile(TypedNode node){
		
	}
	
	public void visitEndWhile(){
		
	}
}
	

