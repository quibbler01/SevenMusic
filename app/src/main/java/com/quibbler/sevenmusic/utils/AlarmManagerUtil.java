package com.quibbler.sevenmusic.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.broadcast.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

/**
 * Package:        com.quibbler.alarm.utils
 * ClassName:      AlarmManagerUtil
 * Description:    闹钟管理工具类
 * Author:         11103876
 * CreateDate:     2019/9/25 16:05
 */
public class AlarmManagerUtil {

    public static final String ALARM_ACTION = "com.quibbler.alarm.utils.clock";

    /**
     * 描述：设置闹钟时间
     *
     * @param context
     * @param timeInMillis
     * @param intent
     */
    public static void setAlarmTime(Context context, long timeInMillis, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, intent.getIntExtra("id",
                0), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        int interval = (int) intent.getLongExtra("intervalMillis", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, timeInMillis, interval, sender);
        }

    }

    /**
     * 描述：取消闹钟
     *
     * @param context
     * @param action
     * @param id
     */
    public static void cancelAlarm(Context context, String action, int id) {
        Intent intent = new Intent(ALARM_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * 描述：设置闹钟
     *
     * @param context
     * @param flag    周期性时间间隔的标志,flag = 0 表示一次性的闹钟, flag = 1 表示每天提醒的闹钟(1天的时间间隔),flag = 2
     *                表示按周每周提醒的闹钟（一周的周期性时间间隔）
     * @param hour    时
     * @param minute  分
     * @param id      闹钟的id
     * @param week    week=0表示一次性闹钟或者按天的周期性闹钟，非0 的情况下是几就代表以周为周期性的周几的闹钟
     * @param tips    闹钟提示信息
     */
    public static void setAlarm(Context context, int flag, int hour, int minute, int id, int
            week, String tips) {
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);   // 得到AlarmManager实例
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        String time = hour + ":" + minute;  // 闹钟设置的时间
        intent.putExtra("time", time);
        intent.putExtra("msg", "铃声");

        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));     // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                hour, minute, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {   //API19以上使用
            if (flag == 0) { // flag = 0 表示设置每天重复闹钟
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis()),
                        AlarmManager.INTERVAL_DAY, sender);
            } else {  // flag = -1 表示设置一次重复闹钟
                am.setExact(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis()), sender);
            }
        } else {
            if (flag == 0) {
                am.setRepeating(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis()),
                        AlarmManager.INTERVAL_DAY, sender);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            }
        }
        Toast.makeText(context, ResUtil.getString(R.string.str_alarm_setting_toast_msg_1) + getTime(calMethod(week, calendar.getTimeInMillis()))
                + ResUtil.getString(R.string.str_alarm_setting_toast_msg_2), Toast.LENGTH_LONG).show();
    }


    /**
     * 描述：闹钟时间设置方法
     *
     * @param weekflag 传入的是周几
     * @param dateTime 传入的是时间戳（设置当天的年月日+从选择框拿来的时分秒）
     * @return 返回起始闹钟时间的时间戳
     */
    public static long calMethod(int weekflag, long dateTime) {
        long time = 0;
        if (weekflag != 0) {  // weekflag == 0表示是按天为周期性的时间间隔或者是一次行的，weekfalg非0时表示每周几的闹钟并以周为时间间隔
            Calendar c = Calendar.getInstance();
            int week = c.get(Calendar.DAY_OF_WEEK);
            if (1 == week) {
                week = 7;
            } else if (2 == week) {
                week = 1;
            } else if (3 == week) {
                week = 2;
            } else if (4 == week) {
                week = 3;
            } else if (5 == week) {
                week = 4;
            } else if (6 == week) {
                week = 5;
            } else if (7 == week) {
                week = 6;
            }

            if (weekflag == week) {
                if (dateTime > System.currentTimeMillis()) {
                    time = dateTime;
                } else {
                    time = dateTime + 7 * 24 * 3600 * 1000;
                }
            } else if (weekflag > week) {
                time = dateTime + (weekflag - week) * 24 * 3600 * 1000;
            } else if (weekflag < week) {
                time = dateTime + (weekflag - week + 7) * 24 * 3600 * 1000;
            }
        } else {
            if (dateTime > System.currentTimeMillis()) {
                time = dateTime;
            } else {
                time = dateTime + 24 * 3600 * 1000;
            }
        }
        return time;
    }

    /**
     * 描述：设置推迟闹钟的时间
     *
     * @param context
     * @param timeInMillis
     * @param id
     * @param intervalMillis
     */
    public static void setAlarmLaterInMinute(Context context, long timeInMillis, int id, int intervalMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(ALARM_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, intervalMillis, sender);

    }

    /**
     * 描述:获取格式化的时间
     *
     * @param time
     * @return
     */
    private static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        return t1;
    }
}
