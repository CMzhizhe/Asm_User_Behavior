package com.gxx.collectionuserbehaviorlibrary.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.text.TextUtils
import com.google.gson.Gson
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel
import com.gxx.collectionuserbehaviorlibrary.model.StatisticesModel
import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper.Companion.TABLE_ML_EVENT_TABLE
import com.gxx.collectionuserbehaviorlibrary.utils.FileUtils
import java.io.File
import java.io.RandomAccessFile
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
        const val ML_STATISTICS_STATISTICES_JSON_MODEL = "StatisticesJsonModel"

        const val ML_STATISTICS_INSERT_APP_CLICK = "InsertAppClick";//点击行为
        const val ML_STATISTICS_APP_PAGE = "AppPage";//页面统计
        const val ML_STATISTICS_SELECT_APP_CLICK = "selectAppClick";//查询点击的行为

        const val ML_STATISTICS_APP_CLICK_FILE_PATH = "appClickFilePath"//文件路径
    }


    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder;
    }


    class MessengerHandler : Handler {
        val gson = Gson();
        var mlStatisticsServiceWeakReference: WeakReference<MlStatisticsService>? = null;
        var replyToClient:Message? = null;
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
                if (replyToClient == null){
                    replyToClient = Message.obtain(msg);
                }
                val bundle = msg.data;
                val statisticesModelJsonString = bundle.getString(
                    ML_STATISTICS_STATISTICES_JSON_MODEL,
                    ""
                );

                if (mlStatisticsServiceWeakReference == null || mlStatisticsServiceWeakReference!!.get() == null || TextUtils.isEmpty(
                        statisticesModelJsonString
                    )
                ) {
                    return
                }

                if (mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper == null) {
                    mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper =
                        MlSqLiteOpenHelper(
                            mlStatisticsServiceWeakReference!!.get()!!.applicationContext
                        );
                }

                val statisticesModel = gson.fromJson<StatisticesModel>(
                    statisticesModelJsonString,
                    StatisticesModel::class.java
                )
                //存储数据到本地数据库
                if (statisticesModel.statisticesType.equals(ML_STATISTICS_INSERT_APP_CLICK)) {
                    val appClickEventModel = statisticesModel.appClickEventModel;
                    if (appClickEventModel != null) {
                        //存储数据到本地
                        val contentValues = ContentValues();
                        contentValues.put("eventName", appClickEventModel.eventName ?: "")
                        contentValues.put("deviceId", appClickEventModel.deviceId ?: "")
                        contentValues.put("userUniCode", appClickEventModel.userUniCode ?: "")
                        contentValues.put("activityName", appClickEventModel.activityName ?: "")
                        contentValues.put("fragmentName", appClickEventModel.fragmentName ?: "")
                        contentValues.put("elementContent", appClickEventModel.elementContent ?: "")
                        contentValues.put("elementType", appClickEventModel.elementType ?: "")
                        contentValues.put("elementId", appClickEventModel.elementId ?: "")
                        contentValues.put("clickTime", appClickEventModel.clickTime)
                        contentValues.put("createTime", appClickEventModel.createTime)
                        contentValues.put("extrans", appClickEventModel.extrans ?: "")
                        mlStatisticsServiceWeakReference!!.get()!!.mlSqLiteOpenHelper!!.insert(
                            TABLE_ML_EVENT_TABLE,
                            contentValues
                        )
                    }
                } else if (statisticesModel.statisticesType.equals(ML_STATISTICS_SELECT_APP_CLICK)) {//开启线程去查询，然后将结果存储到本地 cache目录，通知前端自行去获取 && 解析数据出来
                    mlStatisticsServiceWeakReference!!.get()!!.singleThreadExecutor.execute(
                        SelectEnventTableRunnable(
                            mlStatisticsServiceWeakReference!!.get()!!,
                            statisticesModel.dayTime,
                            statisticesModel.isNeedDeleteHistory
                        )
                    )
                }
            } else if (msg.what == ML_STATISTICS_MSG_WHAT_101) {//文件已经存储到本地了，需要通知前端
                if (replyToClient == null){
                    return
                }
                val message = Message.obtain();
                message.data = msg.data;
                message.what = SensorsDataAPI.ML_STATISTICS_MSG_WHAT_FROM_SERVICE_10
                replyToClient?.let {
                    it.replyTo.send(message);
                }
            }
        }
    }

    /**
     * @date 创建时间:2021/7/26 0026
     * @auther gaoxiaoxiong
     * @Descriptiion 用于查询 eventTable 表里面的数据
     **/
    class SelectEnventTableRunnable : Runnable {
        private var mlStatisticsServiceWeakRefrence: WeakReference<MlStatisticsService>? = null;
        private val anyArray = ArrayList<String>();
        private val fileUtils = FileUtils();
        private var isNeedDelete = true;

        constructor(service: MlStatisticsService, dayTime: Long, isNeedDelete: Boolean) : super() {
            anyArray.add(dayTime.toString())
            anyArray.add("\$AppClick")
            this.isNeedDelete = isNeedDelete;
            mlStatisticsServiceWeakRefrence = WeakReference<MlStatisticsService>(service);
        }

        override fun run() {
            if (mlStatisticsServiceWeakRefrence != null && mlStatisticsServiceWeakRefrence!!.get() != null) {
                if (mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper == null) {
                    mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper =
                        MlSqLiteOpenHelper(
                            mlStatisticsServiceWeakRefrence!!.get()!!.applicationContext
                        );
                }
                val cursor =
                    mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper!!.rawQuery(
                        "select * from " + TABLE_ML_EVENT_TABLE + " t where t.createTime <= ? and t.eventName = ? ",
                        anyArray
                    )
                cursor?.let {
                    if (!it.moveToFirst()){//没有任何一条数据
                        return
                    }
                    val list = mutableListOf<AppClickEventModel>()
                    do {
                        val model = AppClickEventModel();
                        val id = cursor.getInt(cursor.getColumnIndex("id"));
                        val eventName = cursor.getString(cursor.getColumnIndex("eventName"))
                        val deviceId = cursor.getString(cursor.getColumnIndex("deviceId"))
                        val userUniCode = cursor.getString(cursor.getColumnIndex("userUniCode"))
                        val activityName = cursor.getString(cursor.getColumnIndex("activityName"))
                        val fragmentName = cursor.getString(cursor.getColumnIndex("fragmentName"))
                        val elementContent = cursor.getString(cursor.getColumnIndex("elementContent"))
                        val elementType = cursor.getString(cursor.getColumnIndex("elementType"))
                        val elementId = cursor.getString(cursor.getColumnIndex("elementId"));
                        val clickTime = cursor.getLong(cursor.getColumnIndex("clickTime"))
                        val createTime = cursor.getLong(cursor.getColumnIndex("createTime"))
                        val extrans = cursor.getString(cursor.getColumnIndex("extrans"))

                        model.id = id;
                        model.eventName = eventName
                        model.deviceId = deviceId;
                        model.userUniCode = userUniCode;
                        model.activityName = activityName;
                        model.fragmentName = fragmentName;
                        model.elementContent = elementContent;
                        model.elementType = elementType;
                        model.elementId = elementId;
                        model.clickTime = clickTime;
                        model.createTime = createTime;
                        model.extrans = extrans;
                        list.add(model)
                    } while (it.moveToNext())
                    it.close()
                    if (mlStatisticsServiceWeakRefrence == null || mlStatisticsServiceWeakRefrence!!.get() == null) {
                        return
                    }

                    val file =
                        File(fileUtils.getSandboxPublickDiskCacheDir(mlStatisticsServiceWeakRefrence!!.get()!!.applicationContext) + "/" + anyArray[0] + ".txt")
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();

                    val raf = RandomAccessFile(file, "rwd")
                    raf.seek(file.length())
                    raf.write(Gson().toJson(list).toByteArray())
                    raf.close()

                    //进行删除历史记录操作
                    if (isNeedDelete) {
                        mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper!!.delete(
                            TABLE_ML_EVENT_TABLE,
                            "createTime < ? and eventName = ? ",
                            anyArray
                        )
                    }

                    //进行消息发送
                    val message = Message.obtain();
                    message.what = ML_STATISTICS_MSG_WHAT_101;
                    val bundler = Bundle();
                    bundler.putString(ML_STATISTICS_APP_CLICK_FILE_PATH, file.absolutePath);
                    message.data = bundler
                    mlStatisticsServiceWeakRefrence!!.get()!!.messengerHandler.sendMessage(message)
                }
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mlSqLiteOpenHelper != null) {
            mlSqLiteOpenHelper!!.clear()
        }

        if (singleThreadExecutor!=null){
            singleThreadExecutor.shutdown()
        }
    }

}