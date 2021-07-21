package com.gxx.collectionuserbehaviorlibrary.sensors;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
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

import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel;
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel;
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_INSERT_APP_CLICK;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_STATISTICES_MODEL;

/**
 * Created by 王灼洲 on 2018/7/22
 */
public class SensorsDataAPI {
    private final String TAG = this.getClass().getSimpleName();
    private static SensorsDataAPI INSTANCE;
    public static final String SENSORS_DATA_API_SERVICE_MESSAGE = "serviceMessage";
    public static final String SDK_VERSION = "1.0.0";
    private Map<String, Object> mDeviceInfo = null;
    private String mDeviceId = "";
    private OnSensorsDataAPITrackClickListener onSensorsDataAPITrackClickListener;
    private MessageHandler messageHandler = null;
    private Messenger clientMessenger = null;//客户端
    //服务器通信使用
    private Messenger serviceMessenger = null;
    private Application application = null;

    public void setOnSensorsDataAPITrackClickListener(OnSensorsDataAPITrackClickListener onSensorsDataAPITrackClickListener) {
        this.onSensorsDataAPITrackClickListener = onSensorsDataAPITrackClickListener;
    }

    public interface OnSensorsDataAPITrackClickListener {
        void onSensorsDataAPITrackClick(JSONObject jsonObject);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static SensorsDataAPI init(Application application) {
        if (INSTANCE == null) {
            synchronized (SensorsDataAPI.class) {
                if (null == INSTANCE) {
                    INSTANCE = new SensorsDataAPI(application);
                }
                return INSTANCE;
            }
        }
        return INSTANCE;
    }

    public static SensorsDataAPI getInstance() {
        return INSTANCE;
    }

    private SensorsDataAPI(Application application) {
        this.application = application;
        messageHandler = new MessageHandler(application.getMainLooper());
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
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", eventName);
            jsonObject.put("device_id", mDeviceId);
            JSONObject sendProperties = new JSONObject(mDeviceInfo);

            if (properties != null) {
                SensorsDataPrivate.mergeJSONObject(properties, sendProperties);
            }

            jsonObject.put("properties", sendProperties);
            jsonObject.put("time", System.currentTimeMillis());

            if (onSensorsDataAPITrackClickListener != null) {
                onSensorsDataAPITrackClickListener.onSensorsDataAPITrackClick(jsonObject);
            }

            if (serviceMessenger != null) {
                //事件点击事件
                if (eventName.equals("$AppClick")) {
                    AppClickEventModel appClickEventModel = new AppClickEventModel();
                    appClickEventModel.setEventName(eventName);
                    SimpleDateFormat simpleCreateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String yyyyMMdd = simpleCreateTimeFormat.format(new Date());
                    Date yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd);
                    appClickEventModel.setCreateTime(yyyyMMddDate.getTime());
                    if (!TextUtils.isEmpty(mDeviceId)) {
                        appClickEventModel.setDeviceId(mDeviceId);
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String yyyyMMddHHmmss = simpleDateFormat.format(new Date());
                    Date yyyyMMddHHmmssDate = simpleDateFormat.parse(yyyyMMddHHmmss);
                    appClickEventModel.setClickTime(yyyyMMddHHmmssDate.getTime());

                    if (jsonObject.has("$activity") && !TextUtils.isEmpty("$activity")) {
                        appClickEventModel.setActivityName(jsonObject.getString("$activity"));
                    }

                    if (jsonObject.has("$element_content") && !TextUtils.isEmpty("$element_content")) {
                        appClickEventModel.setElementContent(jsonObject.getString("$element_content"));
                    }

                    if (jsonObject.has("$element_type") && !TextUtils.isEmpty("$element_type")) {
                        appClickEventModel.setElementType(jsonObject.getString("$element_type"));
                    }

                    if (jsonObject.has("$element_id") && !TextUtils.isEmpty("$element_id")) {
                        appClickEventModel.setElementId(jsonObject.getString("$element_id"));
                    }

                    StatisticesModel statisticesModel = new StatisticesModel();
                    statisticesModel.setStatisticesType(ML_STATISTICS_INSERT_APP_CLICK);
                    statisticesModel.setAppClickEventModel(appClickEventModel);
                    Message message =  Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ML_STATISTICS_STATISTICES_MODEL,statisticesModel);
                    message.setData(bundle);
                    //使用send方法发送
                    message.replyTo = clientMessenger;
                    serviceMessenger.send(message);
                }
            }


            Log.i(TAG, SensorsDataPrivate.formatJson(jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            bindService(application);
        }
    };


    private static class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
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
