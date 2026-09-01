package com.openos.virtualcar.entity

import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

/**
 * OpenOS VirtualCar property configuration container.
 * Matches ground truth from `com.openos.virtualcar.entity.VirtualPropertyConfig`.
 */
data class VirtualPropertyConfig(
    var mAccess: Int = 0,
    var mAreaType: Int = 0,
    var mConfigArray: ArrayList<Int> = ArrayList(),
    var mConfigString: String? = null,
    var mFuncId: Int = 0,
    var mSupportedAreas: SparseArray<Parcelable> = SparseArray(),
    var mType: Class<*>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this() {
        mAccess = parcel.readInt()
        mAreaType = parcel.readInt()
        val arraySize = parcel.readInt()
        mConfigArray = ArrayList(arraySize)
        for (i in 0 until arraySize) {
            mConfigArray.add(parcel.readInt())
        }
        mConfigString = parcel.readString()
        mFuncId = parcel.readInt()
        val areasSize = parcel.readInt()
        mSupportedAreas = SparseArray(areasSize)
        for (i in 0 until areasSize) {
            val key = parcel.readInt()
            val value: Parcelable? = parcel.readParcelable(VirtualPropertyConfig::class.java.classLoader)
            if (value != null) {
                mSupportedAreas.put(key, value)
            }
        }
        val typeName = parcel.readString()
        if (!typeName.isNullOrEmpty()) {
            try {
                mType = Class.forName(typeName)
            } catch (_: Throwable) {}
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mAccess)
        parcel.writeInt(mAreaType)
        parcel.writeInt(mConfigArray.size)
        for (item in mConfigArray) {
            parcel.writeInt(item)
        }
        parcel.writeString(mConfigString)
        parcel.writeInt(mFuncId)
        parcel.writeInt(mSupportedAreas.size())
        for (i in 0 until mSupportedAreas.size()) {
            parcel.writeInt(mSupportedAreas.keyAt(i))
            parcel.writeParcelable(mSupportedAreas.valueAt(i), flags)
        }
        parcel.writeString(mType?.name ?: "")
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VirtualPropertyConfig> {
        override fun createFromParcel(parcel: Parcel): VirtualPropertyConfig = VirtualPropertyConfig(parcel)
        override fun newArray(size: Int): Array<VirtualPropertyConfig?> = arrayOfNulls(size)
    }
}
