package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

class StatisticesModel() : Parcelable{
    var statisticesType:String = "";//统计行为
    var appClickEventModel:AppClickEventModel? = null;

    constructor(parcel: Parcel) : this() {
        statisticesType = parcel.readString()?:""
        appClickEventModel = parcel.readParcelable(AppClickEventModel::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(statisticesType)
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