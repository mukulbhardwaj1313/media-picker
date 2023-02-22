package com.mukulbhardwaj1313.mediaPicker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mukulbhardwaj1313.library.ui.MediaOption
import com.mukulbhardwaj1313.library.ui.MediaPicker

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private val mediaPicker = MediaPicker(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.layout).setOnClickListener {
            selectSource()
        }

    }

    private fun selectSource(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Select source")
        builder.setItems(arrayOf<CharSequence>("Camera photo", "Camera Video", "Gallery Photo", "Gallery Video")) { _, which ->
            when (which) {
                0 -> {
                    mediaPicker.actionCameraXPhotoIntent({ path, source, type ->
                        Toast.makeText(this, "onCreate: $path $source $type", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "onCreate: $path $source $type")
                    })
                }
                1 -> {
                    mediaPicker.actionCameraXVideoIntent({ path, source, type ->
                        Toast.makeText(this, "onCreate: $path $source $type", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "onCreate: $path $source $type")
                    },mediaOption = MediaOption().setDuration(60000).setMaxVideoSize(20))
                }
                2 -> {
                    mediaPicker.actionGalleryPhotoIntent({ path, source, type ->
                        Toast.makeText(this, "onCreate: $path $source $type", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "onCreate: $path $source $type")
                    })
                }
                3 -> {
                    mediaPicker.actionGalleryVideoIntent({ path, source, type ->
                        Toast.makeText(this, "onCreate: $path $source $type", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "onCreate: $path $source $type")
                    },mediaOption = MediaOption().setDuration(25000).setMaxVideoSize(25))
                }
            }
        }
        builder.create().show()
    }
}