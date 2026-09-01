package com.openos.virtualcar.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * OpenOS VirtualCar polymorphic parcelable value container.
 * Matches ground truth from `com.openos.virtualcar.entity.VirtualCarValue`.
 */
data class VirtualCarValue(
    var mCategoryId: Int = 0,
    var mFuncId: Int = 0,
    var mAreaId: Int = 0,
    var mCode: Int = 0,
    var mTimestamp: Long = 0L,
    var mValue: Any? = null
) : Parcelable {

    constructor(parcel: Parcel) : this() {
        mCode = 0
        mCategoryId = parcel.readInt()
        mFuncId = parcel.readInt()
        mAreaId = parcel.readInt()
        mCode = parcel.readInt()
        mTimestamp = parcel.readLong()
        val typeString = parcel.readString()
        val classLoader = if (!typeString.isNullOrEmpty() && typeString != "null") {
            try {
                Class.forName(typeString).classLoader
            } catch (_: Throwable) {
                null
            }
        } else null
        mValue = parcel.readValue(classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mCategoryId)
        parcel.writeInt(mFuncId)
        parcel.writeInt(mAreaId)
        parcel.writeInt(mCode)
        parcel.writeLong(mTimestamp)
        val value = mValue
        if (value == null) {
            parcel.writeString("null")
        } else {
            parcel.writeString(value.javaClass.name)
        }
        parcel.writeValue(value)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VirtualCarValue> {
        override fun createFromParcel(parcel: Parcel): VirtualCarValue = VirtualCarValue(parcel)
        override fun newArray(size: Int): Array<VirtualCarValue?> = arrayOfNulls(size)
    }
}
