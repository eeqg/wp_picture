package com.example.wp.wp_picture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.wp.picture.banner.Banner;
import com.wp.picture.banner.callback.BindViewCallBack;
import com.wp.picture.banner.callback.CreateViewCaller;
import com.wp.picture.banner.callback.OnClickBannerListener;
import com.wp.picture.banner.core.BaseBanner;
import com.wp.picture.ninegrid.NineGridView;
import com.example.wp.wp_picture.picker.BoxingUcrop;
import com.example.wp.wp_picture.picker.PicturePicker;
import com.wp.picture.picker.PictureLayout;
import com.wp.picture.preview.PPView;
import com.wp.picture.video.SimpleVideoView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int CODE_CAMERA = 1;
    private final int CODE_PICK_MULTI = 2;

    private final String picUrl1 = "http://img.zcool.cn/community/01700557a7f42f0000018c1bd6eb23.jpg";
    private final String picUrl2 = "http://img2.woyaogexing.com/2018/01/25/f5d815584c61d376!500x500.jpg";
    private final String picUrl3 = "http://img2.woyaogexing.com/2018/01/25/991349aa8c98c502!500x500.jpg";
    private final String picUrl4 = "https://img.alicdn.com/bao/uploaded/i4/642874349/O1CN01XoCmT91hzsxinFho2_!!642874349.jpg";

    private ImageView ivSample;
    private PictureLayout pictureLayout;

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
        observeSimpleVideo();
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

    private void observePictureLayout() {
        pictureLayout = findViewById(R.id.pictureLayout);
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
                Log.d("test", "-----position = " + position);
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
        NineGridView nineGridView = findViewById(R.id.nineGridView);
        String[] stringArray = getResources().getStringArray(R.array.test_num_url);
        List<String> images = Arrays.asList(stringArray);
        ArrayList<ImageInfoBean> imageInfo = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ImageInfoBean info = new ImageInfoBean();
            info.imgUrl = images.get(i);
            imageInfo.add(info);
            // break;
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
    }

    private void observeBanner() {
        Banner banner = findViewById(R.id.banner);
        String[] stringArray = getResources().getStringArray(R.array.url4);
        List<String> images = Arrays.asList(stringArray);
        banner.setViewIndex(BaseBanner.VERTICAL)
                .createView(CreateViewCaller.build())
                .bindView(new BindViewCallBack<FrameLayout, String>() {
                    @Override
                    public void bindView(FrameLayout imageRootView, String data, int position) {
                        FrameLayout view = (FrameLayout) CreateViewCaller.findFrameLayout(imageRootView);
                        view.removeAllViews();
                        View bannerRooter = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_banner_index, null);
                        view.addView(bannerRooter);
                        ImageView ivBanner = bannerRooter.findViewById(R.id.ivBanner);
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
        SimpleVideoView simpleVideo = findViewById(R.id.simpleVideo);
        simpleVideo.setup("https://flv2.bn.netease.com/videolib1/1811/26/OqJAZ893T/HD/OqJAZ893T-mobile.mp4");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
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
}
