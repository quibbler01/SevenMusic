package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.service.MusicPlayerService
import java.lang.ref.WeakReference

/**
 * Package:        com.quibbler.sevenmusic.adapter.my
 * ClassName:      MusicAdapter
 * Description:    抽象Adapter类
 * Author:         zhaopeng
 * CreateDate:     2019/11/15 21:37
 */
abstract class MusicAdapter internal constructor(
    context: Context,
    resource: Int,
    objects: MutableList<MusicInfo?>
) : ArrayAdapter<MusicInfo?>(context, resource, objects) {
    var mList: MutableList<MusicInfo?>
    var mContext: Context?
    var mButtonWeakReference: WeakReference<ImageView?>? = null

    init {
        mList = objects
        this.mContext = context
    }

    fun update(list: MutableList<MusicInfo?>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnClickListener(view: View, playButton: ImageView, musicInfo: MusicInfo) {
        view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (MusicPlayerService.Companion.isPlaying) {
                    if (musicInfo.getId() == MusicPlayerService.Companion.sMusicID) {
                        MusicPlayerService.Companion.pauseMusic()
                        playButton.setImageResource(R.drawable.my_music_play_icon)
                    } else {
                        MusicPlayerService.Companion.playMusic(musicInfo)
                        if (mButtonWeakReference != null) {
                            mButtonWeakReference!!.get()!!
                                .setImageResource(R.drawable.my_music_play_icon)
                        }
                        playButton.setImageResource(R.drawable.my_music_pause_icon)
                        mButtonWeakReference = WeakReference<ImageView?>(playButton)
                    }
                } else {
                    if (musicInfo.getId() == MusicPlayerService.Companion.sMusicID) {
                        MusicPlayerService.Companion.playMusic(musicInfo)
                        playButton.setImageResource(R.drawable.my_music_pause_icon)
                    } else if (MusicPlayerService.Companion.sMusicID == null) {
                        MusicPlayerService.Companion.playMusic(musicInfo)
                        playButton.setImageResource(R.drawable.my_music_pause_icon)
                        mButtonWeakReference = WeakReference<ImageView?>(playButton)
                    } else {
                        MusicPlayerService.Companion.playMusic(musicInfo)
                        if (mButtonWeakReference != null) {
                            mButtonWeakReference!!.get()!!
                                .setImageResource(R.drawable.my_music_play_icon)
                        }
                        playButton.setImageResource(R.drawable.my_music_pause_icon)
                        mButtonWeakReference = WeakReference<ImageView?>(playButton)
                    }
                }
            }
        })
    }

    protected open class ViewHolder {
        var icon: ImageView? = null
        var playButton: ImageView? = null
        var songName: TextView? = null
    }

    companion object {
        private const val TAG = "MusicAdapter"
    }
}
