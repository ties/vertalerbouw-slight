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
			fv = cw.visitField(0, "global", "I", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitIntInsn(BIPUSH, 21);
			mv.visitFieldInsn(PUTFIELD, "DefineLocalVariable", "global", "I");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "main", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(ICONST_2);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
					"([Ljava/lang/String;)V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "DefineLocalVariable");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "DefineLocalVariable", "<init>",
					"()V");
			mv.visitInsn(POP);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
