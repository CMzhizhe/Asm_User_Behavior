package com.gxx.android_asm_1_project.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.gxx.android_asm_1_project.R

class NoticeDialog(context: Context) : Dialog(context) {
     companion object{
         val TAG = NoticeDialog::class.java.canonicalName;
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_notice)
        findViewById<Button>(R.id.bt_dialog_common_twobutton_left).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.i(TAG,"我被点击了")
            }
        })


        findViewById<Button>(R.id.bt_dialog_common_twobutton_right).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.i(TAG,"我被点击了")
            }
        })


        //设置宽度
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay //获取屏幕宽高

        val point = Point()
        display.getSize(point)
        val window = window
        val layoutParams = window!!.attributes //获取当前对话框的参数值

        layoutParams.gravity = Gravity.CENTER
        layoutParams.width = (point.x * 0.9).toInt() //宽度设置为屏幕宽度的0.5
        window!!.attributes = layoutParams

    }

}