package com.quibbler.sevenmusic.activity;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      ActivityCollector
 * Description:    Activity管理公共类
 * CreateDate:     2019/9/16 21:59
 */


public class ActivityCollector {

    public static List<Activity> activityStack = new ArrayList<Activity>();

    /**
     * 描述：添加Activity到堆栈
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new ArrayList<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 描述：移除指定的Activity
     *
     * @param activity
     */
    public static void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity = null;
        }

    }

    /**
     * 描述：结束所有的Activity
     */
    public static void finishAllActivity() {
        for (int i = 0; i < activityStack.size(); i++) {
            if (activityStack.get(i) != null) {
                Log.i("ActivityCollector", activityStack.get(i).toString());
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }
}
