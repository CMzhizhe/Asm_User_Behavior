package com.sensorsdata.analytics.android.plugin

import com.gxx.collectionuserbehaviorlibrary.costtime.CostTime
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public class SensorsAnalyticsCostTimeClassVisitor extends ClassVisitor implements Opcodes {
    private ClassVisitor classVisitor
    public SensorsAnalyticsCostTimeClassVisitor(int api) {
        super(api);
    }

    public SensorsAnalyticsCostTimeClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }

    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access)
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 拿到需要修改的方法，执行修改操作
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        String nameDesc = name + desc
        methodVisitor = new SensorsAnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc){
            boolean isCostAnnotation = false

            @Override
            protected void onMethodEnter() {
               if (isCostAnnotation){
                    //插入class
                   methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                   methodVisitor.visitLdcInsn("========start=========");
                   methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


                   methodVisitor.visitLdcInsn(name);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "com/gxx/android_asm_1_project/TimeCache", "setStartTime", "(Ljava/lang/String;J)V", false);


                   methodVisitor.visitLdcInsn(name);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "com/gxx/android_asm_1_project/TimeCache", "setEndTime", "(Ljava/lang/String;J)V", false);


                   methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                   methodVisitor.visitLdcInsn(name);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "com/gxx/android_asm_1_project/TimeCache", "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
                   methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


                   methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                   methodVisitor.visitLdcInsn("========end=========");
                   methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
               }
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (isCostAnnotation){
                    //结算class
                }
            }

            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                if (Type.getDescriptor(CostTime.class).equals(s)){
                    isCostAnnotation = true;
                }
                return super.visitAnnotation(s, b)
            }
        }
        return methodVisitor;
    }
}
