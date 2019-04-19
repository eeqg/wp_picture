package com.wp.picture.preview;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.wp.picture.preview.loader.PPViewImageLoader;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/9 0009.
 */

public class PPView {
	
	
	private static PPViewImageLoader imageLoader;
	
	private static PPView config;
	
	public int pageLayout = -1;
	public ArrayList<String> pictureUrlList;
	public int position;
	public PicturePreview.OnLongClickListener longClickListener;
	
	public PPView() {
	}
	
	public static void setImageLoader(PPViewImageLoader loader) {
		imageLoader = loader;
	}
	
	public static PPViewImageLoader getImageLoader() {
		return imageLoader;
	}
	
	public static Builder build() {
		return new Builder();
	}
	
	public static PPView getConfig() {
		return config;
	}
	
	public static void setConfig(PPView config) {
		PPView.config = config;
	}
	
	public static class Builder {
		private int pageLayout = -1;
		private ArrayList<String> pictureUrlList;
		private int position;
		private PicturePreview.OnLongClickListener longClickListener;
		
		public Builder url(String url) {
			this.pictureUrlList = new ArrayList<>();
			this.pictureUrlList.add(url);
			return this;
		}
		
		public Builder urlList(ArrayList<String> pictureUrlList) {
			this.pictureUrlList = pictureUrlList;
			return this;
		}
		
		public Builder position(int position) {
			this.position = position;
			return this;
		}
		
		public Builder pageLayout(int pageLayout) {
			this.pageLayout = pageLayout;
			return this;
		}
		
		public Builder longClickListener(PicturePreview.OnLongClickListener longClickListener) {
			this.longClickListener = longClickListener;
			return this;
		}
		
		private void with(Fragment fragment) {
			Intent intent = new Intent(fragment.getActivity(), PicturePreviewActivity.class);
			PPView.setConfig(this.buildViewer());
			fragment.startActivity(intent);
		}
		
		public void with(Activity activity) {
			Intent intent = new Intent(activity, PicturePreviewActivity.class);
			PPView.setConfig(this.buildViewer());
			activity.startActivity(intent);
		}
		
		public void with(Activity activity, int requestCode) {
			Intent intent = new Intent(activity, PicturePreviewActivity.class);
			PPView.setConfig(this.buildViewer());
			activity.startActivityForResult(intent, requestCode);
		}
		
		private PPView buildViewer() {
			PPView ppView = new PPView();
			ppView.pictureUrlList = this.pictureUrlList;
			ppView.position = this.position;
			ppView.pageLayout = this.pageLayout;
			ppView.longClickListener = this.longClickListener;
			return ppView;
		}
	}
}
