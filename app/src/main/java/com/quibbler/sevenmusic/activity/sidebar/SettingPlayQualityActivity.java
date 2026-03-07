package com.quibbler.sevenmusic.activity.sidebar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置->在线播放音质选择功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 * CompoundButton.OnCheckedChangeListener,
 */
public class SettingPlayQualityActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "PlayQualityActivity";
    /**
     * 保存在线播放歌曲的音质选择类别，默认为空，没有选择
     */
    private static String sPlayQuality = "";
    /**
     * 在线播放音质界面返回图标实例
     */
    private Toolbar mSidebarSettingBackPlayQualityToolbar;
    /**
     * 在线播放音质->自动实例
     */
    private TextView mSidebarPlayQualityAutoSelectTv;
    private CheckBox mSidebarPlayQualityAutoSelectCb;
    /**
     * 在线播放音质->标准实例
     */
    private TextView mSidebarPlayQualityStandardTv;
    private CheckBox mSidebarPlayQualityStandardCb;
    /**
     * 在线播放音质->较高实例
     */
    private TextView mSidebarPlayQualityHighTv;
    private CheckBox mSidebarPlayQualityHighCb;
    /**
     * 在线播放音质->极高实例
     */
    private TextView mSidebarPlayQualityExtremelyHighTv;
    private CheckBox mSidebarPlayQualityExtremelyHighCb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_setting_play_quality_activity);
        initView();
    }

    /**
     * 描述：初始化设置->在线播放音质界面布局控件
     */
    private void initView() {

        mSidebarSettingBackPlayQualityToolbar = findViewById(R.id.sidebar_toolbar_setting_back_play_quality);
        mSidebarPlayQualityAutoSelectTv = findViewById(R.id.sidebar_tv_play_quality_auto_select);
        mSidebarPlayQualityAutoSelectCb = findViewById(R.id.sidebar_cb_play_quality_auto_select);
        mSidebarPlayQualityStandardTv = findViewById(R.id.sidebar_tv_play_quality_standard);
        mSidebarPlayQualityStandardCb = findViewById(R.id.sidebar_cb_play_quality_standard);
        mSidebarPlayQualityHighTv = findViewById(R.id.sidebar_tv_play_quality_high);
        mSidebarPlayQualityHighCb = findViewById(R.id.sidebar_cb_play_quality_high);
        mSidebarPlayQualityExtremelyHighTv = findViewById(R.id.sidebar_tv_play_quality_extremely_high);
        mSidebarPlayQualityExtremelyHighCb = findViewById(R.id.sidebar_cb_play_quality_extremely_high);

        setSupportActionBar(mSidebarSettingBackPlayQualityToolbar); // 为在线播放音质界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // 添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSidebarPlayQualityAutoSelectCb.setOnCheckedChangeListener(this);
        mSidebarPlayQualityStandardCb.setOnCheckedChangeListener(this);
        mSidebarPlayQualityHighCb.setOnCheckedChangeListener(this);
        mSidebarPlayQualityExtremelyHighCb.setOnCheckedChangeListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recoverySettingPlayQualityState();
    }

    /**
     * 描述：恢复在线播放音质参数状态
     */
    private void recoverySettingPlayQualityState() {
        mSidebarPlayQualityAutoSelectCb.setChecked((Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false));   // 恢复在线播放音质-自动-选中状态（布尔类型,此处false只表示布尔类型）
        mSidebarPlayQualityStandardCb.setChecked((Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false));  // 恢复在线播放音质-标准-选中状态
        mSidebarPlayQualityHighCb.setChecked((Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false));  // 恢复在线播放音质-较高-选中状态
        mSidebarPlayQualityExtremelyHighCb.setChecked((Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false));  // 恢复在线播放音质-极高-选中状态
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            if (compoundButton.getId() == R.id.sidebar_cb_play_quality_auto_select) {
                sPlayQuality = ResUtil.getString(R.string.str_play_quality_automatic);
                mSidebarPlayQualityAutoSelectCb.setChecked(true); // 实现CheckBox单选效果
                mSidebarPlayQualityStandardCb.setChecked(false);
                mSidebarPlayQualityHighCb.setChecked(false);
                mSidebarPlayQualityExtremelyHighCb.setChecked(false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, isChecked);   // 保存在线播放音质的选中or未选中状态-自动
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false);
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_standard) {
                sPlayQuality = mSidebarPlayQualityStandardTv.getText().toString();
                mSidebarPlayQualityAutoSelectCb.setChecked(false);
                mSidebarPlayQualityStandardCb.setChecked(true);
                mSidebarPlayQualityHighCb.setChecked(false);
                mSidebarPlayQualityExtremelyHighCb.setChecked(false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, isChecked);  // 保存在线播放音质的选中or未选中状态-标准
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false);
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_high) {
                sPlayQuality = mSidebarPlayQualityHighTv.getText().toString();
                mSidebarPlayQualityAutoSelectCb.setChecked(false);
                mSidebarPlayQualityStandardCb.setChecked(false);
                mSidebarPlayQualityHighCb.setChecked(true);
                mSidebarPlayQualityExtremelyHighCb.setChecked(false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, isChecked); // 保存在线播放音质的选中or未选中状态-较高
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false);
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_extremely_high) {
                sPlayQuality = mSidebarPlayQualityExtremelyHighTv.getText().toString();
                mSidebarPlayQualityAutoSelectCb.setChecked(false);
                mSidebarPlayQualityStandardCb.setChecked(false);
                mSidebarPlayQualityHighCb.setChecked(false);
                mSidebarPlayQualityExtremelyHighCb.setChecked(true);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, isChecked);  // 保存在线播放音质的选中or未选中状态-极高
            }
            savePlayQuality();
        }

    }

    /**
     * 描述：将在线播放音质选择的类别保存到SharedPreference中
     */
    private void savePlayQuality() {
        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_PLAY_QUALITY, sPlayQuality);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 退出当前Activity,返回设置界面
//                savePlayQuality();// 返回到设置界面时，首先保存在线播放音质选择类别
                finish();
                break;
            default:
                break;
        }
        return true;
    }


}
