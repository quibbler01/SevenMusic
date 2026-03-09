package com.quibbler.sevenmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.activity.LockScreenActivity;
import com.quibbler.sevenmusic.service.MusicPlayerService;

/**
 * Package:        com.quibbler.sevenmusic.broadcast
 * ClassName:      LockScreenBroadcastReceiver
 * Description:    熄屏广播接收
 * Author:         yanwuyang
 * CreateDate:     2019/9/27 11:41
 */
public class LockScreenBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MusicPlayerService.isPlaying) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Intent lockScreenIntent = new Intent(MusicApplication.getContext(), LockScreenActivity.class);
                //标志位FLAG_ACTIVITY_NEW_TASK等同于singleTask。记得在manifest中为锁屏activity设置taskAffinity，让这个activity不要与app一个栈
                //标志位FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS，是为了避免在最近使用程序列表出现Service所启动的Activity。
                lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(lockScreenIntent);
            }
        }
    }
}
