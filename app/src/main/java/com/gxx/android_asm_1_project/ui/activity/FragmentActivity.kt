package com.gxx.android_asm_1_project.ui.activity

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.gxx.android_asm_1_project.R
import com.gxx.android_asm_1_project.ui.fragment.TestFragment

class FragmentActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val framelayout = findViewById<FrameLayout>(R.id.fl_fragment_containt);
        val transtionsaction = supportFragmentManager.beginTransaction();

        val fragment = TestFragment.getInstance();
        transtionsaction.add(R.id.fl_fragment_containt, fragment, "fragment");
        transtionsaction.show(fragment)
        transtionsaction.commit()
    }
}