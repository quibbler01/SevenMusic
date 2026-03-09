package com.quibbler.sevenmusic.utils;

import android.util.Log;
import android.widget.ScrollView;

import java.lang.reflect.Method;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      PageEffectUtil
  * Description:    设置页面效果的util
  * Author:         lishijun
  * CreateDate:     2019/10/11 15:55
 */
public class PageEffectUtil {

    private static final String TAG = "PageEffectUtil";

    //设置scroll的回弹效果
    public static void setScrollViewSpringback(ScrollView scrollView) {
        try {
            Class<?> cls = null;
            cls = Class.forName("android.widget.ScrollView");
            Method mMethod_setSpringEffect = cls.getMethod("setSpringEffect", boolean.class);
            Method mMethod_setEdgeEffect = cls.getMethod("setEdgeEffect", boolean.class);
            if (mMethod_setSpringEffect != null) {
                try {
                    mMethod_setSpringEffect.invoke(scrollView, true);
                } catch (Exception e) {
                    Log.e(TAG, "setSpringEffect e: " + e.getMessage());
                }
            }
            if (mMethod_setEdgeEffect != null) {
                try {
                    mMethod_setEdgeEffect.invoke(scrollView, false);
                } catch (Exception e) {
                    Log.e(TAG, "setSpringEffect e: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "initMethod fail e: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
