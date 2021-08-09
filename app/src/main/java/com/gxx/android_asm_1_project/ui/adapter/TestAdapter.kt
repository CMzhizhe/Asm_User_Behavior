package com.gxx.android_asm_1_project.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gxx.android_asm_1_project.R

class TestAdapter(list:MutableList<String>) : BaseQuickAdapter<String,BaseViewHolder>(R.layout.item_string,list){

    init {
        addChildClickViewIds(R.id.tv_item_string);
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tv_item_string,item);
    }

}