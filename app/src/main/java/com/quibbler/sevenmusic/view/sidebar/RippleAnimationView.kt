package com.quibbler.sevenmusic.view.sidebar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.quibbler.sevenmusic.R
import java.util.Collections

/**
 * Package:        com.quibbler.music.view
 * ClassName:      RippleAnimationView
 * Description:    听歌识曲-博文动画效果
 * Author:         11103876
 * CreateDate:     2019/9/29 11:51
 */
class RippleAnimationView : RelativeLayout {
    private var mRippleType = 0
    private var mRippleColor = 0
    private var mRippleAmount = 0
    private var mRippleScale = 0f
    private var mRippleRadius = 0f
    private var mRippleDuration = 0
    var mRippleStrokeWidth: Float = 0f
    private var mTypedArray: TypedArray? = null

    /**
     * 画笔
     */
    var mPaint: Paint? = null

    /**
     * 动画集合实例
     */
    private var mAnimatorSet: AnimatorSet? = null
    /**
     * 描述:是否正在执行
     * 
     * @return
     */
    /**
     * 判断动画是否播放
     */
    var isRippleRunning: Boolean = false
        private set

    /**
     * 保存RippleCircleView实例集合
     */
    private val mRippleViewList = ArrayList<RippleCircleView>()

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * 描述：初始化绘制波纹环形的相关属性
     * 
     * @param context
     * @param attrs
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        //判断View当前是否处于 IDE 布局编辑（预览）状态，只有在编辑状态下才会返回true，
        //在编写只有在运行时才能看到绘制效果的自定义View时，可以使用该方法查看布局预览。
        if (isInEditMode()) {
            return
        }

        mTypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RippleAnimationView) // 加载自定义属性
        mRippleType = mTypedArray!!.getInt(
            R.styleable.RippleAnimationView_ripple_anim_type,
            DEFAULT_FILl_TYPE
        )
        mRippleColor = mTypedArray!!.getColor(
            R.styleable.RippleAnimationView_ripple_anim_color,
            ContextCompat.getColor(context, R.color.rippleColor)
        )
        mRippleAmount = mTypedArray!!.getInt(
            R.styleable.RippleAnimationView_ripple_anim_amount,
            DEFAULT_RIPPLE_COUNT
        )
        mRippleScale =
            mTypedArray!!.getFloat(R.styleable.RippleAnimationView_ripple_anim_scale, DEFAULT_SCALE)
        mRippleRadius = mTypedArray!!.getDimension(
            R.styleable.RippleAnimationView_ripple_anim_radius,
            getResources().getDimension(R.dimen.rippleRadius)
        )
        mRippleDuration = mTypedArray!!.getInt(
            R.styleable.RippleAnimationView_ripple_anim_duration,
            DEFAULT_DURATION_TIME
        )
        mRippleStrokeWidth = mTypedArray!!.getDimension(
            R.styleable.RippleAnimationView_ripple_anim_strokeWidth,
            getResources().getDimension(R.dimen.rippleStrokeWidth)
        )

        mTypedArray!!.recycle() // 回收TypedArray

        val rippleDelay = mRippleDuration / mRippleAmount
        mPaint = Paint()
        mPaint!!.setAntiAlias(true) // 抗锯齿
        if (mRippleType == DEFAULT_FILl_TYPE) {
            mRippleStrokeWidth = 0f
            mPaint!!.setStyle(Paint.Style.FILL) // 只绘制图形内容
        } else {
            mPaint!!.setStyle(Paint.Style.STROKE) // 只绘制图形轮廓（描边）
        }
        mPaint!!.setColor(mRippleColor)

        val rippleParams = LayoutParams(
            (2 * (mRippleRadius + mRippleStrokeWidth)).toInt(),
            (2 * (mRippleRadius + mRippleStrokeWidth)).toInt()
        )
        rippleParams.addRule(CENTER_IN_PARENT, TRUE)


        val animatorList = ArrayList<Animator?>() // 分析该动画后将其拆分为缩放、渐变
        for (i in 0..<mRippleAmount) {
            val rippleView = RippleCircleView(this, context)
            addView(rippleView, rippleParams)
            mRippleViewList.add(rippleView)
            //ScaleX缩放
            val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, mRippleScale)
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE) // 无限重复
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART)
            scaleXAnimator.setStartDelay((i * rippleDelay).toLong())
            scaleXAnimator.setDuration(mRippleDuration.toLong())
            animatorList.add(scaleXAnimator)
            //ScaleY缩放
            val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, mRippleScale)
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE) // 无限重复
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART)
            scaleYAnimator.setStartDelay((i * rippleDelay).toLong())
            scaleYAnimator.setDuration(mRippleDuration.toLong())
            animatorList.add(scaleYAnimator)
            //Alpha渐变
            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f)
            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE) // 无限重复
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART)
            alphaAnimator.setStartDelay((i * rippleDelay).toLong())
            alphaAnimator.setDuration(mRippleDuration.toLong())
            animatorList.add(alphaAnimator)
        }
        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.setInterpolator(AccelerateDecelerateInterpolator()) // 插值器：开始与结束的地方速率改变比较慢，在中间的时候加速
        mAnimatorSet!!.playTogether(animatorList)
    }

    /**
     * 描述：开始动画
     */
    fun startRippleAnimation() {
        if (!this.isRippleRunning) {
            for (rippleCircleView in mRippleViewList) {
                rippleCircleView.setVisibility(VISIBLE)
            }
            mAnimatorSet!!.start()
            this.isRippleRunning = true
        }
    }

    /**
     * 描述：停止动画
     */
    fun stopRippleAnimation() {
        if (this.isRippleRunning) {
            Collections.reverse(mRippleViewList)
            for (rippleCircleView in mRippleViewList) {
                rippleCircleView.setVisibility(INVISIBLE)
            }
            mAnimatorSet!!.end()
            this.isRippleRunning = false
        }
    }

    companion object {
        /**
         * 默认实心圆圈
         */
        private const val DEFAULT_FILl_TYPE = 0

        /**
         * 默认伸缩大小
         */
        private const val DEFAULT_SCALE = 5.0f

        /**
         * 默认圆圈个数
         */
        private const val DEFAULT_RIPPLE_COUNT = 5

        /**
         * 默认扩散时间
         */
        private const val DEFAULT_DURATION_TIME = 2500
    }
}
