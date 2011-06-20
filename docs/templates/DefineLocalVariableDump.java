import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class DefineLocalVariableDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "DefineLocalVariable", null,
				"java/lang/Object", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V",
					null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);//On instance method invocation, local variable 0 is always used to pass a reference to the object on which the instance method is being invoked (this in the Java programming language). Any parameters are subsequently passed in consecutive local variables starting from local variable 1.
			//http://java.sun.com/docs/books/jvms/second_edition/html/Overview.doc.html
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitIntInsn(BIPUSH, 106);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitLdcInsn("Foobar");
			mv.visitVarInsn(ASTORE, 3);
			mv.visitIntInsn(BIPUSH, 42);
			mv.visitVarInsn(ISTORE, 2);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ASTORE, 5);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 6);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
					"([Ljava/lang/String;)V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "DefineLocalVariable");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("arg");
			mv.visitMethodInsn(INVOKESPECIAL, "DefineLocalVariable", "<init>",
					"(Ljava/lang/String;)V");
			mv.visitInsn(POP);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
