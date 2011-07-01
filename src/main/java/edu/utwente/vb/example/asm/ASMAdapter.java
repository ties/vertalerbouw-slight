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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import edu.utwente.vb.tree.AppliedOccurrenceNode;
import edu.utwente.vb.tree.BindingOccurrenceNode;
import edu.utwente.vb.tree.FunctionNode;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.example.Builtins;
import edu.utwente.vb.example.Lexer;
import edu.utwente.vb.symbols.ExampleType;
import edu.utwente.vb.symbols.FunctionId;
import edu.utwente.vb.symbols.LocalsMap;
import edu.utwente.vb.symbols.LocalsMap.LocalVariable;

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

	// Nodig om methodes en instantievariabelen te passeren
	private FieldVisitor fv;
	private AnnotationVisitor av0;

	/** Label voor return van functie */
	private Label funcDefReturn;
	/** Type voor return van functie */
	private Type funcDefReturnType;
	/** Are we in a function */
	private boolean inFunction = false;
	/** The constructor */
	private GeneratorAdapter constructorMethodGenerator;
	/** The current variable */
	private BindingOccurrenceNode currentVar;
	/** The class name */
	private final Type internalClassType;
	private final Type superClassName;
	/** The method adapter */
	private GeneratorAdapter mg;

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
	
	public ASMAdapter(String className, String sourceName) {
		this(className);
		cv.visitSource(sourceName, null);
	}

	public ASMAdapter(String cn) {
		this.internalClassType = Type.getObjectType(cn.replace('.', '/'));
		
		log.debug("instantiating ASMAdapter for {}", internalClassType);
		// Instantieer een ClassWriter
		$__cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		// Instantieer de classVisitor die delegeert naar de classwriter
		TraceClassVisitor tcv = new TraceClassVisitor($__cw, new PrintWriter(
				buffer));
		// En een CheckClassAdapter die controleert of de bytecode geldig is.
		CheckClassAdapter checkAdapter = new CheckClassAdapter(tcv);
		cv = checkAdapter;


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
		superClassName = Type.getType(Builtins.class);
		cv.visit(V1_5, ACC_PUBLIC, internalClassType.getInternalName(), null, superClassName.getInternalName(),
				null);
		// Constructor stub
		// Code hieronder komt uit javadoc van ASM GeneratorAdapter		
		Method m = Method.getMethod("void <init> ()");
		constructorMethodGenerator = new GeneratorAdapter(ACC_PUBLIC, m, null,
				null, cv);
		constructorMethodGenerator.visitCode();
		constructorMethodGenerator.loadThis();
		constructorMethodGenerator.invokeConstructor(
				Type.getType(Builtins.class), m);
		constructorMethodGenerator.loadThis();
		
		Method mainMethod = Method.getMethod("void main(String[])");
		GeneratorAdapter main = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, mainMethod, null, null, cv);
		main.visitCode();
		main.newInstance(internalClassType);
		main.invokeConstructor(internalClassType, m);
		main.returnValue();
		main.visitMaxs(1, 1);
		main.visitEnd();
	}
	
	/**
	 * Initialises Example builtin-functions like print() and read()
	 */
	private void builtinFunctions(){
		{	//print(String)mg = 
			MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "print", "(Ljava/lang/String;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(Ljava/lang/String;)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
	}
	
	
	/**
	 * Einde van de visit - Debug bytecode + sluit constructor af
	 */
	public void visitEnd() {
		log.debug("visitEnd(), bytecode:");
		/* Sluit de constructor af */
		constructorMethodGenerator.returnValue();
		constructorMethodGenerator.visitMaxs(1000, 1000); // maxStack door classWriter
		constructorMethodGenerator.visitEnd();
				
		/* Sluit nu de klasse af */
		cv.visitEnd();
		//
		log.debug(buffer.toString());
	}

	public void visitEnd(File file) {
		log.debug("visitEnd({})", file.getName());
		visitEnd();

		try {
			Files.write($__cw.toByteArray(), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setInFunction(boolean inFunction) {
		log.debug("inFunction: {}", inFunction);
		this.inFunction = inFunction;
	}

	public boolean isInFunction() {
		return inFunction;
	}

	// ///////////////////////////////////////////////////////
	// Visit methods for codeGenerator.g //
	// ///////////////////////////////////////////////////////

	public void visitFuncDef(TypedNode node, List<TypedNode> params) {
		checkArgument(node instanceof FunctionNode);
		FunctionNode funcNode = (FunctionNode) node;
		FunctionId<TypedNode> funcId = funcNode.getBoundMethod();
		String name = node.getText();
		
		inFunction = true;// Set "In a function"  state

		mg = new GeneratorAdapter(ACC_PUBLIC, funcId.asMethod(), null, null, cv);
		mg.visitCode();
		// Laad this - sowieso nodig voor de return
		mg.loadThis();

		log.debug("visitFuncDef {} {}", name, Type.getMethodDescriptor(node
				.getNodeType().toASM(), ExampleType.nodeListToASM(params)));
	}

	public void visitEndFuncDef() {
		log.debug("visitEndFuncDef, in function before? {}", inFunction);
		inFunction = false;
		
		mg.returnValue();
		mg.visitMaxs(1000, 1000);
		mg.visitEnd();
		
		mg = null;
	}

	public void visitBecomes(TypedNode node) {
		checkArgument(node instanceof AppliedOccurrenceNode);
		AppliedOccurrenceNode appNode = (AppliedOccurrenceNode) node;
		checkArgument(appNode.getBindingNode() instanceof BindingOccurrenceNode);
		BindingOccurrenceNode bindingOccurrence = (BindingOccurrenceNode) appNode.getBindingNode();
		
		if(bindingOccurrence.isLocal()){
			mg.storeLocal(bindingOccurrence.getNumber());
		}else{
			mg.loadThis();
			mg.swap(); // swap Object/Value voor PUTFIELD instructie. Gaat goed zolang wij geen long/double primitieven hebben ...
			mg.putField(internalClassType, appNode.getText(), appNode.getNodeType().toASM());
		}
	}

	public void visitDecl(TypedNode node) {
		checkArgument(mg == null || inFunction);
		checkArgument(node instanceof BindingOccurrenceNode);
		
		currentVar = (BindingOccurrenceNode) node;

		if (!inFunction) {// Initialisatie van variabele in constructor
			log.debug("declVar {} {}", node.getText(), node.getNodeType()
					.toASM().getDescriptor());
			cv.visitField(ACC_PUBLIC, node.getText(),
					node.getNodeType().toASM().getDescriptor(), null, null)
					.visitEnd();

			mg = constructorMethodGenerator;
		} else {
			/*
			 * Maak lokale variabele aan en store zijn index. Index management
			 * (tov argumenten bijvoorbeeld) wordt gedaan door ASM magie
			 */
			int i = mg.newLocal(node.getNodeType().toASM());
			currentVar.setNumber(i);
		}
	}

	/**
	 * Omdat wij constanten niet statisch definieren is er geen verschil tussen
	 * einde van var & const definitie
	 */
	public void visitDeclEnd(TypedNode node) {
		// Moeten we storen? -> heeft ie een expressie
		boolean hasValue = node != null;

		if (!inFunction) {
			log.debug("visitDeclEnd LOCAL {} @ {}", currentVar.getText(), currentVar);

			if (hasValue) {
				mg.loadThis();
				mg.swap();
				mg.visitFieldInsn(PUTFIELD, internalClassType.getInternalName(),
						currentVar.getText(), currentVar.getNodeType().toASM()
								.getDescriptor());
			}
			
			// Remove the pointer to the constructor
			mg = null;
		} else {
			log.debug("VarInsn {} {}", currentVar.getNodeType().toASM()
					.getOpcode(ISTORE), currentVar);

			if (hasValue) {
				mg.storeLocal(currentVar.getNumber());
			}
		}
		
		currentVar = null;
	}
	
	public void visitFuncCallBegin(TypedNode n, List<TypedNode> params){
		// Load this onto the stack
		// stack protocol of InvokeVirtual: ..., objectref, [arg1, [arg2 ...]]  => ...
		mg.loadThis();
	}

	public void visitFuncCallEnd(TypedNode n, List<TypedNode> params) {
		checkArgument(n instanceof AppliedOccurrenceNode);
		
		AppliedOccurrenceNode node = (AppliedOccurrenceNode)n;
		
		String name = node.getText();
		Method target = new Method(name,  node.getNodeType().toASM(), ExampleType.nodeListToASM(params));
		
		// Lelijke check op functies die gedefinieerd zijn in prelude - maar kan erger
		if(n.getToken().getType() == Lexer.SYNTHETIC){// Ingebouwde functie
			log.debug("Builtin function "  + name);
			mg.invokeVirtual(superClassName, target);
		} else {
			log.debug("User-Defined function " + name);
			// The difference between the invokespecial and the invokevirtual instructions is that invokevirtual invokes a method based on the class of the object. 
			// The invokespecial instruction is used to invoke instance initialization methods (§3.9) as well as private methods and methods of a superclass of the current class.
			mg.invokeVirtual(internalClassType, target);
		}
	}
	
	public void visitIfBegin(TypedNode node, Label ifEnd){
		//Type van expressiezijden opvragen
		Type type = node.getNodeType().toASM();
		//Jumpen naar ifEnd als niet gelijk, later backpatchen
		mg.ifCmp(type, IFNE, ifEnd);
	}
	
	public void visitIfHalf(TypedNode node, Label ifEnd, Label elseEnd){
		//Backpatchen
		mg.mark(ifEnd);
		//0 (false) op de stack
		mg.visitInsn(ICONST_1);
		//Jumpen naar elseEnd als gelijk, later backpatchen
		mg.ifICmp(IFEQ, elseEnd); 
	}
	public void visitIfEnd(TypedNode node, Label elseEnd){
		mg.mark(elseEnd);
	}

	public void visitWhileBegin(TypedNode node, Label loopBegin, Label loopEnd) {
		//Backpatchen
		mg.mark(loopBegin);
		//0 (false) op de stack
		mg.visitInsn(ICONST_0);
		//Jumpen naar loopEnd als gelijk, later backpatchen
		mg.ifICmp(IFEQ, loopEnd);
	}

	public void visitWhileEnd(TypedNode node, Label loopBegin, Label loopEnd) {
		//Backpatchen
		mg.mark(loopEnd);
		//1 (true) op de stack
		mg.visitInsn(ICONST_1);
		//Jumpen naar loopBegin als gelijk, later backpatchen
		mg.ifICmp(IFEQ, loopBegin);
	}

	public void visitBecomes(TypedNode lhs, TypedNode rhs){
		checkArgument(lhs instanceof BindingOccurrenceNode);
		BindingOccurrenceNode bindingNode = (BindingOccurrenceNode) lhs;
		if(bindingNode.isLocal()){
			mg.storeLocal(bindingNode.getNumber());
		}else{
			mg.putField(internalClassType, bindingNode.getText(), bindingNode.getNodeType().toASM());
		}
		
	}
	
	public void visitBinaryOperator(/* Opcode */ int opcode, TypedNode lhs, TypedNode rhs){
		if(ExampleType.STRING.equals(lhs.getNodeType()))
			throw new UnsupportedOperationException("Todo");
		mg.visitInsn(lhs.getNodeType().toASM().getOpcode(opcode));	
	}
	
	public void visitCompareOperator(int opcode, TypedNode lhs, TypedNode rhs){
		Label l3 = new Label();
		Label l4 = new Label();
		mg.visitJumpInsn(opcode, l3);
		mg.visitInsn(ICONST_0);
		mg.visitJumpInsn(GOTO, l4);
		mg.visitLabel(l3);
		mg.visitInsn(ICONST_1);
		mg.visitLabel(l4);
	}
	
	public void visitReturn(TypedNode expr){
		mg.visitInsn(expr.getNodeType().toASM().getOpcode(IRETURN));
	}
	
	public void visitNot(){
		mg.not();
	}
	
	public void visitVariable(TypedNode n){
		checkArgument(n instanceof AppliedOccurrenceNode);
		
		BindingOccurrenceNode node = (BindingOccurrenceNode)((AppliedOccurrenceNode)n).getBindingNode();
		if(node.isLocal()){
			// Load a local
			// Note that we define method arguments in a slightly odd way - this might cause problems later on. 
			mg.loadLocal(node.getNumber());
		} else {
			mg.getField(internalClassType, node.getText(), node.getNodeType().toASM());
		}
	}
	
	public void visitCharAtom(TypedNode node) {
		visitAtom(node.getNodeType().toASM(), (int) node.getText().charAt(0));
	}

	public void visitBooleanAtom(TypedNode node) {
		if (Boolean.valueOf(node.getText())) {
			mg.visitInsn(ICONST_1);
		} else {
			mg.visitInsn(ICONST_0);
		}
	}

	public void visitStringAtom(TypedNode node) {
		mg.visitLdcInsn(node.getText());
	}

	/**
	 * We weten zeker dat dit een int is
	 * 
	 * @param node
	 * @param negative
	 */
	public void visitIntegerAtom(TypedNode node, boolean negative) {
		int value = Integer.valueOf(node.getText());
		value = negative ? -value : value;

		visitAtom(node.getNodeType().toASM(), value);
	}

	private void visitAtom(Type type, int value) {
		switch (value) {
		case 0:
			mg.visitInsn(ICONST_0);
			break;
		case 1:
			mg.visitInsn(ICONST_1);
			break;
		case 2:
			mg.visitInsn(ICONST_2);
			break;
		case 3:
			mg.visitInsn(ICONST_3);
			break;
		case 4:
			mg.visitInsn(ICONST_4);
			break;
		case 5:
			mg.visitInsn(ICONST_5);
			break;
		default:
			if (value > Byte.MIN_VALUE && value < Byte.MAX_VALUE) {
				mg.visitIntInsn(BIPUSH, value);
			} else {
				mg.visitLdcInsn(new Integer(value));
			}
		}
	}
	
}
