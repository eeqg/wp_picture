package com.wp.picture.video

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
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
    private lateinit var llController: View
    private lateinit var ivStartOrPause: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var ivChangeType: ImageView

    private lateinit var mVideoView: SimpleVideoView
    private lateinit var mVideoPlayer: MediaPlayer

    init {
        onInit()
    }

    private fun onInit() {
        mViewRoot = View.inflate(context, R.layout.view_simple_video_controller, this)
        ivThumb = findViewById(R.id.ivThumb)
        ivPlayState = findViewById(R.id.ivPlayState)
        progressBar = findViewById(R.id.progressBar)
        llController = findViewById(R.id.llController)
        ivStartOrPause = findViewById(R.id.ivStartOrPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        ivChangeType = findViewById(R.id.ivChangeType)

        ivPlayState.setOnClickListener { startPlay() }
        ivStartOrPause.setOnClickListener {
            if (mVideoPlayer.isPlaying()) {
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
    }

    override fun onStateChanged(state: Int) {
        when (state) {
            SimpleVideoView.STATE_PREPARING -> {
                ivPlayState.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
            SimpleVideoView.STATE_PREPARED -> {
                mVideoPlayer = mVideoView.getMediaPlayer()
                progressBar.visibility = View.GONE
                ivThumb.visibility = View.GONE
                seekBar.apply {
                    isClickable = true
                    isEnabled = true
                    isFocusable = true
                    max = mVideoView.getMediaPlayer().duration
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
                ivPlayState.setVisibility(View.VISIBLE)
                ivPlayState.visibility = View.VISIBLE
                ivStartOrPause.setImageResource(R.drawable.ic_player_start)
                mHandler.removeMessages(MSG_UPDATE_TIME)
            }
            SimpleVideoView.STATE_BUFFERING_PAUSED -> {
            }
            SimpleVideoView.STATE_COMPLETED -> {
                ivPlayState.visibility = View.GONE
                ivThumb.visibility = View.VISIBLE
                ivStartOrPause.setImageResource(R.drawable.ic_player_start)
                mHandler.removeMessages(MSG_UPDATE_TIME)
            }
            SimpleVideoView.STATE_ERROR -> {
                ivThumb.visibility = View.VISIBLE
                mHandler.removeMessages(MSG_UPDATE_TIME)
                seekBar.progress = 0
            }
        }
    }

    override fun onScreenTypeChanged(type: Int) {
        when (type) {
            SimpleVideoView.TYPE_SCREEN_FULL -> {
                llController.visibility = View.VISIBLE
                ivChangeType.setImageResource(R.drawable.ic_player_shrink)
            }
            SimpleVideoView.TYPE_SCREEN_TINY -> {
                llController.visibility = View.GONE
            }
            SimpleVideoView.TYPE_SCREEN_NORMAL -> {
                llController.visibility = View.VISIBLE
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
        seekBar.progress = mVideoView.getMediaPlayer().currentPosition
        tvTime.text = String.format("%s/%s",
                formatTime(mVideoView.getMediaPlayer().currentPosition), formatTime(mVideoView.getMediaPlayer().duration))
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
        if (mHandler.hasMessages(MSG_UPDATE_CONTROLLER)) {
            mHandler.removeMessages(MSG_UPDATE_CONTROLLER)
        }
        llController.visibility = View.VISIBLE
    }

    private fun hideController() {
        if (mHandler.hasMessages(MSG_UPDATE_CONTROLLER)) {
            mHandler.removeMessages(MSG_UPDATE_CONTROLLER)
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CONTROLLER, 4000)
    }

    fun startPlay() {
        mVideoView.startPlay()
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

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPDATE_TIME -> {
                    updateTime()
                    this.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 200)
                }
                MSG_UPDATE_CONTROLLER -> {
                    llController.visibility = View.GONE
                    this.removeMessages(MSG_UPDATE_CONTROLLER)
                }
            }
        }
    }
}
