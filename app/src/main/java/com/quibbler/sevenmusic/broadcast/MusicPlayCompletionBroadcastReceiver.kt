package com.quibbler.sevenmusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      MusicPlayCompletionBroadcastReceiver
 * Description:    后台暂时不用
 * Author:         11103905
 * CreateDate:     2019/9/26 15:52
 */
class MusicPlayCompletionBroadcastReceiver : BroadcastReceiver() {
    override              /**
              * Brief description for onReceive.
              *
              * @param context the operating context
              * @return the result of the operation
              */
fun onReceive(context: Context?, intent: Intent?) {
                 Log.d("MusicPlayCompletionBroadcastReceiver", "onReceive() called")
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
