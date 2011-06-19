import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class ReadTemplateDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "ReadTemplate", null, "java/lang/Object", null);

{
fv = cw.visitField(0, "input", "Ljava/io/BufferedReader;", null, null);
fv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PRIVATE, "readString", "()Ljava/lang/String;", null, null);
mv.visitCode();
Label l0 = new Label();
Label l1 = new Label();
Label l2 = new Label();
mv.visitTryCatchBlock(l0, l1, l2, "java/io/IOException");
mv.visitLabel(l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "ReadTemplate", "input", "Ljava/io/BufferedReader;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "readLine", "()Ljava/lang/String;");
mv.visitLabel(l1);
mv.visitInsn(ARETURN);
mv.visitLabel(l2);
mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/io/IOException"});
mv.visitVarInsn(ASTORE, 1);
mv.visitLdcInsn("");
mv.visitInsn(ARETURN);
mv.visitMaxs(1, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PRIVATE, "readInt", "()I", null, null);
mv.visitCode();
Label l0 = new Label();
Label l1 = new Label();
Label l2 = new Label();
mv.visitTryCatchBlock(l0, l1, l2, "java/io/IOException");
mv.visitLabel(l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "ReadTemplate", "input", "Ljava/io/BufferedReader;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "read", "()I");
mv.visitLabel(l1);
mv.visitInsn(IRETURN);
mv.visitLabel(l2);
mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/io/IOException"});
mv.visitVarInsn(ASTORE, 1);
mv.visitInsn(ICONST_M1);
mv.visitInsn(IRETURN);
mv.visitMaxs(1, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PRIVATE, "readInteger", "(Ljava/lang/Integer;)V", null, null);
mv.visitCode();
Label l0 = new Label();
Label l1 = new Label();
Label l2 = new Label();
mv.visitTryCatchBlock(l0, l1, l2, "java/io/IOException");
mv.visitLabel(l0);
mv.visitTypeInsn(NEW, "java/lang/Integer");
mv.visitInsn(DUP);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "ReadTemplate", "input", "Ljava/io/BufferedReader;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/BufferedReader", "read", "()I");
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V");
mv.visitVarInsn(ASTORE, 1);
mv.visitLabel(l1);
Label l3 = new Label();
mv.visitJumpInsn(GOTO, l3);
mv.visitLabel(l2);
mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/io/IOException"});
mv.visitVarInsn(ASTORE, 2);
mv.visitLabel(l3);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 3);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
mv.visitVarInsn(ALOAD, 0);
mv.visitTypeInsn(NEW, "java/io/BufferedReader");
mv.visitInsn(DUP);
mv.visitTypeInsn(NEW, "java/io/InputStreamReader");
mv.visitInsn(DUP);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
mv.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V");
mv.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V");
mv.visitFieldInsn(PUTFIELD, "ReadTemplate", "input", "Ljava/io/BufferedReader;");
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "ReadTemplate", "readInt", "()I");
mv.visitVarInsn(ISTORE, 1);
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "ReadTemplate", "readString", "()Ljava/lang/String;");
mv.visitVarInsn(ASTORE, 2);
mv.visitInsn(RETURN);
mv.visitMaxs(6, 3);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}
