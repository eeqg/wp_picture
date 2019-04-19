package com.example.wp.wp_picture.loder;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wp.picture.preview.loader.PPViewImageLoader;

/**
 * Created by wp on 2019/4/17.
 */
public class PPViewGlideLoader implements PPViewImageLoader {
	@Override
	public void displayImage(ImageView imageView, String url) {
		Glide.with(imageView.getContext()).load(url).into(imageView);
	}
}
