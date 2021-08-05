package com.gxx.android_asm_1_project;

import java.util.HashMap;
import java.util.Map;

public class TimeCache {
    public static Map sStartTime = new HashMap<>();
    public static Map sEndTime = new HashMap<>();
    public static void setStartTime(String methodName, long time) {
        sStartTime.put(methodName, time);
    }
    public static void setEndTime(String methodName, long time) {
        sEndTime.put(methodName, time);
    }
    public static String getCostTime(String methodName) {
        long start = (long) sStartTime.get(methodName);
        long end = (long) sEndTime.get(methodName);
        return "method: " + methodName + " main " + Long.valueOf(end - start) + " ns";
    }

}
