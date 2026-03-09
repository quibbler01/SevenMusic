package com.quibbler.sevenmusic.view.sidebar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Package:        com.quibbler.music.view
 * ClassName:      RippleCircleView
 * Description:    自定义CricleView组件
 * Author:         11103876
 * CreateDate:     2019/9/29 12:26
 */
class RippleCircleView : View {
    /**
     * RippleAnimationView对象实例
     */
    private var mRippleAnimationView: RippleAnimationView? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(rippleAnimationView: RippleAnimationView, context: Context?) : super(context) {
        this.mRippleAnimationView = rippleAnimationView
        this.setVisibility(INVISIBLE)
    }

    override fun onDraw(canvas: Canvas) {
        val radius = (min(getWidth(), getHeight())) / 2
        canvas.drawCircle(
            radius.toFloat(),
            radius.toFloat(),
            radius - mRippleAnimationView!!.mRippleStrokeWidth,
            mRippleAnimationView!!.mPaint
        )
    }
}
