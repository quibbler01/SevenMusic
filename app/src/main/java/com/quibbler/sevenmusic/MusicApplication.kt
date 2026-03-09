package com.quibbler.sevenmusic

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.androidkun.xtablayoutlibrary.BuildConfig
import com.quibbler.sevenmusic.utils.ActivityLifecycle
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicInfo
 * Description:    全局Application，获取全局Context技巧,已在AndroidManifest文件中修改全局android:name=".utils.MusicApplication";初始化各种东西
 * Author:         zhaopeng
 * CreateDate:     2019/9/16 21:34
 */
class MusicApplication : Application() {
    //    private static RefWatcher mRefWatcher = null;
    private var mActivityLifeCycleCallback: ActivityLifecycle? = null

    override fun onCreate() {
        super.onCreate()
        context = this
        mActivityLifeCycleCallback = ActivityLifecycle()
        initSharedPreference()
        initAppMode()
        //        setUpMemoryLeakUtils();
//        initStrictMode();
        MusicThreadPool.initThreadPool()
        registerActivityLifecycleCallbacks(mActivityLifeCycleCallback) // 注册Activity生命周期回调接口
    }

    /* private void setUpMemoryLeakUtils() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            mRefWatcher = RefWatcher.DISABLED;
        } else {
            mRefWatcher = LeakCanary.install(this);
        }
    }

    public static RefWatcher getRefWatcher() {
        return mRefWatcher;
    }*/
    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .detectResourceMismatches() //                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks() //                    .detectCleartextNetwork()
                    .detectFileUriExposure()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit(MainActivity::class.java, 1)
                    .penaltyLog()
                    .build()
            )
        }
    }

    /**
     * 描述：全局初始化一个SharedPreferencesUtils类的对象，保证全局唯一，用于存储设置参数
     */
    private fun initSharedPreference() {
        SharedPreferencesUtils.Companion.init(context)
    }

    /**
     * 描述：初始化App的模式-默认是日间模式
     */
    private fun initAppMode() {
        val isNightMode = SharedPreferencesUtils.Companion.getInstance().getData(
            Constant.KEY_NIGHT_MODE,
            false
        ) as Boolean //初始化时，读取保存的夜间模式键值，根据键值为true还是false，设置相应的日间or夜间模式
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onTerminate() {
        unregisterActivityLifecycleCallbacks(mActivityLifeCycleCallback)
        super.onTerminate()
    }

    companion object {
        /**
         * 描述：获取上下文
         * 
         * @return
         */
        lateinit var context: Application
    }

}
