package com.example.wp.wp_picture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.BoxingCrop;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.example.wp.wp_picture.loder.BoxingGlideLoader;
import com.example.wp.wp_picture.loder.GlideImageLoader;
import com.example.wp.wp_picture.loder.PPViewGlideLoader;
import com.example.wp.wp_picture.loder.PictureLayoutImageLoader;
import com.example.wp.wp_picture.ninegrid.ImageInfoBean;
import com.example.wp.wp_picture.ninegrid.NineGridImageAdapter;
import com.example.wp.wp_picture.picker.BoxingUcrop;
import com.example.wp.wp_picture.picker.PicturePicker;
import com.wp.picture.banner.Banner;
import com.wp.picture.banner.callback.BindViewCallBack;
import com.wp.picture.banner.callback.CreateViewCallBack;
import com.wp.picture.banner.callback.OnClickBannerListener;
import com.wp.picture.banner.core.BaseBanner;
import com.wp.picture.ninegrid.NineGridView;
import com.wp.picture.picker.PictureLayout;
import com.wp.picture.preview.PPView;
import com.wp.picture.utils.CommUtil;
import com.wp.picture.video.SimpleVideoView;
import com.wp.picture.widget.CommonViewPager;
import com.wp.picture.widget.SimpleFloating;
import com.wp.picture.widget.StarView;
import com.wp.picture.widget.TagLayoutView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int CODE_CAMERA = 1;
    private final int CODE_PICK_MULTI = 2;

    private final String picUrl1 = "http://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg";
    private final String picUrl2 = "http://img2.woyaogexing.com/2018/01/25/f5d815584c61d376!500x500.jpg";
    private final String picUrl3 = "http://img2.woyaogexing.com/2018/01/25/991349aa8c98c502!500x500.jpg";
    private final String picUrl4 = "https://img.alicdn.com/bao/uploaded/i4/642874349/O1CN01XoCmT91hzsxinFho2_!!642874349.jpg";

    //private String videoUrl = "https://flv2.bn.netease.com/videolib1/1811/26/OqJAZ893T/HD/OqJAZ893T-mobile.mp4";
    //private String videoUrl2 = "http://lusl-goods.oss-cn-shenzhen.aliyuncs.com/adminVideo/3677041.mp4";
    private String videoUrl2 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
    private String videoUrl3 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-20-26.mp4";
    private String thumb3 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg";
    private String videoUrl4 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-03_13-02-41.mp4";
    private String thumb4 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-03_12-52-08.jpg";
    private String videoUrl5 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-28_18-20-56.mp4";
    private String thumb5 = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-28_18-18-22.jpg";

    private ImageView ivSample;
    private PictureLayout pictureLayout;
    private SimpleVideoView simpleVideo;
    private SimpleVideoView simpleVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPictureLayout();
        initBoxing();
        initPicturePreview();

        initView();
        observePictureLayout();
        observeNineGridView();

        observeBanner();
        observeScrollView();
        observeSimpleVideo();
        observeWithVideo();

        observeStarView();
        observeTagLayout();

        //floating view.
        TextView textView = new TextView(this);
        textView.setText("sample2");
        textView.setTextColor(Color.WHITE);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundResource(R.drawable.bg_transparent_50_r10);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "-----", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, Sample2Activity.class));
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.rightMargin = 20;
        layoutParams.topMargin = 20;
        SimpleFloating simpleFloating = new SimpleFloating(this, textView, layoutParams);
        simpleFloating.show();
    }

    private void initPictureLayout() {
        PictureLayout.setImageLoader(new PictureLayoutImageLoader());
    }

    private void initPicturePreview() {
        PPView.setImageLoader(new PPViewGlideLoader());
    }

    private void initBoxing() {
        IBoxingMediaLoader loader = new BoxingGlideLoader();
        BoxingMediaLoader.getInstance().init(loader);
        BoxingCrop.getInstance().init(new BoxingUcrop());
    }

    private void initView() {
        ivSample = findViewById(R.id.ivSample);
        GlideImageLoader.getInstance().load(ivSample, picUrl3);

        ivSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PPView.build().url(picUrl3).with(MainActivity.this);
                PPView.build().url(picUrl4).show(MainActivity.this);
            }
        });
    }

    private void observeScrollView() {
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
                final boolean videoVisible = CommUtil.isVisibleLocal(simpleVideo);
                // Log.d("-----", "videoVisible : " + videoVisible);
                // Log.d("-----", "simpleVideo.isPlaying() : " + simpleVideo.isPlaying());
                if (videoVisible) {
                    if (simpleVideo.isTinyModel()) {
                        simpleVideo.enterNormalScreen();
                    }
                } else {
                    if (simpleVideo.isNormalModel() && simpleVideo.isPlaying()) {
                        simpleVideo.enterTinyScreen();
                    }
                }
            }
        });
    }

    private void observePictureLayout() {
        pictureLayout = findViewById(R.id.pictureLayout);
        pictureLayout.addPictureUrl(picUrl3);
        pictureLayout.setOnPictureListener(new PictureLayout.OnPictureListener() {
            @Override
            public void onInsert() {
                // pictureLayout.addPictureUrl("http://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg");
                PicturePicker.pickMulti(MainActivity.this, CODE_PICK_MULTI,
                        pictureLayout.getMaxCount() - pictureLayout.size());
            }

            @Override
            public void onEdit(int position, Uri pictureUri) {
                pictureLayout.removePictureUri(position);
            }

            @Override
            public void onSelect(int position, Uri pictureUri) {
                Log.d("test666", "-----position = " + position);
                ArrayList<String> picList = new ArrayList<>();
                for (Uri uri : pictureLayout.getPictureList()) {
                    picList.add(uri.toString());
                }
                // PPView.build().urlList(picList).position(position).with(MainActivity.this);
                PPView.build().urlList(picList).position(position).show(MainActivity.this);
            }
        });
    }

    private void observeNineGridView() {
        final NineGridView nineGridView = findViewById(R.id.nineGridView);
        String[] stringArray = getResources().getStringArray(R.array.test_num_url);
        List<String> images = Arrays.asList(stringArray);
        final ArrayList<ImageInfoBean> imageInfo = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ImageInfoBean info = new ImageInfoBean();
            info.imgUrl = images.get(i);
            imageInfo.add(info);
        }
        ImageInfoBean info1 = new ImageInfoBean();
        info1.imgUrl = "https://img.alicdn.com/bao/uploaded/i1/2683201295/O1CN01stv3RB1LR9Q5oVrSq_!!2683201295.jpg";
        imageInfo.add(0, info1);
        ImageInfoBean info0 = new ImageInfoBean();
        info0.imgUrl = "https://img.alicdn.com/bao/uploaded/i4/2683201295/O1CN01l7KUTs1LR9Q30cmhb_!!2683201295.jpg";
        info0.isVideo = true;
        info0.videoUrl = "https://cloud.video.taobao.com/play/u/2683201295/p/2/e/6/t/1/226176442207.mp4?appKey=38829";
        imageInfo.add(0, info0);
        nineGridView.setAdapter(new NineGridImageAdapter(this, imageInfo));

        findViewById(R.id.refreshNine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageInfoBean imageInfoBean = imageInfo.get(0);
                String[] stringArray = getResources().getStringArray(R.array.url);
                imageInfoBean.imgUrl = stringArray[new Random().nextInt(stringArray.length - 1)];
                nineGridView.setAdapter(new NineGridImageAdapter(MainActivity.this, imageInfo));
            }
        });

        //2
        final NineGridView nineGridView2 = findViewById(R.id.nineGridView2);
        final ArrayList<ImageInfoBean> imageInfo2 = new ArrayList<>();
        ImageInfoBean info = new ImageInfoBean();
        info.imgUrl = "https://img.vilgou.com/vilgou/img/3741263.jpeg";
        imageInfo2.add(info);
        nineGridView2.setAdapter(new NineGridImageAdapter(this, imageInfo2));
    }

    private void observeBanner() {
        Banner banner = findViewById(R.id.banner);
        String[] stringArray = getResources().getStringArray(R.array.url4);
        List<String> images = Arrays.asList(stringArray);
        banner.setViewIndex(1)
                //.setOrientation(1)//orientation
                .createView(new CreateViewCallBack() {
                    @Override
                    public View createView(Context context, ViewGroup parent, int viewType, int viewIndex) {
                        return LayoutInflater.from(MainActivity.this).inflate(R.layout.item_banner_index, null);
                    }
                })
                .bindView(new BindViewCallBack<FrameLayout, String>() {
                    @Override
                    public void bindView(FrameLayout imageRootView, String data, int position) {
                        ImageView ivBanner = imageRootView.findViewById(R.id.ivBanner);
                        GlideImageLoader.getInstance().load(ivBanner, data);
                    }
                })
                .setOnClickBannerListener(new OnClickBannerListener() {
                    @Override
                    public void onClickBanner(View view, Object data, int position) {
                        Toast.makeText(getApplicationContext(), "position: " + position, Toast.LENGTH_SHORT).show();

                    }
                })
                .execute(images);
    }

    private void observeSimpleVideo() {
        simpleVideo = findViewById(R.id.simpleVideo);
        SimpleVideoView.VideoInfo videoInfo = new SimpleVideoView.VideoInfo(
                videoUrl3
                , "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg",
                "title");
        simpleVideo.setImageLoader(new GlideImageLoader())
                .setTinyWindowAdaptive(true)
                .setTinyWindowTopMargin(25)
                .setup(videoInfo);

        findViewById(R.id.btnStopPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleVideo.stopPlay();
            }
        });
    }

    private void observeWithVideo() {
        String[] urls = getResources().getStringArray(R.array.url);
        List<String> images = Arrays.asList(urls);

        CommonViewPager viewPager = findViewById(R.id.viewPager);
        viewPager
                .createItemView(new CommonViewPager.ViewCreator<FrameLayout, String>() {
                    @Override
                    public View onCreateView(int position) {
                        // if (position == 0) {
                        //     return LayoutInflater.from(mActivity).inflate(R.layout.item_video, null);
                        // }
                        return LayoutInflater.from(MainActivity.this).inflate(R.layout.item_banner_index, null);
                    }

                    @Override
                    public void onBindView(FrameLayout view, String data, int position) {
                        if (position == 0) {
                            simpleVideoView = new SimpleVideoView(MainActivity.this);
                            view.addView(simpleVideoView);
                            SimpleVideoView.VideoInfo videoInfo = new SimpleVideoView.VideoInfo(
                                    videoUrl3
                                    , "",
                                    "title");
                            simpleVideoView.setImageLoader(GlideImageLoader.getInstance()).setup(videoInfo);
                        } else {
                            ImageView ivBanner = view.findViewById(R.id.ivBanner);
                            GlideImageLoader.getInstance().load(ivBanner, data);
                        }
                    }
                })
                .setOnItemSelectedListener(new CommonViewPager.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(Object data, int position) {
                        if (position != 0) {
                            simpleVideoView.onPause();
                        }
                    }
                })
                .execute(images);
    }

    private void observeStarView() {
        StarView starView = findViewById(R.id.starView);
        starView.setOnStarChangedListener(new StarView.OnStarChangedListener() {
            @Override
            public void onChanged(double score) {
                android.util.Log.d("test", "-----" + score);
            }
        });
    }

    private void observeTagLayout() {
        ArrayList<String> dataList = new ArrayList<>();
        dataList.add("sku1");
        dataList.add("sku2");
        dataList.add("sku3");

        final TagListAdapter tagListAdapter = new TagListAdapter(this);
        tagListAdapter.setOnTagClickListener(new TagLayoutView.TagViewAdapter.OnTagClickListener() {
            @Override
            public void onClicked(int position) {
                tagListAdapter.setCurrentPosition(position);
            }
        });
        final TagLayoutView tagLayoutView = findViewById(R.id.tagLayoutView);
        tagLayoutView.setAdapter(tagListAdapter);

        tagListAdapter.setTagList(dataList);

//        tagLayoutView.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("test", "----getWidth = "+tagLayoutView.getWidth());
//                Log.d("test", "----getMeasuredWidth = "+tagLayoutView.getMeasuredWidth());
//                tagLayoutView.requestLayout();
//            }
//        });
    }

    @Override
    protected void onPause() {
        if (simpleVideo != null) {
            simpleVideo.onPause();
        }
        if (simpleVideoView != null) {
            simpleVideoView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (simpleVideo != null) {
            simpleVideo.onStop();
        }
        if (simpleVideoView != null) {
            simpleVideoView.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (simpleVideo != null) {
            simpleVideo.onDestroy();
        }
        if (simpleVideoView != null) {
            simpleVideoView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_CAMERA:
                break;
            case CODE_PICK_MULTI:
                if (resultCode == RESULT_OK) {
                    final ArrayList<BaseMedia> medias = Boxing.getResult(data);
                    if (medias != null && medias.size() > 0) {
                        for (BaseMedia media : medias) {
                            Log.d("", "-----" + media.getPath());
                            pictureLayout.addPictureUrl(media.getPath());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (simpleVideo != null && simpleVideo.isFullscreenModel()) {
            simpleVideo.enterNormalScreen();
            return;
        }
        super.onBackPressed();
    }
}
