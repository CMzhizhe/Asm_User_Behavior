package com.sensorsdata.analytics.android.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class SensorsAnalyticsClassVisitor extends ClassVisitor implements Opcodes {
    private final
    static String SDK_API_CLASS = "com/gxx/collectionuserbehaviorlibrary/sensors/SensorsDataAutoTrackHelper"
    private String[] mInterfaces
    private ClassVisitor classVisitor

    private HashMap<String, SensorsAnalyticsMethodCell> mLambdaMethodCells = new HashMap<>()

    SensorsAnalyticsClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }

    private static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }

    /**
     * 可以拿到类的详细信息，然后对满足条件的类进行过滤
     * 该方法是当扫描类时，第一个会调用的方法
     * @param version
     * @param access
     * @param name 形如
     * name = androidx/annotation/FractionRes
     * name = androidx/annotation/GuardedBy
     * name = androidx/annotation/HalfFloat
     * name = androidx/constraintlayout/solver/LinearSystem$ValuesRow
     * name = androidx/constraintlayout/solver/Pools
     * name = androidx/constraintlayout/solver/state/Reference
     * @param signature
     * @param superName
     * @param interfaces 接口，一个类可以实现多个不同的接口
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces = interfaces
    }

    /**
     * 访问内部类信息
     * @param name
     * @param outerName
     * @param innerName
     * @param access
     */
    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access)
    }

    /**
     * 拿到需要修改的方法，然后进行修改操作
     * 扫描器，扫描到类的方法时候进行调用
     * @param access
     * @param name 方法名 比如 onCreate
     * @param desc 方法签名 (Landroid/os/Bundle;)V
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        String nameDesc = name + desc

        methodVisitor = new SensorsAnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc) {
            boolean isSensorsDataTrackViewOnClickAnnotation = false

            @Override
            void visitEnd() {
                super.visitEnd()

                if (mLambdaMethodCells.containsKey(nameDesc)) {
                    mLambdaMethodCells.remove(nameDesc)
                }
            }

            /**
             * 访问一个 invokedynamic 指令，一般是 Lambda 访问时
             * @param name1 the method's name.
             * @param desc1 the method's descriptor (see Type).
             * @param bsm the bootstrap method.
             * @param bsmArgs the bootstrap method constant arguments. Each argument must be an Integer, Float, Long, Double, String, Type, Handle or ConstantDynamic value. This method is allowed to modify the content of the array so a caller should expect that this array may change.
             */
            @Override
            void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
                super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs)
                try {
                    String desc2 = (String) bsmArgs[0]
                /*    println("Type.getReturnType(desc1).getDescriptor() = " + Type.getReturnType(desc1).getDescriptor())
                    println("desc1 = " + desc1) //()Landroid/view/View$OnClickListener;
                    println("name1 = " + name1) //onClick
                    println("desc2 = " + desc2) //(Landroid/view/View;)V*/
                    //Landroid/view/View$OnClickListener; + onClick + (Landroid/view/View;)V
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2)
                    if (sensorsAnalyticsMethodCell != null) {
                        Handle it = (Handle) bsmArgs[1]
                     /*   println("it.name = " + it.name)//lambda$onCreate$0
                        println("it.desc = " + it.desc)//(Landroid/view/View;)V*/
                        mLambdaMethodCells.put(it.name + it.desc, sensorsAnalyticsMethodCell)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodExit(int opcode) {//方法退出节点
                super.onMethodExit(opcode)
            }

            /**
             * 进入方法时，插入字节码
             */
            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()

                /**
                 * 在 android.gradle 的 3.2.1 版本中，针对 view 的 setOnClickListener 方法 的 lambda 表达式做特殊处理。
                 */
                SensorsAnalyticsMethodCell lambdaMethodCell = mLambdaMethodCells.get(nameDesc)
                if (lambdaMethodCell != null) {
                    Type[] types = Type.getArgumentTypes(lambdaMethodCell.desc)
                    int length = types.length
                    Type[] lambdaTypes = Type.getArgumentTypes(desc)
                 /*   println("lambdaMethodCell.parent = " + lambdaMethodCell.parent) //
                    println("lambdaMethodCell.desc = " + lambdaMethodCell.desc) //
                    println("lambdaTypes[] = " + lambdaTypes.toString()) //[Landroid/view/View;]
                    println("types[] = " + types.toString()) //[Landroid/view/View;]*/
                    int paramStart = lambdaTypes.length - length
                    if (paramStart < 0) {
                        return
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                return
                            }
                        }
                    }
                    boolean isStaticMethod = SensorsAnalyticsUtils.isStatic(access)
                    if (!isStaticMethod) {
                        if (lambdaMethodCell.desc == '(Landroid/view/ MenuItem;)Z') {
                            methodVisitor.visitVarInsn(ALOAD, 0)
                            methodVisitor.visitVarInsn(ALOAD, getVisitPosition(lambdaTypes, paramStart, isStaticMethod))
                            methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, lambdaMethodCell.agentName, '(Ljava/lang/Object;Landroid/view/MenuItem;)V', false)
                            return
                        }
                    }

                    /**
                     for(int i = 0;i<2;i++){
                       if(i == 0){
                        methodVisitor.visitVarInsn(lambdaMethodCell.opcodes.get(0),getVisitPosition([Landroid/view/View;], i, false))
                      }
                     }*/
                    //
                    //methodVisitor.visitVarInsn(Opcodes.ALOAD,)
                    for (int i = paramStart; i < paramStart + lambdaMethodCell.paramsCount; i++) {
                        methodVisitor.visitVarInsn(lambdaMethodCell.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
                    }
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, lambdaMethodCell.agentName, lambdaMethodCell.agentDesc, false)
                    return
                }

                if (nameDesc == 'onContextItemSelected(Landroid/view/MenuItem;)Z' ||
                        nameDesc == 'onOptionsItemSelected(Landroid/view/MenuItem;)Z') {
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false)
                }

                if (isSensorsDataTrackViewOnClickAnnotation) {
                    if (desc == '(Landroid/view/View;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/view/View;)V", false)
                        return
                    }
                }


                if ((mInterfaces != null && mInterfaces.length > 0)) {
                    if ((mInterfaces.contains('android/view/View$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V')) {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/view/View;)V", false)
                    } else if (mInterfaces.contains('android/content/DialogInterface$OnClickListener') && nameDesc == 'onClick(Landroid/content/DialogInterface;I)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ILOAD, 2)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/content/DialogInterface;I)V", false)
                    } else if (mInterfaces.contains('android/content/DialogInterface$OnMultiChoiceClickListener') && nameDesc == 'onClick(Landroid/content/DialogInterface;IZ)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ILOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/content/DialogInterface;IZ)V", false)
                    } else if (mInterfaces.contains('android/widget/CompoundButton$OnCheckedChangeListener') && nameDesc == 'onCheckedChanged(Landroid/widget/CompoundButton;Z)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ILOAD, 2)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/widget/CompoundButton;Z)V", false)
                    } else if (mInterfaces.contains('android/widget/RatingBar$OnRatingBarChangeListener') && nameDesc == 'onRatingChanged(Landroid/widget/RatingBar;FZ)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/view/View;)V", false)
                    } else if (mInterfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener') && nameDesc == 'onStopTrackingTouch(Landroid/widget/SeekBar;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/view/View;)V", false)
                    } else if (mInterfaces.contains('android/widget/AdapterView$OnItemSelectedListener') && nameDesc == 'onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ALOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                    } else if (mInterfaces.contains('android/widget/TabHost$OnTabChangeListener') && nameDesc == 'onTabChanged(Ljava/lang/String;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackTabHost", "(Ljava/lang/String;)V", false)
                    } else if (mInterfaces.contains('android/widget/AdapterView$OnItemClickListener') && nameDesc == 'onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ALOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackViewOnClick", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                    } else if (mInterfaces.contains('android/widget/ExpandableListView$OnGroupClickListener') && nameDesc == 'onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ALOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackExpandableListViewGroupOnClick", "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V", false)
                    } else if (mInterfaces.contains('android/widget/ExpandableListView$OnChildClickListener') && nameDesc == 'onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitVarInsn(ALOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitVarInsn(ILOAD, 4)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SDK_API_CLASS, "trackExpandableListViewChildOnClick", "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V", false)
                    }
                }
            }

            /**
             * 可以在这里通过注解的方式操作字节码
             * @param s
             * @param b
             * @return
             */
            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                if (s == 'Lcom/gxx/collectionuserbehaviorlibrary/sensors/SensorsDataTrackViewOnClick;') {
                    isSensorsDataTrackViewOnClickAnnotation = true
                }

                return super.visitAnnotation(s, b)
            }
        }
        return methodVisitor
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     * @param types 方法参数类型数组 [Landroid/view/View;]
     * @param index 方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }
}