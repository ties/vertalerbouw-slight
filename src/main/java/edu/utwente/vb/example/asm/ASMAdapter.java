package edu.utwente.vb.example.asm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import edu.utwente.vb.example.Builtins;
import edu.utwente.vb.example.Lexer;
import edu.utwente.vb.symbols.ExampleType;
import edu.utwente.vb.symbols.FunctionId;
import edu.utwente.vb.symbols.Id.IdType;
import edu.utwente.vb.tree.AppliedOccurrenceNode;
import edu.utwente.vb.tree.BindingOccurrenceNode;
import edu.utwente.vb.tree.FunctionNode;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.tree.BindingOccurrenceNode.VariableType;

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
    private GeneratorAdapter mainFunctionMethodGenerator;
    private boolean loadVars = true;

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
	this(className, Builtins.class);
	cv.visitSource(sourceName, null);
    }

    public ASMAdapter(String cn, Class superclass) {
	this.internalClassType = Type.getObjectType(cn.replace('.', '/'));

	log.debug("instantiating ASMAdapter for {}", internalClassType);
	// Instantieer een ClassWriter
	$__cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

	// Instantieer de classVisitor die delegeert naar de classwriter
	TraceClassVisitor tcv = new TraceClassVisitor($__cw, new PrintWriter(buffer));
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
	superClassName = Type.getType(superclass);
	cv.visit(V1_5, ACC_PUBLIC, internalClassType.getInternalName(), null, superClassName.getInternalName(), null);
	// Constructor stub
	// Code hieronder komt uit javadoc van ASM GeneratorAdapter
	Method m = Method.getMethod("void <init> ()");
	constructorMethodGenerator = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cv);
	constructorMethodGenerator.visitCode();
	constructorMethodGenerator.loadThis();
	constructorMethodGenerator.invokeConstructor(superClassName, m);
	constructorMethodGenerator.loadThis();

	Method mainMethod = Method.getMethod("void main(String[])");
	mainFunctionMethodGenerator = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, mainMethod, null, null, cv);
	mainFunctionMethodGenerator.visitCode();
	mainFunctionMethodGenerator.newInstance(internalClassType);
	mainFunctionMethodGenerator.dup(); // Not needed if main is not there,
					   // but nm for now.
	mainFunctionMethodGenerator.invokeConstructor(internalClassType, m);
    }

    /**
     * Einde van de visit - Debug bytecode + sluit constructor af
     */
    public void visitEnd() {
	log.debug("visitEnd(), bytecode:");
	/* Sluit de constructor af */
	constructorMethodGenerator.returnValue();
	constructorMethodGenerator.visitMaxs(1000, 1000); // maxStack door
							  // classWriter
	constructorMethodGenerator.visitEnd();

	mainFunctionMethodGenerator.returnValue();
	mainFunctionMethodGenerator.visitMaxs(2, 1);
	mainFunctionMethodGenerator.visitEnd();
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

	mg = new GeneratorAdapter(ACC_PUBLIC, funcId.asMethod(), null, null, cv);
	mg.visitCode();
	mg.visitLineNumber(node.token.getLine(), mg.mark());
	// Laad this - sowieso nodig voor de return
	mg.loadThis();

	log.debug("visitFuncDef {} {}", name, Type.getMethodDescriptor(node.getNodeType().toASM(), ExampleType.nodeListToASM(params)));

	// Now check if this is the main.
	// If so, add a call to the main function to the classes main[] in order
	// to actually execute the code
	// Adding MAIN to the Lexer causes some trouble (FUNC DEF/FUNC DEF need
	// to be specified for both IDENTIFIER and MAIN, etc
	// The other option (IDENTIFIED : MAIN | ...) is also ugly
	if ("main".equals(name)) {
	    mainFunctionMethodGenerator.dup();
	    mainFunctionMethodGenerator.invokeVirtual(internalClassType, funcId.asMethod());
	}
    }

    public void visitArgument(TypedNode id, int n) {
	checkArgument(id instanceof BindingOccurrenceNode);
	BindingOccurrenceNode node = (BindingOccurrenceNode) id;
	node.setVariableType(VariableType.ARGUMENT);
	node.setNumber(n);
    }

    public void visitEndFuncDef() {
	log.debug("visitEndFuncDef, in function before? {}", inFunction);

	mg.returnValue();
	mg.visitMaxs(1000, 1000);
	mg.visitEnd();

	mg = null;
    }

    public void visitBecomes(TypedNode becomes, TypedNode node) {
	checkArgument(node instanceof AppliedOccurrenceNode);
	AppliedOccurrenceNode appNode = (AppliedOccurrenceNode) node;
	checkArgument(appNode.getBindingNode() instanceof BindingOccurrenceNode);
	BindingOccurrenceNode bindingOccurrence = (BindingOccurrenceNode) appNode.getBindingNode();
	
	if(becomes.isResultUsed())
	    mg.dup();

	switch (bindingOccurrence.getVariableType()) {
	case LOCAL:
	    mg.storeLocal(bindingOccurrence.getNumber());
	    break;
	case ARGUMENT:
	    mg.storeArg(bindingOccurrence.getNumber());
	    break;
	case FIELD:
	    mg.loadThis();
	    mg.swap(); // swap Object/Value voor PUTFIELD instructie. Gaat goed
		       // zolang wij geen long/double primitieven hebben ...
	    mg.putField(internalClassType, appNode.getText(), appNode.getNodeType().toASM());
	    break;
	}
    }

    public void visitDecl(TypedNode node) {
	checkArgument(mg == null || inFunction);
	checkArgument(node instanceof BindingOccurrenceNode);
	currentVar = (BindingOccurrenceNode) node;

	if (currentVar.getAssignCount() == 0)
	    return;

	if (!inFunction) {// Initialisatie van variabele in constructor
	    log.debug("declVar {} {}", node.getText(), node.getNodeType().toASM().getDescriptor());
	    cv.visitField(ACC_PUBLIC, node.getText(), node.getNodeType().toASM().getDescriptor(), null, null).visitEnd();

	    currentVar.setVariableType(VariableType.FIELD);

	    mg = constructorMethodGenerator;
	} else {
	    /*
	     * Maak lokale variabele aan en store zijn index. Index management
	     * (tov argumenten bijvoorbeeld) wordt gedaan door ASM magie
	     */
	    int i = mg.newLocal(node.getNodeType().toASM());
	    currentVar.setNumber(i);
	    currentVar.setVariableType(VariableType.LOCAL);
	}

	assert currentVar.getVariableType() != null;
    }

    /**
     * Omdat wij constanten niet statisch definieren is er geen verschil tussen
     * einde van var & const definitie
     * 
     * @post currentVar = null
     */
    public void visitDeclEnd(TypedNode node) {
	checkNotNull(currentVar);
	// Moeten we storen? -> heeft ie een expressie
	boolean hasValue = node != null;

	if (currentVar.getAssignCount() == 0) {
	    currentVar = null;
	    return;
	}

	if (!inFunction) {
	    log.debug("visitDeclEnd LOCAL {} @ {}", currentVar.getText(), currentVar);

	    if (!hasValue) {
		loadPlaceholder();
	    }
	    mg.loadThis();
	    mg.swap();
	    mg.visitFieldInsn(PUTFIELD, internalClassType.getInternalName(), currentVar.getText(), currentVar.getNodeType().toASM()
		    .getDescriptor());
	    // Remove the pointer to the constructor
	    mg = null;
	} else {
	    log.debug("VarInsn {} {}", currentVar.getNodeType().toASM().getOpcode(ISTORE), currentVar);

	    if (!hasValue) {
		loadPlaceholder();
	    }
	    mg.storeLocal(currentVar.getNumber());
	}

	currentVar = null;
    }

    private void loadPlaceholder() {
	if (currentVar.getNodeType() == ExampleType.STRING) {
	    mg.visitLdcInsn("");
	} else {
	    // Primitive -> 0 is goed
	    mg.visitInsn(ICONST_0);
	}

    }

    public void visitFuncCallBegin(TypedNode n, List<TypedNode> params) {
	mg.visitLineNumber(n.getLine(), mg.mark());
	// Special handling of VarArgs/READ function
	checkArgument(n instanceof AppliedOccurrenceNode);

	FunctionNode func = (FunctionNode) ((AppliedOccurrenceNode) n).getBindingNode();

	log.debug("Loading arguments? {}", loadVars);
	// Load this onto the stack
	// stack protocol of InvokeVirtual: ..., objectref, [arg1, [arg2 ...]]
	// => ...
	switch(func.getBoundMethod().getIdType()){
		case VARARGS:
		    // Doet zijn eigen stack management
		    // Deze ALOAD0 wordt anders niet gepopped
		    break;
		default:
		    mg.loadThis();
	}
	log.debug("visitFuncCallBegin");
    }

    public void visitFuncCallEnd(TypedNode call, TypedNode n, List<TypedNode> params) {
	log.debug("visit function call: " + n);

	checkArgument(n instanceof AppliedOccurrenceNode);

	AppliedOccurrenceNode an = (AppliedOccurrenceNode) n;

	checkArgument(an.getBindingNode() instanceof FunctionNode);

	FunctionNode node = (FunctionNode) an.getBindingNode();

	checkArgument(!node.getBoundMethod().getIdType().equals(IdType.VARIABLE));

	String name = node.getText();
	Method target;

	switch (node.getBoundMethod().getIdType()) {
	case BUILTIN:
	    log.debug("Builtin function " + name);
	    target = new Method(name, node.getNodeType().toASM(), ExampleType.nodeListToASM(params));
	    mg.invokeVirtual(superClassName, target);
	    break;
	case FUNCTION:
	    log.debug("User-Defined function " + name);
	    // The difference between the invokespecial and the invokevirtual
	    // instructions is that invokevirtual invokes a method based on the
	    // class of the object.
	    // The invokespecial instruction is used to invoke instance
	    // initialization methods (§3.9) as well as private methods and
	    // methods of a superclass of the current class.
	    target = new Method(name, node.getNodeType().toASM(), ExampleType.nodeListToASM(params));
	    mg.invokeVirtual(internalClassType, target);
	    break;
	case VARARGS:
	    // For each argument: dispatch a [functionname] call for the given
	    // argument
	    // then save it into the variable
	    for (int i = params.size() - 1; i >= 0; i--) {
		boolean last = i == 0;
		TypedNode arg = params.get(i);
		
		mg.loadThis();
		target = new Method(name, arg.getNodeType().toASM(), new Type[] { arg.getNodeType().toASM() });
		mg.swap();
		mg.invokeVirtual(superClassName, target);
		
		if(last){
		    mg.dup();// Leave result on stack
		}
		visitBecomes(arg, arg);
	    }
	    break;
	}
	if (node.getBoundMethod().getType() != ExampleType.VOID && !call.isResultUsed())
	    mg.pop();
    }

    public void visitIfBegin(TypedNode node, Label ifEnd) {
	// Type van expressiezijden opvragen
	Type type = node.getNodeType().toASM();
	// Jump naar ifEnd als true
	mg.visitJumpInsn(IFEQ, ifEnd);
	// mg.ifCmp(type, GeneratorAdapter.EQ, ifEnd);
    }

    /**
     * Mark het label dat het einde van de de TRUE expressies definiteert en
     * GOTO het label aan het einde van de else.
     */
    public void visitIfHalf(TypedNode node, Label ifEnd, Label elseEnd) {
	mg.goTo(elseEnd);
	mg.mark(ifEnd);
    }

    public void visitIfEnd(TypedNode node, Label elseEnd) {
	mg.mark(elseEnd);
    }

    public void visitWhileCond(Label condBegin) {
	mg.mark(condBegin);
    }

    public void visitWhileBegin(Label loopEnd) {
	// Jumpen naar loopEnd als false/gelijk aan 0 (ifEQ vergelijkt immers
	// met 0)
	mg.visitJumpInsn(Opcodes.IFEQ, loopEnd);
    }

    public void visitWhileEnd(Label condBegin, Label loopEnd) {
	// Jumpen naar loopBegin als gelijk, later backpatchen
	mg.goTo(condBegin);
	// Backpatchen
	mg.mark(loopEnd);
    }

    public void visitBinaryOperator(/* Opcode */int opcode, TypedNode node, TypedNode lhs, TypedNode rhs) {
	if(node.isResultUsed()){
	    if (!ExampleType.STRING.equals(lhs.getNodeType())) {
		mg.visitInsn(lhs.getNodeType().toASM().getOpcode(opcode));
	    } else {
		log.debug("String append {} of {} and {}", new Object[]{node, lhs, rhs});
		checkArgument(opcode == Opcodes.IADD, "Invalid binary operator on a string");
		prependThisToLhsRhs();
		// invokeVirtual
		mg.invokeVirtual(superClassName, new Method("stringAppend", Type.getType(String.class), new Type[] {
			lhs.getNodeType().toASM(), rhs.getNodeType().toASM() }));
	    }
	}
    }
    
    /**
     * A utility function that applies the following transformation to the stack;
     * ... [lhs] [rhs] => ... [this] [lhs] [rhs]
     * 
     * You usually want to use this when using string functions.
     */
    private void prependThisToLhsRhs(){
	// Stack protocol
	// [lhs] [rhs]
	mg.swap();
	// [rhs] [lhs]
	mg.loadThis();
	// [rhs] [lhs]] [this]
	mg.swap();
	// [rhs] [this] [lhs]
	mg.dup2X1();
	// [this] [lhs] [rhs] [this] [lhs]
	mg.pop2();
	// [this] [lhs] [rhs]
    }

    public void visitCompareOperator(int opcode, TypedNode node, TypedNode lhs, TypedNode rhs) {
	if (node.isResultUsed()) {
	    if (!ExampleType.STRING.equals(lhs.getNodeType())) {
		Label exprTrue = new Label();
		Label exprFalse = new Label();
		mg.visitJumpInsn(opcode, exprTrue);
		mg.visitInsn(ICONST_0);
		mg.goTo(exprFalse);
		mg.mark(exprTrue);
		mg.visitInsn(ICONST_1);
		mg.mark(exprFalse);
	    } else {
		checkArgument(opcode == Opcodes.IF_ICMPNE || opcode == Opcodes.IF_ICMPEQ);
		switch (opcode) {
		case Opcodes.IF_ICMPNE:
		    prependThisToLhsRhs();
		    mg.invokeVirtual(superClassName, new Method("stringNE", Type.BOOLEAN_TYPE, new Type[] { lhs.getNodeType().toASM(),
			    rhs.getNodeType().toASM() }));
		    break;
		case Opcodes.IF_ICMPEQ:
		    prependThisToLhsRhs();
		    mg.invokeVirtual(superClassName, new Method("stringEQ", Type.BOOLEAN_TYPE, new Type[] { lhs.getNodeType().toASM(),
			    rhs.getNodeType().toASM() }));
		    break;
		}
	    }
	}
    }

    public void visitReturn(TypedNode expr) {
	mg.visitInsn(expr.getNodeType().toASM().getOpcode(IRETURN));
    }

    public void visitNot(TypedNode base) {
	if (base.isResultUsed())
	    mg.not();
    }

    public void visitVariable(TypedNode n) {
	checkArgument(n instanceof AppliedOccurrenceNode);

	BindingOccurrenceNode node = (BindingOccurrenceNode) ((AppliedOccurrenceNode) n).getBindingNode();
	if (loadVars) {
	    switch (node.getVariableType()) {
	    case ARGUMENT:
		mg.loadArg(node.getNumber());
		break;
	    case LOCAL:
		mg.loadLocal(node.getNumber());
		break;
	    case FIELD:
		mg.loadThis();
		mg.getField(internalClassType, node.getText(), node.getNodeType().toASM());
		break;
	    }
	}
	log.debug("loading argument {} #{}? {}", new Object[] { node, node.getNumber(), loadVars });
    }

    public void loadVars(boolean state) {
	loadVars = state;
    }

    public void visitCharAtom(TypedNode node) {
	if (node.isResultUsed())
	    visitAtom(node.getNodeType().toASM(), (int) node.getText().charAt(0));
    }

    public void visitBooleanAtom(TypedNode node) {
	if (node.isResultUsed()) {
	    if (Boolean.valueOf(node.getText())) {
		mg.visitInsn(ICONST_1);
	    } else {
		mg.visitInsn(ICONST_0);
	    }
	}
    }

    public void visitStringAtom(TypedNode node) {
	if (node.isResultUsed())
	    mg.visitLdcInsn(node.getText());
    }

    /**
     * We weten zeker dat dit een int is
     * 
     * @param node
     * @param negative
     */
    public void visitIntegerAtom(TypedNode node) {
	int value = Integer.valueOf(node.getText());

	if (node.isResultUsed())
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
