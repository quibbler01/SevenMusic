package com.quibbler.sevenmusic.view.playbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
class MaqueeTextView : TextView {
    /**
     * 字体行宽度
     */
    private var x = 0

    /**
     * 字体总高度
     */
    private var y = 0
    /**
     * 获取文字滚动速度
     * 
     * @return
     */
    /**
     * 设置文字滚动速度
     * 
     * @param speed
     */
    /**
     * 默认滚动速度
     */
    var speed: Int = 2

    /**
     * 默认文字滚动类型-从右往左
     */
    private var scrollType: Int = FROM_RIGHT

    /**
     * 默认文字不滚动
     */
    private var scrollStatus: Boolean = START

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        x = this.textWidth
        y = this.textHeight
    }

    override fun onDraw(canvas: Canvas?) {
        scrollType(scrollType)
        super.onDraw(canvas!!)
    }

    /**
     * 文字滚动类型
     * 
     * @param type
     */
    private fun scrollType(type: Int) {
        if (scrollStatus) {
            when (type) {
                FROM_RIGHT -> {
                    // 右到左
                    if (x >= this.textWidth) {
                        x = -getWidth()
                    }
                    scrollTo(x, 0)
                    x = x + speed
                    postInvalidate()
                }

                FROM_LEFT -> {
                    // 左到右
                    if (x <= -getWidth()) {
                        x = this.textWidth
                    }
                    scrollTo(x, 0)
                    x = x - speed
                    postInvalidate()
                }

                FROM_TOP -> {
                    // 上到下
                    if (y <= -getHeight()) {
                        y = this.textHeight
                    }
                    scrollTo(0, y)
                    y = y - speed
                    postInvalidate()
                }

                FROM_BOTTOM -> {
                    // 下到上
                    if (y >= this.textHeight) {
                        y = -getHeight()
                    }
                    scrollTo(0, y)
                    y = y + speed
                    postInvalidate()
                }

                else -> {}
            }
        }
    }

    private val textWidth: Int
        /**
         * 获取字体行宽度
         * 
         * @return
         */
        get() {
            val mTextWidth: Int
            val mPaint: Paint = getPaint()
            if (getLineCount() > 1) {
                val lineContent: Array<String?> =
                    getText().toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 如果有多行文字，则获取最长的一行文字宽度
                var maxLine = 0
                var maxLineNumber = 0
                for (i in lineContent.indices) {
                    if (lineContent[i]!!.length > maxLine) {
                        maxLine = lineContent[i]!!.length
                        maxLineNumber = i
                    }
                }
                mTextWidth = mPaint.measureText(lineContent[maxLineNumber]).toInt()
            } else {
                mTextWidth = mPaint.measureText(getText().toString()).toInt()
            }
            return mTextWidth
        }

    private val textHeight: Int
        /**
         * 获取字体总高度
         * 
         * @return
         */
        get() = getLineHeight() * getLineCount()

    /**
     * 获取文字滚动方向类型
     * 
     * @return
     */
    fun getScrollType(): Int {
        return scrollType
    }

    /**
     * 设置文字滚动方向类型
     * 
     * @param scrollType 方向类型
     */
    fun setScrollType(scrollType: Int) {
        this.scrollType = scrollType
        setScrollStatus(START)
    }

    /**
     * 获取当前文字是否是在滚动
     * 
     * @return
     */
    fun isScrollStatus(): Boolean {
        return scrollStatus
    }

    /**
     * 设置滚动状态，开始or暂停
     * 
     * @param scrollStatus
     */
    fun setScrollStatus(scrollStatus: Boolean) {
        this.scrollStatus = scrollStatus
        postInvalidate()
    }

    companion object {
        /**
         * 字幕滚动方向
         */
        const val FROM_RIGHT: Int = 0
        const val FROM_LEFT: Int = 1
        const val FROM_TOP: Int = 2
        const val FROM_BOTTOM: Int = 3

        /**
         * 文字滚动标识
         */
        const val START: Boolean = true

        /**
         * 文字停止标识
         */
        const val STOP: Boolean = false
    }
}
