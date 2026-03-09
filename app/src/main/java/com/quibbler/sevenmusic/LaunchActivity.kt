package com.quibbler.sevenmusic

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils
import java.lang.ref.WeakReference

/**
 * Package:        com.quibbler.sevenmusic
 * ClassName:      LaunchActivity
 * Description:    启动页
 * Author:         11103876
 * CreateDate:     2019/10/7 17:01
 */
class LaunchActivity : AppCompatActivity() {
    private class LaunchHandler(activity: LaunchActivity?) : Handler() {
        var mWeakReference: WeakReference<LaunchActivity?>

        init {
            mWeakReference = WeakReference<LaunchActivity?>(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mWeakReference.get()
            if (activity == null) {
                return
            }

            when (msg.what) {
                START_SPLASH_ACTIVITY -> {
                    ActivityStart.startActivity(activity, SplashActivity::class.java)
                    Log.i("LaunchActivity", "startActivity---SplashActivity")
                }

                START_MAIN_ACTIVITY -> {
                    ActivityStart.startActivity(activity, MainActivity::class.java)
                    Log.i("LaunchActivity", "startActivity---MainActivity")
                }

                else -> {}
            }
            //            activity.finish();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // 此处，在主题里面配置了背景，并且背景图一样，所以不需要布局文件了

        val handler = LaunchHandler(this)
        //        Message message = new Message();
        val message = Message.obtain()
        if (!SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_IS_FIRST_LOGIN, false) as Boolean?
        ) {
            message.what = START_SPLASH_ACTIVITY
        } else {
            message.what = START_MAIN_ACTIVITY
        }
        handler.sendMessageDelayed(message, DELAY_TIME.toLong())
        //        ActivityCollector.addActivity(this);
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    companion object {
        private const val START_SPLASH_ACTIVITY = 1
        private const val START_MAIN_ACTIVITY = 2
        private const val DELAY_TIME = 500 //延迟启动时间
    }
}
