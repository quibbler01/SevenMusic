package com.quibbler.sevenmusic.view.sidebar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Package:        com.quibbler.music.view
 * ClassName:      RippleCircleView
 * Description:    自定义CricleView组件
 * Author:         11103876
 * CreateDate:     2019/9/29 12:26
 */
public class RippleCircleView extends View {
    /**
     * RippleAnimationView对象实例
     */
    private RippleAnimationView mRippleAnimationView;

    public RippleCircleView(Context context) {
        super(context);
    }

    public RippleCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RippleCircleView(RippleAnimationView rippleAnimationView, Context context) {
        super(context);
        this.mRippleAnimationView = rippleAnimationView;
        this.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = (Math.min(getWidth(), getHeight())) / 2;
        canvas.drawCircle(radius, radius, radius - mRippleAnimationView.mRippleStrokeWidth, mRippleAnimationView.mPaint);
    }
}
