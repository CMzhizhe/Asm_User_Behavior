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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel;
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel;
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_APP_CLICK_FILE_PATH;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_INSERT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_SELECT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_STATISTICES_JSON_MODEL;

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
    public static final int ML_STATISTICS_MSG_WHAT_20 = 20;

    private Map<String, Object> mDeviceInfo = null;
    private String mDeviceId = "";
    private OnSensorsDataEveryTimeAPITrackClickListener onSensorsDataAPITrackClickListener;
    private OnSensorsDataUserUniCodeListener onSensorsDataUserUniCodeListener;//用户唯一userUnicode
    private OnSensorsDataAPITrackAllClickListener onSensorsDataAPITrackAllClickListener;//所有的点击统计事件
    private MessageHandler messageHandler = null;
    private Messenger clientMessenger = null;//客户端
    //服务器通信使用
    private Messenger serviceMessenger = null;
    private Application application = null;
    private boolean isDebug = false;//是否为debug模式
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

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

    @SuppressWarnings("UnusedReturnValue")
    public static SensorsDataAPI init() {
        if (INSTANCE == null) {
            synchronized (SensorsDataAPI.class) {
                if (null == INSTANCE) {
                    INSTANCE = new SensorsDataAPI();
                }
                return INSTANCE;
            }
        }
        return INSTANCE;
    }

    public static SensorsDataAPI getInstance() {
        return INSTANCE;
    }

    private SensorsDataAPI() {
        super();
    }

    /**
     * @param isDebug 如果为true，会打印每次点击的log日志
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion
     **/
    public void SensorsDataAPI(Application application, boolean isDebug) {
        this.application = application;
        this.isDebug = isDebug;
        messageHandler = new MessageHandler(this,application.getMainLooper());
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
                if (eventName.equals("$AppClick")) {
                    AppClickEventModel appClickEventModel = new AppClickEventModel();
                    String yyyyMMdd = simpleCreateTimeFormat.format(new Date());
                    Date yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd);

                    appClickEventModel.setEventName(eventName);
                    appClickEventModel.setCreateTime(yyyyMMddDate.getTime());
                    appClickEventModel.setClickTime(System.currentTimeMillis());

                    if (!TextUtils.isEmpty(mDeviceId)) {
                        appClickEventModel.setDeviceId(mDeviceId);
                    }

                    if (properties.has("$activity") && !TextUtils.isEmpty(properties.getString("$activity"))) {
                        appClickEventModel.setActivityName(properties.getString("$activity"));
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

                    if (onSensorsDataUserUniCodeListener != null && !TextUtils.isEmpty(onSensorsDataUserUniCodeListener.onUserUniCode())) {
                        appClickEventModel.setUserUniCode(onSensorsDataUserUniCodeListener.onUserUniCode());
                    }

                    StatisticesModel statisticesModel = new StatisticesModel();
                    statisticesModel.setStatisticesType(ML_STATISTICS_INSERT_APP_CLICK);
                    statisticesModel.setAppClickEventModel(appClickEventModel);
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


    private static class MessageHandler extends Handler {
        private WeakReference<SensorsDataAPI> sensorsDataAPIWeakReference = null;

        public MessageHandler(SensorsDataAPI sensorsDataAPI,Looper looper) {
            super(looper);
            sensorsDataAPIWeakReference = new WeakReference<SensorsDataAPI>(sensorsDataAPI);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (sensorsDataAPIWeakReference == null || sensorsDataAPIWeakReference.get() == null){
                return;
            }
            if (msg.what == ML_STATISTICS_MSG_WHAT_FROM_SERVICE_10){//从服务器传递来消息
                //解析文件，将最终结果转为一个json
                String filePath = msg.getData().getString(ML_STATISTICS_APP_CLICK_FILE_PATH);
                sensorsDataAPIWeakReference.get().singleThreadExecutor.execute(new ParseLocalCacheFile(sensorsDataAPIWeakReference.get(),filePath));
            }else if (msg.what == ML_STATISTICS_MSG_WHAT_20){//通知结果
                String jsonArrayString = msg.obj.toString();
                try {
                    JSONArray jsonArray = new JSONArray(jsonArrayString); //将String转换成JsonArray对象
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("event", "$AppClick");
                    if (!TextUtils.isEmpty(sensorsDataAPIWeakReference.get().mDeviceId)) {
                        jsonObject.put("device_id", sensorsDataAPIWeakReference.get().mDeviceId);
                    }
                    jsonObject.put("list",jsonArray);
                    if (sensorsDataAPIWeakReference!=null &&  sensorsDataAPIWeakReference.get()!=null && sensorsDataAPIWeakReference.get().onSensorsDataAPITrackAllClickListener!=null){
                        sensorsDataAPIWeakReference.get().onSensorsDataAPITrackAllClickListener.onSensorsDataAPITrackAllClick(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 解析本地缓存文件
     * https://www.jianshu.com/p/fdf6178d1a66
     **/
    private static class ParseLocalCacheFile implements Runnable{
        private String filePath;
        private WeakReference<SensorsDataAPI> sensorsDataAPIWeakReference = null;

        public ParseLocalCacheFile(SensorsDataAPI sensorsDataAPI,String filePath){
            this.filePath = filePath;
            sensorsDataAPIWeakReference = new WeakReference<SensorsDataAPI>(sensorsDataAPI);
        }
        @Override
        public void run() {
            if (sensorsDataAPIWeakReference!=null && sensorsDataAPIWeakReference.get()!=null && !TextUtils.isEmpty(filePath)){
                File file = new File(filePath);
                if (file.exists()){
                    try {
                        String content = "";
                        InputStream instream = new FileInputStream(file);
                        if (instream != null) {
                            InputStreamReader inputreader = new InputStreamReader(instream, "UTF-8");
                            BufferedReader buffreader = new BufferedReader(inputreader);
                            String line = "";
                            //分行读取
                            while ((line = buffreader.readLine()) != null) {
                                content += line;
                            }
                            instream.close();//关闭输入流
                        }
                        if (sensorsDataAPIWeakReference.get().messageHandler!=null){
                            Message message =  Message.obtain();
                            message.what = ML_STATISTICS_MSG_WHAT_20;
                            message.obj =content;
                            sensorsDataAPIWeakReference.get().messageHandler.sendMessage(message);
                        }
                    } catch (java.io.FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param dayTime             格式必须为 yyyy-MM-dd 的long类型
     * @param isNeedDeleteHistory 是否需要删除 < dayTime 的时间
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 获取历史的点击 < dayTime 都获取
     **/
    public void getHistoryClickData(long dayTime, boolean isNeedDeleteHistory) {
        StatisticesModel statisticesMode = new StatisticesModel();
        statisticesMode.setDayTime(dayTime);
        statisticesMode.setStatisticesType(ML_STATISTICS_SELECT_APP_CLICK);
        statisticesMode.setNeedDeleteHistory(isNeedDeleteHistory);
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
    public interface OnSensorsDataAPITrackAllClickListener{
        void onSensorsDataAPITrackAllClick(JSONObject jsonObject);
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


}
