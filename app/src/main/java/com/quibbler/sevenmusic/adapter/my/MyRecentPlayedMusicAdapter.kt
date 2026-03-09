package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.service.MusicPlayerService
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat

class MyRecentPlayedMusicAdapter(context: Context?, objects: MutableList<MusicInfo?>?) :
    MusicAdapter(context, R.layout.my_recently_played_music_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val musicInfo = getItem(position)
        var viewHolder: ItemViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.my_recently_played_music_item, parent, false)
            viewHolder = ItemViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView?>(R.id.my_local_music_icon_cover)
            viewHolder.songName = convertView.findViewById<TextView?>(R.id.songOriginalName)
            viewHolder.singerName = convertView.findViewById<TextView>(R.id.singleNames)
            viewHolder.playTime = convertView.findViewById<TextView>(R.id.fileSizeInfo)
            viewHolder.playButton = convertView.findViewById<ImageView?>(R.id.playMusicButton)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ItemViewHolder?
        }

        Glide.with(mContext).load(Constant.SEVEN_MUSIC_IMAGE + "/" + musicInfo!!.getId())
            .placeholder(R.drawable.music_item_icon).into(viewHolder!!.icon)

        if (MusicPlayerService.Companion.isPlaying && musicInfo.getId() == MusicPlayerService.Companion.sMusicID) {
            viewHolder.playButton.setImageResource(R.drawable.my_music_pause_icon)
            mButtonWeakReference = WeakReference<ImageView?>(viewHolder.playButton)
        } else {
            viewHolder.playButton.setImageResource(R.drawable.my_music_play_icon)
        }
        viewHolder.songName.setText(musicInfo.getMusicSongName())
        viewHolder.singerName!!.setText(musicInfo.getSinger())

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        viewHolder.playTime!!.setText(simpleDateFormat.format(musicInfo.getLastPlayedTime()))

        val playButton = viewHolder.playButton
        setOnClickListener(convertView, playButton, musicInfo)
        return convertView
    }

    private class ItemViewHolder : ViewHolder() {
        var singerName: TextView? = null
        var playTime: TextView? = null
    }

    companion object {
        private const val TAG = "MyRecentPlayedMusicAdapter"
    }
}
