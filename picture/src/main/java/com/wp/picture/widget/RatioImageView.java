package com.wp.picture.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.wp.picture.R;


public class RatioImageView extends AppCompatImageView {
 
    /* 优先级从大到小：
     mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
     mWidthRatio mHeightRatio
     即如果设置了mIsWidthFitDrawableSizeRatio为true，则优先级较低的三个值不生效 */
	
	private float mDrawableSizeRatio = -1f; // src图片(前景图)的宽高比例
	// 根据前景图宽高比例测量View,防止图片缩放变形
	private boolean mIsWidthFitDrawableSizeRatio; // 宽度是否根据src图片(前景图)的比例来测量（高度已知）
	private boolean mIsHeightFitDrawableSizeRatio; // 高度是否根据src图片(前景图)的比例来测量（宽度已知）
	// 宽高比例
	private float mWidthRatio = -1; // 宽度 = 高度*mWidthRatio
	private float mHeightRatio = -1; // 高度 = 宽度*mHeightRatio
	
	private float width, height;
	private int defaultRadius = 0;
	private int radius;
	private int leftTopRadius;
	private int rightTopRadius;
	private int rightBottomRadius;
	private int leftBottomRadius;
	private int boundWidth = 2;
	Paint boundPaint;
	RectF boundRect = new RectF();
	
	public RatioImageView(Context context) {
		this(context, null);
	}
	
	public RatioImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr); // 虽然此处会调用setImageDrawable，但此时成员变量还未被正确初始化
		init(attrs);
		// 一定要有此代码
		if (getDrawable() != null) {
			mDrawableSizeRatio = 1f * getDrawable().getIntrinsicWidth()
					/ getDrawable().getIntrinsicHeight();
		}
	}
	
	/**
	 * 初始化变量
	 */
	private void init(AttributeSet attrs) {
		
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.RatioImageView);
		mIsWidthFitDrawableSizeRatio = a.getBoolean(R.styleable.RatioImageView_is_width_fix_drawable_size_ratio,
				mIsWidthFitDrawableSizeRatio);
		mIsHeightFitDrawableSizeRatio = a.getBoolean(R.styleable.RatioImageView_is_height_fix_drawable_size_ratio,
				mIsHeightFitDrawableSizeRatio);
		mHeightRatio = a.getFloat(
				R.styleable.RatioImageView_height_to_width_ratio, mHeightRatio);
		mWidthRatio = a.getFloat(
				R.styleable.RatioImageView_width_to_height_ratio, mWidthRatio);
		
		radius = a.getDimensionPixelOffset(R.styleable.RatioImageView_round_radius, defaultRadius);
		leftTopRadius = a.getDimensionPixelOffset(R.styleable.RatioImageView_left_top_radius, defaultRadius);
		rightTopRadius = a.getDimensionPixelOffset(R.styleable.RatioImageView_right_top_radius, defaultRadius);
		rightBottomRadius = a.getDimensionPixelOffset(R.styleable.RatioImageView_right_bottom_radius, defaultRadius);
		leftBottomRadius = a.getDimensionPixelOffset(R.styleable.RatioImageView_left_bottom_radius, defaultRadius);
		
		//如果四个角的值没有设置，那么就使用通用的radius的值。
		if (defaultRadius == leftTopRadius) {
			leftTopRadius = radius;
		}
		if (defaultRadius == rightTopRadius) {
			rightTopRadius = radius;
		}
		if (defaultRadius == rightBottomRadius) {
			rightBottomRadius = radius;
		}
		if (defaultRadius == leftBottomRadius) {
			leftBottomRadius = radius;
		}
		
		a.recycle();
		
		// boundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// boundPaint.setColor(Color.RED);
		// boundPaint.setStyle(Paint.Style.STROKE);
		// boundPaint.setStrokeWidth(boundWidth);
		//
		// setPadding(boundWidth, boundWidth, boundWidth, boundWidth);
	}
	
	public void setHeightRatio(float ratio) {
		this.mHeightRatio = ratio;
	}
	
	public void setWidthRatio(float ratio) {
		this.mWidthRatio = ratio;
	}
	
	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		if (getDrawable() != null) {
			mDrawableSizeRatio = 1f * getDrawable().getIntrinsicWidth()
					/ getDrawable().getIntrinsicHeight();
			if (mDrawableSizeRatio > 0
					&& (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio)) {
				requestLayout();
			}
		}
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		if (getDrawable() != null) {
			mDrawableSizeRatio = 1f * getDrawable().getIntrinsicWidth()
					/ getDrawable().getIntrinsicHeight();
			if (mDrawableSizeRatio > 0
					&& (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio)) {
				requestLayout();
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 优先级从大到小：
		// mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
		// mWidthRatio mHeightRatio
		if (mDrawableSizeRatio > 0) {
			// 根据前景图宽高比例来测量view的大小
			if (mIsWidthFitDrawableSizeRatio) {
				mWidthRatio = mDrawableSizeRatio;
			} else if (mIsHeightFitDrawableSizeRatio) {
				mHeightRatio = 1 / mDrawableSizeRatio;
			}
		}
		
		if (mHeightRatio > 0 && mWidthRatio > 0) {
			throw new RuntimeException("高度和宽度不能同时设置百分比！！");
		}
		
		if (mWidthRatio > 0) { // 高度已知，根据比例，设置宽度
			int height = View.MeasureSpec.getSize(heightMeasureSpec);
			super.onMeasure(View.MeasureSpec.makeMeasureSpec(
					(int) (height * mWidthRatio), View.MeasureSpec.EXACTLY),
					View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
		} else if (mHeightRatio > 0) { // 宽度已知，根据比例，设置高度
			int width = View.MeasureSpec.getSize(widthMeasureSpec);
			super.onMeasure(View.MeasureSpec.makeMeasureSpec(width,
					View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
					(int) (width * mHeightRatio), View.MeasureSpec.EXACTLY));
		} else { // 系统默认测量
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		width = getWidth();
		height = getHeight();
		boundRect.set(0, 0, width, height);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
		int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
		int maxRight = Math.max(rightTopRadius, rightBottomRadius);
		int minWidth = maxLeft + maxRight;
		int maxTop = Math.max(leftTopRadius, rightTopRadius);
		int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
		int minHeight = maxTop + maxBottom;
		if (width >= minWidth && height > minHeight) {
			Path path = new Path();
			//四个角：右上，右下，左下，左上
			path.moveTo(leftTopRadius, 0);
			path.lineTo(width - rightTopRadius, 0);
			path.quadTo(width, 0, width, rightTopRadius);
			
			path.lineTo(width, height - rightBottomRadius);
			path.quadTo(width, height, width - rightBottomRadius, height);
			
			path.lineTo(leftBottomRadius, height);
			path.quadTo(0, height, 0, height - leftBottomRadius);
			
			path.lineTo(0, leftTopRadius);
			path.quadTo(0, 0, leftTopRadius, 0);
			
			canvas.clipPath(path);
			
			// if (boundWidth > 0) {
			// 	int saveLayer = canvas.saveLayer(boundRect, boundPaint, Canvas.ALL_SAVE_FLAG);
			// 	canvas.drawPath(path, boundPaint);
			// 	canvas.restoreToCount(saveLayer);
			// }
		}
		super.onDraw(canvas);
	}
}
