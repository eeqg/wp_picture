package com.wp.picture.preview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class PicturePreviewActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// //设置全屏
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// 		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//Translucent
		// setTheme(android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		
		PicturePreview picturePreview = new PicturePreview(this);
		setContentView(picturePreview);
		picturePreview.setConfig(PPView.getConfig());
		picturePreview.setTransformOutListener(new PicturePreview.OnTransformOutListener() {
			@Override
			public void onTransformOut() {
				finish();
			}
		});
	}
}
