package com.quibbler.sevenmusic.adapter.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.search.SearchHistoryRecyclerAdapter.SearchHistoryTextHolder

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchHistoryRecyclerAdapter
 * Description:    搜索历史适配器
 * Author:         zhaopeng
 * CreateDate:     2019/10/10 9:54
 */
class SearchHistoryRecyclerAdapter(
    mContext: Context?,
    mResource: Int,
    history: MutableList<String?>
) : RecyclerView.Adapter<SearchHistoryTextHolder?>() {
    private val mContext: Context?
    private val mResource: Int
    private val mHistory: MutableList<String?>
    private var mOnItemClickListener: OnItemClickListener? = null

    init {
        this.mContext = mContext
        this.mResource = mResource
        this.mHistory = history
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryTextHolder {
        val view = LayoutInflater.from(mContext).inflate(mResource, parent, false)
        return SearchHistoryTextHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHistoryTextHolder, position: Int) {
        holder.searchText.setText(mHistory.get(position))
        holder.searchText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mOnItemClickListener!!.onItemClick(null, null, position, -1)
            }
        })
    }

    override fun getItemCount(): Int {
        return mHistory.size
    }

    inner class SearchHistoryTextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var searchText: TextView

        init {
            searchText = itemView.findViewById<TextView>(R.id.search_history_text_record_item)
        }
    }

    fun updateSearchDataHistory(historys: MutableList<String?>) {
        this.mHistory.clear()
        this.mHistory.addAll(historys)
        notifyDataSetChanged()
    }

    fun clearSearchData() {
        this.mHistory.clear()
        notifyDataSetChanged()
    }

    /*
    在SearchMainActivity中处理点击事件
     */
    fun addOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }
}
