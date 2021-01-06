package com.wp.picture.common.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast


//----------toast----------
inline fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.centerToast(resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    var t = Toast.makeText(this, resId, duration)
    t.setGravity(Gravity.CENTER, 0, 0)
    t.show()
}

//----------尺寸转换----------

fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun Context.sp2px(spValue: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (spValue * scale + 0.5f).toInt()
}

fun Context.px2sp(pxValue: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (pxValue / scale + 0.5f).toInt()
}

//----------屏幕尺寸----------

fun Context.getScreenWidth(): Int {
//    val wm: WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            ?: return resources.displayMetrics.widthPixels
//    val point = Point()
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//        wm.defaultDisplay.getRealSize(point)
//    } else {
//        wm.defaultDisplay.getSize(point)
//    }
//    return point.x
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    val display = windowManager!!.defaultDisplay
    val displayMetrics = DisplayMetrics()
    display.getMetrics(displayMetrics)
    val isPortrait = displayMetrics.widthPixels < displayMetrics.heightPixels
    return if (isPortrait) displayMetrics.widthPixels else displayMetrics.heightPixels
}

fun Context.getScreenHeight(): Int {
//    val wm: WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            ?: return resources.displayMetrics.heightPixels
//    val point = Point()
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//        wm.defaultDisplay.getRealSize(point)
//    } else {
//        wm.defaultDisplay.getSize(point)
//    }
//    return point.y
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    val display = windowManager!!.defaultDisplay
    val displayMetrics = DisplayMetrics()
    display.getMetrics(displayMetrics)
    val isPortrait = displayMetrics.widthPixels < displayMetrics.heightPixels
    return if (isPortrait) displayMetrics.heightPixels else displayMetrics.widthPixels
}

fun Context.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

fun Context.getNavigationHeight(): Int {
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

//----------NetWork----------

/**
 * 打开网络设置
 */
fun Context.openWirelessSettings() {
    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * 网络是否连接
 */
fun Context.isConnected(): Boolean {
    var info = this.getActiveNetworkInfo()
    return info.isConnected
}

/**
 * 判断网络是否是移动数据
 */
fun Context.isMobileData(): Boolean {
    var info = this.getActiveNetworkInfo()
    return (null != info
            && info.isAvailable
            && info.type == ConnectivityManager.TYPE_MOBILE)
}

/**
 * 退回到桌面
 */
fun Context.startHomeActivity() {
    val homeIntent = Intent(Intent.ACTION_MAIN)
    homeIntent.addCategory(Intent.CATEGORY_HOME)
    startActivity(homeIntent)
}


@SuppressLint("MissingPermission")
private fun Context.getActiveNetworkInfo(): NetworkInfo {
    var manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return manager.activeNetworkInfo
}


