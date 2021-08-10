package com.gxx.collectionuserbehaviorlibrary.sensors;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel;
import com.gxx.collectionuserbehaviorlibrary.model.CostMethodModel;
import com.gxx.collectionuserbehaviorlibrary.model.OperationModel;
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel;
import com.gxx.collectionuserbehaviorlibrary.runable.ParseLocalCacheFileRunnable;
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService;
import com.gxx.collectionuserbehaviorlibrary.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.gxx.collectionuserbehaviorlibrary.Constant.CONSTANT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.Constant.CONSTANT_APP_COST_METHOD_TIME;
import static com.gxx.collectionuserbehaviorlibrary.runable.ParseLocalCacheFileRunnable.PARSE_LOCAL_CACHE_FILE;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_OPERATION_STATUS_SUCCESS;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_DELETE_APP_CLICK_BY_TIME;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_INSERT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_INSERT_METHOD_COST_TIME;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_OPERATION_CALLBACK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_SELECT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_SELECT_APP_CLICK_BY_TIME;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_STATISTICES_JSON_MODEL;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME;

/**
 * Created by 王灼洲 on 2018/7/22
 */
public class SensorsDataAPI {
    static final String TAG = "SensorsDataAPI";
    private Gson gson = new Gson();
    private SimpleDateFormat simpleCreateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SensorsDataAPI INSTANCE;
    public static final String SENSORS_DATA_API_SERVICE_MESSAGE = "serviceMessage";
    public static final String SDK_VERSION = "1.0.0";
    public static final int ML_STATISTICS_MSG_WHAT_FROM_SERVICE_10 = 10;//从服务器来
    public static final int ML_STATISTICS_MSG_WHAT_20 = 20;//表示事件

    private Map<String, Object> mDeviceInfo = null;
    private String mDeviceId = "";

    private OnSensorsDataAPITrackCostTimeListener onSensorsDataAPITrackCostTimeListener;
    private OnSensorsDataEveryTimeAPITrackClickListener onSensorsDataAPITrackClickListener;
    private OnSensorsDataUserUniCodeListener onSensorsDataUserUniCodeListener;//用户唯一userUnicode
    private OnSensorsDataAPITrackAllClickListener onSensorsDataAPITrackAllClickListener;//所有的点击统计事件

    public MessageHandler messageHandler = null;
    private Messenger clientMessenger = null;//客户端
    //服务器通信使用
    private Messenger serviceMessenger = null;
    private Application application = null;
    private boolean isDebug = false;//是否为debug模式
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static String PARSE_CONTENT = "";//内容存放

    public void setOnSensorsDataAPITrackCostTimeListener(OnSensorsDataAPITrackCostTimeListener onSensorsDataAPITrackCostTimeListener) {
        this.onSensorsDataAPITrackCostTimeListener = onSensorsDataAPITrackCostTimeListener;
    }

    //用于获取用户设置的userUniCode 唯一ID，可为空
    public void setOnSensorsDataUserUniCodeListener(OnSensorsDataUserUniCodeListener onSensorsDataUserUniCodeListener) {
        this.onSensorsDataUserUniCodeListener = onSensorsDataUserUniCodeListener;
    }

    //用户每次点击某个按钮，都会告诉调用者，可为空
    public void setOnSensorsDataAPITrackClickListener(OnSensorsDataEveryTimeAPITrackClickListener onSensorsDataAPITrackClickListener) {
        this.onSensorsDataAPITrackClickListener = onSensorsDataAPITrackClickListener;
    }

    //获取今天之前所有的点击事件，可为空
    public void setOnSensorsDataAPITrackAllClickListener(OnSensorsDataAPITrackAllClickListener onSensorsDataAPITrackAllClickListener) {
        this.onSensorsDataAPITrackAllClickListener = onSensorsDataAPITrackAllClickListener;
    }

    public static class Builder {
        private Application application;
        private Boolean isDebug;

        public Builder setApplication(Application application) {
            this.application = application;
            return this;
        }

        public Builder setDebug(Boolean debug) {
            isDebug = debug;
            return this;
        }

        public SensorsDataAPI build() {
            return new SensorsDataAPI(this);
        }
    }

    public SensorsDataAPI() {
        super();
    }

