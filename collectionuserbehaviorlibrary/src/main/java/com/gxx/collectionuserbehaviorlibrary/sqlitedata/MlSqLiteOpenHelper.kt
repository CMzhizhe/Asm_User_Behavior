package com.gxx.collectionuserbehaviorlibrary.sqlitedata

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @date 创建时间:2021/7/26 0026
 * @auther gaoxiaoxiong
 * @Descriptiion  https://www.bbsmax.com/A/rV574oyadP/
 **/
class MlSqLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, "mluserbehaviordata.db", null, 1) {

    companion object {
        const val TABLE_ML_EVENT_TABLE = "mlEventTable"//事件表
        const val TABLE_ML_PAGE_TABLE = "mlPageTable"//页面表
        const val TABLE_ML_TIME_TABLE = "mlTimeTable"//时间表
    }


    private val CREATE_EVENT_TABLE = "create table if not exists " + TABLE_ML_EVENT_TABLE + " ( " +
            "id integer primary key autoincrement, " +
            "eventName varchar(100)," + //事件名称，不可为空
            "deviceId varchar(100)," + //手机deviceID
            "userUniCode varchar(100)," +//用户唯一ID
            "activityName varchar(100)," + //activity 名称
            "fragmentName varchar(100)," +//fragment 名称
            "elementContent varchar(100)," +//事件里面的文本内容
            "elementType varchar(100)," +//事件类型
            "elementId varchar(100)," +//事件的id
            "clickTime long," + //事件点击时间 yyyy-MM-dd HH:mm:ss
            "createTime long," +//创建时间  yyyy-MM-dd
            "isVisit int," + //是否可见 0 不可见  1可见
            "extrans varchar(255)" + //额外参数
            " )"


    private val CREATE_PAGE_TABLE = "create table if not exists " + TABLE_ML_PAGE_TABLE + " ( " +
            "id integer primary key autoincrement, " +
            "eventName varchar(100)," + //事件名称，不可为空
            "deviceId varchar(100)," + //手机deviceID
            "userUniCode varchar(100)," +//用户唯一ID
            "activityName varchar(100)," + //activity 名称
            "fragmentName varchar(100)," +//fragment 名称
            "createTime long," +//创建时间   yyyy-MM-dd
            "pageStartTime long," +//页面开始时间  yyyy-MM-dd HH:mm:ss
            "pageEndTime long," +//页面结束时间   yyyy-MM-dd HH:mm:ss
            "fromRoute varchar(100)," + //来源路径，可以为空
            "extrans varchar(255)" + //额外参数，可以为空
            " )";

    private val CREATE_TIME_TABLE = "create table if not exists " + TABLE_ML_TIME_TABLE + " ( " +
            "id integer primary key autoincrement, " +
            "eventName varchar(100)," + //事件名称，不可为空
            "deviceId varchar(100)," + //手机deviceID
            "userUniCode varchar(100)," +//用户唯一ID
            "className varchar(100)," + //类名称
            "methodName varchar(100)," +//方法名称
            "createTime long," +//创建时间   yyyy-MM-dd
            "SysTemStartTime long," +//微妙开始值
            "SysTemMiddleTime long,"+//微妙中间时间，可以用于存储其它值
            "SysTemEndTime long," +//微妙结束值
            "isVisit int," + //是否可见 0 不可见  1可见
            "extrans varchar(255)" + //额外参数，可以为空
            " )";

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_EVENT_TABLE);
        db?.execSQL(CREATE_PAGE_TABLE);
        db?.execSQL(CREATE_TIME_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


    /**
     *
     * @Title: execSQL
     * @Description: Sql写入
     * @param @param sql
     * @param @param bindArgs
     * @return void
     * @author lihy
     */
    fun execSQL(sql: String, bindArgs: ArrayList<Any>) {
        val database: SQLiteDatabase = getWritableDatabase()
        database.execSQL(sql, bindArgs.toArray())
    }


    /**
     *
     * @Title: rawQuery
     * @Description:
     * @param @param sql查询
     * @param @param bindArgs
     * @param @return
     * @return Cursor
     * @author lihy
     */
    fun rawQuery(sql: String, bindArgs: ArrayList<String>): Cursor? {
        val database: SQLiteDatabase = getWritableDatabase()
        return database.rawQuery(sql, bindArgs.toTypedArray())
    }

    /**
     *
     * @Title: insert
     * @Description: 插入数据
     * @param @param table
     * @param @param contentValues 设定文件
     * @return void 返回类型
     * @author lihy
     * @throws
     */
    fun insert(table: String?, contentValues: ContentValues?) {
        val database: SQLiteDatabase = getWritableDatabase()
        database.insert(table, null, contentValues)
    }

    /**
     *
     * @Title: update
     * @Description: 更新
     * @param @param table
     * @param @param values
     * @param @param whereClause
     * @param @param whereArgs 设定文件
     * @return void 返回类型
     * @throws
     */
    fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?) {
        val database: SQLiteDatabase = getWritableDatabase()
        database.update(table, values, whereClause, whereArgs)
    }

    /**
     *
     * @Title: delete
     * @Description:删除
     * @param @param table
     * @param @param whereClause
     * @param @param whereArgs
     * @return void
     * @author lihy
     */
    fun delete(table: String, whereClause: String, whereArgs: ArrayList<String>) {
        val database: SQLiteDatabase = getWritableDatabase()
        database.delete(table, whereClause, whereArgs.toTypedArray())
    }

    /**
     *
     * @Title: query
     * @Description: 查
     * @param @param table
     * @param @param columns
     * @param @param selection
     * @param @param selectionArgs
     * @param @param groupBy
     * @param @param having
     * @param @param orderBy
     * @return void
     * @author lihy
     */
    fun query(table: String?, columns: Array<String?>?, selection: String?, selectionArgs: Array<String?>?, groupBy: String?, having: String?,
              orderBy: String?): Cursor? {
        val database: SQLiteDatabase = getReadableDatabase()
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
    }

    /**
     *
     * @Description:查
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     * Cursor
     * @exception:
     * @author: lihy
     * @time:2015-4-3 上午9:37:29
     */
    fun query(table: String?, columns: Array<String?>?, selection: String?, selectionArgs: Array<String?>?, groupBy: String?, having: String?,
              orderBy: String?, limit: String?): Cursor? {
        val database: SQLiteDatabase = getReadableDatabase()
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
    }

    /**
     *
     * @Description 查询，方法重载,table表名，sqlString条件
     * @param @return
     * @return Cursor
     * @author lihy
     */
    fun query(tableName: String, sqlString: String): Cursor? {
        val database: SQLiteDatabase = getReadableDatabase()
        return database.rawQuery("select * from $tableName $sqlString", null)
    }

    /**
     * @see android.database.sqlite.SQLiteOpenHelper.close
     */
    fun clear() {
        this.close()
    }
}