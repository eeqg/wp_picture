package com.example.wp.wp_picture.picker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingCropOption;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.bilibili.boxing_impl.ui.BoxingActivity;
import com.example.wp.wp_picture.R;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by wp on 2019/4/10.
 */
public class PicturePicker {
	
	public static void pickSingle(Activity activity, int requestCode) {
		BoxingConfig singleImgConfig = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
				.needCamera(R.drawable.ic_boxing_camera_white)
				.withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image);
		Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity.class).start(activity, requestCode);
	}
	
	public static void pickCrop(Activity activity, int requestCode, float xRatio, float yRatio) {
		String cachePath = BoxingFileHelper.getCacheDir(activity);
		if (TextUtils.isEmpty(cachePath)) {
			Toast.makeText(activity.getApplicationContext(), R.string.boxing_storage_deny, Toast.LENGTH_SHORT).show();
			return;
		}
		Uri destUri = new Uri.Builder()
				.scheme("file")
				.appendPath(cachePath)
				.appendPath(String.format(Locale.US, "%s.png", System.currentTimeMillis()))
				.build();
		BoxingConfig singleCropImgConfig = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
				.withCropOption(new BoxingCropOption(destUri).aspectRatio(xRatio, yRatio))
				.withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image);
		Boxing.of(singleCropImgConfig).withIntent(activity, BoxingActivity.class).start(activity, requestCode);
	}
	
	public static void pickMulti(Activity activity, int requestCode, int count) {
		BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.MULTI_IMG)
				.withMaxCount(count)
				.needCamera(R.drawable.ic_boxing_camera_white).needGif();
		Boxing.of(config).withIntent(activity, BoxingActivity.class).start(activity, requestCode);
	}
	
	public static Uri pickCamera(Activity activity, int requestCode) {
		Uri uri;
		File outputImage = new File(activity.getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
		try {
			if (outputImage.exists()) {
				outputImage.delete();
			}
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			uri = FileProvider.getUriForFile(activity,
					activity.getApplicationContext().getPackageName() + ".file.provider", outputImage);
		} else {
			uri = Uri.fromFile(outputImage);
		}
		//启动相机程序
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		activity.startActivityForResult(intent, requestCode);
		
		return uri;
	}
}
