package com.wp.picture.common;

import android.graphics.Color;

import java.security.SecureRandom;

/**
 * Created by Administrator on 2017/6/5 0005.
 */

public class ColorUtils {
	public static int getRandomColor() {
		return getRandomColor(0xFF);
	}
	
	public static int getRandomColor(int alpha) {
		SecureRandom rgen = new SecureRandom();
		return Color.HSVToColor(alpha, new float[]{
				rgen.nextInt(359), 1, 1
		});
	}
	
	public static int getRandomColorAlpha(){
		SecureRandom rgen = new SecureRandom();
		return getRandomColor(rgen.nextInt(100) + 100);
	}
	
	public static int changeAlpha(int color, float fraction) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		int alpha = (int) (Color.alpha(color) * fraction);
		return Color.argb(alpha, red, green, blue);
	}
}
