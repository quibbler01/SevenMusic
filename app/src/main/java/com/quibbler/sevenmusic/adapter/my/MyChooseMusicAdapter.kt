package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyChooseMusicAdapter
 * Description:    简单的音乐选择适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 19:27
 */
class MyChooseMusicAdapter(context: Context, objects: MutableList<MusicInfo?>) :
    MusicAdapter(context, R.layout.local_music_list_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val musicInfo = mList.get(position)
        var viewHolder: ViewHolder? = null

        if (convertView == null) {
            convertView =
                LayoutInflater.from(mContext).inflate(R.layout.local_music_list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView>(R.id.my_local_music_icon_cover)
            viewHolder.alarm = convertView.findViewById<ImageView>(R.id.playMusicButton)
            viewHolder.name = convertView.findViewById<TextView>(R.id.songOriginalName)
            viewHolder.singer = convertView.findViewById<TextView>(R.id.singleNames)
            viewHolder.size = convertView.findViewById<TextView>(R.id.fileSizeInfo)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
        }

        viewHolder!!.icon!!.setImageResource(R.drawable.music_item_icon)

        viewHolder.alarm!!.setImageResource(R.drawable.music_choose_alarm)

        viewHolder.name!!.setText(
            musicInfo.getMusicSongName().substring(0, musicInfo.getMusicSongName().lastIndexOf("."))
        )

        viewHolder.singer!!.setText(musicInfo.getSinger())

        val fileSizeM = String.format("%.2f", musicInfo.getMusicFileSize() / 1024.0 / 1024.0)
        viewHolder.size!!.setText(fileSizeM + "Mb")

        return convertView
    }

    private class ViewHolder : MusicAdapter.ViewHolder() {
        var icon: ImageView? = null
        var alarm: ImageView? = null
        var name: TextView? = null
        var singer: TextView? = null
        var size: TextView? = null
    }

    companion object {
        private const val TAG = "MyChooseMusicAdapter"
    }
}
