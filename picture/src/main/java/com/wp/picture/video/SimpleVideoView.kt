package com.wp.picture.video

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.wp.picture.common.ext.dp2px
import com.wp.picture.utils.CommUtil
import java.io.IOException

/**
 * Created by wp on 2019/11/4.
 */
class SimpleVideoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs), TextureView.SurfaceTextureListener {
    private val TAG = "SimpleVideoView"
    private val printLogFlag = false

    private var mInitWidth = 0
    private var mInitHeight = 0
    private lateinit var mVideoInfo: VideoInfo
    private var mImageLoader: ImageLoader? = null

    private lateinit var mController: VideoController
    private lateinit var mContainer: FrameLayout

    private lateinit var mTextureView: TextureView;
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mActivity: Activity? = null

    private var mTinyWidth = 0
    private var mTinyWindowRightMargin = context.dp2px(12f)
    private var mTinyWindowTopMargin = mTinyWindowRightMargin
    private var mTinyWindowAdaptive = false
    private var mCurrentState = STATE_IDLE
    private var mScreenType = TYPE_SCREEN_NORMAL

    companion object {
        val STATE_ERROR = -1          // 播放错误
        val STATE_IDLE = 0            // 播放未开始
        val STATE_PREPARING = 1       // 播放准备中
        val STATE_PREPARED = 2        // 播放准备就绪
        val STATE_PLAYING = 3         // 正在播放
        val STATE_PAUSED = 4          // 暂停播放
        val STATE_STOPPED = 5          // 暂停停止
        val STATE_BUFFERING_PLAYING = 6 // 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
        val STATE_BUFFERING_PAUSED = 7 // 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停)
        val STATE_COMPLETED = 8       // 播放完成

        val TYPE_SCREEN_NORMAL = 0
        val TYPE_SCREEN_FULL = 1
        val TYPE_SCREEN_TINY = 2
    }

    init {
        onInit()
    }

    private fun onInit() {
        setBackgroundColor(Color.BLACK)
        mContainer = FrameLayout(context);
        mContainer.setBackgroundColor(Color.BLACK)
        mContainer.isClickable = true
        addView(mContainer, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        mActivity = getActivity(context)
        mTinyWidth = (CommUtil.getScreenWidth(context) * 0.38).toInt()
    }

    fun setImageLoader(loader: ImageLoader): SimpleVideoView {
        this.mImageLoader = loader
        return this
    }

    fun setTinyWindowAdaptive(value: Boolean): SimpleVideoView {
        mTinyWindowAdaptive = value
        return this
    }

    fun setTinyWindowTopMargin(value: Int): SimpleVideoView {
        this.mTinyWindowTopMargin = context.dp2px(value.toFloat())
        return this
    }

    fun setTinyWindowRightMargin(value: Int): SimpleVideoView {
        this.mTinyWindowRightMargin = context.dp2px(value.toFloat())
        return this
    }

    fun setup(videoInfo: VideoInfo) {
        setup(videoInfo, SimpleVideoController(context))
    }

    fun setup(videoInfo: VideoInfo, controller: VideoController) {
        mVideoInfo = videoInfo
        mController = controller
        mController.setVideoView(this)
        mController.setVideoInfo(mVideoInfo)

        mTextureView = TextureView(context).apply {
            surfaceTextureListener = this@SimpleVideoView
        }
        mContainer.addView(mTextureView, 0,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        mContainer.addView(mController.getControllerView(),
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    fun getVideoState(): Int {
        return mCurrentState
    }

    fun getScreenType(): Int {
        return mScreenType
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mInitWidth == 0) mInitWidth = w
        if (mInitHeight == 0) mInitHeight = h
        printLog("-----onSizeChanged()--mInitWidth = $mInitWidth, mInitHeight = $mInitHeight")
    }

    /**
     * 初始化好SurfaceTexture后调用
     */
    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        printLog("-----onSurfaceTextureAvailable()")
        // start(new Surface(surface));
        //切换到全屏后重新调用onSurfaceTextureAvailable
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture
            initMediaPlayer(Surface(mSurfaceTexture))
        } else {
            mTextureView.surfaceTexture = mSurfaceTexture
        }
    }

    /**
     * 尺寸改变后调用
     */
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    /**
     * SurfaceTexture即将被销毁时调用
     */
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return mSurfaceTexture == null
    }

