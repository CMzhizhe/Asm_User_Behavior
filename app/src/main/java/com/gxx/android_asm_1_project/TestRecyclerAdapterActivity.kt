package com.gxx.android_asm_1_project

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.gxx.collectionuserbehaviorlibrary.sensors.TrackViewOnClick

class TestRecyclerAdapterActivity : AppCompatActivity() {

    private val stringList = mutableListOf<String>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recycler_adapter)

        for (i in 1..10) {
            stringList.add("我是 + " + i);
        }

        val testAdapter = TestAdapter(stringList);
        val recyclerView = findViewById<RecyclerView>(R.id.test_recycler_adapter_recyclerview);
        recyclerView.layoutManager = LinearLayoutManager(this);
        recyclerView.adapter = testAdapter;
        testAdapter.setOnItemClickListener(object : OnItemClickListener {
            @TrackViewOnClick
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                Log.e("Test","onItemClick")
            }
        })

        testAdapter.setOnItemChildClickListener(object : OnItemChildClickListener {
            @TrackViewOnClick
            override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                Log.e("Test","onItemChildClick")
            }
        })
    }
}