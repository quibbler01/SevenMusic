package com.quibbler.sevenmusic.view

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

/**
 * 
 * Package:        com.quibbler.sevenmusic.view
 * ClassName:      WrapContentHeightViewPager
 * Description:    自定义viewPager，使得height的wrap_content参数可以使用。重写onMeasure方法，遍历childView的高度，取最大值。
 * Author:         yanwuyang
 * CreateDate:     2019/10/10 20:35
 */
class WrapContentHeightViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        var height = 0
        //下面遍历所有child的高度
        for (i in 0..<getChildCount()) {
            val child = getChildAt(i)
            child.measure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            val h = child.getMeasuredHeight()
            if (h > height)  //采用最大的view的高度。
                height = h
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
            height,
            MeasureSpec.EXACTLY
        )

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
