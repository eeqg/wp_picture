package com.wp.picture.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wp.picture.R;

public class StarView extends LinearLayout {
    private Drawable mDrawableFull;
    private Drawable mDrawableHalf;
    private Drawable mDrawableEmpty;

    private Context context;
    private ImageView[] mIVStars;
    private Rect[] mRectStars;
    private boolean isIndicator;

    private double mStarScore;
    private int starNum;

    public StarView(Context context) {
        super(context);
    }

    public StarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StarView);
        mDrawableFull = a.getDrawable(R.styleable.StarView_star_full);
        mDrawableHalf = a.getDrawable(R.styleable.StarView_star_half);
        mDrawableEmpty = a.getDrawable(R.styleable.StarView_star_empty);
        starNum = a.getInt(R.styleable.StarView_stat_num, 5);
        int starInitScore = a.getInteger(R.styleable.StarView_star_init_score, 0);
        isIndicator = a.getBoolean(R.styleable.StarView_star_indicator, false);
        a.recycle();

        if (mDrawableFull == null && mDrawableHalf == null && mDrawableEmpty == null) {
            mDrawableFull = ContextCompat.getDrawable(context, R.mipmap.ic_star_full);
            mDrawableHalf = ContextCompat.getDrawable(context, R.mipmap.ic_star_half);
            mDrawableEmpty = ContextCompat.getDrawable(context, R.mipmap.ic_star_empty);
        }

        setStarNum(starNum);
        setStarScore(starInitScore);
    }

    public double getStarScore() {
        return mStarScore;
    }

    public StarView setDrawableFull(int resId) {
        mDrawableFull = ContextCompat.getDrawable(context, resId);
        return this;
    }

    public StarView setDrawableEmpty(int resId) {
        mDrawableEmpty = ContextCompat.getDrawable(context, resId);
        return this;
    }

    public StarView setDrawableHalf(int resId) {
        mDrawableHalf = ContextCompat.getDrawable(context, resId);
        return this;
    }

    public StarView setStarNum(int starNum) {
        this.starNum = starNum;
        mIVStars = new ImageView[starNum];
        mRectStars = new Rect[starNum];
        removeAllViews();
        for (int i = 0; i < mIVStars.length; i++) {
            mIVStars[i] = new ImageView(context);
            mIVStars[i].setImageDrawable(mDrawableEmpty);
            mIVStars[i].setPadding(1, 0, 1, 0);
            addView(mIVStars[i], new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mRectStars[i] = new Rect();
        }
        return this;
    }

    public void setStarScore(double starScore) {
        mStarScore = starScore;
        for (int index = 0; index < mIVStars.length; index++) {
            if (index < starScore && index + 1 > starScore) {
                mIVStars[index].setImageDrawable(mDrawableHalf);
            } else if (index < starScore) {
                mIVStars[index].setImageDrawable(mDrawableFull);
            } else {
                mIVStars[index].setImageDrawable(mDrawableEmpty);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        for (int index = 0; index < starNum; index++) {
            mRectStars[index].left = mIVStars[index].getLeft();
            mRectStars[index].top = mIVStars[index].getTop();
            mRectStars[index].right = mIVStars[index].getRight();
            mRectStars[index].bottom = mIVStars[index].getBottom();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isIndicator) {
            return super.onInterceptTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isIndicator) {
            return super.onTouchEvent(event);
        }

        for (int position = 0; position < starNum; position++) {
            if (mRectStars[position].contains((int) event.getX(), (int) event.getY())) {
                for (int index = 0; index < starNum; index++) {
                    if (index <= position) {
                        mIVStars[index].setImageDrawable(mDrawableFull);
                    } else {
                        mIVStars[index].setImageDrawable(mDrawableEmpty);
                    }
                }
                mStarScore = position + 1;
                break;
            }
        }
        return true;
    }
}
