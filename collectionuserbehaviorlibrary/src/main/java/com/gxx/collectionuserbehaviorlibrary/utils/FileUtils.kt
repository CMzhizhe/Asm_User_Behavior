package com.gxx.collectionuserbehaviorlibrary.utils

import android.content.Context
import android.os.Environment
import java.io.File

class FileUtils {
    private final val userBehaviorDirs = "userBehaviorDirs";

    /**
     * 作者：GaoXiaoXiong
     * 创建时间:2019/1/26
     * 注释描述:获取缓存目录
     * @fileName 获取沙盒存储目录下缓存的,缓存目录文件夹
     */
    fun getSandboxPublickDiskCacheDir(context: Context): String {
        var cachePath: String = ""
        cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) { //此目录下的是外部存储下的私有的fileName目录
                context.externalCacheDir!!.path + "/" + userBehaviorDirs //SDCard/Android/data/你的应用包名/cache/fileName
            } else {
                context.cacheDir.path + "/" + userBehaviorDirs
            }
        val file = File(cachePath)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath //SDCard/Android/data/你的应用包名/cache/fileName
    }
}