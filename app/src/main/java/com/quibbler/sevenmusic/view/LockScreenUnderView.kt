package com.quibbler.sevenmusic.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.quibbler.sevenmusic.interfaces.ScrollScreenInterface

/**
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      LockScreenUnderView
 * Description:    锁屏页面衬底view，获取用户滑动事件
 * Author:         yanwuyang
 * CreateDate:     2019/9/27 14:19
 */
class LockScreenUnderView : LinearLayout {
    private val TAG = "LockScreenUnderView"

    //滑动操作起始x坐标
    private var mStartX = 0f

    //被滑动操作移动的view
    private var mMoveView: View? = null

    //屏幕宽度
    private val mScreenWidth: Int

    private var mScrollScreenInterface: ScrollScreenInterface? = null

    constructor(context: Context?) : super(context) {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels
        //        Log.d(TAG, "1参数构造函数: " + mScreenWidth);
    }

    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels
        //        Log.d(TAG, "2参数构造函数" + mScreenWidth);
    }

    fun setMoveView(view: View) {
        mMoveView = view
    }

    fun setScrollInterface(scrollInterface: ScrollScreenInterface) {
        mScrollScreenInterface = scrollInterface
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.getAction()
        val nx = event.getX()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //                Log.d(TAG, "ACTION_DOWN:" + nx + "mStartx:" + mStartX);
                mStartX = nx
                onAnimationEnd()
            }

            MotionEvent.ACTION_MOVE -> //                Log.d(TAG, "ACTION_MOVE:" + nx + "mStartx:" + mStartX);
                handleMoveView(nx)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> //                Log.d(TAG, "ACTION_UP:" + nx + "mStartx:" + mStartX);
                doTriggerEvent(nx)
        }
        return true
    }

    private fun handleMoveView(x: Float) {
        val moveX = x - mStartX
        if (moveX < 0) {
//            moveX = 0;
            return
        }
        mMoveView!!.setTranslationX(moveX)

        if (this.getBackground() != null) {
//            Log.d(TAG,this.getBackground().toString());
//            Log.d(TAG, "getTranslationX" + mMoveView.getTranslationX());
            this.getBackground()
                .setAlpha(((mScreenWidth - mMoveView!!.getTranslationX()) / mScreenWidth * 255).toInt())
        } else {
//            Log.d(TAG,"background null");
        }
    }

    private fun doTriggerEvent(x: Float) {
        val moveX = x - mStartX
        if (moveX > (mScreenWidth * 0.3)) {
            //自动移到屏幕右边界外，并finish掉
            moveMoveView((mScreenWidth - mMoveView!!.getLeft()).toFloat(), true)
        } else {
            //自动移回初始化位置，重新覆盖
            moveMoveView(-mMoveView!!.getLeft().toFloat(), false)
        }
    }

    private fun moveMoveView(to: Float, exit: Boolean) {
        val animator = ObjectAnimator.ofFloat(mMoveView, "translationX", to)
        animator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                if (getBackground() != null) {
                    val alpha =
                        ((mScreenWidth - mMoveView!!.getTranslationX()) / mScreenWidth * 255).toInt()
                    getBackground().setAlpha(alpha)
                }
            }
        })
        animator.setDuration(250).start()
        if (exit) {
            //滑动结束后销毁activity
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation!!)
                    mScrollScreenInterface!!.onScreenScrolledToEnd()
                }
            })
        }
    }

    fun reset() {
        getBackground().setAlpha(255)
    }
}
