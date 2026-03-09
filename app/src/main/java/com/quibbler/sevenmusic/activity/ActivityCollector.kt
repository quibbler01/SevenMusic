package com.quibbler.sevenmusic.activity

import android.app.Activity
import android.util.Log

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      ActivityCollector
 * Description:    Activity管理公共类
 * CreateDate:     2019/9/16 21:59
 */
object ActivityCollector {
    var activityStack: MutableList<Activity?>? = ArrayList<Activity?>()

    /**
     * 描述：添加Activity到堆栈
     * 
     * @param activity
     */
    fun addActivity(activity: Activity?) {
        if (activityStack == null) {
            activityStack = ArrayList<Activity?>()
        }
        activityStack!!.add(activity)
    }

    /**
     * 描述：移除指定的Activity
     * 
     * @param activity
     */
    fun removeActivity(activity: Activity?) {
        var activity = activity
        if (activity != null) {
            activityStack!!.remove(activity)
            activity = null
        }
    }

    /**
     * 描述：结束所有的Activity
     */
    fun finishAllActivity() {
        for (i in activityStack!!.indices) {
            if (activityStack!!.get(i) != null) {
                Log.i("ActivityCollector", activityStack!!.get(i).toString())
                activityStack!!.get(i)!!.finish()
            }
        }
        activityStack!!.clear()
    }
}
