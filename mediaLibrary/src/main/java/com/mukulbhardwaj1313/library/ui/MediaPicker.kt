package com.mukulbhardwaj1313.library.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mukulbhardwaj1313.library.utils.FileHandler.createNewFile
import com.mukulbhardwaj1313.library.utils.TrimVideo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


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
                val file = contentUriToImageFile(it.data!!.data!!)
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
                val file = contentUriToVideoFile(it.data!!.data!!)
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


    private fun contentUriToImageFile(uri: Uri): File {
        val bitmap =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        val resizedSignature: Bitmap = Bitmap.createBitmap(bitmap)
        val quality = if (bitmap.byteCount > 5145728) 50 else 80
        resizedSignature.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        val myFile = createNewFile(activity, name = mediaOption.name)
        val outputStream = FileOutputStream(myFile)
        outputStream.write(byteArrayOutputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        return myFile
    }

    private fun contentUriToVideoFile(uri: Uri): File {
        val myFile = createNewFile(activity, isVideo = true, name = mediaOption.name)
        val inputStream = activity.contentResolver.openInputStream(uri)
        val myOutputStream = FileOutputStream(myFile)
        val maxBufferSize = 1 * 1024 * 1024
        val bytesAvailable = inputStream!!.available()
        val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            myOutputStream.write(buffer, 0, read)
        }
        inputStream.close()
        myOutputStream.close()

        return myFile
    }

}