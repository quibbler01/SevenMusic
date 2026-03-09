package com.quibbler.sevenmusic.utils

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import com.google.android.material.tabs.TabLayout

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      TabLayoutUtil
 * Description:    TabLayout工具类
 * Author:         lishijun
 * CreateDate:     2019/9/28 20:21
 */
object TabLayoutUtil {
    fun reduceMarginsInTabs(tabLayout: TabLayout, marginOffset: Int) {
        val tabStrip = tabLayout.getChildAt(0)
        if (tabStrip is ViewGroup) {
            val tabStripGroup = tabStrip
            for (i in 0..<tabStrip.getChildCount()) {
                val tabView = tabStripGroup.getChildAt(i)
                if (tabView.getLayoutParams() is MarginLayoutParams) {
                    (tabView.getLayoutParams() as MarginLayoutParams).leftMargin = marginOffset
                    (tabView.getLayoutParams() as MarginLayoutParams).rightMargin = marginOffset
                }
            }
            tabLayout.requestLayout()
        }
    }
}
