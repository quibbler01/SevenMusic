package com.quibbler.sevenmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.SeekBar;

import com.quibbler.sevenmusic.activity.song.MusicPlayActivity;
import com.quibbler.sevenmusic.service.MusicPlayerService;

/**
  *
  * Package:        com.quibbler.sevenmusic.broadcast
  * ClassName:      UpdateProgressBarReceiver
  * Description:    更新进度条广播
  * Author:         lishijun
  * CreateDate:     2019/10/10 16:51
 */
public class UpdateProgressBarReceiver extends BroadcastReceiver {

    private static final String TAG = "UpdateProgressBarReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayActivity activity = MusicPlayActivity.getInstance();
        if(activity != null){
            SeekBar progressBar = activity.getPlayBar();
            if(progressBar != null){
                int progress = MusicPlayerService.getPlayProgress();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
                Log.i(TAG,"UpdateProgressBarReceiver");
            }
        }
    }
}
