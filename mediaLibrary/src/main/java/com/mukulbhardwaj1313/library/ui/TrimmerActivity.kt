package com.mukulbhardwaj1313.library.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.mukulbhardwaj1313.library.R
import com.mukulbhardwaj1313.library.ui.seekbar.widgets.CrystalRangeSeekbar
import com.mukulbhardwaj1313.library.ui.seekbar.widgets.CrystalSeekbar
import com.mukulbhardwaj1313.library.utils.TrimVideo
import com.mukulbhardwaj1313.library.utils.TrimVideoOptions
import com.mukulbhardwaj1313.library.utils.TrimmerUtils
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.resize.AspectRatioResizer
import com.otaliastudios.transcoder.resize.FractionResizer
import com.otaliastudios.transcoder.resize.PassThroughResizer
import com.otaliastudios.transcoder.source.TrimDataSource
import com.otaliastudios.transcoder.source.UriDataSource
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.strategy.TrackStrategy
import com.otaliastudios.transcoder.validator.DefaultValidator
import java.io.File
import java.util.concurrent.Future

private const val TAG = "TrimmerActivity"
class TrimmerActivity : AppCompatActivity(), TranscoderListener {

    private lateinit var playerView: StyledPlayerView
    private lateinit var videoPlayer: ExoPlayer
    private lateinit var imagePlayPause: ImageView
    private lateinit var imageViews: Array<ImageView>
    private lateinit var uri: Uri
    private lateinit var txtStartDuration: TextView
    private lateinit var txtTotalDuration: TextView
    private lateinit var txtEndDuration: TextView
    private lateinit var seekbar: CrystalRangeSeekbar
    private lateinit var seekbarController: CrystalSeekbar
    private lateinit var bundle: Bundle
    private lateinit var progressBar: View
    private lateinit var trimVideoOptions: TrimVideoOptions
    private lateinit var mTranscodeOutputFile: File
    private lateinit var donebtn:Button

    private var totalDuration: Long = 0
    private var lastMinValue: Long = 0
    private var lastMaxValue: Long = 0
    private var isVideoEnded = false
    private var currentDuration: Long = 0

    private var trimType = 0
    private var fixedGap: Long = 0
    private var minGap: Long = 0
    private var minFromGap: Long = 0
    private var maxToGap: Long = 0
    private var hidePlayerSeek = false
    private var mTranscodeFuture: Future<Void>? = null
    private var count = 0




//    private lateinit var menuDone: MenuItem
    private var isValidVideo = true
    private val seekHandler: Handler = Handler(Looper.myLooper()!!)

