package com.quibbler.sevenmusic.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      UiUtil
 * Description:    Ui帮助类
 * Author:         11103876
 * CreateDate:     2019/10/16 20:28
 */
public class UiUtil {

    /**
     * 描述：弹出Toast,默认使用短显示
     */
    public static void showToast(@NonNull Context context, String msg) {
        UiUtil.showToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(@NonNull Context context, String text, int duration) {
        Toast.makeText(context, text, duration).show();
    }


    /**
     * 描述：转换dp为px
     */
    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * 描述：转换dp为px
     */
    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * 描述：获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        int width;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = context.getResources().getDisplayMetrics().heightPixels;
        } else {
            width = context.getResources().getDisplayMetrics().widthPixels;
        }
        return width;
    }

    /**
     * 描述：获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        int height;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = context.getResources().getDisplayMetrics().widthPixels;
        } else {
            height = context.getResources().getDisplayMetrics().heightPixels;
        }
        return height;
    }

}
