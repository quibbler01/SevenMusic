package com.quibbler.sevenmusic.activity.mv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;

/**
 * Package:        com.quibbler.sevenmusic.activity.mv
 * ClassName:      ActivityStart
 * Description:    启动activity的静态类
 * Author:         lishijun
 * CreateDate:     2019/9/23 19:14
 */
public class ActivityStart {
    public static void startMvPlayActivity(Context context, MvInfo mvInfo) {
        Intent intent = new Intent(context, MvPlayActivity.class);
        intent.putExtra("mvInfo", mvInfo);
        context.startActivity(intent);
    }

    public static void startMusicPlayActivity(Activity context, MvMusicInfo mvMusicInfo) {
        Intent intent = new Intent(context, MusicPlayActivity.class);
        intent.putExtra("musicInfo", mvMusicInfo);
        context.startActivity(intent);
        //进入播放页面的动画，从下往上
        context.overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom);
    }

    public static void startMusicPlayActivity(Context context, MvMusicInfo mvMusicInfo) {
        Intent intent = new Intent(context, MusicPlayActivity.class);
        intent.putExtra("musicInfo", mvMusicInfo);
        context.startActivity(intent);
        //进入播放页面的动画，从下往上
//        context.overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom);
    }

    /**
     * 描述：从一个activity 跳转到另一个activity，此时目标activity将会存在于启动他的activity所在的栈中
     *
     * @param activity
     * @param cls
     */
    public static void startActivity(Activity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);

    }

    /**
     * 描述：当上下文信息是应用全局Context时启动activity， 此时目标activity将会在一个新的任务栈中, 内部设置了Flag:Intent.FLAG_ACTIVITY_NEW_TASK
     *
     * @param context
     * @param cls
     */
    public static void startNewActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    /**
     * 描述：带参数启动Activity
     *
     * @param context 上下文
     * @param cls     目标class文件
     * @param bundle  内容bundle
     */
    public static void startActivity(Context context, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, cls);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


}
