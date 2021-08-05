package com.sensorsdata.analytics.android.plugin

import com.gxx.collectionuserbehaviorlibrary.costtime.CostTime
import org.objectweb.asm.*

public class SensorsAnalyticsCostTimeClassVisitor extends ClassVisitor implements Opcodes {
    private ClassVisitor classVisitor
    private String className = "";
    public SensorsAnalyticsCostTimeClassVisitor(int api) {
        super(api);
    }

    public SensorsAnalyticsCostTimeClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }


    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 拿到需要修改的方法，执行修改操作
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        methodVisitor = new SensorsAnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc){
            boolean isCostAnnotation = false

            @Override
            protected void onMethodEnter() {
               if (isCostAnnotation){
                   methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                   methodVisitor.visitLdcInsn("========start=========");
                   methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

                   methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                   methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                   methodVisitor.visitVarInsn(ASTORE, 1);

               }
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (isCostAnnotation){
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                    methodVisitor.visitVarInsn(ASTORE, 2);

                    methodVisitor.visitVarInsn(ALOAD, 1);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                    methodVisitor.visitVarInsn(ALOAD, 2);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                    methodVisitor.visitLdcInsn(className);
                    methodVisitor.visitLdcInsn(name);
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "com/gxx/collectionuserbehaviorlibrary/costtime/TimeCostHelper", "trackTime", "(JJLjava/lang/String;Ljava/lang/String;)V", false);



                    methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    methodVisitor.visitLdcInsn("========end=========");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
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