    private var lastClickedTime: Long = 0
    private var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer.currentPosition / 1000
                if (!videoPlayer.playWhenReady) return
                if (currentDuration <= lastMaxValue) seekbarController.setMinStartValue(
                    currentDuration.toInt().toFloat()
                ).apply() else videoPlayer.playWhenReady = false
            } finally {
                seekHandler.postDelayed(this, 1000)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        getIntentData()
        initiateViews()
        initPlayer()
        initTrimData()
        buildMediaSource(uri)
        loadThumbnails()
        setUpSeekBar()
    }

    private fun getIntentData(){
        bundle = intent.extras!!
        trimVideoOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(TrimVideo.TRIM_VIDEO_OPTION,TrimVideoOptions::class.java)!!
        }else{
            @Suppress("DEPRECATION")
            bundle.getParcelable(TrimVideo.TRIM_VIDEO_OPTION)!!
        }

        uri = Uri.parse(bundle.getString(TrimVideo.TRIM_VIDEO_URI))
    }



    private fun initiateViews(){
        playerView = findViewById(R.id.player_view_lib)
        imagePlayPause = findViewById(R.id.image_play_pause)
        seekbar = findViewById(R.id.range_seek_bar)
        txtStartDuration = findViewById(R.id.txt_start_duration)
        txtTotalDuration = findViewById(R.id.txt_total_duration)
        donebtn = findViewById(R.id.done_btn)
        txtEndDuration = findViewById(R.id.txt_end_duration)
        seekbarController = findViewById(R.id.seekbar_controller)
        progressBar = findViewById(R.id.progress_circular)
        progressBar.visibility =View.GONE
        val imageOne = findViewById<ImageView>(R.id.image_one)
        val imageTwo = findViewById<ImageView>(R.id.image_two)
        val imageThree = findViewById<ImageView>(R.id.image_three)
        val imageFour = findViewById<ImageView>(R.id.image_four)
        val imageFive = findViewById<ImageView>(R.id.image_five)
        val imageSix = findViewById<ImageView>(R.id.image_six)
        val imageSeven = findViewById<ImageView>(R.id.image_seven)
        val imageEight = findViewById<ImageView>(R.id.image_eight)
        val imageNine = findViewById<ImageView>(R.id.image_nine)
        val imageTen = findViewById<ImageView>(R.id.image_ten)
        val imageEleven = findViewById<ImageView>(R.id.image_eleven)
        val imageTwelve = findViewById<ImageView>(R.id.image_twelve)
        imageViews = arrayOf(imageOne, imageTwo, imageThree, imageFour, imageFive, imageSix, imageSeven, imageEight,imageNine,imageTen,imageEleven,imageTwelve)

        donebtn.setOnClickListener {
            //preventing multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime >= 800) {
                lastClickedTime = SystemClock.elapsedRealtime()
                if (totalDuration>trimVideoOptions.maxDuration/1000){
                    Toast.makeText(this, "Video must be less than ${trimVideoOptions.maxDuration/1000} seconds", Toast.LENGTH_SHORT).show()
                }else{
                    transcode()
                }
            }
        }
    }

    private fun initPlayer() {
        videoPlayer = ExoPlayer.Builder(this).build()
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.player = videoPlayer
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        videoPlayer.setAudioAttributes(audioAttributes, true)
        totalDuration = TrimmerUtils.getDuration(this@TrimmerActivity, uri)

        imagePlayPause.setOnClickListener { onVideoClicked() }
        playerView.videoSurfaceView!!.setOnClickListener { onVideoClicked() }

    }


    private fun initTrimData() {
        trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType)
        hidePlayerSeek = trimVideoOptions.hideSeekBar
        fixedGap = trimVideoOptions.fixedDuration
        fixedGap = if (fixedGap != 0L) fixedGap else totalDuration
        minGap = trimVideoOptions.minDuration
        minGap = if (minGap != 0L) minGap else totalDuration
        if (trimType == 3) {
            minFromGap = trimVideoOptions.minToMax!![0]
            maxToGap = trimVideoOptions.minToMax!![1]
            minFromGap = if (minFromGap != 0L) minFromGap else totalDuration
            maxToGap = if (maxToGap != 0L) maxToGap else totalDuration
        }

    }

    private fun onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue)
                videoPlayer.playWhenReady = true
                return
            }
            if (currentDuration - lastMaxValue > 0) seekTo(lastMinValue)
            videoPlayer.playWhenReady = !videoPlayer.playWhenReady
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seekTo(sec: Long) {
        videoPlayer.seekTo(sec * 1000)
    }

    private fun buildMediaSource(mUri: Uri?) {
        try {
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mUri!!))
            videoPlayer.addMediaSource(mediaSource)
            videoPlayer.prepare()
            videoPlayer.playWhenReady = true
            videoPlayer.addListener(object : Player.Listener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    imagePlayPause.visibility = if (playWhenReady) View.GONE else View.VISIBLE
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            Log.v(TAG,"onPlayerStateChanged: Video ended.")
                            imagePlayPause.visibility = View.VISIBLE
                            isVideoEnded = true
                        }
                        Player.STATE_READY -> {
                            isVideoEnded = false
                            imagePlayPause.visibility = View.GONE
                            startProgress()
                            Log.v(TAG,"onPlayerStateChanged: Ready to play.")
                        }
                        Player.STATE_BUFFERING -> Log.v(TAG,"onPlayerStateChanged: STATE_BUFFERING.")
                        Player.STATE_IDLE -> Log.v(TAG,"onPlayerStateChanged: STATE_IDLE.")
                        else -> {}
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     *  loading thumbnails
     * */
    private fun loadThumbnails() {
        try {
            val diff = totalDuration / imageViews.size
            var sec = 1
            for (img in imageViews) {
                val interval = diff * sec * 1000000
                val options = RequestOptions().frame(interval)
                Glide.with(this)
                    .load(bundle.getString(TrimVideo.TRIM_VIDEO_URI))
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(img)
                if (sec < totalDuration) sec++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpSeekBar() {
        seekbar.visibility = View.VISIBLE
        txtStartDuration.visibility = View.VISIBLE
        txtTotalDuration.visibility = View.VISIBLE
        txtEndDuration.visibility = View.VISIBLE
        seekbarController.setMaxValue(totalDuration.toFloat()).apply()
        seekbar.setMaxValue(totalDuration.toFloat()).apply()
        seekbar.setMaxStartValue(totalDuration.toFloat()).apply()
        lastMaxValue = when (trimType) {
            1 -> {
                seekbar.setFixGap(fixedGap.toFloat()).apply()
                totalDuration
            }
            2 -> {
                seekbar.setMaxStartValue(minGap.toFloat())
                seekbar.setGap(minGap.toFloat()).apply()
                totalDuration
            }
            3 -> {
                seekbar.setMaxStartValue(maxToGap.toFloat())
                seekbar.setGap(minFromGap.toFloat()).apply()
                maxToGap
            }
            else -> {
                seekbar.setGap(2f).apply()
                totalDuration
            }
        }
        if (hidePlayerSeek) seekbarController.visibility = View.GONE
        seekbar.setOnRangeSeekbarFinalValueListener { _: Number?, _: Number? ->
            if (!hidePlayerSeek) seekbarController.visibility = View.VISIBLE
        }
        seekbar.setOnRangeSeekbarChangeListener { minValue: Number, maxValue: Number ->
            val minVal = minValue as Long
            val maxVal = maxValue as Long
            if (lastMinValue != minVal) {
                seekTo(minValue)
                if (!hidePlayerSeek) seekbarController.visibility = View.INVISIBLE
            }
            lastMinValue = minVal
            lastMaxValue = maxVal
            totalDuration=(maxVal-minVal)
            txtStartDuration.text = TrimmerUtils.formatSeconds(minVal)
            txtEndDuration.text = TrimmerUtils.formatSeconds(maxVal)
            txtTotalDuration.text = "Duration : ${TrimmerUtils.formatSeconds(maxVal-minVal)}"
            if (trimType == 3) setDoneColor(minVal, maxVal)
        }
        seekbarController.setOnSeekbarFinalValueListener { value: Number ->
            val value1 = value as Long
            if (value1 in (lastMinValue + 1) until lastMaxValue) {
                seekTo(value1)
                return@setOnSeekbarFinalValueListener
            }
            if (value1 > lastMaxValue) {
                seekbarController.setMinStartValue(lastMaxValue.toInt().toFloat()).apply()
            }
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue(lastMinValue.toInt().toFloat()).apply()
                if (videoPlayer.playWhenReady)
                    seekTo(lastMinValue)
            }
        }
    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     *
     */
    @Suppress("DEPRECATION")
    private fun setDoneColor(minVal: Long, maxVal: Long) {
        try {
            if (maxVal - minVal <= maxToGap) {
//                menuDone.icon!!.colorFilter = PorterDuffColorFilter(
//                    ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN
//                )
                isValidVideo = true
                donebtn.setBackgroundColor(resources.getColor(R.color.colorWhite))
            } else {
//                menuDone.icon!!.colorFilter = PorterDuffColorFilter(
//                    ContextCompat.getColor(this, R.color.colorWhiteLt), PorterDuff.Mode.SRC_IN
//                )
                isValidVideo = false
                donebtn.setBackgroundColor(resources.getColor(R.color.colorWhiteLt))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.release()
        deleteFile("temp_file")
        stopRepeatingTask()
    }

    fun startProgress() {
        updateSeekbar.run()
    }

    private fun stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar)
    }

    override fun onTranscodeProgress(progress: Double) {
        Log.e(TAG, "onTranscodeProgress: $progress")
    }


    override fun onTranscodeCompleted(successCode: Int) {
        if (successCode == Transcoder.SUCCESS_TRANSCODED) {
            val fileSize = (mTranscodeOutputFile.length() / 1024).toString().toInt()
            if (fileSize > trimVideoOptions.maxVideoSize * 1024) {
                transcode()
            } else {
                progressBar.visibility =View.GONE
                val finalFile = File(uri.path!!)
                mTranscodeOutputFile.copyTo(finalFile, true)
                val intent = Intent()
                intent.putExtra("data", finalFile.path)
                setResult(RESULT_OK, intent)
                if (this::alert.isInitialized && alert.isShowing){
                    alert.cancel()
                    alert.dismiss()
                }
                finish()
            }
            Log.e(TAG, "onTranscodeCompleted: $fileSize")
        }
    }

    override fun onTranscodeCanceled() {}
    override fun onTranscodeFailed(exception: Throwable) { Log.e(TAG, "onTranscodeFailed: ", exception) }

    private fun transcode() {
        videoPlayer.playWhenReady =false
        progressBar.visibility =View.VISIBLE
        val uriToParse = if (count == 0) uri else Uri.parse(
            mTranscodeOutputFile.path
        )
        mTranscodeOutputFile = File(cacheDir, count.toString() + "_abc.mp4")
        val builder = Transcoder.into(mTranscodeOutputFile.absolutePath)
        val dataSource = UriDataSource(this, uriToParse!!)
        val trimDataSource = TrimDataSource(dataSource, lastMinValue, lastMaxValue)
        builder.addDataSource(trimDataSource)
        count++



 val mTranscodeAudioStrategy: TrackStrategy                   //           mTranscodeAudioStrategy = new RemoveTrackStrategy();  // to remove video
        mTranscodeAudioStrategy = DefaultAudioStrategy.builder()
            .channels(trimVideoOptions.channels)
            .sampleRate(trimVideoOptions.sampleRate)
            .build()
        val mTranscodeVideoStrategy: TrackStrategy = DefaultVideoStrategy.Builder()
            .addResizer(if (trimVideoOptions.aspectRatio > 0) AspectRatioResizer(trimVideoOptions.aspectRatio) else PassThroughResizer())
            .addResizer(FractionResizer(trimVideoOptions.fraction))
            .frameRate(trimVideoOptions.frames) // .keyFrameInterval(4F)
            .build()
        mTranscodeFuture = builder.setListener(this)
            .setAudioTrackStrategy(mTranscodeAudioStrategy)
            .setVideoTrackStrategy(mTranscodeVideoStrategy)
            .setVideoRotation(trimVideoOptions.rotation)
            .setValidator(object : DefaultValidator() {})
            .setSpeed(trimVideoOptions.speed)
            .transcode()
    }

    private lateinit var alert:AlertDialog
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (progressBar.visibility == View.VISIBLE) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Do you want to cancel?")
                .setPositiveButton("Yes") { _, _ ->
                    run {
                        cancelTranscoding()
                        super.onBackPressed()
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog!!.cancel() }

            alert = builder.create()
            alert.show()
        }else{
            super.onBackPressed()
        }
    }

    private fun cancelTranscoding() {
        mTranscodeFuture!!.cancel(true)
    }

}