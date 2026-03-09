package com.quibbler.sevenmusic.adapter.search

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.search.HotSearchBean

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchHotAdapter
 * Description:    热搜适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 20:39
 */
class SearchHotAdapter(context: Context, resource: Int, objects: MutableList<HotSearchBean.Data?>) :
    ArrayAdapter<HotSearchBean.Data?>(context, resource, objects) {
    private val mLists: MutableList<HotSearchBean.Data?>
    private val mResource: Int
    private val mContext: Context?

    init {
        this.mContext = context
        this.mResource = resource
        this.mLists = objects
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.positionTextView =
                convertView.findViewById<TextView>(R.id.search_hot_position)
            viewHolder.titleTextView = convertView.findViewById<TextView>(R.id.search_hot_title)
            viewHolder.detailTextView = convertView.findViewById<TextView>(R.id.search_hot_details)
            viewHolder.hotImage = convertView.findViewById<ImageView>(R.id.search_top_hot_file_icon)
            viewHolder.numberTextView = convertView.findViewById<TextView>(R.id.search_hot_number)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        if (position < 3) {
            viewHolder!!.positionTextView!!.setText((position + 1).toString())
            viewHolder.positionTextView!!.setTextColor(Color.RED)
            viewHolder.titleTextView!!.setText(mLists.get(position)!!.getSearchWord())
            viewHolder.titleTextView!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        } else {
            viewHolder!!.positionTextView!!.setText((position + 1).toString())
            viewHolder.titleTextView!!.setText(mLists.get(position)!!.getSearchWord())
            viewHolder.hotImage!!.setVisibility(View.INVISIBLE)
        }
        viewHolder.detailTextView!!.setText(mLists.get(position)!!.getContent())
        viewHolder.numberTextView!!.setText(mLists.get(position)!!.getScore())

        return convertView
    }

    private class ViewHolder {
        var positionTextView: TextView? = null
        var titleTextView: TextView? = null
        var detailTextView: TextView? = null
        var hotImage: ImageView? = null
        var numberTextView: TextView? = null
    }
}
