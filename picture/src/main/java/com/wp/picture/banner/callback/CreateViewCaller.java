package com.wp.picture.banner.callback;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 默认加载布局
 */
public class CreateViewCaller implements CreateViewCallBack<FrameLayout> {
    private ImageView.ScaleType scaleType;

    public static CreateViewCaller build() {
        return new CreateViewCaller(ImageView.ScaleType.FIT_XY);
    }

    /**
     * @param scaleType 缩放类型
     * @return CreateViewCaller
     */
    public static CreateViewCaller build(ImageView.ScaleType scaleType) {
        return new CreateViewCaller(scaleType);
    }


    public CreateViewCaller(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    /**
     * 得到第一个ImageView
     *
     * @param view
     * @return
     */
    public static View findImageView(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View imageView = ((ViewGroup) view).getChildAt(i);
                if (imageView instanceof View) {
                    return (View) imageView;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * 得到第一个View
     *
     * @param layout
     * @return
     */
    public static View findFrameLayout(View layout) {
        if (layout instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) layout).getChildCount(); i++) {
                View v = ((ViewGroup) layout).getChildAt(i);
                if (v instanceof View) {
                    return v;
                }
            }
            return null;
        }
        return null;
    }

    @Override
    public FrameLayout createView(Context context, ViewGroup parent, int viewType, int viewIndex) {
        if (viewIndex == 0)
            return createImageView(context);
        else
            return createLayout(context);
    }

    private FrameLayout createImageView(Context context) {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(params);

        AppCompatImageView view = new AppCompatImageView(context);
        view.setLayoutParams(params);
        view.setScaleType(scaleType);

        layout.addView(view, params);
        return layout;
    }

    private FrameLayout createLayout(Context context) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(params);

        FrameLayout view = new FrameLayout(context);
        view.setLayoutParams(params);

        layout.addView(view, params);
        return layout;
    }
}
