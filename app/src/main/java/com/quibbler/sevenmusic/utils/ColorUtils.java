package com.quibbler.sevenmusic.utils;

import android.util.Log;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      ColorUtils
  * Description:    颜色判断工具类
  * Author:         yanwuyang
  * CreateDate:     2019/10/16 21:07
 */
public class ColorUtils {

    /**
     * 判断一个像素点颜色是不是浅色（偏白色），防止背景图片与白色文字无法区分
     * @param rgb
     * @return
     */
    public static boolean isPixelShallow(int rgb) {
        int r = (rgb & 16711680) >> 16;
        int g = (rgb & 65280) >> 8;
        int b = (rgb & 255);
        if (r > 190 && g > 190 && b > 190) {
            //为浅色
            return true;
        } else {
            //为深色
           return false;
        }
    }
}
