package com.example.wp.wp_picture.ninegrid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wp.wp_picture.R;
import com.example.wp.wp_picture.loder.GlideImageLoader;
import com.wp.picture.ninegrid.NineGridView;
import com.wp.picture.ninegrid.NineGridViewAdapter;
import com.wp.picture.preview.PPView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wp on 2019/8/3.
 */
public class NineGridImageAdapter extends NineGridViewAdapter<ImageInfoBean> {
	private AppCompatActivity activity;
	private ArrayList<String> imgList;
	
	public NineGridImageAdapter(Context context, List<ImageInfoBean> imageInfo) {
		super(context, imageInfo);
		activity = (AppCompatActivity) context;
		imgList = new ArrayList<>();
		for (ImageInfoBean infoBean : imageInfo) {
			imgList.add(infoBean.imgUrl);
		}
	}
	
	@Override
	protected View onCreateView(Context context) {
		return View.inflate(context, R.layout.item_nine_grid, null);
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	protected void onBindView(NineGridView parent, ViewGroup itemView, int position) {
		Log.d("picture", "-----onBindView()--" + position);
		ImageView ivPicture = itemView.findViewById(R.id.ivPicture);
		ImageInfoBean imageInfoBean = getImageInfo().get(position);
		Log.d("picture", "-----onBindView()--" + imageInfoBean.imgUrl);
		GlideImageLoader.getInstance().load(ivPicture, imageInfoBean.imgUrl);
		
		boolean hasMore = position == 8 && getImageInfo().size() > 9;
		View maskerView = itemView.findViewById(R.id.viewMask);
		maskerView.setVisibility(hasMore || imageInfoBean.isVideo ? View.VISIBLE : View.GONE);
		TextView tvMoreNum = itemView.findViewById(R.id.tvMoreNum);
		tvMoreNum.setVisibility(hasMore ? View.VISIBLE : View.GONE);
		tvMoreNum.setText(String.format("+ %d", getImageInfo().size() - parent.getMaxSize()));
		View ivPlay = itemView.findViewById(R.id.ivPlay);
		ivPlay.setVisibility(imageInfoBean.isVideo ? View.VISIBLE : View.GONE);
	}
	
	@Override
	protected void onImageItemClick(Context context, NineGridView nineGridView, int index, List imageInfo) {
		PPView.build().urlList(imgList).disableTransform(false).position(index).show(activity);
	}
}
