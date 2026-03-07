package com.quibbler.sevenmusic.activity.sidebar;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.quibbler.sevenmusic.R;

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      AboutAppActivity
 * Description:    app作者信息
 * Author:         11103876
 * CreateDate:     2019/10/26 17:11
 */
public class AboutAppActivity extends AppCompatActivity {
    /**
     * 关于界面返回图标实例
     */
    private Toolbar mSidebarAboutBackToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_setting_about);
        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        mSidebarAboutBackToolBar = findViewById(R.id.sidebar_toolbar_setting_about_back);

        setSupportActionBar(mSidebarAboutBackToolBar);  // 为设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);     //添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 退出当前Activity,返回主界面
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
