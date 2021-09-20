package com.example.myproject.project.detail

import android.os.Parcel
import android.os.Parcelable
import com.example.myproject.project.note.Note

data class DetailFragmentParams(
        val note: Note,
        val isNew: Boolean,
        val callerFragment: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Note::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(note, flags)
        parcel.writeInt(if (isNew) 1 else 0)
        parcel.writeString(callerFragment)
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
