package com.wp.picture.video

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.wp.picture.R
import java.util.*

/**
 * Created by wp on 2019/11/4.
 */
class SimpleVideoController(context: Context) : FrameLayout(context), VideoController {

    private val MSG_UPDATE_TIME = 1
    private val MSG_UPDATE_CONTROLLER = 2

    private lateinit var mViewRoot: View
    private lateinit var ivThumb: ImageView
    private lateinit var ivPlayState: ImageView
    private lateinit var progressBar: View
    private lateinit var ivStartOrPause: ImageView
    private lateinit var llTitleBar: View
    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ivClose: ImageView
    private lateinit var llBottomBar: View
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var ivChangeType: ImageView
    private lateinit var ivVolumeOff: ImageView

    private lateinit var mVideoView: SimpleVideoView
    private var mMediaPlayer: MediaPlayer? = null

    private var volumeOn = true

    init {
        onInit()
    }

    private fun onInit() {
        mViewRoot = View.inflate(context, R.layout.view_simple_video_controller, this)
        ivThumb = findViewById(R.id.ivThumb)
        ivPlayState = findViewById(R.id.ivPlayState)
        progressBar = findViewById(R.id.progressBar)
        ivStartOrPause = findViewById(R.id.ivStartOrPause)
        llTitleBar = findViewById(R.id.llTitleBar)
        ivBack = findViewById(R.id.ivBack);
        ivClose = findViewById(R.id.ivClose);
        tvTitle = findViewById(R.id.tvTitle);
        llBottomBar = findViewById(R.id.llBottomBar)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        ivChangeType = findViewById(R.id.ivChangeType)
        ivVolumeOff = findViewById(R.id.ivVolumeOff)

        llTitleBar.visibility = View.GONE
        llBottomBar.visibility = View.GONE
        ivVolumeOff.visibility = View.GONE
        ivBack.setOnClickListener { enterNormalScreen() }
        ivClose.setOnClickListener {
            enterNormalScreen()
            pausePlay()
        }
        ivPlayState.setOnClickListener { startPlay() }
        ivStartOrPause.setOnClickListener {
            if (mVideoView.getVideoState() == SimpleVideoView.STATE_IDLE) {
                startPlay()
                return@setOnClickListener
            }
            if (mVideoView.isPlaying()) {
                pausePlay()
            } else {
                startPlay()
            }
        }
        ivChangeType.setOnClickListener {
            if (mVideoView.isNormalModel()) {
                enterFullScreen()
            } else {
                enterNormalScreen()
            }
        }
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekTo(seekBar.progress)
            }
        })
        ivVolumeOff.setOnClickListener {
            if (volumeOn) {
                mVideoView.turnOffVolume()
                ivVolumeOff.setImageResource(R.drawable.ic_volume_off_white_24dp)
            } else {
                mVideoView.turnOnVolume()
                ivVolumeOff.setImageResource(R.drawable.ic_volume_up_white_24dp)
            }
            volumeOn = !volumeOn
        }
    }

    override fun setVideoView(view: SimpleVideoView) {
        mVideoView = view;
    }

    override fun getControllerView(): View? {
        return mViewRoot
    }

    override fun setVideoInfo(videoInfo: SimpleVideoView.VideoInfo) {
        if (videoInfo.videoThumb.isNotEmpty()) {
            mVideoView.getImageLoader()?.displayThumb(ivThumb, videoInfo.videoThumb)
        }
        tvTitle.text = videoInfo.title
    }

    override fun onStateChanged(state: Int) {
        when (state) {
            SimpleVideoView.STATE_PREPARING -> {
                ivPlayState.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
            SimpleVideoView.STATE_PREPARED -> {
                mMediaPlayer = mVideoView.getMediaPlayer()
                progressBar.visibility = View.GONE
                ivThumb.visibility = View.GONE
                seekBar.apply {
                    isClickable = true
                    isEnabled = true
                    isFocusable = true
                    max = mMediaPlayer?.duration ?: 0
                    progress = 0
                }
                showController()
                startPlay()
            }
            SimpleVideoView.STATE_PLAYING -> {
                ivThumb.visibility = View.GONE
                progressBar.visibility = View.GONE
                ivPlayState.visibility = View.GONE
                ivPlayState.visibility = View.GONE
                ivStartOrPause.setImageResource(R.drawable.ic_player_pause)
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 200)
            }
            SimpleVideoView.STATE_PAUSED -> {
                ivPlayState.visibility = View.VISIBLE
                ivPlayState.visibility = View.VISIBLE
                ivStartOrPause.setImageResource(R.drawable.ic_player_start)
                mHandler.removeMessages(MSG_UPDATE_TIME)
            }
            SimpleVideoView.STATE_BUFFERING_PAUSED -> {
            }
            SimpleVideoView.STATE_STOPPED -> {
                ivPlayState.visibility = View.VISIBLE
                ivThumb.visibility = View.VISIBLE
                ivStartOrPause.setImageResource(R.drawable.ic_player_start)
                mHandler.removeMessages(MSG_UPDATE_TIME)
                if (mVideoView.isTinyModel()) {
                    enterNormalScreen()
                }
                seekBar.progress = 0
            }
            SimpleVideoView.STATE_COMPLETED -> {
                ivPlayState.visibility = View.VISIBLE
                ivThumb.visibility = View.VISIBLE
                ivStartOrPause.setImageResource(R.drawable.ic_player_start)
                mHandler.removeMessages(MSG_UPDATE_TIME)
                if (mVideoView.isTinyModel()) {
                    enterNormalScreen()
                }
            }
            SimpleVideoView.STATE_ERROR -> {
                ivThumb.visibility = View.VISIBLE
                mHandler.removeMessages(MSG_UPDATE_TIME)
                seekBar.progress = 0
            }
        }
    }

    override fun onBufferingChanged(percent: Double) {
        seekBar.secondaryProgress = (percent * seekBar.max).toInt()
    }

    override fun onScreenTypeChanged(type: Int) {
        when (type) {
            SimpleVideoView.TYPE_SCREEN_FULL -> {
                llTitleBar.visibility = View.VISIBLE
                ivBack.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
                ivClose.visibility = View.GONE
                llBottomBar.visibility = View.VISIBLE
                ivVolumeOff.visibility = View.VISIBLE
                ivChangeType.setImageResource(R.drawable.ic_player_shrink)
            }
            SimpleVideoView.TYPE_SCREEN_TINY -> {
                llTitleBar.visibility = View.VISIBLE
                ivBack.visibility = View.GONE
                tvTitle.visibility = View.INVISIBLE
                ivClose.visibility = View.VISIBLE
                llBottomBar.visibility = View.GONE
                ivVolumeOff.visibility = View.GONE
            }
            SimpleVideoView.TYPE_SCREEN_NORMAL -> {
                llTitleBar.visibility = View.VISIBLE
                ivBack.visibility = View.GONE
                tvTitle.visibility = View.VISIBLE
                ivClose.visibility = View.GONE
                llBottomBar.visibility = View.VISIBLE
                ivVolumeOff.visibility = View.VISIBLE
                ivChangeType.setImageResource(R.drawable.ic_player_enlarge)
            }
        }
    }

    override fun onPaused() {
        mHandler.removeMessages(MSG_UPDATE_TIME)
    }

    override fun onDestroyed() {
        mHandler.removeMessages(MSG_UPDATE_TIME)
    }

    private fun updateTime() {
        if (mMediaPlayer != null) {
            seekBar.progress = mMediaPlayer!!.currentPosition
            tvTime.text = String.format("%s/%s",
                    formatTime(mMediaPlayer!!.currentPosition), formatTime(mMediaPlayer!!.duration))
        }
    }

    private fun formatTime(ms: Int): String {
        val minuteP = 60 * 1000
        var minute = 0
        var second = 0
        minute = ms / minuteP
        second = (ms - minute * minuteP) / 1000
        return String.format(Locale.CHINA, "%02d:%02d", minute, second)
    }

    private fun showController() {
//        Log.d("-----", "showController")
        if (mVideoView.isTinyModel()) {
            return
        }
        if (mHandler.hasMessages(MSG_UPDATE_CONTROLLER)) {
            mHandler.removeMessages(MSG_UPDATE_CONTROLLER)
        }
        llTitleBar.visibility = View.VISIBLE
        llBottomBar.visibility = View.VISIBLE
        ivVolumeOff.visibility = View.VISIBLE
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CONTROLLER, 5000)
    }

    private fun hideController() {
//        Log.d("-----", "hideController")
        if (mHandler.hasMessages(MSG_UPDATE_CONTROLLER)) {
            mHandler.removeMessages(MSG_UPDATE_CONTROLLER)
        }
        if (!mVideoView.isTinyModel()) {
            llTitleBar.visibility = View.GONE
        }
        llBottomBar.visibility = View.GONE
        ivVolumeOff.visibility = View.GONE
    }

    fun startPlay() {
        mVideoView.startPlay()
        showController()
    }

    fun pausePlay() {
        mVideoView.pausePlay()
    }

    fun releasePlayer() {
        mVideoView.releasePlayer()
    }

    fun enterFullScreen() {
        mVideoView.enterFullScreen()
    }

    fun enterTinyScreen() {
        mVideoView.enterTinyScreen()
    }

    fun enterNormalScreen() {
        mVideoView.enterNormalScreen()
    }

    fun seekTo(progress: Int) {
        mVideoView.seekTo(progress)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        Log.d("-----", "dispatchTouchEvent--ev.action: " + ev.action)
        showController()
        return super.dispatchTouchEvent(ev)
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPDATE_TIME -> {
                    updateTime()
                    this.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 200)
                }
                MSG_UPDATE_CONTROLLER -> {
                    hideController()
                }
            }
        }
    }
}
