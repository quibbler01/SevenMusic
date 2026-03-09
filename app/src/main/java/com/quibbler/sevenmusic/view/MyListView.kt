package com.quibbler.sevenmusic.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

/**
 * Package:        com.quibbler.sevenmusic.view.found
 * ClassName:      MyListView
 * Description:    自定义ListView 在我的页面显示歌单,原生ListView不能嵌套在ScrollView中全部显示数据.需要重写onMeasure()方法；三个构造函数都需要
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:26
 */
class MyListView : ListView {
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(Int.Companion.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        )
    }
}
