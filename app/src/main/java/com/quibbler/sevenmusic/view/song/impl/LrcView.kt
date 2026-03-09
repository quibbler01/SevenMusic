package com.quibbler.sevenmusic.view.song.impl

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.bean.song.impl.LrcRow
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.view.song.ILrcView
import kotlin.math.abs

/**
 * Package:        com.quibbler.sevenmusic.view.song.impl
 * ClassName:      LrcView
 * Description:    自定义歌词view
 * Author:         lishijun
 * CreateDate:     2019/9/28 19:21
 */
class LrcView(context: Context?, attr: AttributeSet?) : View(context, attr), ILrcView {
    private var mLrcRowList: MutableList<LrcRow>? = null

    private var mNextTime = 0L // 保存下一句开始的时间

    private var mOffsetY = 0 // y上的偏移

    //歌词字体大小默认值
    private val mLrcFontSize = 47

    //当没有歌词的时候展示的内容
    private val mLoadingLrcTip: String? = "本歌曲暂无歌词"

    //当前高亮歌词的行数
    private var mHignlightRow = 0

    //当前高亮歌词的字体颜色为白色
    private val mHignlightRowColor = Color.WHITE

    //拖动高亮歌词的字体颜色为浅白色
    private val mSrollHignlightRowColor = Color.rgb(220, 220, 220)

    //不高亮歌词的字体颜色为灰色
    private val mNormalRowColor = Color.rgb(156, 156, 156)

    //歌词间距
    private val mPaddingY = 80

    private val mMaxScroll = mLrcFontSize + mPaddingY // 最大滑动距离=一行歌词高度+歌词间距

    private val mPaint: Paint

    //平滑滚动
    private val mScroller: Scroller

    //是否允许用户滑动，默认允许，锁屏界面不允许
    private var mCanScroll = true

    private var mIsScrolling = false

    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的字体颜色
     */
    private val mSeekLineColor = Color.WHITE

    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体颜色
     */
    private val mSeekLineTextColor = Color.WHITE

    /**
     * 拖动歌词时，展示当前高亮歌词的时间的字体大小默认值
     */
    private val mSeekLineTextSize = 25

    /**
     * 拖动歌词时，在当前高亮歌词下面的一条直线的起始位置
     */
    private val mSeekLinePaddingX = 10

    /**
     * 拖动歌词时的高亮行
     */
    private var mScrollHignlightRow = 0

    private var mLastMotionY = 0f

    private var mLastMotionX = 0f

    private var mLastHignlightRow = 0

    private var mIsFirstTouch = false

    override fun isScrolling(): Boolean {
        return mIsScrolling
    }

    override fun setLrc(lrcRows: MutableList<LrcRow>?) {
        mHignlightRow = 0
        mLrcRowList = lrcRows
        invalidate()
    }

