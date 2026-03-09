package com.quibbler.sevenmusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quibbler.sevenmusic.listener.BroadcastDatabaseListener
import com.quibbler.sevenmusic.listener.BroadcastListener
import com.quibbler.sevenmusic.listener.BroadcastMusicPlayListener
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      MusicBroadcastReceiver
 * Description:    本应用内的广播接收
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 17:37
 */
class MusicBroadcastReceiver(mListener: BroadcastListener?) : BroadcastReceiver() {
    private var mListener: BroadcastListener? = null

    init {
        this.mListener = mListener
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.getAction()
        when (action) {
            MusicBroadcastManager.MUSIC_GLOBAL_PLAY -> (mListener as BroadcastMusicPlayListener).onMusicPlay()
            MusicBroadcastManager.MUSIC_GLOBAL_PAUSE -> (mListener as BroadcastMusicPlayListener).onMusicPause()
            MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE -> (mListener as BroadcastDatabaseListener).onDatabaseChanged()
            MusicBroadcastManager.MUSIC_GLOBAL_NO_COPYRIGHT -> (mListener as BroadcastMusicStateChangeListener).onNoCopyright()
            MusicBroadcastManager.MUSIC_GLOBAL_SOMETHING_WRONG -> (mListener as BroadcastMusicStateChangeListener).onSomethingWrong()
            else -> {}
        }
    }
}
