package com.quibbler.sevenmusic.adapter.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.quibbler.sevenmusic.R

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      GroupItemDecoration
 * Description:    RecyclerView分组器
 * Author:         zhaopeng
 * CreateDate:     2019/10/8 19:02
 */
class GroupItemDecoration(context: Context, callback: TitleDecorationCallback) : ItemDecoration() {
    private val mTitleHeight: Int
    private val mTitleTextSize: Int
    private val mPaint: Paint
    private val mTextPaint: Paint
    private val textRect: Rect
    private val callback: TitleDecorationCallback
    private val mGrayPaint: Paint

    init {
        this.callback = callback
        mTitleHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30f,
            context.getResources().getDisplayMetrics()
        ).toInt()
        mTitleTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            18f,
            context.getResources().getDisplayMetrics()
        ).toInt()
        mTextPaint = Paint()
        mTextPaint.setTextSize(mTitleTextSize.toFloat())
        mTextPaint.setAntiAlias(true)
        mTextPaint.setColor(context.getColor(R.color.found_tab_selected))

        mPaint = Paint()
        mPaint.setAntiAlias(true)
        mPaint.setColor(context.getColor(R.color.search_history_text_color))

        mGrayPaint = Paint()
        mGrayPaint.setAntiAlias(true)
        mGrayPaint.setColor(Color.DKGRAY)


        textRect = Rect()
    }

    // 这个方法用于给item隔开距离，类似直接给item设padding
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView?,
        state: RecyclerView.State?
    ) {
        val position = (view.getLayoutParams() as RecyclerView.LayoutParams).getViewLayoutPosition()

        if (position == 0 || isFirst(position)) {
            outRect.top = mTitleHeight
        } else {
            outRect.top = 1
        }
    }

    // 这个方法用于给getItemOffsets()隔开的距离填充图形,
    // 在item绘制之前时被调用，将指定的内容绘制到item view内容之下；
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        // 获取当前屏幕可见 item 数量，而不是 RecyclerView 所有的 item 数量

        val childCount = parent.getChildCount()
        val left = parent.getPaddingLeft()
        val right = parent.getWidth() - parent.getPaddingRight()

        for (i in 0..<childCount) {
            val view = parent.getChildAt(i)
            val params = view
                .getLayoutParams() as RecyclerView.LayoutParams
            val position = params.getViewLayoutPosition()

            if (position == 0 || isFirst(position)) {
                val top = (view.getTop() - mTitleHeight).toFloat()
                val bottom = view.getTop().toFloat()
                canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, mPaint)

                val groupName = callback.getGroupName(position)
                mTextPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                val x = view.getPaddingLeft().toFloat()
                val y = top + (mTitleHeight - textRect.height()) / 2 + textRect.height()
                canvas.drawText(callback.getGroupName(position), x, y, mTextPaint)
            } else {
                val top = (view.getTop() - 1).toFloat()
                val bottom = view.getTop().toFloat()
                canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, mGrayPaint)
            }
        }
    }

    // 在item被绘制之后调用，将指定的内容绘制到item view内容之上
    // 这个方法可以将内容覆盖在item上，可用于制作悬停效果，角标等（这里只实现悬停效果）
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val position =
            ((parent.getLayoutManager()) as LinearLayoutManager).findFirstVisibleItemPosition()
        if (position <= -1 || position >= parent.getAdapter()!!.getItemCount() - 1) {
            // 越界检查
            return
        }

        val firstVisibleView = parent.findViewHolderForAdapterPosition(position)!!.itemView

        val left = parent.getPaddingLeft()
        val right = parent.getWidth() - parent.getPaddingRight()
        var top = parent.getPaddingTop()
        var bottom = top + mTitleHeight


        // 如果当前屏幕上第二个显示的item是下一组的的第一个，并且第一个被title覆盖，则开始移动上个title。
        // 原理就是不断改变title所在矩形的top与bottom的值。
        if (isFirst(position + 1) && firstVisibleView.getBottom() < mTitleHeight) {
            if (mTitleHeight <= firstVisibleView.getHeight()) {
                val d = firstVisibleView.getHeight() - mTitleHeight
                top = firstVisibleView.getTop() + d
            } else {
                val d = mTitleHeight - firstVisibleView.getHeight()
                top = firstVisibleView.getTop() - d // 这里有bug,mTitleHeight过高时 滑动有问题
            }
            bottom = firstVisibleView.getBottom()
        }
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)

        val groupName = callback.getGroupName(position)
        mTextPaint.getTextBounds(groupName, 0, groupName.length, textRect)
        val x = (left + firstVisibleView.getPaddingLeft()).toFloat()
        val y = (top + (mTitleHeight - textRect.height()) / 2 + textRect.height()).toFloat()
        c.drawText(groupName, x, y, mTextPaint)
    }

    /**
     * 判断是否是同一组的第一个item
     * 
     * @param position
     * @return
     */
    private fun isFirst(position: Int): Boolean {
        if (position == 0) {
            return true
        } else {
            val prevGroupId = callback.getGroupId(position - 1)
            val groupId = callback.getGroupId(position)
            return prevGroupId != groupId
        }
    }

    interface TitleDecorationCallback {
        fun getGroupId(position: Int): Long

        fun getGroupName(position: Int): String
    }
}


