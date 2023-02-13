package com.mukulbhardwaj1313.library.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mukulbhardwaj1313.library.utils.FileHandler.contentUriToImageFile
import com.mukulbhardwaj1313.library.utils.FileHandler.contentUriToVideoFile
import com.mukulbhardwaj1313.library.utils.TrimVideo


class MediaPicker(private val activity: AppCompatActivity) {

    private lateinit var onResultFinalize: OnResultFinalize
    private lateinit var mediaOption: MediaOption
    private lateinit var source:String

    fun interface OnResultFinalize {
        fun onResultFinalize(path: String, source: String, type: String)
    }

    fun actionCameraXPhotoIntent(onResultFinalize: OnResultFinalize, openFrontCamera : Boolean = false, mediaOption: MediaOption= MediaOption()){
        this.onResultFinalize=onResultFinalize
        this.mediaOption=mediaOption
        CameraActivity.startPhoto(activity, captureImageResultViaCameraX, openFrontCamera, mediaOption)
    }
    fun actionCameraXVideoIntent(onResultFinalize: OnResultFinalize, mediaOption: MediaOption= MediaOption()){
        this.onResultFinalize=onResultFinalize
        this.mediaOption=mediaOption
        CameraActivity.startVideo(activity, captureVideoResultViaCameraX,mediaOption)
    }
    fun actionGalleryPhotoIntent(onResultFinalize: OnResultFinalize, mediaOption: MediaOption= MediaOption()) {
        this.onResultFinalize=onResultFinalize
        this.mediaOption=mediaOption
        val i = Intent(Intent.ACTION_PICK, null)
        i.type = "image/*"
        galleryImageResult.launch(Intent.createChooser(i, "Select Picture"))

    }
    fun actionGalleryVideoIntent(onResultFinalize: OnResultFinalize, mediaOption: MediaOption = MediaOption()) {
        this.onResultFinalize=onResultFinalize
        this.mediaOption=mediaOption
        val i = Intent(Intent.ACTION_PICK, null)
        i.type = "video/*"
        galleryVideoResult.launch(Intent.createChooser(i, "Select Video"))
    }

    private val captureImageResultViaCameraX = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            onResultFinalize.onResultFinalize(it.data!!.getStringExtra("data")!!, "Camera","Image")
        }
    }
    private val galleryImageResult = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val file = contentUriToImageFile(activity, it.data!!.data!!, mediaOption.name)
                onResultFinalize.onResultFinalize(file.path,"Gallery","Image")
            }
        }

    }
    private val captureVideoResultViaCameraX = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            onCapturedVideo(it.data!!.getStringExtra("data")!!,"Camera")
        }
    }
    private val galleryVideoResult = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val file = contentUriToVideoFile(activity,it.data!!.data!!,mediaOption.name)
                onCapturedVideo(file.path,"Gallery")
            }
        }
    }
    private val trimVideoResult = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK)
            onResultFinalize.onResultFinalize(result.data!!.getStringExtra("data")!!,source,"Video" )
    }

    private fun onCapturedVideo(path: String, source: String) {
        this.source=source
        TrimVideo
            .ActivityBuilder(path)
            .setMaxVideoSize(mediaOption.maxVideoSize)
            .setMaxDuration(mediaOption.duration)
            .setFraction(0.5f)
//            .setTrimType(TrimType.FIXED_DURATION).setMinToMax(0,15)
            .start(activity, trimVideoResult)
    }



}