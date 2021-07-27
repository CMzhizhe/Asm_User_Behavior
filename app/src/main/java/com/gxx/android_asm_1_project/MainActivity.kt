package com.gxx.android_asm_1_project

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() , SensorsDataAPI.OnSensorsDataAPITrackAllClickListener, SensorsDataAPI.OnSensorsDataUserUniCodeListener {
    private val simpleCreateTimeFormat = SimpleDateFormat("yyyy-MM-dd")
    var yyyyMMdd = simpleCreateTimeFormat.format(Date())
    var yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd)

    var tvLog:TextView? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val test_recycler_adapter_bt =
            findViewById<Button>(R.id.bt_main_test_base_recycler_adapter);
        test_recycler_adapter_bt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                startActivity(Intent(this@MainActivity, TestRecyclerAdapterActivity::class.java))
            }
        })

        val dialogBuilder = AlertDialog.Builder(this);
        dialogBuilder.setTitle("标题")
        dialogBuilder.setMessage("哈哈哈")
        dialogBuilder.setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which ->
        })
        val dialog = dialogBuilder.create();
        dialog.show()



        //设置获取时间点的结果回调
        SensorsDataAPI.getInstance().setOnSensorsDataAPITrackAllClickListener(this)
        //设置用户唯一ID
        SensorsDataAPI.getInstance().setOnSensorsDataUserUniCodeListener(this)

        val btHistory = findViewById<Button>(R.id.bt_main_gethistory);
        btHistory.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                SensorsDataAPI.getInstance().getHistoryAppClickData(yyyyMMddDate.time,true);
            }
        })

        //某个历史时期的日期
        val tvTodayLog = findViewById<TextView>(R.id.bt_main_today);
        tvTodayLog.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                SensorsDataAPI.getInstance().getHistoryAppClickDataByFileName("1627315200000.txt")
            }
        })

        tvLog = findViewById<TextView>(R.id.tv_main_log);
    }

    override fun onSensorsDataAPITrackAllClick(jsonObject: JSONObject) {
         Log.e("MainActivity",jsonObject?.toString())
        tvLog?.setText(jsonObject?.toString())
        //todo 可以拿到结果，自行处理不需要的activity
    }

    override fun onUserUniCode(): String {
         return "zhangsan_unicode"
    }


}