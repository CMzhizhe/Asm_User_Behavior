package com.sensorsdata.analytics.android.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.apache.commons.io.FileUtils
import  com.android.build.api.transform.Format


public class SensorsAnalyticsTransform extends Transform{
    public static HashSet<String> CONTAINS_LIBNAME = new HashSet<>()

    private static String fuckBug = "\n" +
            "   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗\n" +
            " ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝\n" +
            " ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗\n" +
            " ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║\n" +
            " ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝\n" +
            "  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝\n" +
            "  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░\n" +
            "  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░\n" +
            "           ░     ░ ░      ░  ░\n" +
            "                                                                          --------GXX \n"
    private static Project project
    private SensorsAnalyticsExtension sensorsAnalyticsExtension;
    public SensorsAnalyticsTransform(Project project,SensorsAnalyticsExtension sensorsAnalyticsExtension) {
        this.project = project
        this.sensorsAnalyticsExtension = sensorsAnalyticsExtension;
    }

    @Override
    String getName() {
        return "SensorsAnalyticsTransform"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 打印提示信息
     */
    static void printCopyRight() {
        println()
        println(fuckBug)
        println()
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        printCopyRight()

        if (sensorsAnalyticsExtension.disablePlugin){
            println("已禁插件")
        }

        if (sensorsAnalyticsExtension.disableAppClick){
            println("已禁用点击统计")
        }

        if (sensorsAnalyticsExtension.disableCostTime){
            println("已禁用方法耗时统计")
        }


        CONTAINS_LIBNAME.clear();
        if (sensorsAnalyticsExtension.containsString != null && sensorsAnalyticsExtension.containsString.trim().length() > 0) {
            String[] excludeArray = sensorsAnalyticsExtension.containsString.split(",");
            for (int i = 0; i < excludeArray.length; i++) {
                CONTAINS_LIBNAME.add(excludeArray[i])
            }
        }


        if (!isIncremental()){
            transformInvocation.getOutputProvider().deleteAll()
        }

        // Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历
        transformInvocation.getInputs().each { TransformInput input ->
            //遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //获取 output 目录
                File dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes,  Format.DIRECTORY)
                File dir = directoryInput.file;
                if (dir.exists()){
                    HashMap<String ,File> modifyMap = new HashMap<String ,File>();
                    //遍历以某一扩展名结尾的文件
                    if (!sensorsAnalyticsExtension.disablePlugin){
                        dir.traverse (type :FileType.FILES,nameFilter:~/.*\.class/){
                            File classFile->
                                SensorsAnalyticsClassModifier sensorsAnalyticsClassModifier = new SensorsAnalyticsClassModifier()
                                String classFileName = classFile.getName();
                                classFileName = classFileName.replace(dir.getAbsolutePath(),"");
                                if (sensorsAnalyticsClassModifier.isShouldModify(classFileName,classFile.getAbsolutePath())){
                                    File modified = null;

                                    if (!sensorsAnalyticsExtension.disableAppClick){
                                        modified = sensorsAnalyticsClassModifier.modifyClassFile(dir,classFile,transformInvocation.getContext().getTemporaryDir())
                                    }

                                    if (!sensorsAnalyticsExtension.disableCostTime){
                                        modified = sensorsAnalyticsClassModifier.modifyClassFile2(dir,classFile,transformInvocation.getContext().getTemporaryDir())
                                    }

                                    if (modified != null) {
                                        //println("classFile.absolutePath = " + classFile.absolutePath);
                                        //println("dir.absolutePath = " + dir.absolutePath);
                                        String ke = classFile.absolutePath.replace(dir.absolutePath, "")
                                        modifyMap.put(ke, modified)
                                    }
                                }
                        }
                    }

                    FileUtils.copyDirectory(directoryInput.file, dest)

                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(en.getValue(), target)
                            en.getValue().delete()
                    }
                }
            }


            //遍历 jar
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name

                //截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
               //获取 jar 名字
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                //获得输出文件
                File dest = transformInvocation.getOutputProvider().getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                def modifiedJar = null;

                if (!sensorsAnalyticsExtension.disablePlugin){
                    SensorsAnalyticsClassModifier sensorsAnalyticsClassModifier = new SensorsAnalyticsClassModifier()
                    if (!sensorsAnalyticsExtension.disableAppClick) {
                        modifiedJar = sensorsAnalyticsClassModifier.modifyJar(jarInput.file, transformInvocation.getContext().getTemporaryDir(), true)
                    }

                    if (!sensorsAnalyticsExtension.disableCostTime) {
                        modifiedJar = sensorsAnalyticsClassModifier.modifyJar(jarInput.file, transformInvocation.getContext().getTemporaryDir(), true)
                    }
                }


                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar, dest)
            }

        }


    }
}