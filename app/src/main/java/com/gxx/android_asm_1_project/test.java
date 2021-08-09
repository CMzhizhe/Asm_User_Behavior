package com.gxx.android_asm_1_project;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class test extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void aaa(){
        TextView view = findViewById(R.id.tv_fragment_lamada);
        view.setOnClickListener(v -> {

        });
    }
}
