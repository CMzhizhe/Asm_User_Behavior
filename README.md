# Asm_User_Behavior
ASM 方式收集用户行为，比如点击事件

### 注意，目前还没有上传到gradle,所以demo是本地插件

数据的存储采用了多进程，不用担心卡顿问题

### 使用教程
app.gradle
```
android{
...
}
sensorsAnalytics {
    disablePlugin = false//不禁用插件
    disableAppClick = false //不禁用点击统计
    disableCostTime = false //不禁用耗时统计
    containsString = "com/gxx/android_asm_1_project,com/chad/library/adapter/base"//想扫描的包路径名
}
```
初始化
```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (getProcessName(this).endsWith("com.gxx.android_asm_1_project")){//主进程进行初始化
            new SensorsDataAPI.Builder().setApplication(this).setDebug(true).build();
        }
    }
}

```
提供的接口
```
   /**
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 获取所有的点击统计事件
     **/
    public interface OnSensorsDataAPITrackAllClickListener {
        void onSensorsDataAPITrackAllClick(JSONObject jsonObject);
    }
    
   /**
     * @date 创建时间:2021/8/5 0005
     * @auther gaoxiaoxiong
     * @Descriptiion 耗时回调
     **/
    public interface OnSensorsDataAPITrackCostTimeListener {
        void onSensorsDataAPITrackCostTime(long startTime, long endTime, String className, String methodName);
    }
    
   /**
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 每一次的点击事件
     **/
    public interface OnSensorsDataEveryTimeAPITrackClickListener {
        void onSensorsDataEveryTimeAPITrackClick(JSONObject jsonObject);
    }
    
   /**
     * @date 创建时间: 2021/8/16
     * @auther gaoxiaoxiong
     * @description 可以往数据库插入用户唯一ID，用于识别
     **/
    public interface OnSensorsDataUserUniCodeListener {
        String onUserUniCode();
    }

```
提供的方法
```
   /**
     * @param dayTime             格式必须为 yyyy-MM-dd 的long类型
     * @param isContainSelectTime 是否包含查询的时间 如果是true 获取历史的点击 = dayTime 都获取，如果是false  < dayTime 都获取
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 获取今天的时间
     **/
    public void getHistoryAppClickData(long dayTime, boolean isContainSelectTime) {}
    
    
  /**
     * @param fileName 文件名称，必须为   yyyy-MM-dd的long 类型的.txt的格式时间，否者会直接异常
     * @date 创建时间:2021/7/27 0027
     * @auther gaoxiaoxiong
     * @Descriptiion 通过文件名称获取历史记录信息
     **/
    public void getHistoryAppClickDataByFileName(String fileName) {}
    
    
   /**
     * @date 创建时间:2021/8/10 0010
     * @auther gaoxiaoxiong
     * @Descriptiion 删除历史数据，不包含今天的日期
     **/
    public void deleteHistoryAppClickDataNotIncludingToday(){}
    
    
    /**
     * @param dayTime yyyy-MM-dd 的long类型的时间
     * @date 创建时间:2021/8/9 0009
     * @auther gaoxiaoxiong
     * @Descriptiion 更新点击，是包含当前的时间的，作用是以后查询数据，不会再次查询出这个时间点的
     **/
    public void updateHistoryAppClickDataByTime(long dayTime) {}
    
     /**
     * @date 创建时间: 2021/8/16
     * @auther gaoxiaoxiong
     * @description 耗时标注
     **/
    public fun readNetWork(){
        Thread(object :Runnable{
            @CostTime //耗时标注
            override fun run() {
                 Thread.sleep(5 * 1000)
            }
        }).start()
    }
```






