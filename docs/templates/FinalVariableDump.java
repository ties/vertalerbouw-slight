import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class FinalVariableDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "FinalVariable", null,
				"java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "TRUTH", "I", null,
					null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "getValue", "()I", null, null);
			mv.visitCode();
			mv.visitIntInsn(BIPUSH, 42);//Push byte
			mv.visitInsn(IRETURN);//Return integer
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "FinalVariable", "getValue",
					"()I");
			mv.visitFieldInsn(PUTFIELD, "FinalVariable", "TRUTH", "I");
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "FinalVariable", "TRUTH", "I");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
