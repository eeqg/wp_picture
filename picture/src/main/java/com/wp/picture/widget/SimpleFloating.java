package com.wp.picture.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Message;
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

    private View mFloatingView;
    private ViewGroup rootContentView;
    private FrameLayout.LayoutParams layoutParams;
    private Site mSite = Site.RIGHT;
    private int mWidth = -2, mHeight = -2;
    private boolean showing = false;

    private ValueAnimator collapseAnimator;
    private ValueAnimator expandAnimator;
    private int mDuration = 200;
    private int mDelay = 500;
    float initValue, collapsedValue;
    private MyHandler myHandler;

    public enum Site {
        LEFT, TOP, RIGHT, BOTTOM,
    }

    public SimpleFloating(Context context, View view, FrameLayout.LayoutParams layoutParams) {
        Activity activity = scanForActivity(context);
        rootContentView = activity.findViewById(android.R.id.content);
        mFloatingView = view;
        this.layoutParams = layoutParams;
    }

    public SimpleFloating(FrameLayout contentView, View view, FrameLayout.LayoutParams layoutParams) {
        rootContentView = contentView;
        mFloatingView = view;
        this.layoutParams = layoutParams;
    }

    public SimpleFloating setCollapseSite(Site site) {
        this.mSite = site;
        return this;
    }

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
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(-2, -2);
        }
        rootContentView.addView(mFloatingView, layoutParams);
        showing = true;

        mFloatingView.post(new Runnable() {
            @Override
            public void run() {
                // Log.d(TAG, "-----show()--3");
                mWidth = mFloatingView.getWidth();
                mHeight = mFloatingView.getHeight();

                switch (mSite) {
                    case LEFT:
                        initValue = mFloatingView.getX();
                        collapsedValue = initValue - layoutParams.leftMargin - mWidth * 0.8f;
                        break;
                    case TOP:
                        initValue = mFloatingView.getY();
                        collapsedValue = initValue - layoutParams.topMargin - mHeight * 0.8f;
                        break;
                    case RIGHT:
                        initValue = mFloatingView.getX();
                        collapsedValue = initValue + layoutParams.rightMargin + mWidth * 0.8f;
                        break;
                    case BOTTOM:
                        initValue = mFloatingView.getY();
                        collapsedValue = initValue + layoutParams.bottomMargin + mHeight * 0.8f;
                        break;
                }
            }
        });
    }

    public void startCollapseAnimation() {
        if (!isShowing()) {
            return;
        }
        if (myHandler != null && myHandler.hasMessages(MSG_EXPAND)) {
            myHandler.removeMessages(MSG_EXPAND);
        }
        if (expandAnimator != null && expandAnimator.isStarted()) {
            expandAnimator.cancel();
        }
        if (collapseAnimator != null && collapseAnimator.isStarted()) {
            collapseAnimator.cancel();
        }

        float startValue = 0;
        if (mSite == Site.LEFT || mSite == Site.RIGHT) {
            startValue = mFloatingView.getX();
        } else if (mSite == Site.TOP || mSite == Site.BOTTOM) {
            startValue = mFloatingView.getY();
        }
        collapseAnimator = ValueAnimator.ofFloat(startValue, collapsedValue).setDuration(mDuration);
        collapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!isShowing()) {
                    return;
                }
                float values = (float) animation.getAnimatedValue();
                if (mSite == Site.LEFT || mSite == Site.RIGHT) {
                    mFloatingView.setX(values);
                } else if (mSite == Site.TOP || mSite == Site.BOTTOM) {
                    mFloatingView.setY(values);
                }
            }
        });
        collapseAnimator.start();
    }

    public void startExpandAnimationDelay() {
        if (!isShowing()) {
            return;
        }
        if (myHandler == null) {
            myHandler = new MyHandler(this);
        }
        if (myHandler.hasMessages(MSG_EXPAND)) {
            myHandler.removeMessages(MSG_EXPAND);
        }
        myHandler.sendEmptyMessageDelayed(MSG_EXPAND, mDelay);
    }

    public void startExpandAnimationRe() {
        if (!isShowing()) {
            return;
        }
        if (collapseAnimator != null && collapseAnimator.isStarted()) {
            collapseAnimator.cancel();
        }
        if (expandAnimator != null && expandAnimator.isStarted()) {
            expandAnimator.cancel();
        }
        float startValue = 0;
        if (mSite == Site.LEFT || mSite == Site.RIGHT) {
            startValue = mFloatingView.getX();
        } else if (mSite == Site.TOP || mSite == Site.BOTTOM) {
            startValue = mFloatingView.getY();
        }
        expandAnimator = ValueAnimator.ofFloat(startValue, initValue).setDuration(mDuration);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!isShowing()) {
                    return;
                }
                float values = (float) animation.getAnimatedValue();
                if (mSite == Site.LEFT || mSite == Site.RIGHT) {
                    mFloatingView.setX(values);
                } else if (mSite == Site.TOP || mSite == Site.BOTTOM) {
                    mFloatingView.setY(values);
                }
            }
        });
        expandAnimator.start();
    }

    public void hide() {
        if (myHandler != null && myHandler.hasMessages(MSG_EXPAND)) {
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
        showing = false;
    }

    public boolean isShowing() {
        return showing;
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
