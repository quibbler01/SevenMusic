package com.quibbler.sevenmusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.activity.sidebar.MusicAlarmTipsActivity
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      AlarmReceiver
 * Description:    音乐闹钟广播接收类
 * Author:         11103876
 * CreateDate:     2019/9/30 17:41
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val songName = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_MUSIC_ALARM_SONG_NAME, "").toString()
        val songPath = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_MUSIC_ALARM_SONG_PATH, "").toString()
        val time = intent.getStringExtra("time")

        val intentAlarm = Intent(context, MusicAlarmTipsActivity::class.java)
        intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intentAlarm)

        Log.i(
            "AlarmReceiver",
            "onReceive 方法中，歌曲名字 = " + songName + ", 歌曲路径 = " + songPath + ", 设置闹钟时间：" + time + " , 当前触发时间：" + this.time
        )
    }

    private val time: String
        get() {
            val time =
                System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
            val format =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d1 = Date(time)
            val t1 = format.format(d1)
            return t1
        }
}
