package com.quibbler.sevenmusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.quibbler.sevenmusic.listener.IScrollViewListener;

/**
  *
  * Package:        com.quibbler.sevenmusic.view
  * ClassName:      IScrollView
  * Description:    继承ScrollView,重写监听滑动到底部事件
  * Author:         lishijun
  * CreateDate:     2019/10/7 10:32
 */
public class IScrollView extends ScrollView {

    private int mIndex = 0;

    private IScrollViewListener mIScrollViewListener;

    public IScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIScrollViewListener(IScrollViewListener IScrollViewListener) {
        mIScrollViewListener = IScrollViewListener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN :
                performClick();
                break;
            case MotionEvent.ACTION_MOVE :
                mIndex++;
                break;
            default :
                break;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP &&  mIndex > 0) {
            mIndex = 0;
            View view = getChildAt(0);
            if (view.getMeasuredHeight() <= getScrollY() + getHeight()) {
                //回调
                mIScrollViewListener.onScrollToBottom();
            }
        }
        return super.onTouchEvent(ev);
    }
}
