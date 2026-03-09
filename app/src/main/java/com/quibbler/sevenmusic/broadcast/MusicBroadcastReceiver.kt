package com.quibbler.sevenmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quibbler.sevenmusic.listener.BroadcastDatabaseListener;
import com.quibbler.sevenmusic.listener.BroadcastListener;
import com.quibbler.sevenmusic.listener.BroadcastMusicPlayListener;
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener;

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      MusicBroadcastReceiver
 * Description:    本应用内的广播接收
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 17:37
 */
public class MusicBroadcastReceiver extends BroadcastReceiver {
    private BroadcastListener mListener = null;

    public MusicBroadcastReceiver(BroadcastListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case MusicBroadcastManager.MUSIC_GLOBAL_PLAY:
                ((BroadcastMusicPlayListener) mListener).onMusicPlay();
                break;
            case MusicBroadcastManager.MUSIC_GLOBAL_PAUSE:
                ((BroadcastMusicPlayListener) mListener).onMusicPause();
                break;
            case MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE:
                ((BroadcastDatabaseListener) mListener).onDatabaseChanged();
                break;
            case MusicBroadcastManager.MUSIC_GLOBAL_NO_COPYRIGHT:
                ((BroadcastMusicStateChangeListener) mListener).onNoCopyright();
                break;
            case MusicBroadcastManager.MUSIC_GLOBAL_SOMETHING_WRONG:
                ((BroadcastMusicStateChangeListener) mListener).onSomethingWrong();
                break;
            default:
                break;
        }
    }
}
