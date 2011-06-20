import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

public class WhileTemplateDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "WhileTemplate", null,
				"java/lang/Object", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitIntInsn(BIPUSH, 10);//Push byte value 10
			mv.visitVarInsn(ISTORE, 1);//Store in local 1
			Label l0 = new Label();//Label pre-while
			mv.visitLabel(l0);//visit label -> bind aan plek in code
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] { "WhileTemplate",
					Opcodes.INTEGER }, 0, new Object[] {});
			mv.visitVarInsn(ILOAD, 1);//load 1
			Label l1 = new Label();//label post-load, 1 op top stack
			mv.visitJumpInsn(IFLE, l1);//i < 1, jump L1 (post-while)
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ILOAD, 1);//
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			mv.visitIincInsn(1, -1);
			mv.visitJumpInsn(GOTO, l0);//Goto pre-while label
			mv.visitLabel(l1);//post-while
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
