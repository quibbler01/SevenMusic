package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.my.MySongListManagerActivity.ChangeViewCallBack
import com.quibbler.sevenmusic.bean.MySongListInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MySongListAdapter
 * Description:    歌单数据适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:49
 */
class MySongListAdapter : ArrayAdapter<MySongListInfo?> {
    private val mContext: Context
    private val mResourceID: Int
    private val mSongsList: MutableList<MySongListInfo>
    private var mMode = false
    private var mCallBack: ChangeViewCallBack? = null

    constructor(context: Context, resource: Int, objects: MutableList<MySongListInfo>) : super(
        context,
        resource,
        objects
    ) {
        this.mResourceID = resource
        this.mContext = context
        this.mSongsList = objects
    }

    constructor(
        context: Context,
        resource: Int,
        objects: MutableList<MySongListInfo>,
        deleteMode: Boolean
    ) : super(context, resource, objects) {
        this.mResourceID = resource
        this.mContext = context
        this.mSongsList = objects
        this.mMode = deleteMode
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val mySongListInfo = mSongsList.get(position)
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResourceID, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView>(R.id.my_music_lists_item_icon)
            viewHolder.name = convertView.findViewById<TextView>(R.id.my_song_list_item_name_text)
            viewHolder.number =
                convertView.findViewById<TextView>(R.id.my_song_list_song_number_hint)
            viewHolder.detail = convertView.findViewById<TextView>(R.id.my_song_list_item_detail)
            viewHolder.delete =
                convertView.findViewById<ImageView>(R.id.my_song_list_item_delete_button)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }
        Glide.with(mContext).load(mySongListInfo.getImageUrl())
            .placeholder(R.drawable.search_online_play_list_item)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(20))).into(viewHolder!!.icon!!)

        if (mMode) {
            Log.d(TAG, "deleteMode")
            viewHolder.delete!!.setVisibility(View.VISIBLE)
        }

        viewHolder.name!!.setText(mySongListInfo.getListName())
        viewHolder.number!!.setText(mySongListInfo.getNumber().toString() + "首")
        if (mySongListInfo.getType() == 0) {
            viewHolder.detail!!.setVisibility(View.GONE)
        } else {
            viewHolder.detail!!.setText(mySongListInfo.getDescription())
            viewHolder.detail!!.setSelected(true)
        }

        viewHolder.delete!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mSongsList.remove(mySongListInfo)
                notifyDataSetChanged()
                MusicThreadPool.postRunnable(object : Runnable {
                    override fun run() {
                        MusicApplication.Companion.getContext().getContentResolver().delete(
                            MusicContentProvider.Companion.SONGLIST_URL,
                            "name = ?",
                            arrayOf<String?>(mySongListInfo.getListName())
                        )
                    }
                })
                if (mSongsList.size == 0) {
                    if (mCallBack != null) {
                        mCallBack!!.hideList()
                    }
                }
            }
        })
        return convertView
    }

    fun update(songListInfos: MutableList<MySongListInfo?>) {
        this.mSongsList.clear()
        this.mSongsList.addAll(songListInfos)
        notifyDataSetChanged()
    }

    fun setCallBack(callBack: ChangeViewCallBack?) {
        this.mCallBack = callBack
    }

    private class ViewHolder {
        var icon: ImageView? = null
        var name: TextView? = null
        var number: TextView? = null
        var detail: TextView? = null
        var delete: ImageView? = null
    }

    companion object {
        private const val TAG = "MySongListAdapter"
    }
}
