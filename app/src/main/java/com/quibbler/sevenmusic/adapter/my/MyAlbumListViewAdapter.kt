package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity
import com.quibbler.sevenmusic.bean.AlbumInfo
import com.quibbler.sevenmusic.bean.MusicInfo
import java.io.Serializable

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyAlbumListViewAdapter
 * Description:    我的:专辑适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:55
 */
class MyAlbumListViewAdapter(context: Context, resource: Int, objects: MutableList<AlbumInfo?>) :
    ArrayAdapter<AlbumInfo?>(context, resource, objects) {
    private val mContext: Context
    private val mAlbumInfoList: MutableList<AlbumInfo?> = ArrayList<AlbumInfo?>()
    private val mResource: Int

    init {
        mResource = resource
        mContext = context
        mAlbumInfoList.addAll(objects)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.name = convertView.findViewById<TextView>(R.id.album_list_item_name)
            viewHolder.number = convertView.findViewById<TextView>(R.id.album_list_song_count)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        convertView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent(mContext, MyLocalMusicDetailActivity::class.java)
                intent.putExtra("title", "专辑:" + mAlbumInfoList.get(position)!!.albumName)
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.addAll(mAlbumInfoList.get(position)!!.getMusicInfoList())
                intent.putExtra("music", list as Serializable)
                mContext.startActivity(intent)
            }
        })
        viewHolder!!.name!!.setText(mAlbumInfoList.get(position)!!.albumName)
        viewHolder.number!!.setText(
            mAlbumInfoList.get(position)!!.getMusicInfoList().size.toString() + "首"
        )
        return convertView
    }

    private class ViewHolder {
        var name: TextView? = null
        var number: TextView? = null
    }
}
