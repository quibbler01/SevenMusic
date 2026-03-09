package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MusicPathInfo
import java.io.Serializable

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyFilePathListVIewAdapter
 * Description:    我的:文件夹适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:55
 */
class MyFilePathListVIewAdapter(
    context: Context,
    resource: Int,
    objects: MutableList<MusicPathInfo?>
) : ArrayAdapter<MusicPathInfo?>(context, resource, objects) {
    private val mContext: Context
    private val mResource: Int
    private val mMusicPathInfoLists: MutableList<MusicPathInfo> = ArrayList<MusicPathInfo>()

    init {
        mContext = context
        mResource = resource
        mMusicPathInfoLists.addAll(objects)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val musicPathInfo = mMusicPathInfoLists.get(position)
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView>(R.id.my_local_file_dir_icon)
            viewHolder.detail = convertView.findViewById<TextView>(R.id.path_detail_item)
            viewHolder.path = convertView.findViewById<TextView>(R.id.music_list_item_path)
            viewHolder.count = convertView.findViewById<TextView>(R.id.path_list_song_count)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }
        convertView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent(mContext, MyLocalMusicDetailActivity::class.java)
                intent.putExtra("title", mMusicPathInfoLists.get(position).getPathName())
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.addAll(mMusicPathInfoLists.get(position).getMusicInfoLists())
                intent.putExtra("music", list as Serializable)
                mContext.startActivity(intent)
            }
        })
        if ("QQ音乐" == musicPathInfo.getPathName()) {
            viewHolder!!.icon!!.setImageResource(R.drawable.path_icon_qq)
        } else if ("网易云音乐" == musicPathInfo.getPathName()) {
            viewHolder!!.icon!!.setImageResource(R.drawable.path_icon_easynet)
        } else if ("七音" == musicPathInfo.getPathName()) {
            viewHolder!!.icon!!.setImageResource(R.drawable.ic_launcher_web)
        }

        viewHolder!!.detail!!.setText(musicPathInfo.getPathDetail())
        viewHolder.path!!.setText(musicPathInfo.getPathName())
        viewHolder.count!!.setText(musicPathInfo.getMusicInfoLists().size.toString() + "首")
        return convertView
    }

    private class ViewHolder {
        var icon: ImageView? = null
        var path: TextView? = null
        var detail: TextView? = null
        var count: TextView? = null
    }
}
