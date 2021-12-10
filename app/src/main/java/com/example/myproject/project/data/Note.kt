package com.example.myproject.project.data

import android.os.Parcel
import android.os.Parcelable

data class Note(
    var typeName: String,
    var title: String = "",
    var content: String = "",
    val id: Int = 0,
    var editTime: String = "",
    var isTop: Boolean = false,
    var wallpaperName: String? = null,
    var removalTime: Long = 0
) : Parcelable, Comparable<Note> {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(typeName)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeInt(id)
        parcel.writeString(editTime)
        parcel.writeByte(if (isTop) 1 else 0)
        parcel.writeString(wallpaperName)
        parcel.writeLong(removalTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }

    override fun compareTo(other: Note): Int {
        var result = other.isTop.compareTo(this.isTop)
        if (!this.isTop && !other.isTop) result = other.editTime.compareTo(this.editTime)
        return result
    }


}