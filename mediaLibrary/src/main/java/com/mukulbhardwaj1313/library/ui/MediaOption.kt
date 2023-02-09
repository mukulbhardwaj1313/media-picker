package com.mukulbhardwaj1313.library.ui

import android.os.Parcel
import android.os.Parcelable

class MediaOption() :Parcelable {

    var maxVideoSize: Int = 2000
    var duration: Long = 2000000
    var name :String? = null

    fun setMaxVideoSize(maxVideoSize: Int): MediaOption {
        this.maxVideoSize = maxVideoSize
        return this
    }

    fun setDuration(duration: Long): MediaOption {
        this.duration = duration
        return this
    }

    fun setName(name: String?): MediaOption {
        this.name = name
        return this
    }

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