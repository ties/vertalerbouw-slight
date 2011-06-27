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
import edu.utwente.vb.tree.FunctionNode;
import edu.utwente.vb.tree.TypedNode;
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
	private TypedNode currentVar;
	/** The locals map */
	private LocalsMap localsMap;
	/** The class name */
	private final String internalClassName;
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
		this.internalClassName = cn.replace('.', '/');
		log.debug("instantiating ASMAdapter for {}", internalClassName);
		// Instantieer een ClassWriter
		$__cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		// Instantieer de classVisitor die delegeert naar de classwriter
		TraceClassVisitor tcv = new TraceClassVisitor($__cw, new PrintWriter(
				buffer));
		// En een CheckClassAdapter die controleert of de bytecode geldig is.
		CheckClassAdapter checkAdapter = new CheckClassAdapter(tcv);
		cv = checkAdapter;

		localsMap = new LocalsMap();

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
		cv.visit(V1_5, ACC_PUBLIC, internalClassName, null, "java/lang/Object",
				null);
		// Constructor stub
		// Code hieronder komt uit javadoc van ASM GeneratorAdapter
		Method m = Method.getMethod("void <init> ()");
		constructorMethodGenerator = new GeneratorAdapter(ACC_PUBLIC, m, null,
				null, cv);
		constructorMethodGenerator.visitCode();
		constructorMethodGenerator.loadThis();
		constructorMethodGenerator.invokeConstructor(
				Type.getType(Object.class), m);
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

		// Reset de locals map
		localsMap.reset();

		mg = new GeneratorAdapter(ACC_PUBLIC, funcId.asMethod(), null, null, cv);
		mg.visitCode();
		// Laad this - sowieso nodig voor de return
		mg.loadThis();

		log.debug("visitFuncDef {} {}", name, Type.getMethodDescriptor(node
				.getNodeType().toASM(), ExampleType.nodeListToASM(params)));
	}

	public void visitEndFuncDef() {
		log.debug("visitEndFuncDef");

		mg.returnValue();
		mg.visitMaxs(1000, 1000);
		mg.visitEnd();
	}

	public void visitBecomes(TypedNode node) {

		// fv.visitEnd();
	}

	public void visitDecl(TypedNode node) {
		checkArgument(mg == null || inFunction);
		currentVar = node;

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
			localsMap.put(currentVar, i);
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
			log.debug("FieldInsn PUTFIELD {} {}", currentVar.getText(),
					currentVar.getNodeType().toASM().getDescriptor());

			if (hasValue) {
				mg.visitFieldInsn(PUTFIELD, internalClassName,
						currentVar.getText(), currentVar.getNodeType().toASM()
								.getDescriptor());
			}
		} else {
			log.debug("VarInsn {} {}", currentVar.getNodeType().toASM()
					.getOpcode(ISTORE), localsMap.get(currentVar));

			if (hasValue) {
				mg.storeLocal(localsMap.getIndex(currentVar.getText()));
			}
		}
		currentVar = null;
	}

	public void instantiate() {
		log.debug("instantiate()");
		for (String id : toInstantiate) {
			// variabele setten.
		}
	}

	public void visitFuncCall(TypedNode node, List<TypedNode> params) {
		String name = node.getText();

		String descriptor = "(";
		for (TypedNode param : params) {
			ExampleType type = param.getNodeType();
			descriptor += type.toASM();
		}
		descriptor += ")";

		ExampleType returnType = node.getNodeType();
		descriptor += returnType.toASM();

		mg.visitMethodInsn(INVOKESPECIAL, null, name, descriptor);
	}

	public void visitIf() {

	}

	public void visitIfElse() {

	}

	public void visitWhile() {

	}

	public void visitWhile(TypedNode node) {

	}

	public void visitEndWhile() {

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
			if (value >= 0 && value < Byte.MAX_VALUE) {
				mg.visitIntInsn(BIPUSH, value);
			} else {
				mg.visitLdcInsn(new Integer(value));
			}
		}
	}
}