    private SensorsDataAPI(Builder builder) {
        if (INSTANCE == null) {
            synchronized (SensorsDataAPI.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SensorsDataAPI();
                    INSTANCE.init(builder.application, builder.isDebug);
                }
            }
        }
    }

    /**
     * @param isDebug 如果为true，会打印每次点击的log日志
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion
     **/
    private void init(Application application, boolean isDebug) {
        this.application = application;
        this.isDebug = isDebug;
        messageHandler = new MessageHandler(this, application.getMainLooper());
        clientMessenger = new Messenger(messageHandler);
        mDeviceId = SensorsDataPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = SensorsDataPrivate.getDeviceInfo(application.getApplicationContext());
        bindService(application);
    }

    /**
     * Track 事件
     *
     * @param eventName  String 事件名称
     * @param properties JSONObject 事件属性
     */
    public void track(@NonNull final String eventName, @Nullable JSONObject properties) {
        try {
            if (serviceMessenger != null && properties != null) {
                //事件点击事件
                if (eventName.equals(CONSTANT_APP_CLICK)) {
                    AppClickEventModel appClickEventModel = new AppClickEventModel();
                    String yyyyMMdd = simpleCreateTimeFormat.format(new Date());
                    Date yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd);

                    appClickEventModel.setEventName(eventName);
                    appClickEventModel.setCreateTime(yyyyMMddDate.getTime());
                    appClickEventModel.setClickTime(System.currentTimeMillis());

                    if (!TextUtils.isEmpty(mDeviceId)) {
                        appClickEventModel.setDeviceId(mDeviceId);
                    }

                    if (properties.has("$uiClassName") && !TextUtils.isEmpty(properties.getString("$uiClassName"))) {
                        appClickEventModel.setUiClassName(properties.getString("$uiClassName"));
                    }

                    if (properties.has("$element_content") && !TextUtils.isEmpty(properties.getString("$element_content"))) {
                        appClickEventModel.setElementContent(properties.getString("$element_content"));
                    }

                    if (properties.has("$element_type") && !TextUtils.isEmpty(properties.getString("$element_type"))) {
                        appClickEventModel.setElementType(properties.getString("$element_type"));
                    }

                    if (properties.has("$element_id") && !TextUtils.isEmpty(properties.getString("$element_id"))) {
                        appClickEventModel.setElementId(properties.getString("$element_id"));
                    }

                    if (properties.has("$viewTag") && !TextUtils.isEmpty(properties.getString("$viewTag"))) {
                        appClickEventModel.setExtrans(properties.getString("$viewTag"));
                    }

                    if (onSensorsDataUserUniCodeListener != null && !TextUtils.isEmpty(onSensorsDataUserUniCodeListener.onUserUniCode())) {
                        appClickEventModel.setUserUniCode(onSensorsDataUserUniCodeListener.onUserUniCode());
                    }

                    StatisticesModel statisticesModel = new StatisticesModel();
                    statisticesModel.setStatisticesType(ML_STATISTICS_INSERT_APP_CLICK);
                    statisticesModel.setJsonString(gson.toJson(appClickEventModel));
                    Message message = Message.obtain();
                    message.what = ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
                    Bundle bundle = new Bundle();
                    bundle.putString(ML_STATISTICS_STATISTICES_JSON_MODEL, gson.toJson(statisticesModel));
                    message.setData(bundle);
                    //使用send方法发送
                    message.replyTo = clientMessenger;
                    serviceMessenger.send(message);
                }
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            if (!TextUtils.isEmpty(mDeviceId)) {
                jsonObject.put("device_id", mDeviceId);
            }

            if (mDeviceInfo != null) {
                JSONObject sendProperties = new JSONObject(mDeviceInfo);
                if (properties != null) {
                    SensorsDataPrivate.mergeJSONObject(properties, sendProperties);
                }
                jsonObject.put("properties", sendProperties);
            }

            if (onSensorsDataAPITrackClickListener != null) {
                onSensorsDataAPITrackClickListener.onSensorsDataEveryTimeAPITrackClick(jsonObject);
            }

            if (isDebug) {
                Log.i(TAG, SensorsDataPrivate.formatJson(jsonObject.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
        }

        //服务端崩溃或被杀死的时候被调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            if (application != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1 * 1000);
                            if (application != null) {
                                bindService(application); //1秒后再次创建service
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    };


    public static class MessageHandler extends Handler {
        private WeakReference<SensorsDataAPI> sensorsDataAPIWeakReference = null;

        public MessageHandler(SensorsDataAPI sensorsDataAPI, Looper looper) {
            super(looper);
            sensorsDataAPIWeakReference = new WeakReference<SensorsDataAPI>(sensorsDataAPI);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (sensorsDataAPIWeakReference == null || sensorsDataAPIWeakReference.get() == null) {
                return;
            }
            if (msg.what == ML_STATISTICS_MSG_WHAT_FROM_SERVICE_10) {//从服务器传递来消息
                if (msg.getData() != null && msg.getData().getString(ML_STATISTICS_OPERATION_CALLBACK) != null) {
                    OperationModel operationModel = sensorsDataAPIWeakReference.get().gson.fromJson(msg.getData().getString(ML_STATISTICS_OPERATION_CALLBACK), OperationModel.class);
                    if (operationModel.getOperaStatus() == ML_OPERATION_STATUS_SUCCESS) {
                        //查询，事件
                        if (operationModel.getMlStatisticeStatus().equals(ML_STATISTICS_SELECT_APP_CLICK) || operationModel.getMlStatisticeStatus().equals(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)) {
                            String filePath = operationModel.getFilePath();
                            sensorsDataAPIWeakReference.get().singleThreadExecutor.execute(new ParseLocalCacheFileRunnable(sensorsDataAPIWeakReference.get(), filePath));
                        }else if (operationModel.getMlStatisticeStatus().equals(ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME) || operationModel.getMlStatisticeStatus().equals(ML_STATISTICS_DELETE_APP_CLICK_BY_TIME)){
                            if (sensorsDataAPIWeakReference.get().isDebug){
                                Log.i(TAG,"操作成功");
                            }
                        }
                    }
                }
            } else if (msg.what == ML_STATISTICS_MSG_WHAT_20) {//通知结果
                if (msg.getData() != null) {
                    OperationModel operationModel = msg.getData().getParcelable(ML_STATISTICS_OPERATION_CALLBACK);
                    if (operationModel.getMlStatisticeStatus().equals(PARSE_LOCAL_CACHE_FILE) && PARSE_CONTENT != null) {//文件解析
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("event", CONSTANT_APP_CLICK);
                            if (sensorsDataAPIWeakReference.get().mDeviceInfo != null) {
                                JSONObject deviceInfoJsonObject = new JSONObject(sensorsDataAPIWeakReference.get().mDeviceInfo);
                                jsonObject.put("deviceInfo", deviceInfoJsonObject);
                            }
                            JSONArray jsonArray = new JSONArray(PARSE_CONTENT); //将String转换成JsonArray对象
                            jsonObject.put("list", jsonArray);
                            if (sensorsDataAPIWeakReference != null && sensorsDataAPIWeakReference.get() != null && sensorsDataAPIWeakReference.get().onSensorsDataAPITrackAllClickListener != null) {
                                sensorsDataAPIWeakReference.get().onSensorsDataAPITrackAllClickListener.onSensorsDataAPITrackAllClick(jsonObject);
                            }
                            PARSE_CONTENT = null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    /**
     * @date 创建时间:2021/7/21 0021
     * @auther gaoxiaoxiong
     * @Descriptiion 绑定
     **/
    private void bindService(Application application) {
        Intent intent = new Intent(application, MlStatisticsService.class);
        application.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    /**
     * @param dayTime             格式必须为 yyyy-MM-dd 的long类型
     * @param isContainSelectTime 是否包含查询的时间 如果是true 获取历史的点击 = dayTime 都获取，如果是false  < dayTime 都获取
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion
     **/
    public void getHistoryAppClickData(long dayTime, boolean isContainSelectTime) {
        StatisticesModel statisticesMode = new StatisticesModel();
        statisticesMode.setDayTime(dayTime);
        if (!isContainSelectTime) {
            statisticesMode.setStatisticesType(ML_STATISTICS_SELECT_APP_CLICK);
        } else {
            statisticesMode.setStatisticesType(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME);
        }
        if (clientMessenger != null && serviceMessenger != null) {
            try {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(ML_STATISTICS_STATISTICES_JSON_MODEL, gson.toJson(statisticesMode));
                message.what = ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
                message.setData(bundle);
                message.replyTo = clientMessenger;
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param dayTime yyyy-MM-dd 的long类型的时间
     * @date 创建时间:2021/8/9 0009
     * @auther gaoxiaoxiong
     * @Descriptiion 更新点击，是包含当前的时间的，
     **/
    public void updateHistoryAppClickDataByTime(long dayTime) {
        StatisticesModel statisticesMode = new StatisticesModel();
        statisticesMode.setDayTime(dayTime);
        statisticesMode.setStatisticesType(ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME);
        if (clientMessenger != null && serviceMessenger != null) {
            try {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(ML_STATISTICS_STATISTICES_JSON_MODEL, gson.toJson(statisticesMode));
                message.what = ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
                message.setData(bundle);
                message.replyTo = clientMessenger;
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @date 创建时间:2021/8/10 0010
     * @auther gaoxiaoxiong
     * @Descriptiion 删除历史数据，不包含今天的日期
     **/
    public void deleteHistoryAppClickDataNotIncludingToday(){
        try {
            StatisticesModel statisticesMode = new StatisticesModel();
            String yyyyMMdd = simpleCreateTimeFormat.format(new Date());
            Date yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd);
            statisticesMode.setDayTime(yyyyMMddDate.getTime());
            statisticesMode.setStatisticesType(ML_STATISTICS_DELETE_APP_CLICK_BY_TIME);
            if (clientMessenger != null && serviceMessenger != null) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(ML_STATISTICS_STATISTICES_JSON_MODEL, gson.toJson(statisticesMode));
                message.what = ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
                message.setData(bundle);
                message.replyTo = clientMessenger;
                serviceMessenger.send(message);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
        }
    }

    /**
     * @param fileName 文件名称，必须为  yyyy-MM-dd.txt的格式时间，否者会直接异常
     * @date 创建时间:2021/7/27 0027
     * @auther gaoxiaoxiong
     * @Descriptiion 通过文件名称获取历史记录信息
     **/
    public void getHistoryAppClickDataByFileName(String fileName) {
        FileUtils fileUtils = new FileUtils();
        File file = new File(fileUtils.getSandboxPublickDiskCacheDir(application) + "/" + fileName);
        if (singleThreadExecutor != null && file.exists()) {
            singleThreadExecutor.execute(new ParseLocalCacheFileRunnable(this, file.getAbsolutePath()));
        }
    }


    /**
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 每一次的点击事件
     **/
    public interface OnSensorsDataEveryTimeAPITrackClickListener {
        void onSensorsDataEveryTimeAPITrackClick(JSONObject jsonObject);
    }

    public interface OnSensorsDataUserUniCodeListener {
        String onUserUniCode();
    }

    /**
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 所有的点击统计事件
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
     * @date 创建时间:2021/8/5 0005
     * @auther gaoxiaoxiong
     * @Descriptiion 耗时统计
     **/
    public void trackCostTime(long startTime, long endTime, String className, String methodName) {
        try {
            String yyyyMMdd = simpleCreateTimeFormat.format(new Date());
            Date yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd);
            StatisticesModel statisticesModel = new StatisticesModel();
            statisticesModel.setStatisticesType(ML_STATISTICS_INSERT_METHOD_COST_TIME);
            CostMethodModel costMethodModel = new CostMethodModel();
            costMethodModel.setEventName(CONSTANT_APP_COST_METHOD_TIME);
            costMethodModel.setCreateTime(yyyyMMddDate.getTime());
            if (!TextUtils.isEmpty(mDeviceId)) {
                costMethodModel.setDeviceId(mDeviceId);
            }
            if (onSensorsDataUserUniCodeListener != null && !TextUtils.isEmpty(onSensorsDataUserUniCodeListener.onUserUniCode())) {
                costMethodModel.setUserUniCode(onSensorsDataUserUniCodeListener.onUserUniCode());
            }
            costMethodModel.setStartTime(startTime);
            costMethodModel.setEndTime(endTime);
            costMethodModel.setUiClassName(className);
            costMethodModel.setMethodName(methodName);
            statisticesModel.setJsonString(gson.toJson(costMethodModel));
            Message message = Message.obtain();
            message.what = ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
            Bundle bundle = new Bundle();
            bundle.putString(ML_STATISTICS_STATISTICES_JSON_MODEL, gson.toJson(statisticesModel));
            message.setData(bundle);
            //使用send方法发送
            message.replyTo = clientMessenger;
            serviceMessenger.send(message);
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (onSensorsDataAPITrackCostTimeListener != null) {
            onSensorsDataAPITrackCostTimeListener.onSensorsDataAPITrackCostTime(startTime, endTime, className, methodName);
        }
        if (isDebug) {
            Log.i(TAG, "className = " + className);
            Log.i(TAG, "methodName = " + methodName);
            Log.i(TAG, "startTime = " + startTime);
            Log.i(TAG, "endTime = " + endTime);
            Log.i(TAG, "超时啦");
        }
    }


    /**
     * @date 创建时间:2021/7/21 0021
     * @auther gaoxiaoxiong
     * @Descriptiion 销毁
     **/
    public void onDestory() {
        if (application != null && serviceConnection != null) {
            application.unbindService(serviceConnection);
        }
    }


    public static SensorsDataAPI getInstance() {
        return INSTANCE;
    }


}
