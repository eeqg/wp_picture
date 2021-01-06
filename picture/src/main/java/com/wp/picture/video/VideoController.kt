package com.wp.picture.video

import android.view.View

/**
 * Created by wp on 2019/11/4.
 */
interface VideoController {

    fun getControllerView(): View?

    fun setVideoView(view: SimpleVideoView)

    fun setVideoInfo(videoInfo: SimpleVideoView.VideoInfo)

    fun onStateChanged(state: Int)

    fun onBufferingChanged(percent: Double)

    fun onScreenTypeChanged(type: Int)

    fun onDestroyed()
}
