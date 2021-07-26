package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

class StatisticesModel() : Parcelable{
    var statisticesType:String = "";//统计行为
    var isNeedDeleteHistory:Boolean = true;//是否需要进行删除操作
    var dayTime:Long = 0L;
    var appClickEventModel:AppClickEventModel? = null;

    constructor(parcel: Parcel) : this() {
        statisticesType = parcel.readString()?:""
        isNeedDeleteHistory = parcel.readByte() != 0.toByte()
        dayTime = parcel.readLong()
        appClickEventModel = parcel.readParcelable(AppClickEventModel::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(statisticesType)
        parcel.writeByte(if (isNeedDeleteHistory) 1 else 0)
        parcel.writeLong(dayTime)
        parcel.writeParcelable(appClickEventModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StatisticesModel> {
        override fun createFromParcel(parcel: Parcel): StatisticesModel {
            return StatisticesModel(parcel)
        }

        override fun newArray(size: Int): Array<StatisticesModel?> {
            return arrayOfNulls(size)
        }
    }


}