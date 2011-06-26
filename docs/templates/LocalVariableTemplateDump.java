import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class LocalVariableTemplateDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "LocalVariableTemplate", null,
				"java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE, "GLOBAL", "I", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(PUTFIELD, "LocalVariableTemplate", "GLOBAL", "I");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "func", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "LocalVariableTemplate", "GLOBAL", "I");
			mv.visitVarInsn(ISTORE, 1);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
