package com.quibbler.sevenmusic.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.quibbler.sevenmusic.activity.ActivityCollector;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ActivityLifeCycle
 * Description:    Activity生命周期监听回调类
 * Author:         11103876
 * CreateDate:     2019/11/5 21:40
 */
public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = ActivityLifecycle.class.getSimpleName();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onCreate");
        ActivityCollector.addActivity(activity);   // 添加当前Activity到集合中
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onResumed");

    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onSaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, activity.getClass().getSimpleName() + "...onDestroyed");
        ActivityCollector.removeActivity(activity);   // 移除当前Activity到集合中
    }
}
