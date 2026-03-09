package com.quibbler.sevenmusic.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import com.quibbler.sevenmusic.listener.IScrollViewListener

/**
 * 
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      IScrollView
 * Description:    继承ScrollView,重写监听滑动到底部事件
 * Author:         lishijun
 * CreateDate:     2019/10/7 10:32
 */
class IScrollView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {
    private var mIndex = 0

    private var mIScrollViewListener: IScrollViewListener? = null

    fun setIScrollViewListener(IScrollViewListener: IScrollViewListener) {
        mIScrollViewListener = IScrollViewListener
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.getAction()) {
            MotionEvent.ACTION_DOWN -> performClick()
            MotionEvent.ACTION_MOVE -> mIndex++
            else -> {}
        }
        if (ev.getAction() == MotionEvent.ACTION_UP && mIndex > 0) {
            mIndex = 0
            val view = getChildAt(0)
            if (view.getMeasuredHeight() <= getScrollY() + getHeight()) {
                //回调
                mIScrollViewListener!!.onScrollToBottom()
            }
        }
        return super.onTouchEvent(ev)
    }
}
