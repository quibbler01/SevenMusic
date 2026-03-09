package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.SingerInfo
import java.io.Serializable

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MySingerInfoListAdapter
 * Description:    本地音乐:歌手适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:56
 */
class MySingerInfoListAdapter(context: Context, resource: Int, objects: MutableList<SingerInfo>) :
    ArrayAdapter<SingerInfo?>(context, resource, objects) {
    private val mContext: Context
    private val mSingerMusicInfoLists: MutableList<SingerInfo>
    private val mResource: Int
    private val mViews: MutableList<View?>?

    init {
        mResource = resource
        mContext = context
        mSingerMusicInfoLists = objects
        mViews = ArrayList<View?>(objects.size)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val singerInfo = mSingerMusicInfoLists.get(position)
        var viewHolder: ViewHolder? = null

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView>(R.id.my_local_singer_head_icon)
            viewHolder.name = convertView.findViewById<TextView>(R.id.singer_list_item_name)
            viewHolder.number = convertView.findViewById<TextView>(R.id.singer_list_song_count)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        convertView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent(mContext, MyLocalMusicDetailActivity::class.java)
                intent.putExtra("title", "歌手:" + singerInfo.getName())
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.addAll(singerInfo.getSongLists())
                intent.putExtra("music", list as Serializable)
                mContext.startActivity(intent)
            }
        })

        Glide.with(mContext).load(Constant.SEVEN_MUSIC_SINGER + "/" + singerInfo.getName())
            .placeholder(R.drawable.my_collection_singer_icon_item).into(viewHolder!!.icon!!)
        viewHolder.name!!.setText(mSingerMusicInfoLists.get(position).getName())
        viewHolder.number!!.setText(
            mSingerMusicInfoLists.get(position).getSongLists().size.toString() + "首"
        )
        return convertView
    }

    private class ViewHolder {
        var icon: ImageView? = null
        var name: TextView? = null
        var number: TextView? = null
    }
}
