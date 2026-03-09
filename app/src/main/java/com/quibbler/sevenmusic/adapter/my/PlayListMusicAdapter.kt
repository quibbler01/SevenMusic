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

class PlayListMusicAdapter(context: Context, objects: MutableList<MusicInfo?>) :
    MusicAdapter(context, R.layout.local_music_list_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val musicInfo = getItem(position)
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.play_list_music_list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView?>(R.id.my_local_music_icon_cover)
            viewHolder.songName = convertView.findViewById<TextView?>(R.id.songOriginalName)
            viewHolder.singerName = convertView.findViewById<TextView>(R.id.singleNames)
            viewHolder.playButton = convertView.findViewById<ImageView?>(R.id.playMusicButton)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
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

        val playButton = viewHolder.playButton
        setOnClickListener(convertView, playButton, musicInfo)
        return convertView
    }

    private class ViewHolder : MusicAdapter.ViewHolder() {
        var singerName: TextView? = null
    }

    companion object {
        private const val TAG = "PlayListMusicAdapter"
    }
}
