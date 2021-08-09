package com.gxx.android_asm_1_project.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gxx.android_asm_1_project.R
import com.gxx.collectionuserbehaviorlibrary.costtime.CostTime
import com.gxx.collectionuserbehaviorlibrary.sensors.SensorsDataAPI
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() , SensorsDataAPI.OnSensorsDataAPITrackAllClickListener, SensorsDataAPI.OnSensorsDataUserUniCodeListener {
    companion object{
        const val TAG = "MainActivity"
    }
    private val simpleCreateTimeFormat = SimpleDateFormat("yyyy-MM-dd")
    var yyyyMMdd = simpleCreateTimeFormat.format(Date())
    var yyyyMMddDate = simpleCreateTimeFormat.parse(yyyyMMdd)

    var tvLog:TextView? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.normal_button_click).setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                Log.i(TAG,"点击事件")
            }
        })

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
        val tvFileLog = findViewById<TextView>(R.id.bt_main_file);
        tvFileLog.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                SensorsDataAPI.getInstance().getHistoryAppClickDataByFileName("1627315200000.txt")
            }
        })

        tvLog = findViewById<TextView>(R.id.tv_main_log);

        val btSetTagProperties = findViewById<Button>(R.id.bt_main_setTag_properties);
        btSetTagProperties.setTag(R.id.sensors_analytics_tag_view_properties,"我是view_properties")
        btSetTagProperties.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                SensorsDataAPI.getInstance().getHistoryAppClickData(yyyyMMddDate.time,true);
            }
        })

        //耗时点击
        val btTime = findViewById<Button>(R.id.timeoutclick);
        btTime.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View) {
                readNetWork();
            }
        })

        val btFragmentClick = findViewById<Button>(R.id.bt_main_fragment_click)
        btFragmentClick.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity,FragmentActivity::class.java))
            }
        })
    }


    public fun readNetWork(){
        Thread(object :Runnable{
            @CostTime
            override fun run() {
                 Thread.sleep(5 * 1000)
            }
        }).start()
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