package com.gxx.collectionuserbehaviorlibrary.runable;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.gxx.collectionuserbehaviorlibrary.model.OperationModel;
import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import static com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI.ML_STATISTICS_MSG_WHAT_20;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_OPERATION_STATUS_SUCCESS;
import static com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.ML_STATISTICS_OPERATION_CALLBACK;

/**
 * @date 创建时间:2021/8/10 0010
 * @auther gaoxiaoxiong
 * @Descriptiion 解析本地缓存文件
 **/
public class ParseLocalCacheFileRunnable implements Runnable {
    private String filePath;
    private WeakReference<SensorsDataAPI> sensorsDataAPIWeakReference = null;
    public static final String PARSE_LOCAL_CACHE_FILE = "parseLocalCacheFile";

    public ParseLocalCacheFileRunnable(SensorsDataAPI sensorsDataAPI, String filePath) {
        this.filePath = filePath;
        sensorsDataAPIWeakReference = new WeakReference<SensorsDataAPI>(sensorsDataAPI);
    }

    @Override
    public void run() {
        if (sensorsDataAPIWeakReference != null && sensorsDataAPIWeakReference.get() != null && !TextUtils.isEmpty(filePath) && sensorsDataAPIWeakReference.get().messageHandler != null) {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    String content = "";
                    InputStream instream = new FileInputStream(file);
                    InputStreamReader inputreader = new InputStreamReader(instream, "UTF-8");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    while ((line = buffreader.readLine()) != null) {
                        content += line;
                    }
                    inputreader.close();
                    buffreader.close();
                    instream.close();
                    if (sensorsDataAPIWeakReference != null && sensorsDataAPIWeakReference.get() != null && sensorsDataAPIWeakReference.get().messageHandler != null) {
                        Message message = Message.obtain();
                        Bundle bundle = new Bundle();
                        SensorsDataAPI.PARSE_CONTENT = content;
                        message.what = ML_STATISTICS_MSG_WHAT_20;
                        OperationModel operationModel = new OperationModel();
                        operationModel.setMlStatisticeStatus(PARSE_LOCAL_CACHE_FILE);
                        operationModel.setOperaStatus(ML_OPERATION_STATUS_SUCCESS);
                        bundle.putParcelable(ML_STATISTICS_OPERATION_CALLBACK,operationModel);
                        message.setData(bundle);
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
