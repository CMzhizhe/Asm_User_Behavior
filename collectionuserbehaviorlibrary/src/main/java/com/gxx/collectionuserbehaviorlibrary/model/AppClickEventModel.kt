package com.gxx.collectionuserbehaviorlibrary.model

import android.os.Parcel
import android.os.Parcelable

/**
 * @date 创建时间:2021/7/21 0021
 * @auther gaoxiaoxiong
 * @Descriptiion 事件model
 **/
public class AppClickEventModel() : Parcelable{
    var id: Int = 0;
    var eventName: String = "";//事件名称
    var deviceId:String = "";//手机deviceID
    var userUniCode:String = "";//用户唯一ID
    var uiClassName:String = "";//ui类名称 名称
    var elementContent:String = "";//事件里面的文本内容
    var elementType:String = "";//事件类型
    var elementId:String = "";//事件的id
    var clickTime:Long = 0L;// 事件点击时间 yyyy-MM-dd HH:mm:ss
    var createTime:Long = 0L;//创建时间  yyyy-MM-dd
    var extrans:String = "";//额外参数
    var functionType:String = "";//事件类型

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        eventName = parcel.readString()?:""
        deviceId = parcel.readString()?:""
        userUniCode = parcel.readString()?:""
        uiClassName = parcel.readString()?:""
        elementContent = parcel.readString()?:""
        elementType = parcel.readString()?:""
        elementId = parcel.readString()?:""
        clickTime = parcel.readLong()
        createTime = parcel.readLong()
        extrans = parcel.readString()?:""
        functionType = parcel.readString()?:""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(eventName)
        parcel.writeString(deviceId)
        parcel.writeString(userUniCode)
        parcel.writeString(uiClassName)
        parcel.writeString(elementContent)
        parcel.writeString(elementType)
        parcel.writeString(elementId)
        parcel.writeLong(clickTime)
        parcel.writeLong(createTime)
        parcel.writeString(extrans)
        parcel.writeString(functionType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppClickEventModel> {
        override fun createFromParcel(parcel: Parcel): AppClickEventModel {
            return AppClickEventModel(parcel)
        }

        override fun newArray(size: Int): Array<AppClickEventModel?> {
            return arrayOfNulls(size)
        }
    }


}