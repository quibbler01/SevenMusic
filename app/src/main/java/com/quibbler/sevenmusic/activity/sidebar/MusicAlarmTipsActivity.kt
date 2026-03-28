package com.quibbler.sevenmusic.activity.sidebar

import android.app.AlertDialog
import android.content.DialogInterface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

class MusicAlarmTipsActivity : AppCompatActivity() {
    /**
     * 媒体播放器实例
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * 对话框实例
     */
    private var mAlertDialog: AlertDialog? = null

    /**
     * 对话框Builder实例
     */
    private var mBuilder: AlertDialog.Builder? = null

    /**
     * 闹钟歌曲名字实例
     */
    private var mSongName: String? = null

    /**
     * 闹钟歌曲文件路径实例
     */
    private var mSongPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    /**
     * 描述：初始化闹钟提醒对话框
     */
    private fun initView() {
        mSongName = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_MUSIC_ALARM_SONG_NAME, "").toString()
        mSongPath = SharedPreferencesUtils.Companion.getInstance()
            .getData(Constant.KEY_MUSIC_ALARM_SONG_PATH, "").toString()
        mAlertDialog = null
        mBuilder = AlertDialog.Builder(this)
        alarmPlayMusic()
        mAlertDialog = mBuilder!!
            .setTitle(ResUtil.getString(R.string.str_alarm_tips_dialog_title))
            .setMessage(ResUtil.getString(R.string.str_alarm_tips_dialog_msg) + mSongName)
            .negativeButton = 
                ResUtil.getString(R.string.str_dialog_btn_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        alarmStopMusic()
                        this@MusicAlarmTipsActivity.finish()
                    }
                }).setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_confirm),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        alarmStopMusic()
                        this@MusicAlarmTipsActivity.finish()
                    }
                }).create() //创建
        mAlertDialog!!.show()
    }

    /**
     * 描述：播放闹钟音乐
     */
    private fun alarmPlayMusic() {
        try {
            if (mSongPath != null) {
                val uri = Uri.parse(mSongPath)
                mMediaPlayer = MediaPlayer.create(this, uri) // 从SD卡加载音乐
                mMediaPlayer!!.setLooping(true)
                mMediaPlayer!!.start() // 播放音乐闹钟
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 描述：停止闹钟音乐
     */
    private fun alarmStopMusic() {
        mMediaPlayer!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
}
