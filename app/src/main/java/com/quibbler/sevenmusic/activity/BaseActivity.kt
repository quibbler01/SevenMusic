package com.quibbler.sevenmusic.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      BaseActivity
 * Description:    基类Activity，用于其他类的继承
 * Author:         11103876
 * CreateDate:     2019/9/21 16:32
 */
public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initAppMode();
    }


    /**
     * 描述：初始化App的模式-默认是日间模式
     * MODE_NIGHT_NO： 使用亮色(light)主题，不使用夜间模式；
     * MODE_NIGHT_YES：使用暗色(dark)主题，使用夜间模式；
     * MODE_NIGHT_AUTO：根据当前时间自动切换 亮色(light)/暗色(dark)主题；
     * MODE_NIGHT_FOLLOW_SYSTEM(默认选项)：设置为跟随系统，通常为 MODE_NIGHT_NO
     * <p>
     * 日夜间模式的切换会导致Activity的重启，需要增加在onCreate方法中对模式进行判断，设置对应的文字提示和保存模式键值对
     */
    private void initAppMode() {
        boolean nightMode = (Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_NIGHT_MODE, false);  //初始化时，读取保存的夜间模式键值，根据键值为true还是false，设置相应的日间or夜间模式
        if (nightMode) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
