package com.google.zxing;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

/**
 * Created by cuiqing on 2015/5/22.
 */
public class UiUtil {

    /**
     * 弹出Toast,默认使用短显示
     */
    public static void showToast(Context context, String msg) {
        UiUtil.showToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String text, int duration) {
        Toast.makeText(context, text, duration).show();
    }



    /**
     * 转换dp为px
     */
    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * 转换dp为px
     */
    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        int width ;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = context.getResources().getDisplayMetrics().heightPixels;
        } else {
            width = context.getResources().getDisplayMetrics().widthPixels;
        }
        return width;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        int height ;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = context.getResources().getDisplayMetrics().widthPixels;
        } else {
            height = context.getResources().getDisplayMetrics().heightPixels;
        }
        return height;
    }

}
