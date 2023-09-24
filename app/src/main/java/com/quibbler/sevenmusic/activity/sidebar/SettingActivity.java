package com.quibbler.sevenmusic.activity.sidebar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
 * Description:    实现系统设置功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "SettingActivity";
    /**
     * 设置界面返回图标实例
     */
    private Toolbar mSidebarSettingBackToolBar;
    /**
     * 2G、3G、4G网络播放开关按钮实例
     */
    private Switch mSidebarSettingNetworkPlaySwitch;
    /**
     * 2G、3G、4G网络下载开关按钮实例
     */
    private Switch mSidebarSettingNetworkDownloadSwitch;
    /**
     * 在线播放音质布局实例
     */
    private RelativeLayout mSidebarSettingPlayQualityLayout;
    /**
     * 在线播放音质选择（自动、标准、较高、极高）实例
     */
    private TextView mSidebarSettingPlayQualityTv;
    /**
     * 下载音质布局实例
     */
    private RelativeLayout mSidebarSettingDownloadQualityLayout;
    /**
     * 下载音质选择（标准、较高、极高）实例
     */
    private TextView mSidebarSettingDownloadQualityTv;
    /**
     * 边听边存布局实例
     */
    private RelativeLayout mSidebarSettingListenSaveLayout;
    /**
     * 边听边存选择（免费歌曲、VIP歌曲）实例
     */
    private TextView mSidebarSettingListenSaveTv;
    /**
     * 下载目录布局实例
     */
    private RelativeLayout mSidebarSettingDownloadDirectoryLayout;
    /**
     * 下载目录选择（存储卡1、存储卡2、、、）实例
     */
    private TextView mSidebarSettingDownloadDirectoryTv;
    /**
     * 缓存设置布局实例
     */
    private RelativeLayout mSidebarSettingCacheLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_setting_activity);
        initView();
    }

    /**
     * 描述：初始化设置界面控件
     */
    private void initView() {
        mSidebarSettingBackToolBar = findViewById(R.id.sidebar_toolbar_setting_back);
        mSidebarSettingNetworkPlaySwitch = findViewById(R.id.sidebar_switch_setting_network_play);
        mSidebarSettingNetworkDownloadSwitch = findViewById(R.id.sidebar_switch_setting_network_download);
        mSidebarSettingPlayQualityLayout = findViewById(R.id.sidebar_rl_setting_play_quality);
        mSidebarSettingPlayQualityTv = findViewById(R.id.sidebar_tv_setting_play_quality);
        mSidebarSettingDownloadQualityLayout = findViewById(R.id.sidebar_rl_setting_download_quality);
        mSidebarSettingDownloadQualityTv = findViewById(R.id.sidebar_tv_setting_download_quality);
        mSidebarSettingListenSaveLayout = findViewById(R.id.sidebar_rl_setting_listen_save);
        mSidebarSettingListenSaveTv = findViewById(R.id.sidebar_tv_setting_cache_video_clear);
        mSidebarSettingDownloadDirectoryLayout = findViewById(R.id.sidebar_rl_setting_download_directory);
        mSidebarSettingDownloadDirectoryTv = findViewById(R.id.sidebar_tv_setting_download_directory);
        mSidebarSettingCacheLayout = findViewById(R.id.sidebar_rl_setting_cache);

        setSupportActionBar(mSidebarSettingBackToolBar);  // 为设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);     //添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSidebarSettingNetworkPlaySwitch.setOnCheckedChangeListener(this);    // 2G、3G、4G网络播放单选按钮点击事件
        mSidebarSettingNetworkDownloadSwitch.setOnCheckedChangeListener(this);    // 2G、3G、4G网络下载单选按钮点击事件
        mSidebarSettingPlayQualityLayout.setOnClickListener(this);     // 在线播放音质布局点击事件
        mSidebarSettingDownloadQualityLayout.setOnClickListener(this);     // 下载音质布局点击事件
        mSidebarSettingListenSaveLayout.setOnClickListener(this);   // 边听边存布局点击事件
        mSidebarSettingDownloadDirectoryLayout.setOnClickListener(this);    // 设置下载目录布局点击事件
        mSidebarSettingCacheLayout.setOnClickListener(this);  // 缓存设置布局点击事件
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recoverySettingState();
    }

    /**
     * 描述：恢复设置界面参数状态
     */
    private void recoverySettingState() {
        mSidebarSettingNetworkPlaySwitch.setChecked((Boolean) SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_NETWORK_PLAY, false));    // 恢复2G/3G/4G网络播放的选中状态（布尔类型,此处false只表示布尔类型）
        mSidebarSettingNetworkDownloadSwitch.setChecked((Boolean) SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, false));   // 恢复2G/3G/4G网络下载的选中状态（布尔类型）
        mSidebarSettingPlayQualityTv.setText(SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_PLAY_QUALITY, "").toString());   // 恢复在线播放音质的选择类别状态（字符串类型）
        mSidebarSettingDownloadQualityTv.setText(SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_DOWNLOAD_QUALITY, "").toString());  // 恢复下载音质的选择类别状态（字符串类型）
        mSidebarSettingListenSaveTv.setText(SharedPreferencesUtils.getInstance()
                .getData(Constant.KEY_SETTING_LISTEN_SAVE, "").toString());   // 恢复边听边存的选择类别状态（字符串类型）
    }

    /**
     * 描述：设置下载目录显示对话框
     */
    private void showDownloadDirectoryDialog() {
        if (SettingDownloadDirectory.getSDTotalSize() != null && SettingDownloadDirectory.getSDAvailableSize() != null) {  // 存在SD卡
            String SDTotalSize = SettingDownloadDirectory.getSDTotalSize();    // SD卡总容量
            String SDAvailable = SettingDownloadDirectory.getSDAvailableSize();    // SD卡可用容量
            String showSDSizeMsg = SDAvailable + "可用，共" + SDTotalSize;    // SD卡总容量和可用容量显示信息
            String SDPath = "存储卡1" + "(" + SettingDownloadDirectory.getSDPath() + ")";   // SD卡路径
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(ResUtil.getString(R.string.str_setting_download_directory_dialog_title))
                    .setMessage(SDPath + "\n" + showSDSizeMsg)
                    .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //确定逻辑实现

                        }
                    }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show();
        } else {
            Toast.makeText(SettingActivity.this, ResUtil.getString(R.string.str_setting_download_directory_no_sd_card), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 描述：网络播放提示对话框
     */
    private void showNetworkPlayDialog() {
        Log.d(TAG, "showNetworkPlayDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ResUtil.getString(R.string.str_setting_network_play_dialog_title))
                .setMessage(ResUtil.getString(R.string.str_setting_network_play_download_dialog_msg))
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSidebarSettingNetworkPlaySwitch.setChecked(true);
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSidebarSettingNetworkPlaySwitch.setChecked(false);
                    }
                }).show();

    }

    /**
     * 描述：网络下载提示对话框
     */
    private void showNetworkDownloadDialog() {
        Log.d(TAG, "showNetworkDownloadDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ResUtil.getString(R.string.str_setting_network_download_dialog_title))
                .setMessage(ResUtil.getString(R.string.str_setting_network_play_download_dialog_msg))
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSidebarSettingNetworkDownloadSwitch.setChecked(true);
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSidebarSettingNetworkDownloadSwitch.setChecked(false);
                    }
                }).show();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sidebar_rl_setting_play_quality) {
            Intent settingPlayQualityIntent = new Intent(SettingActivity.this, SettingPlayQualityActivity.class);
            startActivity(settingPlayQualityIntent);
        } else if (view.getId() == R.id.sidebar_rl_setting_download_quality) {
            Intent settingDownloadQualityIntent = new Intent(SettingActivity.this, SettingDownloadQualityActivity.class);
            startActivity(settingDownloadQualityIntent);
        } else if (view.getId() == R.id.sidebar_rl_setting_listen_save) {
            Intent settingListenSongIntent = new Intent(SettingActivity.this, SettingListenSaveActivity.class);
            startActivity(settingListenSongIntent);
        } else if (view.getId() == R.id.sidebar_rl_setting_download_directory) {
            showDownloadDirectoryDialog();
        } else if (view.getId() == R.id.sidebar_rl_setting_cache) {
            Intent settingCacheIntent = new Intent(SettingActivity.this, SettingCacheActivity.class);
            startActivity(settingCacheIntent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.sidebar_switch_setting_network_play) {
            Log.d(TAG, "showNetworkPlayDialog,isChecked=" + isChecked);
            boolean network_play_sp = (boolean) SharedPreferencesUtils.getInstance()
                    .getData(Constant.KEY_SETTING_NETWORK_PLAY, false);
            if (isChecked && !network_play_sp) {
                showNetworkPlayDialog();
            }
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_NETWORK_PLAY, isChecked);  // 保存2G/3G/4G网络播放按钮的选中状态
        } else if (compoundButton.getId() == R.id.sidebar_switch_setting_network_download) {
            Log.d(TAG, "showNetworkDownloadDialog,isChecked=" + isChecked);
            boolean network_download_sp = (boolean) SharedPreferencesUtils.getInstance()
                    .getData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, false);
            if (isChecked && !network_download_sp) {
                showNetworkDownloadDialog();
            }
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, isChecked);     //保存2G/3G/4G网络下载按钮的选中状态
        }
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
