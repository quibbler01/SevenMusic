package com.quibbler.sevenmusic.broadcast

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.quibbler.sevenmusic.MusicApplication

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      MusicBroadcastManager
 * Description:    发送本应用本地全局广播
 * 注意全局的广播只能使用Activity中的registerReceiver()方法进行注册，否则如果使用本地广播管理器注册的话是接收不到全局广播的
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 17:33
 */
object MusicBroadcastManager {
    const val AUDIO_BECOMING_NOISY: String = "android.media.AUDIO_BECOMING_NOISY"
    const val SYSTEM_BROADCAST_NETWORK_CHANGE: String = "android.net.conn.CONNECTIVITY_CHANGE"
    const val MUSIC_GLOBAL_PLAY_COMPLETION: String = "com.quibbler.sevenmusic.music.play.completion"
    const val MUSIC_GLOBAL_PLAY: String = "quibbler.com.sevenmusic.global.play" //开始播放通知广播
    const val MUSIC_GLOBAL_PAUSE: String =
        "quibbler.com.sevenmusic.global.pause" //播放暂停通知广播，前台可据此切换状态
    const val MUSIC_GLOBAL_NO_COPYRIGHT: String =
        "quibbler.com.sevenmusic.global.copyright" //没有版权发送一条通知广播
    const val MUSIC_GLOBAL_SOMETHING_WRONG: String =
        "quibbler.com.sevenmusic.global.something.wrong" //播放出错通知广播
    const val MUSIC_GLOBAL_DATABASE_UPDATE: String =
        "quibbler.com.sevenmusic.global.database.update" //本地数据库变化通知广播
    const val MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS: String =
        "quibbler.com.sevenmusic.global.download.success" //歌曲下载成功
    const val MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED: String =
        "quibbler.com.sevenmusic.global.download.failed" //歌曲下载失败
    const val MUSIC_GLOBAL_TIMING_STOP_PLAY: String =
        "com.quibbler.sevenmusic.view.global.timingstopplay" //定时停止播放通知广播
    const val MUSIC_GLOBAL_PLAY_BAR_UPDATE: String =
        "com.quibbler.sevenmusic.view.global.playbar" //音乐播放条播放列表歌曲点击通知广播
    const val MUSIC_GLOBAL_MUSIC_PLAY_PROGRESSBAR_UPDATE: String =
        "com.quibbler.sevenmusic.update_progressbar_broadcast" //音乐播放界面进度条更新通知广播

    const val MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_ONE: String =
        "quibbler.com.sevenmusic.global.main.index.one" //改变索引
    const val MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_TWO: String =
        "quibbler.com.sevenmusic.global.main.index.two" //
    const val MAIN_ACTIVITY_CHANGE_VIEW_PAGER_INDEX_THREE: String =
        "quibbler.com.sevenmusic.global.main.index.three" //


    private val mLocalBroadcastManager =
        LocalBroadcastManager.getInstance(MusicApplication.Companion.getContext())

    fun sendBroadcast(intent: Intent) {
        mLocalBroadcastManager.sendBroadcast(intent)
    }

    fun sendBroadcast(action: String?) {
        val intent = Intent(action)
        mLocalBroadcastManager.sendBroadcast(intent)
    }

    fun registerMusicBroadcastReceiver(receiver: BroadcastReceiver) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(MUSIC_GLOBAL_PLAY)
        intentFilter.addAction(MUSIC_GLOBAL_PAUSE)
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter)
    }

    fun registerMusicBroadcastReceiver(receiver: BroadcastReceiver, action: String?) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(action)
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter)
    }

    fun registerMusicBroadcastReceiver(receiver: BroadcastReceiver, intentFilter: IntentFilter) {
        mLocalBroadcastManager.registerReceiver(receiver, intentFilter)
    }

    fun unregisterMusicBroadcastReceiver(broadcastReceiver: BroadcastReceiver) {
        mLocalBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    fun registerMusicBroadcastReceiverForMusicIntent(broadcastReceiver: BroadcastReceiver) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(MUSIC_GLOBAL_PLAY)
        intentFilter.addAction(MUSIC_GLOBAL_PAUSE)
        intentFilter.addAction(MUSIC_GLOBAL_NO_COPYRIGHT)
        intentFilter.addAction(MUSIC_GLOBAL_SOMETHING_WRONG)
        mLocalBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }
}


