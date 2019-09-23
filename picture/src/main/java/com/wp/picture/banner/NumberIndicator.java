package com.wp.picture.banner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wp.picture.banner.core.IndicatorAble;

public class NumberIndicator extends LinearLayout implements IndicatorAble {

    private Context mContext;
    private TextView tvContent;

    private float mRadius = 6f;
    private float mStrokeWidth = 0;
    private int mStrokeColor = Color.TRANSPARENT;
    private Path mBoundPath = null;
    private Type mType = Type.Rect;//默认为 圆角矩形,即未声明type属性时,默认当成圆角矩形.


    public NumberIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setBackgroundColor(Color.parseColor("#88000000"));
        setPadding(10, 5, 10, 5);
        tvContent = new TextView(context);
        tvContent.setTextColor(Color.WHITE);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        addView(tvContent);
    }

    public NumberIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NumberIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public NumberIndicator(Context context) {
        super(context);
        init(context);
    }

    @Override
    public void onBannerScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onBannerScrollStateChanged(int state) {

    }

    @Override
    public void onBannerSelected(int position, int size, Object object) {
        tvContent.setText(position + 1 + "/" + size);
    }

    @Override
    public void initIndicator(int size) {

    }

    public void draw(Canvas canvas) {
        beforeDraw(canvas);
        super.draw(canvas);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        afterDraw(canvas);
    }

    //在绘制前计算出可以剪裁的矩形区域,并根据矩形区域设置自己的形状Path.
    private void beforeDraw(Canvas canvas) {
        Rect rect = new Rect();
        getLocalVisibleRect(rect);
        mBoundPath = onCaculatePath(rect);
        canvas.clipPath(mBoundPath);

    }

    private void afterDraw(Canvas canvas) {
        Rect rect = new Rect();
        getLocalVisibleRect(rect);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(mStrokeColor);
        p.setStrokeWidth(mStrokeWidth);
        Path path = onGetPathStroke(rect, mBoundPath);
        if (path == null)
            return;
        canvas.drawPath(path, p);
    }

    protected Path onCaculatePath(Rect r) {
        switch (mType) {
            case Rect:
                return caculateRoundRectPath(r);
            case Circle:
                return caculateCirclePath(r);
            case Oval:
                return caculateOvalPath(r);
        }
        return caculateRoundRectPath(r);
    }

    protected Path onGetPathStroke(Rect r, Path boundPath) {
        switch (mType) {
            case Circle:
                return getCirclePathWithinStroke(r, boundPath);
            default:
                return getPathWithinStroke(r, boundPath);
        }
    }

    private Path getPathWithinStroke(Rect r, Path path) {
        if (mStrokeWidth <= 0){
            return path;
        }
        // 防止边过宽,完全遮挡内容.
        int minWidth = r.width() > r.height() ? r.height() : r.width();
        if (minWidth <= 0){
            return null;
        }
        if (mStrokeWidth >= minWidth / 2){
            mStrokeWidth = minWidth / 2.5f;
        }
        Path p = new Path();
        Matrix matrix = new Matrix();
        float scaleX = (r.width() - mStrokeWidth / 2) / r.width();
        float scaleY = (r.height() - mStrokeWidth / 2) / r.height();

        matrix.setScale(scaleX, scaleY, r.centerX(), r.centerY());
        path.transform(matrix, p);
        return p;
    }

    private Path getCirclePathWithinStroke(Rect r, Path path) {
        if (mStrokeWidth <= 0)
            return path;
        // 防止边过宽,完全遮挡内容.
        int minWidth = r.width() > r.height() ? r.height() : r.width();
        if (minWidth <= 0){
            return null;
        }
        if (mStrokeWidth >= minWidth / 2){
            mStrokeWidth = minWidth / 2.5f;
        }
        Path p = new Path();
        Matrix matrix = new Matrix();
        float scale = (minWidth - mStrokeWidth / 2) / minWidth;

        matrix.setScale(scale, scale, r.centerX(), r.centerY());
        path.transform(matrix, p);
        return p;
    }

    private Path caculateRoundRectPath(Rect r) {
        Path path = new Path();
        float radius = getRadius();
        float elevation = 0;
        path.addRoundRect(new RectF(r.left + elevation, r.top + elevation,
                        r.right - elevation, r.bottom - elevation), radius, radius,
                Path.Direction.CW);
        return path;
    }

    private Path caculateCirclePath(Rect r) {
        Path path = new Path();
        int radius = r.width() > r.height() ? r.height() / 2 : r.width() / 2;
        path.addCircle(r.left + radius, r.top + radius, radius,
                Path.Direction.CW);
        return path;
    }

    private Path caculateOvalPath(Rect r) {
        Path path = new Path();
        path.addOval(new RectF(r), Path.Direction.CW);
        return path;
    }

    public void setRadius(float radius) {
        if (mRadius == radius)
            return;
        this.mRadius = radius;
        postInvalidate();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setStrokeWidth(float strok) {
        this.mStrokeWidth = strok;
        postInvalidate();
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public enum Type {
        Rect(0), Circle(1), Oval(2);
        private int type;

        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static Type from(int type) {
            switch (type) {
                case 0:
                    return Rect;
                case 1:
                    return Circle;
                case 2:
                    return Oval;
                default:
                    return Rect;
            }
        }
    }

}
