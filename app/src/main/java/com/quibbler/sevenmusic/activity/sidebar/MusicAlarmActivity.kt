package com.quibbler.sevenmusic.activity.sidebar;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.BaseActivity;
import com.quibbler.sevenmusic.activity.my.ChooseMusicActivity;
import com.quibbler.sevenmusic.utils.AlarmManagerUtil;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;
import com.quibbler.sevenmusic.view.sidebar.SelectRemindCyclePopup;

import java.util.Calendar;

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      MusicAlarmActivity
 * Description:    音乐闹钟类
 * Author:         11103876
 * CreateDate:     2019/9/27 17:30
 */
public class MusicAlarmActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "MusicAlarmActivity";

    /**
     * 请求码
     */
    private static final int REQUEST_CODE = 1;
    /**
     * 结果码
     */
    private final int RESULT_OK = 0;

    /**
     * 闹钟开关表示，默认是关闭的
     */
    private boolean isOpen = false;
    /**
     * 闹钟设定的时间
     */
    private String mTime;
    /**
     * 闹钟提醒频率识别id，默认是-1，表示设置一次重复闹钟
     */
    private int mCycle = -1;

    /**
     * 时间对话框实例
     */
    private TimePickerDialog mTimePickDialog;
    /**
     * 音乐闹钟最外层布局实例
     */
    private LinearLayout mSidebarMusicAlarmAllLayout;
    /**
     * 音乐闹钟界面返回图标实例
     */
    private Toolbar mSidebarMusicAlarmToolbar;
    /**
     * 闹钟显示时间布局实例
     */
    private RelativeLayout mSidebarAlarmTimeLayout;
    /**
     * 闹钟显示时间实例
     */
    private TextView mSidebarAlarmTimeTv;
    /**
     * 闹钟开关实例
     */
    private Switch mSidebarAlarmSw;
    /**
     * 闹钟铃声布局实例
     */
    private RelativeLayout mSidebarRingLayout;
    /**
     * 闹钟铃声选择结果实例
     */
    private TextView mSidebarRingValueTv;
    /**
     * 闹钟重复布局实例
     */
    private RelativeLayout mSidebarRepeatLayout;
    /**
     * 闹钟重复选择结果（一次or每天）
     */
    private TextView mSidebarRepeatValueTv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_music_alarm_activity);
        initView();
    }

    /**
     * 描述：初始化设置界面控件
     */
    private void initView() {
        mSidebarMusicAlarmAllLayout = findViewById(R.id.sidebar_ll_music_alarm_all_layout);
        mSidebarMusicAlarmToolbar = findViewById(R.id.sidebar_toolbar_music_alarm);
        mSidebarAlarmTimeLayout = findViewById(R.id.sidebar_rl_alarm_time);
        mSidebarAlarmTimeTv = findViewById(R.id.sidebar_tv_alarm_time);
        mSidebarAlarmSw = findViewById(R.id.sidebar_sw_alarm_time);
        mSidebarRingLayout = findViewById(R.id.sidebar_rl_alarm_ring);
        mSidebarRingValueTv = findViewById(R.id.sidebar_tv_alarm_ring_value);
        mSidebarRepeatLayout = findViewById(R.id.sidebar_rl_alarm_repeat);
        mSidebarRepeatValueTv = findViewById(R.id.sidebar_tv_alarm_repeat_value);


        setSupportActionBar(mSidebarMusicAlarmToolbar);    // 为设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    //添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSidebarAlarmTimeLayout.setOnClickListener(this);   // 闹钟显示时间布局点击事件
        mSidebarAlarmTimeLayout.setClickable(false);   // 初始默认该布局不可点击
        mSidebarAlarmSw.setOnCheckedChangeListener(this);     // 闹钟显示开关点击事件
        mSidebarRingLayout.setOnClickListener(this);      // 闹钟铃声布局点击事件
        mSidebarRingLayout.setClickable(false);   // 初始默认该布局不可点击
        mSidebarRepeatLayout.setOnClickListener(this);    // 闹钟重复布局点击事件
        mSidebarRepeatLayout.setClickable(false);       // 初始默认该布局不可点击


        final int hour;  // 初始化显示闹钟时间，没有则设置显示当前时间
        final int minute;
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(mTime)) {  // 闹钟时间不为空
            String[] times = mTime.split(":");
            hour = Integer.parseInt(times[0]);
            minute = Integer.parseInt(times[1]);
        } else { // 闹钟时间为空
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        // 时间选择对话框
        mTimePickDialog = new TimePickerDialog(MusicAlarmActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                if (hourOfDay < 10) {
                    mTime = "0" + hourOfDay;
                } else {
                    mTime = hourOfDay + "";
                }
                if (minuteOfDay < 10) {
                    mTime = mTime + ":0" + minuteOfDay;
                } else {
                    mTime = mTime + ":" + minuteOfDay;
                }
                mSidebarAlarmTimeTv.setText(mTime);// 显示选择的时间
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_TIME, mTime);  // 保存音乐闹钟设置的时间
                setClock();    // 设置闹钟的方法要在点击时间对话框按钮后，自动进行设置，故放在此处
            }
        }, hour, minute, true);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sidebar_rl_alarm_time) {// 显示时间对话框,设置时间后就自动进行闹钟设置
            mTimePickDialog.show();
        } else if (v.getId() == R.id.sidebar_rl_alarm_ring) {// 闹钟提醒铃声选择
            Intent intent = new Intent(MusicAlarmActivity.this, ChooseMusicActivity.class);   // 跳转到MyLocalMusicActivity，实现本地音乐铃声选择
            startActivityForResult(intent, REQUEST_CODE);
        } else if (v.getId() == R.id.sidebar_rl_alarm_repeat) {// 闹钟提醒频率方式
            selectRemindCycle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.sidebar_sw_alarm_time) { // 闹钟开关按钮
            if (isChecked) { // 开关打开，其他三个布局可点击
                mSidebarAlarmTimeLayout.setClickable(true);
                mSidebarRingLayout.setClickable(true);
                mSidebarRepeatLayout.setClickable(true);
            } else { // 开关关闭，其他三个布局不可点击
                mSidebarAlarmTimeLayout.setClickable(false);
                mSidebarRingLayout.setClickable(false);
                mSidebarRepeatLayout.setClickable(false);
            }
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SWITCH, isChecked); // 保存音乐闹钟Switch按钮的开闭状态
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 退出当前Activity,返回主界面
                finish();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 描述：设置闹钟
     */
    private void setClock() {
        if (mTime != null && mTime.length() > 0) {
            String[] times = mTime.split(":");   // times[0]为小时，times[1]为分钟
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    Integer.parseInt(times[0]), Integer.parseInt(times[1]), 0);
            if (mCycle == -1) { // 是一次的闹钟
                AlarmManagerUtil.setAlarm(this, -1, Integer.parseInt(times[0]), Integer.parseInt(times[1]), -1, 0, "Alarming");
            } else if (mCycle == 0) { // 是每天的闹钟
                AlarmManagerUtil.setAlarm(this, 0, Integer.parseInt(times[0]), Integer.parseInt(times[1]), 0, 0, "Alarming");
            } else {  // 多选，周几的闹钟,暂时不能实现
                String weeksStr = parseRepeat(mCycle, 1);
                String[] weeks = weeksStr.split(",");
                for (int i = 1; i <= weeks.length; i++) {
                    AlarmManagerUtil.setAlarm(this, 2, Integer.parseInt(times[0]), Integer.parseInt(times[1]), i, Integer.parseInt(weeks[i - 1]), "Alarming");
                    Log.d(TAG, "weeks:" + weeks[i - 1] + ",设置时间：" + times[0] + ":" + times[1]);
                }
            }

        } else {
            Toast.makeText(this, ResUtil.getString(R.string.str_alarm_setting_tips), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 描述：设置闹钟提醒频率
     */
    private void selectRemindCycle() {
        final SelectRemindCyclePopup fp = new SelectRemindCyclePopup(this);
        fp.showPopup(mSidebarMusicAlarmAllLayout);   // 设置在哪个父布局上显示弹出窗口
        fp.setOnSelectRemindCyclePopupListener(new SelectRemindCyclePopup.SelectRemindCyclePopupOnClickListener() {
            @Override
            public void obtainMessage(int flag, String ret) {
                switch (flag) {
                    case 0: // 星期一
                        break;
                    case 1: // 星期二
                        break;
                    case 2: // 星期三
                        break;
                    case 3: // 星期四
                        break;
                    case 4:// 星期五
                        break;
                    case 5:// 星期六
                        break;
                    case 6:  // 星期日
                        break;
                    case 7:  // 确定
                        int repeat = Integer.valueOf(ret);
                        mSidebarRepeatValueTv.setText(parseRepeat(repeat, 0));
                        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SONG_REPEAT, mSidebarRepeatValueTv.getText().toString()); // 保存音乐闹钟歌曲设置的重复频率，周一到周日是通过点击确定触发保存
                        mCycle = repeat;
                        fp.dismiss();
                        break;
                    case 8:
                        mSidebarRepeatValueTv.setText(ResUtil.getResources().getString(R.string.str_alarm_reminder_frequency_everyday));
                        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SONG_REPEAT, mSidebarRepeatValueTv.getText().toString()); // 保存音乐闹钟歌曲设置的重复频率，每天和一次闹钟设置是单独通过单击按钮触发保存的，不通过确定按钮
                        mCycle = 0;
                        fp.dismiss();
                        break;
                    case 9:
                        mSidebarRepeatValueTv.setText(ResUtil.getResources().getString(R.string.str_alarm_reminder_frequency_once));
                        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SONG_REPEAT, mSidebarRepeatValueTv.getText().toString()); // 保存音乐闹钟歌曲设置的重复频率
                        mCycle = -1;
                        fp.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });

    }

    /**
     * 描述：解析二进制闹钟周期
     *
     * @param repeat
     * @param flag   flag=0返回带有汉字的周一、周二、周三等，flag=1,返回weeks(1,2,3)
     * @return
     */
    public static String parseRepeat(int repeat, int flag) {
        String cycle = "";
        String weeks = "";
        if (repeat == 0) {
            repeat = 127;
        }
        if (repeat % 2 == 1) {
            cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_monday);
            weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_one);
        }
        if (repeat % 4 >= 2) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_tuesday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_two);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_tuesday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_two);
            }
        }
        if (repeat % 8 >= 4) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_wednesday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_three);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_wednesday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_three);
            }
        }
        if (repeat % 16 >= 8) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_thursday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_four);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_thursday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_four);
            }
        }
        if (repeat % 32 >= 16) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_friday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_five);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_friday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_five);
            }
        }
        if (repeat % 64 >= 32) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_saturday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_six);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_saturday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_six);
            }
        }
        if (repeat / 64 == 1) {
            if ("".equals(cycle)) {
                cycle = ResUtil.getString(R.string.str_alarm_reminder_frequency_sunday);
                weeks = ResUtil.getString(R.string.str_alarm_reminder_frequency_week_seven);
            } else {
                cycle = cycle + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_sunday);
                weeks = weeks + "," + ResUtil.getString(R.string.str_alarm_reminder_frequency_week_seven);
            }
        }
        return flag == 0 ? cycle : weeks;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Log.d(TAG, "onActivityResult");
                    String songName = data.getStringExtra("name");
                    songName = songName.substring(0, songName.lastIndexOf(".")); // 去除歌曲名称后缀
                    String songPath = data.getStringExtra("path");
                    mSidebarRingValueTv.setText(songName);
                    SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SONG_NAME, songName);   // 保存音乐闹钟设置的歌曲铃声
                    SharedPreferencesUtils.getInstance().saveData(Constant.KEY_MUSIC_ALARM_SONG_PATH, songPath);  // 保存音乐闹钟设置的歌曲铃声sd卡文件路径
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        recoveryMusicAlarmState();
    }

    /**
     * 描述：恢复音乐闹钟设置的参数状态
     */
    private void recoveryMusicAlarmState() {
        String time = SharedPreferencesUtils.getInstance().getData(Constant.KEY_MUSIC_ALARM_TIME,
                "").toString();  // 恢复上次设置的音乐闹钟时间
        if ("".equals(time)) { // 第一次进入时，time是""，要设置为默认值
            mSidebarAlarmTimeTv.setText(this.getString(R.string.str_alarm_default_time));
        } else {
            mSidebarAlarmTimeTv.setText(time);
        }
        mSidebarAlarmSw.setChecked((Boolean) SharedPreferencesUtils.getInstance().
                getData(Constant.KEY_MUSIC_ALARM_SWITCH, false));   // 恢复音乐闹钟的选中状态（布尔类型,此处false只表示布尔类型）
        if (mSidebarAlarmSw.isChecked()) {
            mSidebarAlarmTimeLayout.setClickable(true);
            mSidebarRingLayout.setClickable(true);
            mSidebarRepeatLayout.setClickable(true);
        } else {
            mSidebarAlarmTimeLayout.setClickable(false);
            mSidebarRingLayout.setClickable(false);
            mSidebarRepeatLayout.setClickable(false);
        }
        String name = SharedPreferencesUtils.getInstance().getData(Constant.KEY_MUSIC_ALARM_SONG_NAME,
                "").toString();     // 恢复上次设置的音乐闹钟歌曲名字
        if ("".equals(name)) {
            mSidebarRingValueTv.setText(ResUtil.getString(R.string.str_alarm_default_song_name)); // 默认显示的音乐闹钟歌曲名字
        } else {
            mSidebarRingValueTv.setText(name);
        }
        String repeat = SharedPreferencesUtils.getInstance().getData(Constant.KEY_MUSIC_ALARM_SONG_REPEAT,
                "").toString(); // 恢复上次设置的音乐闹钟歌曲重复频率
        if ("".equals(repeat)) {
            mSidebarRepeatValueTv.setText(ResUtil.getString(R.string.str_alarm_reminder_frequency_once));  //默认显示的音乐闹钟歌曲重复频率
        } else {
            mSidebarRepeatValueTv.setText(repeat);
        }
    }


}
