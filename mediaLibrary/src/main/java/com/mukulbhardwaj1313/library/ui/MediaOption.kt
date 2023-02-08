package com.mukulbhardwaj1313.library.ui

import android.os.Parcel
import android.os.Parcelable

class MediaOption() :Parcelable {

    var maxVideoSize: Int = 20
    var duration: Long = 20000
    var name :String? = null

    constructor(parcel: Parcel) : this() {
        duration = parcel.readLong()
        name = parcel.readString()
        maxVideoSize = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeString(name)
        parcel.writeInt(maxVideoSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaOption> {
        override fun createFromParcel(parcel: Parcel): MediaOption {
            return MediaOption(parcel)
        }

        override fun newArray(size: Int): Array<MediaOption?> {
            return arrayOfNulls(size)
        }
    }


}