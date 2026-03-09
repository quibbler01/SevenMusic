package com.quibbler.sevenmusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity
import com.quibbler.sevenmusic.service.MusicPlayerService

/**
 * 
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      UpdateProgressBarReceiver
 * Description:    更新进度条广播
 * Author:         lishijun
 * CreateDate:     2019/10/10 16:51
 */
class UpdateProgressBarReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val activity: MusicPlayActivity? = MusicPlayActivity.Companion.getInstance()
        if (activity != null) {
            val progressBar = activity.getPlayBar()
            if (progressBar != null) {
                val progress: Int = MusicPlayerService.Companion.getPlayProgress()
                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        progressBar.setProgress(progress)
                    }
                })
                Log.i(TAG, "UpdateProgressBarReceiver")
            }
        }
    }

    companion object {
        private const val TAG = "UpdateProgressBarReceiver"
    }
}
