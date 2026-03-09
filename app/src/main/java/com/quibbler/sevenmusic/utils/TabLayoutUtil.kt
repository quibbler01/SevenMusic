package com.quibbler.sevenmusic.utils;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      TabLayoutUtil
  * Description:    TabLayout工具类
  * Author:         lishijun
  * CreateDate:     2019/9/28 20:21
 */
public class TabLayoutUtil {

    public static void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }
            tabLayout.requestLayout();
        }
    }
}
