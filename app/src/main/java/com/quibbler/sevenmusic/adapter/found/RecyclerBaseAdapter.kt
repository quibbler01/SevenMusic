package com.quibbler.sevenmusic.adapter.found

import androidx.recyclerview.widget.RecyclerView

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      RecyclerBaseAdapter
 * Description:    RecyclerView显示list内容的adapter基类。T是显示的具体内容的Bean类，VH是viewHolder类。
 * Author:         yanwuyang
 * CreateDate:     2019/9/28 11:16
 */
abstract class RecyclerBaseAdapter<T, VH : RecyclerView.ViewHolder?>(infoList: MutableList<T?>) :
    RecyclerView.Adapter<VH?>() {
    //实际使用的数据源
    protected var mSourceList: MutableList<T?> = ArrayList<T?>()

    //是否停止更新RecyclerView
    protected var mShouldStop: Boolean = false

    /**
     * 当数据源list改变时，通知recyclerView更新
     * 
     * @param list 新的数据源
     */
    open fun updateData(list: MutableList<T?>?) {
        mShouldStop = false
        if (list == null) {
            return
        }
        //如果传入的就是数据源list，则直接更新
        if (mSourceList === list) {
            notifyDataSetChanged()
            return
        }
        mSourceList.clear()
        mSourceList.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 当页面切换时，停止更新RecyclerView的操作
     */
    fun stopUpdateData() {
        mShouldStop = true
    }

    init {
        mSourceList.clear()
        mSourceList.addAll(infoList)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (mShouldStop) {
            return
        }
    }

    override fun getItemCount(): Int {
        return mSourceList.size
    }
}
