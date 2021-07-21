package com.gxx.android_asm_1_project

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initButton()
        //initLambdaButton();

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


    /**
     * Lambda 语法
     */
    private fun initLambdaButton() {
        val button: Button = findViewById(R.id.lambdaButton)
        button.setOnClickListener { view -> Log.e("MainActivity", "Lambda Click") }
    }

}