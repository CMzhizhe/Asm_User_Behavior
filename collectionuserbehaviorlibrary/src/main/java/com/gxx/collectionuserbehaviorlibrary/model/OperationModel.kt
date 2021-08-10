package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

class OperationModel() : Parcelable {
    var operaStatus = 0;//操作状态码反馈
    var mlStatisticeStatus = ""//操作的类型方式
    var filePath = ""//文件路径

    constructor(parcel: Parcel) : this() {
        operaStatus = parcel.readInt()
        mlStatisticeStatus = parcel.readString()?:""
        filePath = parcel.readString()?:""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(operaStatus)
        parcel.writeString(mlStatisticeStatus)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OperationModel> {
        override fun createFromParcel(parcel: Parcel): OperationModel {
            return OperationModel(parcel)
        }

        override fun newArray(size: Int): Array<OperationModel?> {
            return arrayOfNulls(size)
        }
    }


}