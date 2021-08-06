package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

class StatisticesModel() : Parcelable{
    var statisticesType:String = "";//统计行为
    var dayTime:Long = 0L;
    var jsonString:String = "";

    constructor(parcel: Parcel) : this() {
        statisticesType = parcel.readString()?:""
        dayTime = parcel.readLong()
        jsonString = parcel.readString()?:""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(statisticesType)
        parcel.writeLong(dayTime)
        parcel.writeString(jsonString)
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