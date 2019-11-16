package com.wp.picture.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Created by wp on 2019/11/16.
 */
public class SimpleFloating {

    private final String TAG = "SimpleFloating";
    private final static int MSG_EXPAND = 99;

    private Activity mActivity;
    private View mFloatingView;
    private ViewGroup rootContentView;
    private FrameLayout.LayoutParams layoutParams;
    private Site mSite = Site.RIGHT;
    private int mWidth = -2, mHeight = -2;

    private ValueAnimator collapseAnimator;
    private ValueAnimator expandAnimator;
    private int mDuration = 400;
    private int mDelay = 500;
    float initValue, collapsedValue;
    private MyHandler myHandler;

    public enum Site {
        LEFT, TOP, RIGHT, BOTTOM,
    }

    public SimpleFloating(Context context, View view, FrameLayout.LayoutParams layoutParams) {
        mActivity = scanForActivity(context);
        mFloatingView = view;
        this.layoutParams = layoutParams;
    }

    public SimpleFloating setCollapseSite(Site site) {
        this.mSite = site;
        return this;
    }

    // public SimpleFloating setViewParams(int width, int height) {
    //     this.mWidth = width;
    //     this.mHeight = height;
    //     return this;
    // }

    public SimpleFloating setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public SimpleFloating setDelay(int delay) {
        this.mDelay = delay;
        return this;
    }

    public void show() {
        // Log.d(TAG, "-----show()--1");
        rootContentView = mActivity.findViewById(android.R.id.content);
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(-2, -2);
        }
        rootContentView.addView(mFloatingView, layoutParams);

        mFloatingView.post(new Runnable() {
            @Override
            public void run() {
                // Log.d(TAG, "-----show()--3");
                mWidth = mFloatingView.getWidth();
                mHeight = mFloatingView.getHeight();

                switch (mSite) {
                    case LEFT:
                        initValue = mFloatingView.getX();
                        collapsedValue = initValue - mWidth * 0.8f;
                        break;
                    case TOP:
                        initValue = mFloatingView.getY();
                        collapsedValue = initValue - mHeight * 0.8f;
                        break;
                    case RIGHT:
                        initValue = mFloatingView.getX();
                        collapsedValue = initValue + mWidth * 0.8f;
                        break;
                    case BOTTOM:
                        initValue = mFloatingView.getY();
                        collapsedValue = initValue + mHeight * 0.8f;
                        break;
                }
            }
        });
    }

    public void startCollapseAnimation() {
        if (myHandler != null && myHandler.hasMessages(MSG_EXPAND)) {
            myHandler.removeMessages(MSG_EXPAND);
        }
        if (expandAnimator != null && expandAnimator.isStarted()) {
            expandAnimator.cancel();
        }
        if (collapseAnimator != null && collapseAnimator.isStarted()) {
            collapseAnimator.cancel();
        }
        collapseAnimator = ValueAnimator.ofFloat(mFloatingView.getX(), collapsedValue).setDuration(mDuration);
        collapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float values = (float) animation.getAnimatedValue();
                mFloatingView.setX(values);
            }
        });
        collapseAnimator.start();
    }

    public void startExpandAnimationDelay() {
        if (myHandler == null) {
            myHandler = new MyHandler(this);
        }
        if (myHandler.hasMessages(MSG_EXPAND)) {
            myHandler.removeMessages(MSG_EXPAND);
        }
        myHandler.sendEmptyMessageDelayed(MSG_EXPAND, mDelay);
    }

    public void startExpandAnimationRe() {
        if (collapseAnimator != null && collapseAnimator.isStarted()) {
            collapseAnimator.cancel();
        }
        if (expandAnimator != null && expandAnimator.isStarted()) {
            expandAnimator.cancel();
        }
        expandAnimator = ValueAnimator.ofFloat(mFloatingView.getX(), initValue).setDuration(mDuration);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float values = (float) animation.getAnimatedValue();
                mFloatingView.setX(values);
            }
        });
        expandAnimator.start();
    }

    public void hide() {
        if (myHandler.hasMessages(MSG_EXPAND)) {
            myHandler.removeMessages(MSG_EXPAND);
        }
        if (expandAnimator != null && expandAnimator.isStarted()) {
            expandAnimator.cancel();
            expandAnimator = null;
        }
        if (collapseAnimator != null && collapseAnimator.isStarted()) {
            collapseAnimator.cancel();
            collapseAnimator = null;
        }
        rootContentView.removeView(mFloatingView);
        myHandler = null;
        mFloatingView = null;
    }

    public static class MyHandler extends Handler {
        private final WeakReference<SimpleFloating> weakReference;

        MyHandler(SimpleFloating simpleFloating) {
            weakReference = new WeakReference<>(simpleFloating);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_EXPAND) {
                weakReference.get().startExpandAnimationRe();
            }
        }
    }

    private Activity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
