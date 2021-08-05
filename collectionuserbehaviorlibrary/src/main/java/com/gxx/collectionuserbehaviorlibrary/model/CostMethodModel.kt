package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

class CostMethodModel(): Parcelable {
    var eventName:String = "";
    var startTime:Long = 0L;
    var endTime:Long = 0L;
    var className:String = "";
    var methodName:String = "";
    var deviceId:String="";
    var userUniCode:String = ""
    var createTime:Long=0L

    constructor(parcel: Parcel) : this() {
        eventName = parcel.readString()?:""
        startTime = parcel.readLong()
        endTime = parcel.readLong()
        className = parcel.readString()?:""
        methodName = parcel.readString()?:""
        deviceId = parcel.readString()?:""
        userUniCode = parcel.readString()?:""
        createTime = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventName)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeString(className)
        parcel.writeString(methodName)
        parcel.writeString(deviceId)
        parcel.writeString(userUniCode)
        parcel.writeLong(createTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CostMethodModel> {
        override fun createFromParcel(parcel: Parcel): CostMethodModel {
            return CostMethodModel(parcel)
        }

        override fun newArray(size: Int): Array<CostMethodModel?> {
            return arrayOfNulls(size)
        }
    }


}