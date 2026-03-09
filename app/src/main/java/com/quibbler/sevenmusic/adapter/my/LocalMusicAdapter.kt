package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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

class LocalMusicAdapter(context: Context, objects: MutableList<MusicInfo?>) :
    MusicAdapter(context, R.layout.local_music_list_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val musicInfo = getItem(position)
        var viewHolder: ViewHolder? = null
        if (convertView == null) {
            convertView =
                LayoutInflater.from(mContext).inflate(R.layout.local_music_list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById<ImageView?>(R.id.my_local_music_icon_cover)
            viewHolder.songName = convertView.findViewById<TextView?>(R.id.songOriginalName)
            viewHolder.singerName = convertView.findViewById<TextView>(R.id.singleNames)
            viewHolder.fileSize = convertView.findViewById<TextView>(R.id.fileSizeInfo)
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

        if (musicInfo.getIsMusicMatch().isMusicNameMatch()) {
            val spannableString = SpannableString(musicInfo.getMusicSongName())
            val colorSpan = ForegroundColorSpan(Color.parseColor("#008AFF"))
            spannableString.setSpan(
                colorSpan,
                musicInfo.getIsMusicMatch().getMusicNameStart(),
                musicInfo.getIsMusicMatch().getMusicNameStart() + musicInfo.getIsMusicMatch()
                    .getKeyLength(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            viewHolder.songName.setText(spannableString)
        } else {
            viewHolder.songName.setText(musicInfo.getMusicSongName())
        }

        if (musicInfo.getIsMusicMatch().isSingleNameMatch()) {
            val spannableString = SpannableString(musicInfo.getSinger())
            val colorSpan = ForegroundColorSpan(Color.parseColor("#008AFF"))
            spannableString.setSpan(
                colorSpan,
                musicInfo.getIsMusicMatch().getSingleNameStart(),
                musicInfo.getIsMusicMatch().getSingleNameStart() + musicInfo.getIsMusicMatch()
                    .getKeyLength(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            viewHolder.singerName!!.setText(spannableString)
        } else {
            viewHolder.singerName!!.setText(musicInfo.getSinger())
        }

        val fileSizeM = String.format("%.2f", musicInfo.getMusicFileSize() / 1024.0 / 1024.0)
        viewHolder.fileSize!!.setText(fileSizeM + "Mb")

        val playButton = viewHolder.playButton
        setOnClickListener(convertView, playButton, musicInfo)
        return convertView
    }

    private class ViewHolder : MusicAdapter.ViewHolder() {
        var singerName: TextView? = null
        var fileSize: TextView? = null
    }

    companion object {
        private const val TAG = "LocalMusicAdapter"
    }
}
