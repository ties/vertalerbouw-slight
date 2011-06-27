import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class ControlFlowDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "ControlFlow", null,
				"java/lang/Object", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "controlIf", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(ICONST_3);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitInsn(ICONST_4);
			mv.visitVarInsn(ISTORE, 2);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			Label l0 = new Label();
			mv.visitJumpInsn(IF_ICMPNE, l0);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { Opcodes.INTEGER,
					Opcodes.INTEGER }, 0, null);
			mv.visitInsn(ICONST_M1);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "controlWhile", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(ICONST_1);
			mv.visitVarInsn(ISTORE, 1);
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND, 1,
					new Object[] { Opcodes.INTEGER }, 0, null);
			mv.visitVarInsn(ILOAD, 1);
			Label l1 = new Label();
			mv.visitJumpInsn(IFLE, l1);
			mv.visitIincInsn(1, -1);
			mv.visitJumpInsn(GOTO, l0);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
