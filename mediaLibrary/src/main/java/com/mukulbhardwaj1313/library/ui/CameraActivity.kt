package com.mukulbhardwaj1313.library.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import com.mukulbhardwaj1313.library.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture?=null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var type :String
    private var openFrontCamera : Boolean = false
    private lateinit var binding : ActivityCameraBinding

    companion object{
        private const val TYPE="type"
        private const val FRONT_CAMERA_ENABLED="FRONT_CAMERA_ENABLED"
        private const val VIDEO_CAPTURE="VIDEO_CAPTURE"
        private const val CAMERA_CAPTURE="CAMERA_CAPTURE"
        private const val TAG = "CameraActivity"
        private var duration:Long = 0


        fun startPhoto(activity: AppCompatActivity, activityResult: ActivityResultLauncher<Intent>, openFrontCamera: Boolean){
            activityResult.launch(Intent(activity,CameraActivity::class.java).putExtra(TYPE, CAMERA_CAPTURE).putExtra(FRONT_CAMERA_ENABLED,openFrontCamera))
        }
        fun startVideo(activity: AppCompatActivity, activityResult: ActivityResultLauncher<Intent>, duration:Long = 20000){
            this.duration = duration
            activityResult.launch(Intent(activity,CameraActivity::class.java).putExtra(TYPE, VIDEO_CAPTURE))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type= if(intent.getStringExtra(TYPE) ==null) CAMERA_CAPTURE else intent.getStringExtra(TYPE)!!
        openFrontCamera=  intent.getBooleanExtra(FRONT_CAMERA_ENABLED,false)

        binding.videoProgress.visibility= View.GONE
        startPreview()
        binding.shutterButton.setOnClickListener{
            when (type){
                VIDEO_CAPTURE ->{
                    captureVideo()
                }
                else ->{
                    takePhoto()
                }
            }
        }
    }

    private fun startPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)


            val useCase = when (type){
                VIDEO_CAPTURE -> VideoCapture
                    .withOutput(Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build())
                    .apply { videoCapture=this }

                else -> ImageCapture
                    .Builder()
                    .build()
                    .apply { imageCapture = this }

            }

            val cameraSelector = if (openFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(useCase) // use only one at a time
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup)
            } catch(exc: Exception) {
                Log.e(TAG, "startCamera: ",exc )
            }

        }, ContextCompat.getMainExecutor(this) )
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),object : ImageCapture.OnImageCapturedCallback() {

            override fun onCaptureSuccess(image: ImageProxy) {
                imageProxyToFile(image)
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }
        })

    }

    private fun imageProxyToFile(image: ImageProxy) {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val resizedSignature: Bitmap = Bitmap.createBitmap(bitmap)
        val quality = if (bitmap.byteCount > 5145728) 50 else 80
        resizedSignature.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        val myFile = MediaPicker.createNewFile(this)
        val fos = FileOutputStream(myFile)
        Log.d(TAG, "imageProxyToFile: ${myFile.path}")
        fos.write(byteArrayOutputStream.toByteArray())
        fos.flush()
        fos.close()
        val intent = Intent()
        intent.putExtra("data",myFile.path)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return


        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }


        val myFile = MediaPicker.createNewFile(this,true)

        val fos = FileOutputOptions.Builder(myFile).build()

        recording = videoCapture.output
            .prepareRecording(this, fos)
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.videoProgress.visibility= View.VISIBLE
                        Toast.makeText(baseContext, "Video recording started", Toast.LENGTH_SHORT).show()

                    }
                    is VideoRecordEvent.Finalize -> {
                        binding.videoProgress.visibility= View.GONE
                        if (!recordEvent.hasError()) {
                            val msg = "Video captured"
                            onVideoCreated(recordEvent.outputResults.outputUri)
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                            Toast.makeText(baseContext, "Video capture ends with error ${recordEvent.error}", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            {
                recording?.stop()
                recording?.close()
                recording = null
            }, duration)
    }


    private fun onVideoCreated(uri: Uri){

        val intent = Intent()
        intent.putExtra("data",uri.path)
        setResult(RESULT_OK, intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}