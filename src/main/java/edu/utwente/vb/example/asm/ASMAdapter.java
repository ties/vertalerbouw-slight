package edu.utwente.vb.example.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.google.common.collect.Maps;
import com.google.common.io.Files;

import edu.utwente.vb.tree.AppliedOccurrenceNode;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.symbols.ExampleType;
import edu.utwente.vb.symbols.LocalsMap;
import edu.utwente.vb.symbols.LocalsMap.LocalVariable;

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
	
	/** Label voor return van functie */
	private Label 	funcDefReturn;
	/** Type voor return van functie */
	private Type 	funcDefReturnType;
	/** Are we in a function */
	private boolean inFunction = false;
	/** The constructor */
	private MethodVisitor constructorVisitor;
	/** The current variable */
	private TypedNode currentVar;
	/** The locals map */
	private LocalsMap localsMap;
	/** The class name */
	private final String internalClassName;
	
	
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
	
	public ASMAdapter(String cn) {
		this.internalClassName = cn.replace('.', '/');
		log.debug("instantiating ASMAdapter for {}", internalClassName);
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
		cv.visit(V1_5, ACC_PUBLIC, internalClassName, null,
				"java/lang/Object", null);
		// Constructor stub
		constructorVisitor = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
				null);
		constructorVisitor.visitCode();
		constructorVisitor.visitVarInsn(ALOAD, 0);
		constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		
	}
	
	/**
	 * Einde van de visit - Debug bytecode + sluit constructor af
	 */ 
	public void visitEnd() {
		log.debug("visitEnd(), bytecode:");
		/* Sluit de constructor af */
		constructorVisitor.visitInsn(RETURN);
		constructorVisitor.visitMaxs(1, 1);
		constructorVisitor.visitEnd();
		/* Sluit nu de klasse af */
		cv.visitEnd();
		//
		log.debug(buffer.toString());
	}

	public void visitEnd(File file){
		log.debug("visitEnd({})", file.getName());
		visitEnd();
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
		assert mv == null || inFunction;
		
		log.debug("declVar {} {}", node.getText(), node.getNodeType().toASM().getDescriptor());
		cv.visitField(ACC_PUBLIC, node.getText(), node.getNodeType().toASM().getDescriptor(), null, null).visitEnd();
		
		currentVar = node;		
		if(!inFunction){//Initialisatie van variabele in constructor
			mv = constructorVisitor;
		} else {//Local var
			localsMap.put(currentVar);
		}
	}
	
	public void declConst(TypedNode node){
		assert mv == null || inFunction; 
		
		String name = node.getText();
		
		log.debug("declConst {} {}", node.getText(), node.getNodeType().toASM().getDescriptor());
		
		cv.visitField(ACC_PRIVATE + ACC_FINAL, name, node.getNodeType().toASM().getDescriptor(), null, null).visitEnd();
		
		currentVar = node;		
		if(!inFunction){//Initialisatie van variabele in constructor
			mv = constructorVisitor;
		} else {//Local var
			localsMap.put(mv, currentVar);
		}
	}
	
	/**
	 * Omdat wij constanten niet statisch definieren is er geen verschil tussen einde van var & const definitie
	 */
	public void endDecl(){
		if(!inFunction){
			log.debug("FieldInsn PUTFIELD {} {}", currentVar.getText(), currentVar.getNodeType().toASM().getDescriptor());
			mv.visitFieldInsn(PUTFIELD, internalClassName, currentVar.getText(), currentVar.getNodeType().toASM().getDescriptor());
		} else {
			log.debug("VarInsn {} {}", currentVar.getNodeType().toASM().getOpcode(ISTORE), localsMap.get(currentVar));
			
			mv.visitVarInsn(currentVar.getNodeType().toASM().getOpcode(ISTORE), localsMap.get(currentVar).getIndex());
			//LocalVariable ref
			LocalVariable lv = localsMap.get(currentVar);
			mv.visitLabel(lv.getEnd());
			mv.visitLocalVariable(lv.getNode().getText(), lv.getNode().getNodeType().toASM().getDescriptor(), null, lv.getStart(), lv.getEnd(), lv.getIndex());
		}
		currentVar = null;
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
	
	public void setInFunction(boolean inFunction) {
		log.debug("inFunction: {}", inFunction);
		this.inFunction = inFunction;
	}
	
	public boolean isInFunction() {
		return inFunction;
	}
}
	

