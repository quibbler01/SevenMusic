package com.quibbler.sevenmusic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

import java.lang.ref.WeakReference;

/**
 * Package:        com.quibbler.sevenmusic
 * ClassName:      LaunchActivity
 * Description:    启动页
 * Author:         11103876
 * CreateDate:     2019/10/7 17:01
 */
public class LaunchActivity extends AppCompatActivity {
    private static final int START_SPLASH_ACTIVITY  = 1;
    private static final int START_MAIN_ACTIVITY  = 2;
    private static final int DELAY_TIME = 500;  //延迟启动时间

    private static class LaunchHandler extends Handler {
        WeakReference<LaunchActivity> mWeakReference;

        LaunchHandler(LaunchActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LaunchActivity activity = mWeakReference.get();
            if (activity == null){
                return;
            }

            switch (msg.what) {
                case START_SPLASH_ACTIVITY:
                    ActivityStart.startActivity(activity, SplashActivity.class);
                    Log.i("LaunchActivity", "startActivity---SplashActivity");
                    break;
                case START_MAIN_ACTIVITY:
                    ActivityStart.startActivity(activity, MainActivity.class);
                    Log.i("LaunchActivity", "startActivity---MainActivity");
                    break;
                default:
                    break;
            }
//            activity.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// 此处，在主题里面配置了背景，并且背景图一样，所以不需要布局文件了

        LaunchHandler handler = new LaunchHandler(this);
//        Message message = new Message();
        Message message = Message.obtain();
        if (!(Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_IS_FIRST_LOGIN, false)) {
            message.what = START_SPLASH_ACTIVITY;
        } else {
            message.what = START_MAIN_ACTIVITY;
        }
        handler.sendMessageDelayed(message, DELAY_TIME);
//        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
