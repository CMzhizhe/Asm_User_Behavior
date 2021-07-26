package com.gxx.collectionuserbehaviorlibrary.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper.Companion.TABLE_ML_EVENT_TABLE
import java.lang.ref.WeakReference

/**
 * @date 创建时间:2021/7/21 0021
 * @auther gaoxiaoxiong
 * @Descriptiion 统计service
 **/
class MlStatisticsService : Service() {
    var mlSqLiteOpenHelper: MlSqLiteOpenHelper? = null;
    val messengerHandler = MessengerHandler(this, Looper.getMainLooper());
    val messenger = Messenger(messengerHandler);

    companion object {
        const val TAG = "MlStatisticsService";

        const val ML_STATISTICS_STATISTICES_JSON_MODEL = "StatisticesJsonModel"

        const val ML_STATISTICS_INSERT_APP_CLICK = "InsertAppClick";//点击行为
        const val ML_STATISTICS_APP_PAGE = "AppPage";//页面统计
    }


    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder;
    }


    class MessengerHandler : Handler {
        val gson = Gson();
        var mlStatisticsServiceWeakReference: WeakReference<MlStatisticsService>? = null;

        constructor(mlStatisticsService: MlStatisticsService, looper: Looper) : super(looper) {
            if (mlStatisticsServiceWeakReference == null) {
                mlStatisticsServiceWeakReference = WeakReference<MlStatisticsService>(mlStatisticsService);
            }
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val bundle = msg.data;
            val statisticesModelJsonString =  bundle.getString(ML_STATISTICS_STATISTICES_JSON_MODEL,"");
            if (!TextUtils.isEmpty(statisticesModelJsonString) && mlStatisticsServiceWeakReference != null && mlStatisticsServiceWeakReference!!.get() != null) {
                if (mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper == null) {
                    mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper = MlSqLiteOpenHelper(mlStatisticsServiceWeakReference!!.get()!!.applicationContext);
                }
                val statisticesModel = gson.fromJson<StatisticesModel>(statisticesModelJsonString,StatisticesModel::class.java)
                //存储数据到本地数据库
                if (statisticesModel.statisticesType.equals(ML_STATISTICS_INSERT_APP_CLICK)) {
                    val appClickEventModel = statisticesModel.appClickEventModel;
                    if (appClickEventModel != null) {
                        //存储数据到本地
                        val contentValues = ContentValues();
                        contentValues.put("eventName", appClickEventModel.eventName)
                        contentValues.put("deviceId", appClickEventModel.deviceId)
                        contentValues.put("userUniCode", appClickEventModel.userUniCode)
                        contentValues.put("activityName", appClickEventModel.activityName)
                        contentValues.put("fragmentName", appClickEventModel.fragmentName)
                        contentValues.put("elementContent", appClickEventModel.elementContent)
                        contentValues.put("elementType", appClickEventModel.elementType)
                        contentValues.put("elementId", appClickEventModel.elementId)
                        contentValues.put("clickTime", appClickEventModel.clickTime)
                        contentValues.put("createTime", appClickEventModel.createTime)
                        contentValues.put("extrans", appClickEventModel.extrans)
                        mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper!!.insert(TABLE_ML_EVENT_TABLE, contentValues)

                        Log.e(TAG,"结果 = " + appClickEventModel.toString())
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mlSqLiteOpenHelper != null) {
            mlSqLiteOpenHelper!!.clear()
        }
    }

}