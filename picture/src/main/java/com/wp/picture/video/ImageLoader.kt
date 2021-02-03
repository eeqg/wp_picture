package com.wp.picture.video

import android.widget.ImageView

/**
 * 图片加载器接口
 */
interface ImageLoader {
    fun displayThumb(imageView: ImageView, url: String)

    fun displayThumbFame(imageView: ImageView, url: String)
}
