package com.wp.picture.utils;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

/**
 * Created by wp on 2019/11/5.
 */
public class CommUtil {
    /**
     * 判断View是否可见
     */
    public static boolean isVisibleLocal(View target) {
        Rect rect = new Rect();
        target.getLocalVisibleRect(rect);
        // Log.d("-----", "rect.top : " + rect.top);
        return rect.top == 0;
    }
}
