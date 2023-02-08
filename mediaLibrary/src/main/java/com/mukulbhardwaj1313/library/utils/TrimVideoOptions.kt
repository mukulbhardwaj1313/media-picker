package com.mukulbhardwaj1313.library.utils

import android.os.Parcelable
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrimVideoOptions(
    var trimType:TrimType = TrimType.DEFAULT,
    var minDuration: Long = 0,
    var maxDuration: Long = 20000,
    var fixedDuration: Long = 0,
    var hideSeekBar:Boolean = false,
    var accurateCut:Boolean = false,
    var showFileLocationAlert:Boolean = false,
    var minToMax: LongArray? = null,
    var maxVideoSize:Int = 20,
    var rotation:Int = 0,                                               //           rotation = 0,   90,   180,   270
    var speed:Float = 1f,                                               //           speed = 0.5F,   1F,   2F
    var channels:Int = DefaultAudioStrategy.CHANNELS_AS_INPUT,          //           channels = 1; //mono,   2; // stereo,        DefaultAudioStrategy.CHANNELS_AS_INPUT
    var sampleRate:Int = DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT,                                         //           sampleRate = 32000;  // 32 kHz,   48000; // 48 kHz, DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT
    var aspectRatio:Float = 0f,                                         //           aspectRatio = 0F,  16F / 9F,  4F / 3F,  1F
    var fraction:Float = 1f,                                          //           fraction = 0.5F, 1F / 3F; (resolution)
    var frames:Int = DefaultVideoStrategy.DEFAULT_FRAME_RATE                                                 //           frames = 24,  30,  60,    DefaultVideoStrategy.DEFAULT_FRAME_RATE
):Parcelable
