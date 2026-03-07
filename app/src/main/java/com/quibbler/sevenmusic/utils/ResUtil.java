package com.quibbler.sevenmusic.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.quibbler.sevenmusic.MusicApplication;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ResUtil
 * Description:    获取应用资源的封装
 * Author:         11103876
 * CreateDate:     2019/10/7 15:31
 */
public class ResUtil {

    private ResUtil() {
    }

    /**
     * 描述：获取Resource对象
     *
     * @return Returns a Resources getInstance for your application's Package.
     */
    public static Resources getResources() {
        return MusicApplication.getContext().getResources();
    }

    /**
     * 描述：获取Drawable资源
     *
     * @param resId
     * @return
     */
    public static Drawable getDrawable(int resId) {
        return ContextCompat.getDrawable(MusicApplication.getContext(), resId);
    }

    /**
     * 描述：获取字符串资源
     *
     * @param resId
     * @return
     */
    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    /**
     * 描述：获取color资源
     *
     * @param resId
     * @return
     */
    public static int getColor(int resId) {
        return ContextCompat.getColor(MusicApplication.getContext(), resId);
    }

    /**
     * 描述：获取dimens资源
     *
     * @param resId
     * @return
     */
    public static float getDimens(int resId) {
        return getResources().getDimension(resId);
    }

    /**
     * 描述：获取字符串数组资源
     *
     * @param resId
     * @return
     */
    public static String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }

    /**
     * 描述：根据颜色值计算颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的颜色
     */
    public static int calculateStatusColor(int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }
}
