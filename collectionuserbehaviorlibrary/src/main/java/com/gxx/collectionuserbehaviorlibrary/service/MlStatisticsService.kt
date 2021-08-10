package com.gxx.collectionuserbehaviorlibrary.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.text.TextUtils
import com.google.gson.Gson
import com.gxx.collectionuserbehaviorlibrary.Constant.Companion.CONSTANT_APP_CLICK
import com.gxx.collectionuserbehaviorlibrary.Constant.Companion.CONSTANT_FUNCTION_TYPE_00
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel
import com.gxx.collectionuserbehaviorlibrary.model.CostMethodModel
import com.gxx.collectionuserbehaviorlibrary.model.OperationModel
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel
import com.gxx.collectionuserbehaviorlibrary.runable.EnventTableRunnable
import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper.Companion.TABLE_ML_EVENT_TABLE
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper.Companion.TABLE_ML_TIME_TABLE
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

/**
 * @date 创建时间:2021/7/21 0021
 * @auther gaoxiaoxiong
 * @Descriptiion 统计service
 **/
class MlStatisticsService : Service() {
    var mlSqLiteOpenHelper: MlSqLiteOpenHelper? = null;
    val messengerHandler = MessengerHandler(this, Looper.getMainLooper());
    val messenger = Messenger(messengerHandler);
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    companion object {
        const val TAG = "MlStatisticsService";
        const val ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100 = 100;//从客户端传来消息
        const val ML_STATISTICS_MSG_WHAT_101 = 101;//处理将信息存储到file文件里面，然后要通知前端已经完成

        const val ML_OPERATION_STATUS_SUCCESS = 1;
        const val ML_OPERATION_STATUS_FAIL = 0;

        const val ML_STATISTICS_STATISTICES_JSON_MODEL = "StatisticesJsonModel"

        const val ML_STATISTICS_INSERT_APP_CLICK = "InsertAppClick";//点击行为
        const val ML_STATISTICS_INSERT_METHOD_COST_TIME = "InsertMethodCostTime";//方法耗时行为
        const val ML_STATISTICS_APP_PAGE = "AppPage";//页面统计
        const val ML_STATISTICS_SELECT_APP_CLICK = "selectAppClick";//查询点击的行为
        const val ML_STATISTICS_SELECT_APP_CLICK_BY_TIME = "selectAppClickByTime";//查询某个时间点的时间
        const val ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME = "updateAppClickByTime";//更新数据，根据某个时间点的时间
        const val ML_STATISTICS_DELETE_APP_CLICK_BY_TIME = "deleteAppClickByTime";//删除数据，根据某个时间点的时间

        const val ML_STATISTICS_OPERATION_CALLBACK = "operationCallBack";//操作反馈
    }


    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder;
    }


    class MessengerHandler : Handler {
        val gson = Gson();
        var mlStatisticsServiceWeakReference: WeakReference<MlStatisticsService>? = null;
        var replyToClient: Message? = null;

        constructor(mlStatisticsService: MlStatisticsService, looper: Looper) : super(looper) {
            if (mlStatisticsServiceWeakReference == null) {
                mlStatisticsServiceWeakReference = WeakReference<MlStatisticsService>(
                        mlStatisticsService
                );
            }
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == ML_STATISTICS_MSG_WHAT_FROM_CLIENT_100) {
                if (replyToClient == null) {
                    replyToClient = Message.obtain(msg);
                }
                val bundle = msg.data;
                val statisticesModelJsonString = bundle.getString(
                        ML_STATISTICS_STATISTICES_JSON_MODEL,
                        ""
                );

                if (mlStatisticsServiceWeakReference == null || mlStatisticsServiceWeakReference!!.get() == null || TextUtils.isEmpty(statisticesModelJsonString)) {
                    return
                }

                if (mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper == null) {
                    mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper = MlSqLiteOpenHelper(mlStatisticsServiceWeakReference!!.get()!!.applicationContext);
                }

                val statisticesModel = gson.fromJson<StatisticesModel>(
                        statisticesModelJsonString,
                        StatisticesModel::class.java
                )

                //存储数据到本地数据库
                if (statisticesModel.statisticesType.equals(ML_STATISTICS_INSERT_APP_CLICK)) {
                    val appClickEventModel = gson.fromJson<AppClickEventModel>(
                            statisticesModel.jsonString,
                            AppClickEventModel::class.java
                    );
                    if (appClickEventModel != null) {
                        //存储数据到本地
                        val contentValues = ContentValues();
                        contentValues.put("eventName", appClickEventModel.eventName ?: "")
                        contentValues.put("deviceId", appClickEventModel.deviceId ?: "")
                        contentValues.put("userUniCode", appClickEventModel.userUniCode ?: "")
                        contentValues.put("uiClassName", appClickEventModel.uiClassName ?: "")
                        contentValues.put("elementContent", appClickEventModel.elementContent ?: "")
                        contentValues.put("elementType", appClickEventModel.elementType ?: "")
                        contentValues.put("elementId", appClickEventModel.elementId ?: "")
                        contentValues.put("clickTime", appClickEventModel.clickTime)
                        contentValues.put("createTime", appClickEventModel.createTime)
                        contentValues.put("extrans", appClickEventModel.extrans ?: "")
                        if (TextUtils.isEmpty(appClickEventModel.functionType)){
                            contentValues.put("functionType", CONSTANT_FUNCTION_TYPE_00)
                        }else{
                            contentValues.put("functionType", appClickEventModel.functionType!!)
                        }
                        contentValues.put("isVisit", 1)
                        mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper!!.insert(TABLE_ML_EVENT_TABLE, contentValues)
                    }
                } else if (statisticesModel.statisticesType.equals(ML_STATISTICS_SELECT_APP_CLICK)) {//开启线程去查询，然后将结果存储到本地 cache目录，通知前端自行去获取 && 解析数据出来
                    mlStatisticsServiceWeakReference!!.get()!!.singleThreadExecutor.execute(
                            EnventTableRunnable(mlStatisticsServiceWeakReference!!.get()!!, statisticesModel.dayTime, CONSTANT_APP_CLICK, ML_STATISTICS_SELECT_APP_CLICK)
                    )
                } else if (statisticesModel.statisticesType.equals(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)) {
                    mlStatisticsServiceWeakReference!!.get()!!.singleThreadExecutor.execute(
                            EnventTableRunnable(mlStatisticsServiceWeakReference!!.get()!!, statisticesModel.dayTime, CONSTANT_APP_CLICK, ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)
                    )
                } else if (statisticesModel.statisticesType.equals(ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME)) {//更新点击，更新过后，下次将不会再次查询出来
                    mlStatisticsServiceWeakReference!!.get()!!.singleThreadExecutor.execute(
                            EnventTableRunnable(mlStatisticsServiceWeakReference!!.get()!!, statisticesModel.dayTime, CONSTANT_APP_CLICK, ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME)
                    )
                }else if (statisticesModel.statisticesType.equals(ML_STATISTICS_DELETE_APP_CLICK_BY_TIME)){//删除点击事件，不包含今天的时间
                    mlStatisticsServiceWeakReference!!.get()!!.singleThreadExecutor.execute(
                            EnventTableRunnable(mlStatisticsServiceWeakReference!!.get()!!, statisticesModel.dayTime, CONSTANT_APP_CLICK, ML_STATISTICS_DELETE_APP_CLICK_BY_TIME)
                    )
                }
                else if (statisticesModel.statisticesType.equals(ML_STATISTICS_INSERT_METHOD_COST_TIME)) {//耗时方法统计
                    val costMethodModel = gson.fromJson<CostMethodModel>(statisticesModel.jsonString, CostMethodModel::class.java);
                    if (costMethodModel != null) {
                        //存储数据到本地
                        val contentValues = ContentValues();
                        contentValues.put("eventName", costMethodModel.eventName ?: "")
                        contentValues.put("deviceId", costMethodModel.deviceId ?: "")
                        contentValues.put("userUniCode", costMethodModel.userUniCode ?: "")
                        contentValues.put("uiClassName", costMethodModel.uiClassName ?: "")
                        contentValues.put("methodName", costMethodModel.methodName ?: "")
                        contentValues.put("createTime", costMethodModel.createTime)
                        contentValues.put("SysTemStartTime", costMethodModel.startTime)
                        contentValues.put("SysTemEndTime", costMethodModel.endTime)
                        contentValues.put("isVisit", 1)
                        mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper!!.insert(
                                TABLE_ML_TIME_TABLE,
                                contentValues
                        )
                    }
                }
            } else if (msg.what == ML_STATISTICS_MSG_WHAT_101) {//服务处理
                if (replyToClient == null) {
                    return
                }

                if (msg.data!=null){
                    val message = Message.obtain();
                    val operationModel  = msg.data.getParcelable<OperationModel>(ML_STATISTICS_OPERATION_CALLBACK);
                    val bundler = Bundle();
                    bundler.putString(ML_STATISTICS_OPERATION_CALLBACK,gson.toJson(operationModel))
                    message.data = bundler
                    message.what = SensorsDataAPI.ML_STATISTICS_MSG_WHAT_FROM_SERVICE_10
                    replyToClient!!.replyTo.send(message);
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mlSqLiteOpenHelper != null) {
            mlSqLiteOpenHelper!!.clear()
        }

        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdown()
        }
    }

}