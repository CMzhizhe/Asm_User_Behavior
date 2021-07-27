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




        SensorsDataAPI.getInstance().setOnSensorsDataAPITrackAllClickListener(this)
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


    /**
     * 普通 setOnClickListener
     */
    private fun initButton() {
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //插入代码(for())

                //统计
                Log.e("MainActivity", "普通")
            }
        })
    }

    override fun onSensorsDataAPITrackAllClick(jsonObject: JSONObject) {
         Log.e("MainActivity",jsonObject?.toString())
        tvLog?.setText(jsonObject?.toString())
    }

    override fun onUserUniCode(): String {
         return "zhangsan_unicode"
    }


}