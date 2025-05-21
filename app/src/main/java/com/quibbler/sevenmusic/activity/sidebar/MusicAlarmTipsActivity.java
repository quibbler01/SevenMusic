package com.quibbler.sevenmusic.activity.sidebar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

public class MusicAlarmTipsActivity extends AppCompatActivity {
    /**
     * 媒体播放器实例
     */
    private MediaPlayer mMediaPlayer;
    /**
     * 对话框实例
     */
    private AlertDialog mAlertDialog;
    /**
     * 对话框Builder实例
     */
    private AlertDialog.Builder mBuilder;
    /**
     * 闹钟歌曲名字实例
     */
    private String mSongName;
    /**
     * 闹钟歌曲文件路径实例
     */
    private String mSongPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    /**
     * 描述：初始化闹钟提醒对话框
     */
    private void initView() {

        mSongName = SharedPreferencesUtils.getInstance().getData(Constant.KEY_MUSIC_ALARM_SONG_NAME, "").toString();
        mSongPath = SharedPreferencesUtils.getInstance().getData(Constant.KEY_MUSIC_ALARM_SONG_PATH, "").toString();
        mAlertDialog = null;
        mBuilder = new AlertDialog.Builder(this);
        alarmPlayMusic();
        mAlertDialog = mBuilder
                .setTitle(ResUtil.getString(R.string.str_alarm_tips_dialog_title))
                .setMessage(ResUtil.getString(R.string.str_alarm_tips_dialog_msg) + mSongName)
                .setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alarmStopMusic();
                        MusicAlarmTipsActivity.this.finish();
                    }
                }).setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alarmStopMusic();
                        MusicAlarmTipsActivity.this.finish();
                    }
                }).create();//创建
        mAlertDialog.show();
    }

    /**
     * 描述：播放闹钟音乐
     */
    private void alarmPlayMusic() {

        try {
            if (mSongPath != null) {
                Uri uri = Uri.parse(mSongPath);
                mMediaPlayer = MediaPlayer.create(this, uri);   // 从SD卡加载音乐
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();   // 播放音乐闹钟
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 描述：停止闹钟音乐
     */
    private void alarmStopMusic() {
        mMediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