    override fun seekLrcToTime(time: Long, cb: Boolean) {
        if (mLrcRowList == null || mLrcRowList!!.size == 0) {
            return
        }
        // 如果当前时间小于下一句开始的时间，且为用户手动调整歌曲播放位置
        if (mNextTime > time && cb) {
            // 每次进来都遍历存放的时间
            for (i in mLrcRowList!!.indices) {
                val current = mLrcRowList!!.get(i)
                val currentTime = current.mTime
                if (currentTime > time) {
                    mNextTime = currentTime
                    mHignlightRow = i - 1
                    postInvalidate()
                    return
                }
            }
        } else if (mNextTime > time) {
            return
        } else {
            // 每次进来都遍历存放的时间
            for (i in mLrcRowList!!.indices) {
                // 发现这个时间大于传进来的时间
                // 那么现在就应该显示这个时间前面的对应的那一行
                // 每次都重新显示，是不是要判断：现在正在显示就不刷新了
                val current = mLrcRowList!!.get(i)
                val currentTime = current.mTime
                if (currentTime > time) {
                    mNextTime = currentTime
                    if (cb) {
                        mHignlightRow = i - 1
                    } else {
                        mScroller.abortAnimation()
                        mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME)
                    }
                    postInvalidate()
                    return
                } else if (i == mLrcRowList!!.size - 1) {
                    //如果最后一个还不满足条件就显示最后一句
                    mNextTime = currentTime
                    mHignlightRow = i
                    postInvalidate()
                }
            }
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY()
            if (mScroller.isFinished()) {
                mOffsetY = 0
                val cur = mScroller.getCurrX()
                mHignlightRow = if (cur <= 1) 0 else cur - 1
            }
            postInvalidate()
        }
    }

    /**
     * 设置要高亮的歌词为第几行歌词
     * 
     * @param position 要高亮的歌词行数
     * @param cb       是否是手指拖动后要高亮的歌词
     */
    override fun seekLrc(position: Int, cb: Boolean) {
        if (mLrcRowList == null || position < 0 || position > mLrcRowList!!.size) {
            return
        }
        mHignlightRow = position
        invalidate()
        if (cb) {
            MusicPlayerService.Companion.setPlayProgress(
                mLrcRowList!!.get(position).mTime.toInt(),
                MusicPlayerService.Companion.isPlaying
            )
            seekLrcToTime(mLrcRowList!!.get(position).mTime.toInt().toLong(), true)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val height = getHeight()
        val width = getWidth()
        //当没有歌词的时候
        if (mLrcRowList == null || mLrcRowList!!.size == 0) {
            if (mLoadingLrcTip != null) {
                mPaint.setColor(mHignlightRowColor)
                mPaint.setTextSize(mLrcFontSize.toFloat())
                mPaint.setTextAlign(Paint.Align.CENTER)
                canvas.drawText(
                    mLoadingLrcTip,
                    (width / 2).toFloat(),
                    (height / 2 - mLrcFontSize).toFloat(),
                    mPaint
                )
            }
            return
        }
        var rowY: Float
        val rowX = width / 2
        var rowNum: Int
        /**
         * 分以下三步来绘制歌词：
         * 
         * 第1步：高亮地画出正在播放的那句歌词
         * 第2步：画出正在播放的那句歌词的上面可以展示出来的歌词
         * 第3步：画出正在播放的那句歌词的下面的可以展示出来的歌词
         */
        // 1、 高亮地画出正在要高亮的的那句歌词
        if (mHignlightRow < 0) {
            mHignlightRow = 0
        }
        val highlightText = mLrcRowList!!.get(mHignlightRow).mContent
        val centerY = (2 * height / 5 - mLrcFontSize).toFloat()
        val highlightRowY = centerY + mMicroOffsetY
        mPaint.setColor(mHignlightRowColor)
        mPaint.setTextSize(mLrcFontSize.toFloat())
        mPaint.setTextAlign(Paint.Align.CENTER)
        canvas.drawText(highlightText, rowX.toFloat(), highlightRowY - mOffsetY, mPaint)

        // 上下拖动歌词的时候 画出拖动第二高亮的那句歌词的时间和第二高亮的那句歌词下面的一条直线
        if (mIsScrolling) {
            // 画出高亮的那句歌词下面的一条直线
            mPaint.setColor(mSeekLineColor)
            val lineY = centerY + mPaddingY - mOffsetY
            //该直线的x坐标从0到屏幕宽度
            canvas.drawLine(
                mSeekLinePaddingX.toFloat(),
                lineY,
                (width - mSeekLinePaddingX).toFloat(),
                lineY,
                mPaint
            )
            //得到正在拖动的需要第二高亮的歌词行数和时间
            var tempY = 100000000f
            var time: String? = null
            rowNum = mHignlightRow - 1
            rowY = highlightRowY - mPaddingY - mLrcFontSize
            while (rowY > -mLrcFontSize && rowNum >= 0) {
                val text = mLrcRowList!!.get(rowNum).mContent
                canvas.drawText(text, rowX.toFloat(), rowY - mOffsetY, mPaint)
                if (rowNum >= 0) {
                    if (tempY > abs(rowY - lineY - mLrcFontSize.toFloat() / 2)) {
                        tempY = abs(rowY - lineY - mLrcFontSize.toFloat() / 2)
                        mScrollHignlightRow = rowNum
                        time = mLrcRowList!!.get(rowNum).mString
                    }
                }
                rowY -= (mPaddingY + mLrcFontSize).toFloat()
                rowNum--
            }
            rowNum = mHignlightRow + 1
            rowY = highlightRowY + mPaddingY + mLrcFontSize
            while (rowY < height && rowNum < mLrcRowList!!.size) {
                val text = mLrcRowList!!.get(rowNum).mContent
                canvas.drawText(text, rowX.toFloat(), rowY - mOffsetY, mPaint)
                if (rowNum < mLrcRowList!!.size) {
                    if (tempY > abs(rowY - lineY - mLrcFontSize.toFloat() / 2)) {
                        tempY = abs(rowY - lineY - mLrcFontSize.toFloat() / 2)
                        mScrollHignlightRow = rowNum
                        time = mLrcRowList!!.get(rowNum).mString
                    }
                }
                rowY += (mPaddingY + mLrcFontSize).toFloat()
                rowNum++
            }
            if (time != null) {
                // 画出高亮的那句歌词的时间
                mPaint.setColor(mSeekLineTextColor)
                mPaint.setTextSize(mSeekLineTextSize.toFloat())
                mPaint.setTextAlign(Paint.Align.LEFT)
                canvas.drawText(
                    time,
                    mSeekLinePaddingX.toFloat(),
                    centerY + mPaddingY - mOffsetY - 10,
                    mPaint
                )
            }
        }
        // 2、画出正在播放的那句歌词的上面可以展示出来的歌词
        mPaint.setTextSize(mLrcFontSize.toFloat())
        mPaint.setTextAlign(Paint.Align.CENTER)
        rowNum = mHignlightRow - 1
        rowY = highlightRowY - mPaddingY - mLrcFontSize
        //画出正在播放的那句歌词的上面所有的歌词
        while (rowY > -mLrcFontSize && rowNum >= 0) {
            val text = mLrcRowList!!.get(rowNum).mContent
            if (mIsScrolling && rowNum == mScrollHignlightRow) {
                mPaint.setColor(mSrollHignlightRowColor)
            } else {
                mPaint.setColor(mNormalRowColor)
            }
            canvas.drawText(text, rowX.toFloat(), rowY - mOffsetY, mPaint)
            rowY -= (mPaddingY + mLrcFontSize).toFloat()
            rowNum--
        }
        // 3、画出正在播放的那句歌词的下面的可以展示出来的歌词
        rowNum = mHignlightRow + 1
        rowY = highlightRowY + mPaddingY + mLrcFontSize
        //画出正在播放的那句歌词的所有下面的可以展示出来的歌词
        while (rowY < height && rowNum < mLrcRowList!!.size) {
            val text = mLrcRowList!!.get(rowNum).mContent
            if (mIsScrolling && rowNum == mScrollHignlightRow) {
                mPaint.setColor(mSrollHignlightRowColor)
            } else {
                mPaint.setColor(mNormalRowColor)
            }
            canvas.drawText(text, rowX.toFloat(), rowY - mOffsetY, mPaint)
            rowY += (mPaddingY + mLrcFontSize).toFloat()
            rowNum++
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mCanScroll) {
            return false
        }
        if (mLrcRowList == null || mLrcRowList!!.size == 0) {
            return super.onTouchEvent(event)
        }
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mLastMotionX = event.getX()
                mLastMotionY = event.getY()
                mLastHignlightRow = mHignlightRow
                mIsFirstTouch = true
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                //第一次触摸，屏蔽微小移动
                val y = event.getY() //手指当前位置的y坐标
                val offsetY = y - mLastMotionY //第一次按下的y坐标和目前移动手指位置的y坐标之差
                if (mIsFirstTouch && abs(offsetY) < 20) {
                    mIsScrolling = false
                } else {
                    mIsScrolling = true
                    mIsFirstTouch = false
                }
                //如果一个手指按下，在屏幕上移动的话，拖动歌词上下
                doSeek(event)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (mIsScrolling) {
                updateHighLrc()
                mIsScrolling = false
                mScrollHignlightRow = 0
            }
        }
        return true
    }

    private var mMicroOffsetY = 0f

    init {
        mScroller = Scroller(context, LinearInterpolator())
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.setTextSize(mLrcFontSize.toFloat())
    }

    /**
     * 处理单指在屏幕移动时，歌词上下滚动
     */
    private fun doSeek(event: MotionEvent) {
        val x = event.getX()
        val y = event.getY() //手指当前位置的y坐标
        val offsetY = y - mLastMotionY //第一次按下的y坐标和目前移动手指位置的y坐标之差
        val offsetX = x - mLastMotionX
        //屏蔽左右滑动造成的上下微滑动
        if ((abs(offsetY) / abs(offsetX)) < 0.8) {
            return
        }
        //如果不需要高亮歌词，也要有偏移，保证滑动操作的平滑
        mMicroOffsetY = offsetY
        invalidate()
    }

    private fun updateHighLrc() {
        mMicroOffsetY = 0f
        mHignlightRow = mScrollHignlightRow
        //如果高亮行发生了变化才设置歌曲位置
        if (mHignlightRow != mLastHignlightRow) {
            seekLrc(mHignlightRow, true)
            invalidate()
            val intent = Intent("com.quibbler.sevenmusic.UPDATE_PROGRESSBAR_BROADCAST")
            intent.setPackage(MusicApplication.Companion.getContext().getPackageName())
            MusicApplication.Companion.getContext().sendBroadcast(intent)
        }
    }

    fun setCanScroll(canScroll: Boolean) {
        mCanScroll = canScroll
    }

    companion object {
        private const val TAG = "LrcView"

        private const val SCROLL_TIME = 500
    }
}