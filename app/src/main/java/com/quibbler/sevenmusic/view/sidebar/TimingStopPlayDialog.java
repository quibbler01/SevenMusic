package com.quibbler.sevenmusic.view.sidebar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.utils.ResUtil;

import java.util.Calendar;

import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_TIMING_STOP_PLAY;

/**
 * Package:        com.quibbler.sevenmusic.view.sidebar
 * ClassName:      TimingStopPlayDialog
 * Description:    定时停止播放对话框类
 * Author:         11103876
 * CreateDate:     2019/9/27 19:18
 */
public class TimingStopPlayDialog implements View.OnClickListener {

    /**
     * 不开启
     */
    private TextView mSidebarTimingStopPlayCloseTv;
    /**
     * 10分钟后关闭
     */
    private TextView mSidebarTimingStopPlayCloseTenMinuteTv;
    /**
     * 20分钟后关闭
     */
    private TextView mSidebarTimingStopPlayCloseTwentyMinuteTv;
    /**
     * 30分钟后关闭
     */
    private TextView mSidebarTimingStopPlayCloseThirtyMinuteTv;
    /**
     * 40分钟后关闭
     */
    private TextView mSidebarTimingStopPlayCloseFortyMinuteTv;
    /**
     * 60分钟后关闭
     */
    private TextView mSidebarTimingStopPlayCloseSixtyMinuteTv;
    /**
     * 自定义时间
     */
    private TextView mSidebarTimingStopPlayCloseCustomMinuteTv;
    /**
     * 上下文对象
     */
    private Context mContext;
    /**
     * AlertDialog对象实例
     */
    private AlertDialog mAlertDialog;

    /**
     * 定时停止播放设置的时间(单位：毫秒)
     */
    private long mTime;
    private int hour;
    private int minute;
    private String close = "";
    /**
     * Intent对象实例，用于传递数据
     */
    private Intent intent = new Intent(MUSIC_GLOBAL_TIMING_STOP_PLAY);

    /**
     * 描述：构造函数-初始化定时停止播放对话框
     *
     * @param context
     */
    public TimingStopPlayDialog(Context context) {
        mContext = context;
//        mAlartDialog = new AlertDialog.Builder(mContext).setTitle(ResUtil.getString(R.string.str_timing_stop_play_dialog_title)).setView(initView()).create();
        mAlertDialog = new AlertDialog.Builder(mContext).setView(initView()).create();
        mAlertDialog.show();

    }

