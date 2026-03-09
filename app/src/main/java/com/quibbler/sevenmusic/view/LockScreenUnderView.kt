package com.quibbler.sevenmusic.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.quibbler.sevenmusic.interfaces.ScrollScreenInterface;

/**
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      LockScreenUnderView
 * Description:    锁屏页面衬底view，获取用户滑动事件
 * Author:         yanwuyang
 * CreateDate:     2019/9/27 14:19
 */
public class LockScreenUnderView extends LinearLayout {
    private String TAG = "LockScreenUnderView";
    //滑动操作起始x坐标
    private float mStartX;
    //被滑动操作移动的view
    private View mMoveView;
    //屏幕宽度
    private int mScreenWidth;

    private ScrollScreenInterface mScrollScreenInterface;

    public LockScreenUnderView(Context context) {
        super(context);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
//        Log.d(TAG, "1参数构造函数: " + mScreenWidth);
    }

    public LockScreenUnderView(Context context, AttributeSet attr) {
        super(context, attr);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
//        Log.d(TAG, "2参数构造函数" + mScreenWidth);
    }

    public void setMoveView(View view) {
        mMoveView = view;
    }

    public void setScrollInterface(ScrollScreenInterface scrollInterface) {
        mScrollScreenInterface = scrollInterface;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float nx = event.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                Log.d(TAG, "ACTION_DOWN:" + nx + "mStartx:" + mStartX);
                mStartX = nx;
                onAnimationEnd();
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "ACTION_MOVE:" + nx + "mStartx:" + mStartX);
                handleMoveView(nx);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                Log.d(TAG, "ACTION_UP:" + nx + "mStartx:" + mStartX);
                doTriggerEvent(nx);
                break;
        }
        return true;
    }

    private void handleMoveView(float x) {
        float moveX = x - mStartX;
        if (moveX < 0) {
//            moveX = 0;
            return;
        }
        mMoveView.setTranslationX(moveX);

        if (this.getBackground() != null) {
//            Log.d(TAG,this.getBackground().toString());
//            Log.d(TAG, "getTranslationX" + mMoveView.getTranslationX());
            this.getBackground().setAlpha((int) ((mScreenWidth - mMoveView.getTranslationX()) / mScreenWidth * 255));
        } else {
//            Log.d(TAG,"background null");
        }
    }

    private void doTriggerEvent(float x) {
        float moveX = x - mStartX;
        if (moveX > (mScreenWidth * 0.3)) {
            //自动移到屏幕右边界外，并finish掉
            moveMoveView(mScreenWidth - mMoveView.getLeft(), true);
        } else {
            //自动移回初始化位置，重新覆盖
            moveMoveView(-mMoveView.getLeft(), false);
        }
    }

    private void moveMoveView(float to, boolean exit) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mMoveView, "translationX", to);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (getBackground() != null) {
                    int alpha = (int) ((mScreenWidth - mMoveView.getTranslationX()) / mScreenWidth * 255);
                    getBackground().setAlpha(alpha);
                }
            }
        });
        animator.setDuration(250).start();
        if (exit) {
            //滑动结束后销毁activity
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mScrollScreenInterface.onScreenScrolledToEnd();
                }
            });
        }
    }

    public void reset() {
        getBackground().setAlpha(255);
    }
}
