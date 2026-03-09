package com.quibbler.sevenmusic.utils

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import com.quibbler.sevenmusic.activity.ActivityCollector

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ActivityLifeCycle
 * Description:    Activity生命周期监听回调类
 * Author:         11103876
 * CreateDate:     2019/11/5 21:40
 */
class ActivityLifecycle : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onCreate")
        ActivityCollector.addActivity(activity) // 添加当前Activity到集合中
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onSaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, activity.javaClass.getSimpleName() + "...onDestroyed")
        ActivityCollector.removeActivity(activity) // 移除当前Activity到集合中
    }

    companion object {
        private val TAG: String = ActivityLifecycle::class.java.getSimpleName()
    }
}
