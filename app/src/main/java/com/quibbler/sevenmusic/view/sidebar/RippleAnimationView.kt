package com.quibbler.sevenmusic.view.sidebar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.quibbler.sevenmusic.R;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Package:        com.quibbler.music.view
 * ClassName:      RippleAnimationView
 * Description:    听歌识曲-博文动画效果
 * Author:         11103876
 * CreateDate:     2019/9/29 11:51
 */
public class RippleAnimationView extends RelativeLayout {

    private int mRippleType;
    private int mRippleColor;
    private int mRippleAmount;
    private float mRippleScale;
    private float mRippleRadius;
    private int mRippleDuration;
    public float mRippleStrokeWidth;
    private TypedArray mTypedArray;

    /**
     * 画笔
     */
    public Paint mPaint;
    /**
     * 动画集合实例
     */
    private AnimatorSet mAnimatorSet;
    /**
     * 判断动画是否播放
     */
    private boolean mAnimationRunning = false;
    /**
     * 保存RippleCircleView实例集合
     */
    private ArrayList<RippleCircleView> mRippleViewList = new ArrayList<>();

    /**
     * 默认实心圆圈
     */
    private static final int DEFAULT_FILl_TYPE = 0;
    /**
     * 默认伸缩大小
     */
    private static final float DEFAULT_SCALE = 5.0F;
    /**
     * 默认圆圈个数
     */
    private static final int DEFAULT_RIPPLE_COUNT = 5;
    /**
     * 默认扩散时间
     */
    private static final int DEFAULT_DURATION_TIME = 2500;

    public RippleAnimationView(Context context) {
        super(context);
    }

    public RippleAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 描述：初始化绘制波纹环形的相关属性
     *
     * @param context
     * @param attrs
     */
    private void init(final Context context, final AttributeSet attrs) {
        //判断View当前是否处于 IDE 布局编辑（预览）状态，只有在编辑状态下才会返回true，
        //在编写只有在运行时才能看到绘制效果的自定义View时，可以使用该方法查看布局预览。
        if (isInEditMode()) {
            return;
        }

        mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleAnimationView);  // 加载自定义属性
        mRippleType = mTypedArray.getInt(R.styleable.RippleAnimationView_ripple_anim_type, DEFAULT_FILl_TYPE);
        mRippleColor = mTypedArray.getColor(R.styleable.RippleAnimationView_ripple_anim_color, ContextCompat.getColor(context, R.color.rippleColor));
        mRippleAmount = mTypedArray.getInt(R.styleable.RippleAnimationView_ripple_anim_amount, DEFAULT_RIPPLE_COUNT);
        mRippleScale = mTypedArray.getFloat(R.styleable.RippleAnimationView_ripple_anim_scale, DEFAULT_SCALE);
        mRippleRadius = mTypedArray.getDimension(R.styleable.RippleAnimationView_ripple_anim_radius, getResources().getDimension(R.dimen.rippleRadius));
        mRippleDuration = mTypedArray.getInt(R.styleable.RippleAnimationView_ripple_anim_duration, DEFAULT_DURATION_TIME);
        mRippleStrokeWidth = mTypedArray.getDimension(R.styleable.RippleAnimationView_ripple_anim_strokeWidth, getResources().getDimension(R.dimen.rippleStrokeWidth));

        mTypedArray.recycle();  // 回收TypedArray

        int rippleDelay = mRippleDuration / mRippleAmount;
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 抗锯齿
        if (mRippleType == DEFAULT_FILl_TYPE) {
            mRippleStrokeWidth = 0;
            mPaint.setStyle(Paint.Style.FILL);  // 只绘制图形内容
        } else {
            mPaint.setStyle(Paint.Style.STROKE);// 只绘制图形轮廓（描边）
        }
        mPaint.setColor(mRippleColor);

        LayoutParams rippleParams = new LayoutParams((int) (2 * (mRippleRadius + mRippleStrokeWidth)), (int) (2 * (mRippleRadius + mRippleStrokeWidth)));
        rippleParams.addRule(CENTER_IN_PARENT, TRUE);


        ArrayList<Animator> animatorList = new ArrayList<>();  // 分析该动画后将其拆分为缩放、渐变
        for (int i = 0; i < mRippleAmount; i++) {
            RippleCircleView rippleView = new RippleCircleView(this, context);
            addView(rippleView, rippleParams);
            mRippleViewList.add(rippleView);
            //ScaleX缩放
            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, mRippleScale);
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE); // 无限重复
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(i * rippleDelay);
            scaleXAnimator.setDuration(mRippleDuration);
            animatorList.add(scaleXAnimator);
            //ScaleY缩放
            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, mRippleScale);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE); // 无限重复
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(i * rippleDelay);
            scaleYAnimator.setDuration(mRippleDuration);
            animatorList.add(scaleYAnimator);
            //Alpha渐变
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f);
            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE); // 无限重复
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(i * rippleDelay);
            alphaAnimator.setDuration(mRippleDuration);
            animatorList.add(alphaAnimator);
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());// 插值器：开始与结束的地方速率改变比较慢，在中间的时候加速
        mAnimatorSet.playTogether(animatorList);
    }

    /**
     * 描述：开始动画
     */
    public void startRippleAnimation() {
        if (!isRippleRunning()) {
            for (RippleCircleView rippleCircleView : mRippleViewList) {
                rippleCircleView.setVisibility(VISIBLE);
            }
            mAnimatorSet.start();
            mAnimationRunning = true;
        }
    }

    /**
     * 描述：停止动画
     */
    public void stopRippleAnimation() {
        if (isRippleRunning()) {
            Collections.reverse(mRippleViewList);
            for (RippleCircleView rippleCircleView : mRippleViewList) {
                rippleCircleView.setVisibility(INVISIBLE);
            }
            mAnimatorSet.end();
            mAnimationRunning = false;
        }
    }

    /**
     * 描述:是否正在执行
     *
     * @return
     */
    public boolean isRippleRunning() {
        return mAnimationRunning;
    }
}
