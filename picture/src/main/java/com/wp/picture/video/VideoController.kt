package com.wp.picture.video

import android.view.View

/**
 * Created by wp on 2019/11/4.
 */
interface VideoController {

    fun getControllerView(): View?

    fun setVideoView(view: SimpleVideoView)

    fun onStateChanged(state: Int)

    fun onScreenTypeChanged(type: Int)

    fun onPaused()

    fun onDestroyed()
}