    /**
     * 通过SurfaceTexture.updateteximage()更新SurfaceTexture时调用
     */
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    private fun fixVideoViewAspect(parentWidth: Int, parentHeight: Int) {
        if (mMediaPlayer == null) {
            return
        }
        val textureViewWidth: Int
        val textureViewHeight: Int
        printLog("-----fixVideoViewAspect()--getScreenType = ${this.getScreenType()}")
        printLog("-----parentHeight = $parentHeight , parentWidth = $parentWidth")
        val viewAspectRatio = parentWidth * 1.0 / parentHeight
        printLog("-----viewAspectRatio : $viewAspectRatio")
        printLog("-----videoHeight = ${mMediaPlayer!!.videoHeight}, videoWidth = ${mMediaPlayer!!.videoWidth}")
        val videoAspectRatio = mMediaPlayer!!.videoWidth * 1.0 / mMediaPlayer!!.videoHeight
        printLog("-----videoAspectRatio : $videoAspectRatio")
        if (viewAspectRatio > videoAspectRatio) {
            textureViewHeight = parentHeight
            textureViewWidth = (textureViewHeight * videoAspectRatio).toInt()
        } else {
            textureViewWidth = parentWidth
            textureViewHeight = (textureViewWidth * (1.0 / videoAspectRatio)).toInt()
        }
        printLog("-----textureViewWidth = $textureViewWidth, textureViewHeight = $textureViewHeight")

        val layoutParams = mTextureView.layoutParams as LayoutParams
        layoutParams.width = textureViewWidth
        layoutParams.height = textureViewHeight
        layoutParams.gravity = Gravity.CENTER
        mTextureView.layoutParams = layoutParams
    }

    private fun initMediaPlayer(surface: Surface) {
        try {
            mMediaPlayer = MediaPlayer().apply {
                mCurrentState = STATE_IDLE
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(context, Uri.parse(mVideoInfo.videoUrl))
                setOnPreparedListener {
                    setState(STATE_PREPARED)
                    printLog("-----initMediaPlayer()--mScreenType = $mScreenType")
                    setScreenType(mScreenType)
                }
                setOnCompletionListener { setState(STATE_COMPLETED) }
                setOnBufferingUpdateListener { mp, percent ->
                    //printLog("-----onBufferingUpdate--percent = $percent")
                    mController.onBufferingChanged(percent / 100.0)
                }
                setOnErrorListener { mp, what, extra ->
                    printLog("-----onError()--what: $what/n--extra: $extra")
                    setState(STATE_ERROR)
                    false
                }
                setOnInfoListener(infoListener)
                setScreenOnWhilePlaying(true)
                setSurface(surface)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val infoListener = MediaPlayer.OnInfoListener { mp, what, extra ->
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            // 播放器渲染第一帧
            printLog("onInfo-----MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING")
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            // MediaPlayer暂时不播放，以缓冲更多的数据
            printLog("onInfo-----MEDIA_INFO_BUFFERING_START")
//            progressBar.setVisibility(View.VISIBLE)
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            // 填充缓冲区后，MediaPlayer恢复播放/暂停
            printLog("onInfo-----MEDIA_INFO_BUFFERING_END")
//            progressBar.setVisibility(View.GONE)
        } else {
            printLog("onInfo-----what = $what")
        }
        true
    }

    private fun setState(state: Int) {
        mCurrentState = state
        printLog("-----setState() : mCurrentState = $mCurrentState")
        mController.onStateChanged(mCurrentState)
    }

    private fun setScreenType(type: Int) {
        mScreenType = type
        printLog("-----setScreenType : $mScreenType")
        mController.onScreenTypeChanged(mScreenType)

        when (mScreenType) {
            TYPE_SCREEN_NORMAL ->
                fixVideoViewAspect(mInitWidth, mInitHeight)
            TYPE_SCREEN_TINY ->
                fixVideoViewAspect(mTinyWidth, mTinyWidth)
            TYPE_SCREEN_FULL ->
                fixVideoViewAspect(CommUtil.getScreenHeight(context), CommUtil.getScreenWidth(context))//横屏了宽高取反
        }
    }

    fun getImageLoader(): ImageLoader? {
        return mImageLoader
    }

    fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    fun isFullscreenModel(): Boolean {
        return mScreenType == TYPE_SCREEN_FULL
    }

    fun isTinyModel(): Boolean {
        return mScreenType == TYPE_SCREEN_TINY
    }

    fun isNormalModel(): Boolean {
        return mScreenType == TYPE_SCREEN_NORMAL
    }

    fun startPlay() {
        printLog("------>>>startPlay--$mMediaPlayer ---mCurrentState : $mCurrentState")
        if (mMediaPlayer != null) {
            when (mCurrentState) {
                STATE_IDLE -> {
                    mMediaPlayer!!.prepareAsync()
                    setState(STATE_PREPARING)
                }
                STATE_PREPARED, STATE_PAUSED -> {
                    mMediaPlayer!!.start()
                    setState(STATE_PLAYING)
                }
                STATE_STOPPED -> {
                    mMediaPlayer!!.prepareAsync()
                    setState(STATE_PREPARING)
                }
                STATE_COMPLETED -> {
                    mMediaPlayer!!.start()
                    setState(STATE_PLAYING)
                }
                STATE_ERROR -> {
                    printLog("error")
                    initMediaPlayer(Surface(mSurfaceTexture))
                }
            }
        } else {
            printLog("error: MediaPlayer not init.")
        }
    }

    fun pausePlay() {
        printLog("-----pausePlay()")
        if (mMediaPlayer != null) {
            mMediaPlayer!!.pause()
            setState(STATE_PAUSED)
        }
    }

    fun stopPlay() {
        printLog("-----stopPlay()")
        if (mMediaPlayer != null) {
            mMediaPlayer?.stop()
            setState(STATE_STOPPED)
        }
    }

    fun enterFullScreen() {
        if (mScreenType == TYPE_SCREEN_FULL) {
            return
        }
        post {
            var contentRoot: ViewGroup? = null
            mActivity?.apply {
                if (mActivity is AppCompatActivity) {
                    val ab = (mActivity as AppCompatActivity).supportActionBar
                    if (ab != null) {
                        ab.setShowHideAnimationEnabled(false)
                        ab.hide()
                    }
                }
                window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                contentRoot = mActivity?.findViewById(android.R.id.content)
            }

            this.removeView(mContainer)
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            contentRoot?.addView(mContainer, layoutParams)

            setScreenType(TYPE_SCREEN_FULL)
        }
    }

    fun enterTinyScreen() {
        if (mScreenType == TYPE_SCREEN_TINY) {
            return
        }
        post {
            var contentRoot: ViewGroup? = null
            mActivity?.apply {
                contentRoot = mActivity?.findViewById(android.R.id.content)
            }
            this.removeView(mContainer)

            var width: Int = mTinyWidth
            var height: Int = mTinyWidth
            if (mTinyWindowAdaptive) {
                //小窗口大小按视频大小@{
                val videoAspectRatio = mMediaPlayer!!.videoWidth * 1.0 / mMediaPlayer!!.videoHeight
                if (videoAspectRatio > 1) {
                    width = mTinyWidth
                    height = (mTinyWidth * (1.0 / videoAspectRatio)).toInt()
                } else {
                    width = mTinyWidth
                    height = mTinyWidth
                }
            }
            val layoutParams = LayoutParams(width, height)
            layoutParams.gravity = Gravity.END or Gravity.TOP
            layoutParams.rightMargin = mTinyWindowRightMargin
            layoutParams.topMargin = mTinyWindowTopMargin
            contentRoot?.addView(mContainer, layoutParams)

            setScreenType(TYPE_SCREEN_TINY)
        }
    }

    fun enterNormalScreen() {
        if (mScreenType == TYPE_SCREEN_NORMAL) {
            return
        }
        post {
            mActivity?.apply {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val contentRoot: ViewGroup? = this.findViewById(android.R.id.content)
                val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                contentRoot?.removeView(mContainer)
                addView(mContainer, layoutParams)

                setScreenType(TYPE_SCREEN_NORMAL)
            }
        }
    }

    fun turnOffVolume() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.setVolume(0f, 0f)
        }
    }

