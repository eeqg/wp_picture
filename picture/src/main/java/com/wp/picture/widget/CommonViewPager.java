package com.wp.picture.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.wp.picture.R;
import com.wp.picture.banner.pager.BannerPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wp on 2019/5/9.
 */
public class CommonViewPager extends FrameLayout {
    private int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private BannerPager viewPager;
    private List<View> viewList = new ArrayList<>();
    private List<Object> dataList = new ArrayList<>();

    private ViewCreator viewCreator;
    private OnItemClickListener itemClickListener;
    private OnItemSelectedListener itemSelectedListener;
    private OnSlideListener slideListener;
    private float mLastX, mLastY;
    private float widthOffset = 1f;
    private LinearLayout llRoot;
    protected boolean isVertical = false;
    protected boolean multiPage = false;
    protected int itemPagerWidth;

    public CommonViewPager(@NonNull Context context) {
        this(context, null);
    }

    public CommonViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonViewPager(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // viewPager = new NoScrollViewPager(getContext());
        // viewPager.setOffscreenPageLimit(5);
        // viewPager.setClipChildren(false);
        // setClipChildren(false);
        // addView(viewPager, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //         ViewGroup.LayoutParams.MATCH_PARENT));

        LayoutInflater.from(getContext()).inflate(R.layout.layout_common_viewpager, this);
        llRoot = findViewById(R.id.llRoot);
        viewPager = llRoot.findViewById(R.id.noScrollViewPager);
        viewPager.setOffscreenPageLimit(5);
    }

    public ViewPager getViewPager() {
        return this.viewPager;
    }

    public CommonViewPager setCanScroll(boolean canScroll) {
        viewPager.setCanScroll(canScroll);
        return this;
    }

    public void setCurrentItem(int position) {
        viewPager.setCurrentItem(position);
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        viewPager.setCurrentItem(position, smoothScroll);
    }

    public CommonViewPager setOnSlideListener(OnSlideListener listener) {
        this.slideListener = listener;
        return this;
    }

    public CommonViewPager addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.viewPager.addOnPageChangeListener(listener);
        return this;
    }

    public CommonViewPager createItemView(ViewCreator creator) {
        this.viewCreator = creator;
        return this;
    }

    public CommonViewPager setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
        return this;
    }

    public CommonViewPager setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
        return this;
    }

    public CommonViewPager setVertical(boolean value) {
        this.isVertical = value;
        return this;
    }

    public CommonViewPager setMultiPage(boolean multiPage) {
        this.multiPage = multiPage;
        llRoot.setClipChildren(false);
        llRoot.setLayerType(LAYER_TYPE_SOFTWARE, null);//关闭硬件加速
        return this;
    }

    public CommonViewPager setItemPagerWidth(int itemWidth) {
        this.itemPagerWidth = itemWidth;
        return this;
    }

    public <T> void execute(List<T> dataList) {
        if (dataList == null) {
            return;
        }
        this.dataList.clear();
        this.viewList.clear();
        this.dataList.addAll(dataList);
        generateItemViews();

        viewPager.setAdapter(new Adapter());

        if (itemSelectedListener != null) {
            this.viewPager.addOnPageChangeListener(pageChangeListener);
        }
        if (isVertical) {
            viewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
            viewPager.setVertical(isVertical);
        }
        if (multiPage) {
            observeMultiPage();
        }
    }

    private void generateItemViews() {
        if (this.viewCreator == null) {
            throw new RuntimeException("---请先创建itemView!!---");
        }
        for (int i = 0; i < dataList.size(); i++) {
            View view = viewCreator.onCreateView(i);
            this.viewList.add(view);
            final int position = i;
            this.viewCreator.onBindView(view, dataList.get(i), i);

            if (itemClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClickListener.onItemClick(v, dataList.get(position), position);
                    }
                });
            }
            if (itemSelectedListener != null && position == 0) {
                itemSelectedListener.onItemSelected(dataList.get(0), 0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;
                //向上滑动
                if (dy < 0 && Math.abs(dy) > scaledTouchSlop && Math.abs(dy) > Math.abs(dx)) {
                    if (slideListener != null) {
                        slideListener.onUpSlide(getViewPager().getCurrentItem());
                    }
                }
                //向左滑动
                if (dx < 0 && Math.abs(dx) > scaledTouchSlop && Math.abs(dx) > Math.abs(dy)) {
                    if (slideListener != null) {
                        slideListener.onLeftSlide(getViewPager().getCurrentItem());
                    }
                }

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    private void observeMultiPage() {
        if (itemPagerWidth <= 0) {
            itemPagerWidth = (getResources().getDisplayMetrics().widthPixels) / 3 + 20;
        }
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) viewPager.getLayoutParams();
        layoutParams2.width = itemPagerWidth;
        viewPager.setLayoutParams(layoutParams2);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return viewPager.dispatchTouchEvent(event);
            }
        });

        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            private final float MAX_SCALE = 1.0f;
            private final float MIN_SCALE = 0.9f;

            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position == 0) {
                    page.setScaleX(MAX_SCALE);
                    page.setScaleY(MAX_SCALE);
                } else if (position < 0) {//滑出的页
                    page.setScaleX(MAX_SCALE + (MAX_SCALE - MIN_SCALE) * position);
                    page.setScaleY(MAX_SCALE + (MAX_SCALE - MIN_SCALE) * position);
                } else if (position > 0) {//滑进的页
                    page.setScaleX(MAX_SCALE - (MAX_SCALE - MIN_SCALE) * position);
                    page.setScaleY(MAX_SCALE - (MAX_SCALE - MIN_SCALE) * position);
                }
            }
        });
    }

    private class Adapter extends PagerAdapter {
        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = container.findViewWithTag(position);
            if (view == null) {
                view = viewList.get(position);
                // view.setTag(position);
                view.setTag(R.id.common_view_pager_container, position);
                container.addView(view);
            }
            return view;
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onItemSelected(dataList.get(position), position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public interface ViewCreator<V extends View, T> {
        View onCreateView(int position);

        void onBindView(V view, T data, int position);
    }

    public interface OnItemClickListener<V extends View, T extends Object> {
        void onItemClick(V view, T data, int position);
    }

    public interface OnItemSelectedListener<T extends Object> {
        void onItemSelected(T data, int position);
    }

    public interface OnSlideListener {
        void onUpSlide(int position);

        void onLeftSlide(int position);
    }
}
