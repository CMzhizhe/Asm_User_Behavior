package com.gxx.collectionuserbehaviorlibrary.runable

import android.os.Bundle
import android.os.Message
import com.google.gson.Gson
import com.gxx.collectionuserbehaviorlibrary.model.AppClickEventModel
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.Companion.ML_STATISTICS_SELECT_APP_CLICK
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.Companion.ML_STATISTICS_SELECT_APP_CLICK_BY_TIME
import com.gxx.collectionuserbehaviorlibrary.service.MlStatisticsService.Companion.ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME
import com.gxx.collectionuserbehaviorlibrary.sqlitedata.MlSqLiteOpenHelper
import com.gxx.collectionuserbehaviorlibrary.utils.FileUtils
import java.io.File
import java.io.RandomAccessFile
import java.lang.ref.WeakReference

/**
 * @date 创建时间:2021/7/26 0026
 * @auther gaoxiaoxiong
 * @Descriptiion 事件 eventTable 表里面的数据
 **/
class EnventTableRunnable:Runnable {
    private var mlStatisticsServiceWeakRefrence: WeakReference<MlStatisticsService>? = null;
    private val anyArray = ArrayList<String>();
    private val fileUtils = FileUtils();
    private var status = ""
    private var dayTime = 0L;
    private var eventName = "";
    /**
     * @date 创建时间:2021/7/27 0027
     * @auther gaoxiaoxiong
     * @Descriptiion
     * @param dayTime 查询的时间
     * @param eventName 事件名称
     * @param status 10_dayTime < createTime  20_dayTime == createTime
     **/
    constructor(service: MlStatisticsService, dayTime: Long, eventName: String, status: String) : super() {
        this.anyArray.clear()
        this.dayTime = dayTime;
        this.eventName = eventName;
        this.status = status;
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
            var sql = ""
            if (status.equals(ML_STATISTICS_SELECT_APP_CLICK)){
                sql = "select * from " + MlSqLiteOpenHelper.TABLE_ML_EVENT_TABLE + " t where t.createTime < ? and t.eventName = ? and isVisit = 1 "
            }else if (status.equals(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)){
                sql = "select * from " + MlSqLiteOpenHelper.TABLE_ML_EVENT_TABLE + " t where t.createTime = ? and t.eventName = ? and isVisit = 1 "
            }else if (status.equals(ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME)){
                sql = "update " + MlSqLiteOpenHelper.TABLE_ML_EVENT_TABLE + " set isVisit = 0  where createTime in (?) ";
            }
            
            if (status.equals(ML_STATISTICS_SELECT_APP_CLICK) || status.equals(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)){
                anyArray.add(dayTime.toString())
                anyArray.add(eventName)
                val cursor = mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper!!.rawQuery(sql, anyArray)
                cursor?.let {
                    if (!it.moveToFirst()) {//没有任何一条数据
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
                            File(
                                    fileUtils.getSandboxPublickDiskCacheDir(
                                            mlStatisticsServiceWeakRefrence!!.get()!!.applicationContext
                                    ) + "/" + anyArray[0] + ".txt"
                            )
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();

                    val raf = RandomAccessFile(file, "rwd")
                    raf.seek(file.length())
                    raf.write(Gson().toJson(list).toByteArray())
                    raf.close()

                    //进行删除历史记录操作
                    /*if (isNeedDelete) {
                        var where = ""
                        if (status.equals(ML_STATISTICS_SELECT_APP_CLICK)){
                            where = "createTime < ? and eventName = ? "
                        }else if (status.equals(ML_STATISTICS_SELECT_APP_CLICK_BY_TIME)){
                            where = "createTime = ? and eventName = ? "
                        }
                        mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper!!.delete(
                                MlSqLiteOpenHelper.TABLE_ML_EVENT_TABLE,
                                where,
                                anyArray
                        )
                    }*/

                    //进行消息发送
                    val message = Message.obtain();
                    message.what = MlStatisticsService.ML_STATISTICS_MSG_WHAT_101;
                    val bundler = Bundle();
                    bundler.putString(MlStatisticsService.ML_STATISTICS_APP_CLICK_FILE_PATH, file.absolutePath);
                    message.data = bundler
                    mlStatisticsServiceWeakRefrence!!.get()!!.messengerHandler.sendMessage(message)
                }
            }else if (status.equals(ML_STATISTICS_UPDATE_APP_CLICK_BY_TIME)){
                anyArray.add(dayTime.toString())
                mlStatisticsServiceWeakRefrence!!.get()!!.mlSqLiteOpenHelper!!.execSQL(sql,anyArray)
            }
            
        }

    }
}