    /**
     * 描述：初始化组件
     *
     * @return
     */
    private View initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sidebar_timing_stop_play_dialog, null, false);

        mSidebarTimingStopPlayCloseTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close);
        mSidebarTimingStopPlayCloseTenMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_ten_minute);
        mSidebarTimingStopPlayCloseTwentyMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_twenty_minute);
        mSidebarTimingStopPlayCloseThirtyMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_thirty_minute);
        mSidebarTimingStopPlayCloseFortyMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_forty_minute);
        mSidebarTimingStopPlayCloseSixtyMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_sixty_minute);
        mSidebarTimingStopPlayCloseCustomMinuteTv = view.findViewById(R.id.sidebar_tv_timing_stop_play_close_custom_minute);

        mSidebarTimingStopPlayCloseTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseTenMinuteTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseTwentyMinuteTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseThirtyMinuteTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseFortyMinuteTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseSixtyMinuteTv.setOnClickListener(this);
        mSidebarTimingStopPlayCloseCustomMinuteTv.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        Drawable nav_right = mContext.getResources().getDrawable(R.drawable.sidebar_music_alarm_cycle_check);  // 获取选择的打勾图像
        nav_right.setBounds(0, 0, nav_right.getMinimumWidth(), nav_right.getMinimumHeight());

        if (v.getId() == R.id.sidebar_tv_timing_stop_play_close) {
            if (mSidebarTimingStopPlayCloseTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseTv.setCompoundDrawables(null, null, null, null);
            }
            close = ResUtil.getString(R.string.str_timing_stop_play_close);
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_ten_minute) {
            if (mSidebarTimingStopPlayCloseTenMinuteTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseTenMinuteTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseTenMinuteTv.setCompoundDrawables(null, null, null, null);
            }
            mTime = 10 * 60 * 1000;// 10分钟（单位毫秒）
            close = "";
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_twenty_minute) {
            if (mSidebarTimingStopPlayCloseTwentyMinuteTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseTwentyMinuteTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseTwentyMinuteTv.setCompoundDrawables(null, null, null, null);
            }
            mTime = 20 * 60 * 1000;//20分钟（单位毫秒）
            close = "";
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_thirty_minute) {
            if (mSidebarTimingStopPlayCloseThirtyMinuteTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseThirtyMinuteTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseThirtyMinuteTv.setCompoundDrawables(null, null, null, null);
            }
            mTime = 30 * 60 * 1000; // 30分钟（单位毫秒）
            close = "";
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_forty_minute) {
            if (mSidebarTimingStopPlayCloseFortyMinuteTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseFortyMinuteTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseFortyMinuteTv.setCompoundDrawables(null, null, null, null);
            }
            mTime = 40 * 60 * 1000;// 40分钟（单位毫秒）
            close = "";
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_sixty_minute) {
            if (mSidebarTimingStopPlayCloseSixtyMinuteTv.getCompoundDrawables()[2] == null) {
                mSidebarTimingStopPlayCloseSixtyMinuteTv.setCompoundDrawables(null, null, nav_right, null);
            } else {
                mSidebarTimingStopPlayCloseSixtyMinuteTv.setCompoundDrawables(null, null, null, null);
            }
            mTime = 60 * 60 * 1000;// 60分钟（单位毫秒）
            close = "";
            mAlertDialog.dismiss();
        } else if (v.getId() == R.id.sidebar_tv_timing_stop_play_close_custom_minute) {
            showCustomMinuteDialog();// 自定义时间（单位毫秒）,在此处要单独对其进行时间设置和广播发送，并rerun掉
            mAlertDialog.dismiss();
        }
        if (ResUtil.getString(R.string.str_timing_stop_play_close).equals(close)) {
            Toast.makeText(mContext, ResUtil.getString(R.string.str_timing_stop_play_cancel), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, ResUtil.getString(R.string.str_timing_stop_play_set_success_1) + mTime / (60 * 1000)
                    + ResUtil.getString(R.string.str_timing_stop_play_set_success_2), Toast.LENGTH_SHORT).show();
        }
        intent.putExtra("close", close);
        intent.putExtra("time", mTime);
        MusicBroadcastManager.sendBroadcast(intent);
    }


    /**
     * 描述：显示定时停止播放对话框
     */
    public static void showTimingStopPlayDialog(Context context) {
        TimingStopPlayDialog timingStopPlayDiglog = new TimingStopPlayDialog(context);
    }


    /**
     * 描述：设置自定义定时关闭时间
     */
    private void showCustomMinuteDialog() {
        Calendar calendar = Calendar.getInstance();  // 通过Calendar对象获取时、分信息
//        hour = calendar.get(Calendar.HOUR_OF_DAY);
//        minute = calendar.get(Calendar.MINUTE);
        // 时间选择对话框
        TimePickerDialog mTimePickDialog = new TimePickerDialog(mContext, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minuteOfHour);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                mTime = hour * 60 * 60 * 1000 + minute * 60 * 1000;// 自定义选择的时间（单位毫秒）
                intent.putExtra("close", close);
                intent.putExtra("time", mTime);
                MusicBroadcastManager.sendBroadcast(intent);
                Toast.makeText(mContext, "设置成功，将于" + hour + "小时" + minute + "分钟后关闭", Toast.LENGTH_SHORT).show();

            }
        }, hour, minute, true); // true表示设置为24小时制
        mTimePickDialog.show();
    }
}
