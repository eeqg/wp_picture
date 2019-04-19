/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.wp.wp_picture.loder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.wp.wp_picture.R;

/**
 * use https://github.com/bumptech/glide as media loader.
 * can <b>not</b> be used in Production Environment.
 *
 * @author ChenSL
 */
public class BoxingGlideLoader implements IBoxingMediaLoader {
	
	@Override
	public void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height) {
		String path = "file://" + absPath;
		try {
			Log.d("-----", "path = " + path);
			// https://github.com/bumptech/glide/issues/1531
			RequestOptions options = new RequestOptions()
					.placeholder(R.mipmap.ic_placeholder)
					.centerCrop().override(width, height);
			Glide.with(img.getContext()).load(absPath).apply(options).into(img);
		} catch (IllegalArgumentException ignore) {
		}
		
	}
	
	@Override
	public void displayRaw(@NonNull final ImageView img, @NonNull String absPath, int width, int height, final IBoxingCallback callback) {
		String path = "file://" + absPath;
		RequestBuilder<Bitmap> requestBuilder = Glide.with(img.getContext()).asBitmap().load(path);
		if (width > 0 && height > 0) {
			RequestOptions options = new RequestOptions().override(width, height);
			requestBuilder.apply(options);
		}
		requestBuilder.listener(new RequestListener<Bitmap>() {
			@Override
			public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
				if (callback != null) {
					callback.onFail(e);
					return true;
				}
				return false;
			}
			
			@Override
			public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
				if (resource != null && callback != null) {
					img.setImageBitmap(resource);
					callback.onSuccess();
					return true;
				}
				return false;
			}
		}).into(img);
	}
}
