import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class PrintTemplateDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "PrintTemplate", null,
				"java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE, "instance_var",
					"Ljava/lang/String;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC,
					"instance_constant", "Ljava/lang/String;", null, "Noot");
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "instance_primitive", "I", null,
					null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "print", "(I)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "print", "(Ljava/lang/String;)V",
					null, null);
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
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn("Aap");
			mv.visitFieldInsn(PUTFIELD, "PrintTemplate", "instance_var",
					"Ljava/lang/String;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitIntInsn(BIPUSH, 42);
			mv.visitFieldInsn(PUTFIELD, "PrintTemplate", "instance_primitive",
					"I");
			mv.visitInsn(ICONST_3);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print", "(I)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn("Hello");
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print",
					"(Ljava/lang/String;)V");
			mv.visitLdcInsn("hello");
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print",
					"(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "PrintTemplate", "instance_var",
					"Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print",
					"(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn("Noot");
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print",
					"(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "PrintTemplate", "instance_primitive",
					"I");
			mv.visitMethodInsn(INVOKEVIRTUAL, "PrintTemplate", "print", "(I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
