package com.quibbler.sevenmusic.utils

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      TextureVideoViewOutlineProvider
 * Description:    任何view生成圆角
 * 例:view.setOutlineProvider(new TextureVideoViewOutlineProvider(20));
 * view.setClipToOutline(true);
 * Author:         lishijun
 * CreateDate:     2019/9/19 17:31
 */
class TextureVideoViewOutlineProvider(radius: Float) : ViewOutlineProvider() {
    private val mRadius: Float

    init {
        this.mRadius = radius
    }

    override fun getOutline(view: View, outline: Outline) {
//        Rect rect = new Rect();
//        view.getGlobalVisibleRect(rect);
//        int leftMargin = 0;
//        int topMargin = 0;
//        Rect selfRect = new Rect(leftMargin, topMargin,
//                rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius)
    }
}