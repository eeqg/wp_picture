package com.wp.picture.preview.loader;

import android.widget.ImageView;

/**
 * 图片加载器接口，实现 PPViewImageLoader 可扩展自己的图片加载器
 */
public interface PPViewImageLoader {
	void displayImage(ImageView imageView, String url);
}
