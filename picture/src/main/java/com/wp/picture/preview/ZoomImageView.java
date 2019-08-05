package com.wp.picture.preview;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Administrator on 2017/7/4 0004.
 * <p>
 * 图片预览view :
 * 支持自由缩放,平移...
 * form: http://blog.csdn.net/lmj623565791/article/details/39474553
 */

public class ZoomImageView extends AppCompatImageView implements
		View.OnTouchListener,
		ViewTreeObserver.OnGlobalLayoutListener {
	private final String TAG = "ZoomImageView";
	/** 最大缩放比例 */
	private static final float SCALE_MAX = 4.0f;
	private final float OUT_MIN_VALUE = 0.25f;
	/** 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0 */
	private float initScale = 1.0f;
	/** 用于存放矩阵的9个值 */
	private final float[] matrixValues = new float[9];
	private final Matrix mScaleMatrix = new Matrix();
	int downX, downY;
	private float alpha = 0;
	private int initLeft = 0;
	private int initTop = 0;
	
	private final ScaleGestureDetector scaleGestureDetector;
	private final GestureDetector gestureDetector;
	private boolean initScaleMatrixFlag = false;
	private float mLastX;
	private float mLastY;
	private boolean isCanDrag;
	private int lastPointerCount;
	private boolean isCheckLeftAndRight;
	private boolean isCheckTopAndBottom;
	private double mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	private float SCALE_MID = 2.0f;
	private boolean isAutoScale;
	private OnHandleClickListener onHandleClickListener;
	private String mUrl;
	private boolean isMoved = false;
	private OnAlphaChangeListener alphaChangeListener;
	private OnTransformOutListener transformOutListener;
	
	public ZoomImageView(Context context) {
		this(context, null);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public ZoomImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		super.setScaleType(ScaleType.MATRIX);
		this.setOnTouchListener(this);
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		gestureDetector = new GestureDetector(context, new MyGestureListener());
	}
	
	public String getImageUrl() {
		return this.mUrl;
	}
	
	/**
	 * 网络图片url
	 */
	public void setImageUrl(String urlStr) {
		this.mUrl = urlStr;
		if (PPView.getImageLoader() == null) {
			throw new RuntimeException("---ppview--- : 请配置图片的加载器!!!");
		}
		PPView.getImageLoader().displayImage(this, mUrl);
	}
	
	public void setTransformOutListener(OnTransformOutListener transformOutListener) {
		this.transformOutListener = transformOutListener;
	}
	
	public void setAlphaChangeListener(OnAlphaChangeListener alphaChangeListener) {
		this.alphaChangeListener = alphaChangeListener;
	}
	
	public void setOnHandleClickListener(OnHandleClickListener listener) {
		this.onHandleClickListener = listener;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//将touch事件传递给ScaleGestureDetector
		scaleGestureDetector.onTouchEvent(event);
		
		//双击放大/缩小图片
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}
		
		//平移{ }
		float x = 0, y = 0;
		// 拿到触摸点的个数
		final int pointerCount = event.getPointerCount();
		// 得到多个触摸点的x与y均值
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x = x / pointerCount;
		y = y / pointerCount;
		
		//每当触摸点发生变化时，重置mLasX , mLastY
		if (pointerCount != lastPointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		
		lastPointerCount = pointerCount;
		
		RectF rectF = getMatrixRectF();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// LogUtils.d("action_down: time = " + System.currentTimeMillis());
				// LogUtils.d("action_down: x = " + getX());
				// LogUtils.d("action_down: y = " + getY());
				downX = (int) event.getX();
				downY = (int) event.getY();
				if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
					//防止和父view滑动冲突
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				
				//reset
				isMoved = false;
				break;
			case MotionEvent.ACTION_MOVE:
				if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
					//防止和父view滑动冲突
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				
				float dx = x - mLastX;
				float dy = y - mLastY;
				
				if (!isCanDrag) {
					isCanDrag = isCanDrag(dx, dy);
				}
				// Log.d("-----", "dx = " + dx);
				// Log.d("-----", "dy = " + dy);
				// Log.d("-----", "isCanDrag = " + isCanDrag);
				if (isCanDrag) {
					//RectF rectF = getMatrixRectF();
					if (getDrawable() != null) {
						isCheckLeftAndRight = isCheckTopAndBottom = true;
						// 如果宽度小于屏幕宽度，则禁止左右移动
						if (rectF.width() < getWidth()) {
							dx = 0;
							isCheckLeftAndRight = false;
						}
						// 如果高度小雨屏幕高度，则禁止上下移动
						if (rectF.height() < getHeight()) {
							dy = 0;
							isCheckTopAndBottom = false;
						}
						mScaleMatrix.postTranslate(dx, dy);
						checkMatrixBounds();
						setImageMatrix(mScaleMatrix);
						
						//touch --> parent view
						if (getMatrixRectF().left == 0 && dx > 0) {
							getParent().requestDisallowInterceptTouchEvent(false);
						}
						if (getMatrixRectF().right == getWidth() && dx < 0) {
							getParent().requestDisallowInterceptTouchEvent(false);
						}
					}
				}
				
				
				float offsetX = event.getX() - downX;
				float offsetY = event.getY() - downY;
				// Log.d("-----", "isCheckLeftAndRight = " + isCheckLeftAndRight);
				// Log.d("-----", "isCheckTopAndBottom = " + isCheckTopAndBottom);
				// Log.d(TAG, String.format("-----downX = %s, getX = %s, offsetX = %s", downX, event.getX(), offsetX));
				// Log.d(TAG, String.format("-----downY = %s, getY = %s, offsetY = %s", downY, event.getY(), offsetY));
				// Log.d(TAG, "-----getScale() = " + getScale());
				if ((event.getPointerCount() == 1
						&& !PPView.getConfig().disableTransform
						// && !isCheckTopAndBottom
						&& getScale() <= initScale)
					// && (y - mLastY > 0 || isMoved)
				) {
					isMoved = true;
					offsetLeftAndRight((int) offsetX);
					offsetTopAndBottom((int) offsetY);
					float scale = Math.abs(1.0f * getTop() / getHeight());
					setScaleY(1 - scale * 0.25f);
					setScaleX(1 - scale * 0.25f);
					invalidate();
					
					alpha = 1 - 2.5f * scale;
					alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);
					if (alphaChangeListener != null) {
						alphaChangeListener.onAlphaChange(alpha);
					}
				}
				
				mLastX = x;
				mLastY = y;
				break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (isMoved) {
					// Log.d(TAG, "-----outValue = " + 1.0f * getTop() / getHeight());
					if (1.0f * getTop() / getHeight() > OUT_MIN_VALUE) {
						if (transformOutListener != null) {
							transformOutListener.onTransformOut();
						}
					} else {
						moveToOldPosition();
					}
				}
				//reset
				isMoved = false;
				lastPointerCount = 0;
				break;
		}
		
		return true;
	}
	
	/**
	 * 是否是推动行为
	 */
	private boolean isCanDrag(float dx, float dy) {
		return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
	}
	
	/**
	 * 移动时，进行边界判断，主要判断宽或高大于屏幕的
	 */
	private void checkMatrixBounds() {
		RectF rect = getMatrixRectF();
		
		float deltaX = 0, deltaY = 0;
		final float viewWidth = getWidth();
		final float viewHeight = getHeight();
		// 判断移动或缩放后，图片显示是否超出屏幕边界
		if (rect.top > 0 && isCheckTopAndBottom) {
			deltaY = -rect.top;
		}
		if (rect.bottom < viewHeight && isCheckTopAndBottom) {
			deltaY = viewHeight - rect.bottom;
		}
		if (rect.left > 0 && isCheckLeftAndRight) {
			deltaX = -rect.left;
		}
		if (rect.right < viewWidth && isCheckLeftAndRight) {
			deltaX = viewWidth - rect.right;
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);
	}
	
	/**
	 * 获得当前的缩放比例
	 */
	public final float getScale() {
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}
	
	/**
	 * 在缩放时，进行图片显示范围的控制
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;
		
		int width = getWidth();
		int height = getHeight();
		
		// 如果宽或高大于屏幕，则控制范围
		if (rect.width() >= width) {
			if (rect.left > 0) {
				deltaX = -rect.left;
			}
			if (rect.right < width) {
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height) {
			if (rect.top > 0) {
				deltaY = -rect.top;
			}
			if (rect.bottom < height) {
				deltaY = height - rect.bottom;
			}
		}
		// 如果宽或高小于屏幕，则让其居中
		if (rect.width() < width) {
			deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
		}
		if (rect.height() < height) {
			deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
		}
		
		mScaleMatrix.postTranslate(deltaX, deltaY);
		
	}
	
	/**
	 * 根据当前图片的Matrix获得图片的范围
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = this.mScaleMatrix;
		RectF rectF = new RectF();
		Drawable drawable = getDrawable();
		if (drawable != null) {
			rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			matrix.mapRect(rectF);
		}
		
		return rectF;
	}
	
	/**
	 * 未达到关闭的阈值松手时，返回到初始位置
	 */
	private void moveToOldPosition() {
		// Log.d("-----", "------------------------------------------");
		// Log.d("-----", "getTop = " + getTop());
		// Log.d("-----", "getLeft = " + getLeft());
		// Log.d("-----", "getWidth = " + getWidth());
		ValueAnimator va = ValueAnimator.ofInt(getTop() - initTop, 0);
		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			int startValue = 0;
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (int) animation.getAnimatedValue();
				if (startValue != 0) {
					// Log.d("-----", "topV = " + (value - startValue));
					offsetTopAndBottom(value - startValue);
				}
				startValue = value;
			}
		});
		
		ValueAnimator leftAnim = ValueAnimator.ofInt(getLeft() - initLeft, 0);
		leftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			int startValue = 0;
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (int) animation.getAnimatedValue();
				if (startValue != 0) {
					// Log.d("-----", "leftV = " + (value - startValue));
					offsetLeftAndRight(value - startValue);
				}
				startValue = value;
			}
		});
		
		ValueAnimator alphaAnim = ValueAnimator.ofFloat(alpha, 1);
		alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (alphaChangeListener != null) {
					alphaChangeListener.onAlphaChange((Float) animation.getAnimatedValue());
				}
			}
		});
		
		ValueAnimator scaleAnim = ValueAnimator.ofFloat(getScaleX(), 1);
		scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float scale = (float) animation.getAnimatedValue();
				setScaleX(scale);
				setScaleY(scale);
			}
		});
		
		AnimatorSet as = new AnimatorSet();
		as.setDuration(400);
		as.setInterpolator(new AccelerateDecelerateInterpolator());
		as.playTogether(va, leftAnim, scaleAnim, alphaAnim);
		as.start();
	}
	
	private void initScaleMatrix() {
		// Log.e(TAG, "-----initScaleMatrixFlag == " + initScaleMatrixFlag);
		if (!initScaleMatrixFlag) {
			Drawable drawable = getDrawable();
			if (drawable == null) {
				Log.e(TAG, "-----drawable == null-----");
				initScaleMatrixFlag = false;
				return;
			}
			//view的宽高
			int width = getWidth();
			int height = getHeight();
			//图片的宽高
			int drawableWidth = drawable.getIntrinsicWidth();
			int drawableHeight = drawable.getIntrinsicHeight();
			// Log.d(TAG, String.format("-----width = %s, height = %s", width, height));
			// Log.d(TAG, String.format("-----drawableWidth = %s, drawableHeight = %s", drawableWidth, drawableHeight));
			
			initScale = Math.min(width * 1.0f / drawableWidth, height * 1.0f / drawableHeight);
			// Log.d(TAG, "-----initScale = " + initScale);
			// 图片移动至屏幕中心
			mScaleMatrix.postTranslate((width - drawableWidth) / 2, (height - drawableHeight) / 2);
			mScaleMatrix.postScale(initScale, initScale, getWidth() / 2, getHeight() / 2);
			setImageMatrix(mScaleMatrix);
			
			initScaleMatrixFlag = true;
			removeOnGlobalLayoutListener();
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// initScaleMatrix();
		initLeft = getLeft();
		initTop = getTop();
		// Log.d(TAG, "-----onSizeChanged()-----initLeft = " + initLeft);
		// Log.d(TAG, "-----onSizeChanged()-----initTop = " + initTop);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeOnGlobalLayoutListener();
	}
	
	@Override
	public void onGlobalLayout() {
		initScaleMatrix();
	}
	
	@TargetApi(16)
	private void removeOnGlobalLayoutListener() {
		getViewTreeObserver().removeOnGlobalLayoutListener(this);
	}
	
	private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//缩放
			float scale = getScale();
			float scaleFactor = detector.getScaleFactor();
			// android.util.Log.d(TAG, "--onScale()-0-scale=" + scale);
			// android.util.Log.d(TAG, "--onScale()-0-scaleFactor=" + scaleFactor);
			
			if (getDrawable() == null) {
				return true;
			}
			
			//缩放的范围控制
			if ((scale < SCALE_MAX && scaleFactor > 1.0f)
					|| (scale > initScale && scaleFactor < 1.0f)) {
				if (scaleFactor * scale < initScale) {
					scaleFactor = initScale / scale;
				}
				if (scaleFactor * scale > SCALE_MAX) {
					scaleFactor = SCALE_MAX / scale;
				}
				//设置缩放比例
				// android.util.Log.d("test_wp", TAG + "--onScale()-1-scaleFactor=" + scaleFactor);
				//mScaleMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2, getHeight() / 2);//中心点缩放
				mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);
			}
			
			return true;
		}
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}
		
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
		}
	}
	
	/**
	 * 双击事件listener
	 */
	private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (isAutoScale)
				return true;
			
			float x = e.getX();
			float y = e.getY();
			if (getScale() < SCALE_MID) {
				ZoomImageView.this.postDelayed(
						new AutoScaleRunnable(SCALE_MID, x, y), 16);
				isAutoScale = true;
			}
			// else if (getScale() >= SCALE_MID
			// 		&& getScale() < SCALE_MAX) {
			// 	ZoomImageView.this.postDelayed(
			// 			new AutoScaleRunnable(SCALE_MAX, x, y), 16);
			// 	isAutoScale = true;
			// }
			else {
				ZoomImageView.this.postDelayed(
						new AutoScaleRunnable(initScale, x, y), 16);
				isAutoScale = true;
			}
			
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// Log.d(TAG, "-----onSingleTapUp-----");
			return super.onSingleTapUp(e);
		}
		
		@Override
		public boolean onContextClick(MotionEvent e) {
			// Log.d(TAG, "-----onContextClick-----");
			return super.onContextClick(e);
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.d(TAG, "-----onSingleTapConfirmed-----isMoved =" + isMoved);
			if (!isMoved && onHandleClickListener != null) {
				onHandleClickListener.onClick(getImageUrl());
			}
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			super.onShowPress(e);
			// Log.d(TAG, "-----onShowPress-----");
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			Log.d(TAG, "-----onLongPress-----");
			if (!isMoved && onHandleClickListener != null) {
				onHandleClickListener.onLongClickListener(getImageUrl());
			}
		}
	}
	
	/**
	 * 自动缩放的任务
	 *
	 * @author zhy
	 */
	private class AutoScaleRunnable implements Runnable {
		static final float BIGGER = 1.07f;
		static final float SMALLER = 0.93f;
		private float mTargetScale;
		private float tmpScale;
		
		/**
		 * 缩放的中心
		 */
		private float x;
		private float y;
		
		/**
		 * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
		 */
		AutoScaleRunnable(float targetScale, float x, float y) {
			this.mTargetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < mTargetScale) {
				tmpScale = BIGGER;
			} else {
				tmpScale = SMALLER;
			}
			
		}
		
		@Override
		public void run() {
			// 进行缩放
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
			
			final float currentScale = getScale();
			//如果值在合法范围内，继续缩放
			if (((tmpScale > 1f) && (currentScale < mTargetScale))
					|| ((tmpScale < 1f) && (mTargetScale < currentScale))) {
				ZoomImageView.this.postDelayed(this, 16);
			} else//设置为目标的缩放比例
			{
				final float deltaScale = mTargetScale / currentScale;
				mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;
			}
			
		}
	}
	
	public interface OnTransformOutListener {
		void onTransformOut();
	}
	
	public interface OnAlphaChangeListener {
		void onAlphaChange(float alpha);
	}
	
	public interface OnHandleClickListener {
		void onClick(String url);
		
		void onLongClickListener(String url);
	}
}
