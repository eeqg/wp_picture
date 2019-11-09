package com.wp.picture.preview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.wp.picture.R;

/**
 * Created by wp on 2019/4/23.
 */
public class PicturePreviewDialog extends DialogFragment {
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (getDialog().getWindow() != null) {
			getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置背景为透明
		}
		
		PicturePreview picturePreview = new PicturePreview(getContext());
		picturePreview.setConfig(PPView.getConfig());
		picturePreview.setTransformOutListener(new PicturePreview.OnTransformOutListener() {
			@Override
			public void onTransformOut() {
				Log.d("-----", "dialog -- onTransformOut()");
				getDialog().dismiss();
			}
		});
		return picturePreview;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		Dialog dialog = getDialog();
		if (dialog != null) {
			Window window = dialog.getWindow();
			if (window != null) {
				//全屏
				// window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				// 		WindowManager.LayoutParams.FLAG_FULLSCREEN);
				int width = ViewGroup.LayoutParams.MATCH_PARENT;
				int height = ViewGroup.LayoutParams.MATCH_PARENT;
				window.setLayout(width, height);
				
				WindowManager.LayoutParams windowParams = window.getAttributes();
				windowParams.dimAmount = 0.0f;//设置背景透明
				windowParams.windowAnimations = R.style.AnimationFade;//animation
				window.setAttributes(windowParams);
			}
		}
	}
}
