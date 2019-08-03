package com.wp.picture.ninegrid;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

public abstract class NineGridViewAdapter<T> implements Serializable {
	
	protected Context context;
	private List<T> imageInfo;
	
	public NineGridViewAdapter(Context context, List<T> imageInfo) {
		this.context = context;
		this.imageInfo = imageInfo;
	}
	
	/**
	 * 如果要实现图片点击的逻辑，重写此方法即可
	 *
	 * @param context      上下文
	 * @param nineGridView 九宫格控件
	 * @param index        当前点击图片的的索引
	 * @param imageInfo    图片地址的数据集合
	 */
	protected void onImageItemClick(Context context, NineGridView nineGridView, int index, List<T> imageInfo) {
	}
	
	protected abstract View onCreateView(Context context);
	
	protected abstract void onBindView(Context context, ViewGroup parent, int position);
	
	public List<T> getImageInfo() {
		return imageInfo;
	}
	
	public void setImageInfoList(List<T> imageInfo) {
		this.imageInfo = imageInfo;
	}
}