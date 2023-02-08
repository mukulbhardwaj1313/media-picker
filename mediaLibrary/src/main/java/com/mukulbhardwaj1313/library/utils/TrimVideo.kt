package com.mukulbhardwaj1313.library.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.mukulbhardwaj1313.library.ui.TrimmerActivity

object TrimVideo {
    const val TRIM_VIDEO_OPTION = "trim_video_option"
    const val TRIM_VIDEO_URI = "trim_video_uri"
    private const val TRIMMED_VIDEO_PATH = "trimmed_video_path"

    fun activity(uri: String?): ActivityBuilder {
        return ActivityBuilder(uri)
    }

    fun getTrimmedVideoPath(intent: Intent): String? {
        return intent.getStringExtra(TRIMMED_VIDEO_PATH)
    }

    class ActivityBuilder(private val videoUri: String?) {
        private val options: TrimVideoOptions = TrimVideoOptions()


        fun setTrimType(trimType: TrimType?): ActivityBuilder {
            options.trimType = trimType!!
            return this
        }

        fun setHideSeekBar(hide: Boolean): ActivityBuilder {
            options.hideSeekBar = hide
            return this
        }

        fun showFileLocationAlert(): ActivityBuilder {
            options.showFileLocationAlert = true
            return this
        }

        fun setAccurateCut(accurate: Boolean): ActivityBuilder {
            options.accurateCut = accurate
            return this
        }

        fun setMinDuration(minDuration: Long): ActivityBuilder {
            options.minDuration = minDuration
            return this
        }

        fun setMaxDuration(maxDuration: Long): ActivityBuilder {
            options.maxDuration = maxDuration
            return this
        }

        fun setFixedDuration(fixedDuration: Long): ActivityBuilder {
            options.fixedDuration = fixedDuration
            return this
        }

        fun setMaxVideoSize(maxVideoSize: Int): ActivityBuilder {
            options.maxVideoSize = maxVideoSize
            return this
        }

        fun setFraction(fraction: Float): ActivityBuilder {
            options.fraction = fraction
            return this
        }

        fun setMinToMax(min: Long, max: Long): ActivityBuilder {
            options.minToMax = longArrayOf(min, max)
            return this
        }


        fun start(activity: Activity, launcher: ActivityResultLauncher<Intent?>) {
            validate()
            launcher.launch(getIntent(activity))
        }

        private fun validate() {
            if (videoUri == null) throw NullPointerException("VideoUri cannot be null.")
            require(videoUri.isNotEmpty()) { "VideoUri cannot be empty" }
            require(options.minDuration >= 0) { "Cannot set min duration to a number < 1" }
            require(options.fixedDuration >= 0) { "Cannot set fixed duration to a number < 1" }
            require(!(options.trimType === TrimType.MIN_MAX_DURATION && options.minToMax == null)) {
                "Used trim type is TrimType.MIN_MAX_DURATION." +
                        "Give the min and max duration"
            }
            if (options.minToMax != null) {
                require(!(options.minToMax!![0] < 0 || options.minToMax!![1] < 0)) { "Cannot set min to max duration to a number < 1" }
                require(options.minToMax!![0] <= options.minToMax!![1]) { "Minimum duration cannot be larger than max duration" }
                require(options.minToMax!![0] != options.minToMax!![1]) { "Minimum duration cannot be same as max duration.Use Fixed duration" }
            }
        }

        private fun getIntent(activity: Activity): Intent {
            val intent = Intent(activity, TrimmerActivity::class.java)
            val bundle = Bundle()
            bundle.putString(TRIM_VIDEO_URI, videoUri)
            bundle.putParcelable(TRIM_VIDEO_OPTION, options)
            intent.putExtras(bundle)
            return intent
        }
    }
}