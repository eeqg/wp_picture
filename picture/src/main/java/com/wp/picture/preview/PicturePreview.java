package com.wp.picture.preview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wp.picture.R;
import com.wp.picture.common.ColorUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/6 0006.
 */

public class PicturePreview extends FrameLayout {
	private View rootView;
	private TextView tvIndicator;
	private ViewPager viewPager;
	
	private PPView config;
	private ArrayList<String> urlList;
	private PpvPagerAdapter ppvPagerAdapter;
	private @IdRes
	int pageLayout;
	private OnTransformOutListener transformOutListener;
	
	public PicturePreview(@NonNull Context context) {
		this(context, null);
	}
	
	public PicturePreview(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PicturePreview(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		initView(context);
	}
	
	private void initView(Context context) {
		rootView = LayoutInflater.from(context).inflate(R.layout.layout_ppv, this);
		tvIndicator = (TextView) findViewById(R.id.tvIndicatorPPV);
		viewPager = (ViewPager) findViewById(R.id.viewPagerPPV);
		
		ppvPagerAdapter = new PpvPagerAdapter(context);
		viewPager.setAdapter(ppvPagerAdapter);
		
		// viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
		// 	@Override
		// 	public void transformPage(View page, float position) {
		// 		int pageWidth = page.getWidth();
		// 		int pageHeight = page.getHeight();
		// 		if (position < -1.0F) {
		// 			page.setAlpha(0.0F);
		// 		} else if (position <= 1.0F) {
		// 			float scaleFactor = Math.max(0.85F, 1.0F - Math.abs(position));
		// 			float verticalMargin = (float) pageHeight * (1.0F - scaleFactor) / 2.0F;
		// 			float horizontalMargin = (float) pageWidth * (1.0F - scaleFactor) / 2.0F;
		// 			if (position < 0.0F) {
		// 				page.setTranslationX(horizontalMargin - verticalMargin / 2.0F);
		// 			} else {
		// 				page.setTranslationY(-horizontalMargin + verticalMargin / 2.0F);
		// 			}
		//
		// 			page.setScaleX(scaleFactor);
		// 			page.setScaleY(scaleFactor);
		// 			page.setAlpha(0.5F + (scaleFactor - 0.85F) / 0.14999998F * 0.5F);
		// 		} else {
		// 			page.setAlpha(0.0F);
		// 		}
		// 	}
		// });
		
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageSelected(int position) {
				tvIndicator.setText(String.format("%s/%s", position + 1, urlList.size()));
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}
	
	/**
	 * 配置
	 *
	 * @param viewer 配置信息
	 */
	public void setConfig(PPView viewer) {
		this.config = viewer;
		setImageUrls(viewer.pictureUrlList);
		setCurrentPage(viewer.position);
	}
	
	/**
	 * set urls.
	 *
	 * @param urls url list.
	 */
	private void setImageUrls(ArrayList<String> urls) {
		if (urls == null) {
			return;
		}
		this.urlList = urls;
		tvIndicator.setText(String.format("1/%s", urlList.size()));
		ppvPagerAdapter.notifyDataSetChanged();
	}
	
	/**
	 * set current item.
	 *
	 * @param page position
	 */
	private void setCurrentPage(int page) {
		if (page < 0) return;
		if (page >= urlList.size()) {
			page = urlList.size() - 1;
		}
		viewPager.setCurrentItem(page);
		tvIndicator.setText(String.format("%s/%s", page + 1, urlList.size()));
	}
	
	public void setTransformOutListener(OnTransformOutListener transformOutListener) {
		this.transformOutListener = transformOutListener;
	}
	
	/**
	 * pager adapter.
	 */
	private class PpvPagerAdapter extends PagerAdapter {
		private Context context;
		//private SparseArray<View> mImageViews = new SparseArray<View>();
		
		
		PpvPagerAdapter(Context context) {
			this.context = context;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			ViewGroup pageRootView;
			ZoomImageView pictureView;
			if (config != null && config.pageLayout != -1) {
				pageRootView = (ViewGroup) inflate(getContext(), config.pageLayout, container);
				pictureView = pageRootView.findViewWithTag("picture");
			} else {
				pageRootView = new FrameLayout(getContext());
				pictureView = new ZoomImageView(context);
				pictureView.setImageUrl(urlList.get(position));
				pageRootView.addView(pictureView, 0);
			}
			container.addView(pageRootView);
			//mImageViews.put(position, imageView);
			
			if (pictureView != null) {
				pictureView.setOnHandleClickListener(new ZoomImageView.OnHandleClickListener() {
					@Override
					public void onClick(String url) {
						Log.d("-----", "ppv--onClick()");
						if (transformOutListener != null) {
							transformOutListener.onTransformOut();
						}
					}
					
					@Override
					public void onLongClickListener(String url) {
						Log.d("-----", "ppv--onLongClick()");
						if (config != null && config.longClickListener != null) {
							config.longClickListener.onLongClick(position, url);
						}
					}
				});
				pictureView.setAlphaChangeListener(new ZoomImageView.OnAlphaChangeListener() {
					@Override
					public void onAlphaChange(float alpha) {
						Log.d("-----", "alpha = " + alpha);
						viewPager.setBackgroundColor(ColorUtils.changeAlpha(Color.BLACK, alpha));
						tvIndicator.setVisibility(alpha < 1 ? INVISIBLE : VISIBLE);
					}
				});
				pictureView.setTransformOutListener(new ZoomImageView.OnTransformOutListener() {
					@Override
					public void onTransformOut() {
						Log.d("-----", "ppv--onTransformOut()");
						if (transformOutListener != null) {
							transformOutListener.onTransformOut();
						}
					}
				});
			}
			
			return pageRootView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
		@Override
		public int getCount() {
			return urlList != null ? urlList.size() : 0;
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return object == view;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
			//return super.getItemPosition(object);
		}
	}
	
	public interface OnTransformOutListener {
		void onTransformOut();
	}
	
	public interface OnLongClickListener {
		void onLongClick(int position, String url);
	}
}
