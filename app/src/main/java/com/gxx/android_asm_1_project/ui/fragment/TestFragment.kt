package com.gxx.android_asm_1_project.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gxx.android_asm_1_project.R
import com.gxx.android_asm_1_project.ui.dialog.NoticeDialog

class TestFragment : Fragment() ,View.OnClickListener{
    companion object{
        val TAG = TestFragment::class.java.simpleName;
        fun getInstance():TestFragment{
            val fragment = TestFragment()
            return fragment
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        view.findViewById<TextView>(R.id.tv_fragment_test).setOnClickListener(object :
            View.OnClickListener {
            override fun onClick(v: View?) {
                Log.e(TAG, "我被点击啦")
            }
        })

        val tvLamada =  view.findViewById<TextView>(R.id.tv_fragment_lamada)
        view.setOnClickListener { v: View? ->
            Log.e(TAG, "我被点击啦")
        }

        val tvdialogNotice = view.findViewById<TextView>(R.id.tv_fragment_dialog)
        tvdialogNotice.setOnClickListener { v: View? ->
             val noticeDialog = NoticeDialog(this@TestFragment.context!!);
            noticeDialog.show()
        }


        return view
    }

    override fun onClick(v: View) {
         Log.e(TAG, "我被点击啦")
    }
}