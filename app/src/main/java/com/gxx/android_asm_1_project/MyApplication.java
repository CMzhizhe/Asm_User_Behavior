package com.gxx.android_asm_1_project;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.List;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (getProcessName(this).endsWith("com.gxx.android_asm_1_project")){
            SensorsDataAPI.init(this);
        }
    }


    /**
     * @date :2019/11/21 0021
     * @author : gaoxiaoxiong
     * @description:获取进程名称
     **/
    public String getProcessName(Context context) {
        String currentProcessName = null;
        if (context == null) {
            try {
                File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
                BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
                currentProcessName = mBufferedReader.readLine().trim();
                mBufferedReader.close();
                return currentProcessName;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        //1)通过Application的API获取当前进程名
        currentProcessName = getCurrentProcessNameByApplication();
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName;
        }

        //2)通过反射ActivityThread获取当前进程名
        currentProcessName = getCurrentProcessNameByActivityThread();
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName;
        }

        //3)通过ActivityManager获取当前进程名
        currentProcessName = getCurrentProcessNameByActivityManager(context);
        return currentProcessName;
    }


    /**
     * 通过Application新的API获取进程名，无需反射，无需IPC，效率最高。
     */
    private static String getCurrentProcessNameByApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        return null;
    }

    /**
     * 通过反射ActivityThread获取进程名，避免了ipc
     */
    private static String getCurrentProcessNameByActivityThread() {
        String processName = null;
        try {
            final Method declaredMethod = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
                    .getDeclaredMethod("currentProcessName", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null, new Object[0]);
            if (invoke instanceof String) {
                processName = (String) invoke;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return processName;
    }

    /**
     * 通过ActivityManager 获取进程名，需要IPC通信
     */
    private static String getCurrentProcessNameByActivityManager(@NonNull Context context) {
        if (context == null) {
            return null;
        }
        int pid = Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> runningAppList = am.getRunningAppProcesses();
            if (runningAppList != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningAppList) {
                    if (processInfo.pid == pid) {
                        return processInfo.processName;
                    }
                }
            }
        }
        return null;
    }
}
