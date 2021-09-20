package com.example.myproject.project

import android.os.Parcel
import android.os.Parcelable
import com.example.myproject.project.DetailFragmentParams

class PasswordFragmentParams(
    val isDataChange: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isDataChange) 1 else 0)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetailFragmentParams> {
        override fun createFromParcel(parcel: Parcel): DetailFragmentParams {
            return DetailFragmentParams(parcel)
        }

        override fun newArray(size: Int): Array<DetailFragmentParams?> {
            return arrayOfNulls(size)
        }
    }
}
