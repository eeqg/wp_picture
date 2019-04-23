package com.example.wp.wp_picture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.BoxingCrop;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.example.wp.wp_picture.loder.BoxingGlideLoader;
import com.example.wp.wp_picture.loder.GlideImageLoader;
import com.example.wp.wp_picture.loder.PPViewGlideLoader;
import com.example.wp.wp_picture.loder.PictureLayoutImageLoader;
import com.wp.picture.picker.BoxingUcrop;
import com.wp.picture.picker.Picker;
import com.wp.picture.picker.PictureLayout;
import com.wp.picture.preview.PPView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	
	private final int CODE_CAMERA = 1;
	private final int CODE_PICK_MULTI = 2;
	
	private final String picUrl1 = "http://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg";
	private final String picUrl2 = "http://img2.woyaogexing.com/2018/01/25/f5d815584c61d376!500x500.jpg";
	private final String picUrl3 = "http://img2.woyaogexing.com/2018/01/25/991349aa8c98c502!500x500.jpg";
	
	private ImageView ivSample;
	private PictureLayout pictureLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initPictureLayout();
		initBoxing();
		initPicturePreview();
		
		initView();
		observePictureLayout();
	}
	
	private void initPictureLayout() {
		PictureLayout.setImageLoader(new PictureLayoutImageLoader());
	}
	
	private void initBoxing() {
		IBoxingMediaLoader loader = new BoxingGlideLoader();
		BoxingMediaLoader.getInstance().init(loader);
		BoxingCrop.getInstance().init(new BoxingUcrop());
	}
	
	private void initPicturePreview() {
		PPView.setImageLoader(new PPViewGlideLoader());
	}
	
	private void initView() {
		ivSample = findViewById(R.id.ivSample);
		GlideImageLoader.getInstance().load(ivSample, picUrl3);
		
		ivSample.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// PPView.build().url(picUrl3).with(MainActivity.this);
				PPView.build().url(picUrl3).show(MainActivity.this);
			}
		});
	}
	
	private void observePictureLayout() {
		pictureLayout = findViewById(R.id.pictureLayout);
		pictureLayout.setOnPictureListener(new PictureLayout.OnPictureListener() {
			@Override
			public void onInsert() {
				// pictureLayout.addPictureUrl("http://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg");
				Picker.pickMulti(MainActivity.this, CODE_PICK_MULTI,
						pictureLayout.getMaxCount() - pictureLayout.size());
			}
			
			@Override
			public void onEdit(int position, Uri pictureUri) {
				pictureLayout.removePictureUri(position);
			}
			
			@Override
			public void onSelect(int position, Uri pictureUri) {
				Log.d("test", "-----position = " + position);
				ArrayList<String> picList = new ArrayList<>();
				for (Uri uri : pictureLayout.getPictureList()) {
					picList.add(uri.toString());
				}
				// PPView.build().urlList(picList).position(position).with(MainActivity.this);
				PPView.build().urlList(picList).position(position).show(MainActivity.this);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CODE_CAMERA:
				break;
			case CODE_PICK_MULTI:
				if (resultCode == RESULT_OK) {
					final ArrayList<BaseMedia> medias = Boxing.getResult(data);
					if (medias != null && medias.size() > 0) {
						for (BaseMedia media : medias) {
							Log.d("", "-----" + media.getPath());
							pictureLayout.addPictureUrl(media.getPath());
						}
					}
				}
				break;
			default:
				break;
		}
	}
}
