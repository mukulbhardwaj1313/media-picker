package com.mukulbhardwaj1313.library.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class TrimType:Parcelable {
    DEFAULT, FIXED_DURATION, MIN_DURATION, MIN_MAX_DURATION
}