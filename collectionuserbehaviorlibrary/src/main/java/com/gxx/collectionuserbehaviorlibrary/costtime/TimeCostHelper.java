package com.gxx.collectionuserbehaviorlibrary.costtime;

import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI;

public class TimeCostHelper {
    /**
     * @param startTime  开始微妙值
     * @param endTime    结束微妙值
     * @date 创建时间:2021/8/2 0002
     * @auther gaoxiaoxiong
     * @Descriptiion
     **/
    public static void trackTime(long startTime, long endTime,String className,String methodName) {
        if (endTime - startTime > 1000){
           /* Log.i("TimeCostHelper","className = " + className);
            Log.i("TimeCostHelper","methodName = " + methodName);
            Log.i("TimeCostHelper","startTime = " + startTime);
            Log.i("TimeCostHelper","endTime = " + endTime);
            Log.i("TimeCostHelper","超时啦");*/
            SensorsDataAPI.getInstance().trackCostTime(startTime,endTime,className,methodName);
        }
    }
}