    fun turnOnVolume() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.setVolume(1f, 1f)
        }
    }

    fun seekTo(progress: Int) {
        if (mCurrentState != STATE_IDLE && mCurrentState != STATE_ERROR) {
            mMediaPlayer?.seekTo(progress)
        }
    }

    fun resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            setState(STATE_IDLE)
        }
    }

    fun releasePlayer() {
//        if (mAudioManager != null) {
//            mAudioManager.abandonAudioFocus(null)
//            mAudioManager = null
//        }
        printLog("-----releasePlayer()")
        mMediaPlayer?.release()
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.release()
            mSurfaceTexture = null
        }
    }

    fun onPause() {
        printLog("-----onPause()")
        if (isPlaying()) {
            pausePlay()
        }
    }

    fun onStop() {
        printLog("-----onStop()")
        if (isPlaying()) {
            stopPlay()
        }
    }

    fun onDestroy() {
        printLog("-----onDestroy()")
        if (isPlaying()) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
        }
        mController.onDestroyed()
    }

    fun getVideoController(): VideoController {
        return this.mController
    }

    override fun onDetachedFromWindow() {
        onDestroy()
        super.onDetachedFromWindow()
    }

    private fun printLog(msg: String) {
        if (printLogFlag) Log.d(TAG, msg)
    }

    private fun getActivity(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getActivity(context.baseContext)
        }
        return null
    }

    data class VideoInfo(
            var videoUrl: String,
            var videoThumb: String,
            var title: String
    )
}
