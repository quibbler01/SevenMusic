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
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;


/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置->下载音质选择功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 */
public class SettingDownloadQualityActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "DownloadQualityActivity";
    /**
     * 保存在下载音质选择类别，默认为空，没有选择
     */
    private static String sDownloadQuality = "未开启";
    /**
     * 下载音质界面返回图标实例
     */
    private Toolbar mSidebarSettingBackDownloadQualityToolBar;
    /**
     * 下载音质->标准实例
     */
    private TextView mSidebarDownloadQualityStandardTv;
    private CheckBox mSidebarDownloadQualityStandardCb;
    /**
     * 下载音质->较高实例
     */
    private TextView mSidebarDownloadQualityHighTv;
    private CheckBox mSidebarDownloadQualityHighCb;
    /**
     * 下载音质->极高实例
     */
    private TextView mSidebarDownloadQualityExtremelyHighTv;
    private CheckBox mSidebarDownloadQualityExtremelyHighCb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_setting_download_quality_activity);
        initView();
    }

    /**
     * 描述：初始化设置->下载音质界面控件
     */
    private void initView() {

        mSidebarSettingBackDownloadQualityToolBar = findViewById(R.id.sidebar_toolbar_setting_back_download_quality);
        mSidebarDownloadQualityStandardTv = findViewById(R.id.sidebar_tv_download_quality_standard);
        mSidebarDownloadQualityStandardCb = findViewById(R.id.sidebar_cb_download_quality_standard);
        mSidebarDownloadQualityHighTv = findViewById(R.id.sidebar_tv_download_quality_high);
        mSidebarDownloadQualityHighCb = findViewById(R.id.sidebar_cb_download_quality_high);
        mSidebarDownloadQualityExtremelyHighTv = findViewById(R.id.sidebar_tv_download_quality_extremely_high);
        mSidebarDownloadQualityExtremelyHighCb = findViewById(R.id.sidebar_cb_download_quality_extremely_high);

        setSupportActionBar(mSidebarSettingBackDownloadQualityToolBar);   // 为下载音质界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    // 添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSidebarDownloadQualityStandardCb.setOnCheckedChangeListener(this);
        mSidebarDownloadQualityHighCb.setOnCheckedChangeListener(this);
        mSidebarDownloadQualityExtremelyHighCb.setOnCheckedChangeListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        recoverySettingDownloadQualityState();
    }

    /**
     * 描述：恢复下载音质参数状态
     */
    private void recoverySettingDownloadQualityState() {
        mSidebarDownloadQualityStandardCb.setChecked((Boolean) SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, false));  // 恢复下载音质-标准-选中状态
        mSidebarDownloadQualityHighCb.setChecked((Boolean) SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, false));  // 恢复下载音质-较高-选中状态
        mSidebarDownloadQualityExtremelyHighCb.setChecked((Boolean) SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, false));   // 恢复下载音质-极高-选中状态
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            if (compoundButton.getId() == R.id.sidebar_cb_download_quality_standard) {
                sDownloadQuality = mSidebarDownloadQualityStandardTv.getText().toString();
                mSidebarDownloadQualityStandardCb.setChecked(true); // 实现CheckBox的单选按钮效果
                mSidebarDownloadQualityHighCb.setChecked(false);
                mSidebarDownloadQualityExtremelyHighCb.setChecked(false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, isChecked);    // 保存下载音质的选中or未选中状态-标准
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, false);
            } else if (compoundButton.getId() == R.id.sidebar_cb_download_quality_high) {
                sDownloadQuality = mSidebarDownloadQualityHighTv.getText().toString();
                mSidebarDownloadQualityStandardCb.setChecked(false);
                mSidebarDownloadQualityHighCb.setChecked(true);
                mSidebarDownloadQualityExtremelyHighCb.setChecked(false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, isChecked);  // 保存下载音质的选中or未选中状态-较高
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, false);
            } else if (compoundButton.getId() == R.id.sidebar_cb_download_quality_extremely_high) {
                sDownloadQuality = mSidebarDownloadQualityExtremelyHighTv.getText().toString();
                mSidebarDownloadQualityStandardCb.setChecked(false);
                mSidebarDownloadQualityHighCb.setChecked(false);
                mSidebarDownloadQualityExtremelyHighCb.setChecked(true);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, false);
                SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, isChecked);   // 保存下载音质的选中or未选中状态-极高
            }
            saveDownloadQuality();
        }
    }

    /**
     * 描述：将下载音质选择的类别保存到SharedPreference中
     */
    private void saveDownloadQuality() {
        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY, sDownloadQuality);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 退出当前Activity,返回设置界面
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
