package com.mukulbhardwaj1313.mediaPicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.mukulbhardwaj1313.library.ui.MediaOption
import com.mukulbhardwaj1313.library.ui.MediaPicker

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private val mediaPicker = MediaPicker(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.layout).setOnClickListener {
            mediaPicker.actionGalleryVideoIntent({ path, source, type ->
                Log.w(TAG, "onCreate: $path $source $type")
            },mediaOption = MediaOption().setDuration(60000).setMaxVideoSize(20))
        }

    }
}