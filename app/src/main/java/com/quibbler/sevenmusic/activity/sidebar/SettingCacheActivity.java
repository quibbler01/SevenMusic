package com.quibbler.sevenmusic.activity.sidebar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import static com.quibbler.sevenmusic.utils.CacheUtil.deleteMusicCache;
import static com.quibbler.sevenmusic.utils.CacheUtil.deletePictureCache;
import static com.quibbler.sevenmusic.utils.CacheUtil.getMusicCache;
import static com.quibbler.sevenmusic.utils.CacheUtil.getPictureCache;


/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingCacheActivity
 * Description:    实现系统设置->缓存功能
 * Author:         guojinliang
 * CreateDate:     2019/9/16 21:31
 */
public class SettingCacheActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 日志标识符
     */
    private static final String TAG = "SettingCacheActivity";

    /**
     * 音乐缓存
     */
    private String mMusicCacheValue = "0M";
    /**
     * 图片歌词缓存
     */
    private String mPictureLyricsCacheValue = "0M";
    /**
     * 视频歌词缓存
     */
    private String mVideoCacheValue = "0M";

    /**
     * 缓存设置界面返回图标实例
     */
    private Toolbar mSidebarSettingBackCacheToolBar;
    /**
     * 缓存设置->设置音乐缓存上限布局实例
     */
    private RelativeLayout mSidebarCacheMusicLimitLayout;
    private TextView mSidebarCacheMusicLimitTv;
    /**
     * 缓存设置->清除音乐缓存布局实例
     */
    private RelativeLayout mSidebarCacheMusicClearLayout;
    private TextView mSidebarCacheMusicClearTv;
    /**
     * 缓存设置->清除图片与歌词缓存布局实例
     */
    private RelativeLayout mSidebarCachePictureClearLayout;
    private TextView mSidebarCachePictureClearTv;
    /**
     * 缓存设置->清除视频缓存
     */
    private RelativeLayout mSidebarSettingCacheVideoClearLayout;
    private TextView mSidebarSettingCacheVideoClearTv;
    /**
     * 缓存设置->自动清除缓存开关实例
     */
    private Switch mSidebarCacheAutoClearSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_setting_cache_activity);
        initView();
    }

    /**
     * 描述：初始化设置->缓存设置界面控件
     */
    private void initView() {

        mSidebarSettingBackCacheToolBar = findViewById(R.id.sidebar_toolbar_setting_back_cache);
        mSidebarCacheMusicLimitLayout = findViewById(R.id.sidebar_rl_cache_music_limit);
        mSidebarCacheMusicLimitTv = findViewById(R.id.sidebar_tv_cache_music_limit);
        mSidebarCacheMusicClearLayout = findViewById(R.id.sidebar_rl_cache_music_clear);
        mSidebarCacheMusicClearTv = findViewById(R.id.sidebar_tv_cache_music_clear);
        mSidebarCachePictureClearLayout = findViewById(R.id.sidebar_rl_cache_picture_clear);
        mSidebarCachePictureClearTv = findViewById(R.id.sidebar_tv_cache_picture_clear);
        mSidebarSettingCacheVideoClearLayout = findViewById(R.id.sidebar_rl_setting_cache_video_clear);
        mSidebarSettingCacheVideoClearTv = findViewById(R.id.sidebar_tv_setting_cache_video_clear);
        mSidebarCacheAutoClearSwitch = findViewById(R.id.sidebar_switch_cache_auto_clear);

        setSupportActionBar(mSidebarSettingBackCacheToolBar); // 为缓存设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按钮,同时隐去标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSidebarCacheMusicLimitLayout.setOnClickListener(this);  // 自定义缓存点击事件，弹出输入框，可设置缓存值大小
        mSidebarCacheMusicClearLayout.setOnClickListener(this);   // 清除音乐缓存点击事件，清空显示的音乐缓存值
        mSidebarCachePictureClearLayout.setOnClickListener(this);  // 清除图片与歌词缓存点击事件，清空显示的缓存值
        mSidebarSettingCacheVideoClearLayout.setOnClickListener(this);    // 清除视频缓存点击事件，清空显示的缓存值
        mSidebarCacheAutoClearSwitch.setOnCheckedChangeListener(this); // 自动清除缓存点击事件，清空所有显示的缓存值

    }

    /**
     * 描述：设置自定义缓存值
     */
    private void showCustomCacheDialog() {
        final EditText CustomCacheEt = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ResUtil.getString(R.string.str_setting_cache_dialog_custom_cache_title))
                .setView(CustomCacheEt)
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSidebarCacheMusicLimitTv.setText(CustomCacheEt.getText().toString() + "M"); // 让自定义缓存文本框显示设置的缓存值
                        SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_CACHE_MUSIC_LIMIT, Integer.parseInt(CustomCacheEt.getText().toString()));   // 以整数形式保存自定义缓存值到SharedPerference中
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show();

    }

    /**
     * 描述：清除音乐缓存
     */
    private void showMusicCacheDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_music_cache_msg))
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_eliminate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMusicCache();
                        mSidebarCacheMusicClearTv.setText("0M");
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show();

    }

    /**
     * 描述：清除图片缓存
     */
    private void showPictureCacheDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_picture_lyrice_msg))
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_eliminate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePictureCache();
                        mSidebarCachePictureClearTv.setText("0M");
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show();
    }

    /**
     * 描述：清除视频缓存值
     */
    private void showVideoCacheDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_video_msg))
                .setPositiveButton(ResUtil.getString(R.string.str_dialog_btn_eliminate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSidebarCachePictureClearTv.setText("0M");
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!"0".equals(getPictureCache())) { // 显示图片缓存
            mSidebarCachePictureClearTv.setText(getPictureCache());
        }
        if (!"0".equals(getMusicCache())) { // 显示音乐缓存
            Log.d(TAG, "音乐缓存数值：" + getMusicCache());
            mSidebarCacheMusicClearTv.setText(getMusicCache());
        }
        recoverySettingCacheState();
    }

    /**
     * 描述：恢复缓存设置界面参数状态
     */
    private void recoverySettingCacheState() {
        mSidebarCacheMusicLimitTv.setText(SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_CACHE_MUSIC_LIMIT, 0).toString() + "M");    // 恢复自定义缓存设置的音乐缓存上限(字符串整数类型，此处""表示字符串类型)
        mSidebarCacheAutoClearSwitch.setChecked((Boolean) SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_CACHE_AUTO_CLEAR, false));         // 恢复自动清除缓存选中状态(布尔类型，此处false只表示布尔类型)
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sidebar_rl_cache_music_limit) {
            showCustomCacheDialog();
        } else if (view.getId() == R.id.sidebar_rl_cache_music_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_cache_music_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_cache_music_limit, Integer.parseInt(0)))
             *  显示音乐缓存值得大小
             *
             */
            if ("0M".equals(mSidebarCacheMusicClearTv.getText().toString())) {   // 音乐缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(SettingCacheActivity.this, ResUtil.getString(R.string.str_setting_cache_no_cache), Toast.LENGTH_SHORT).show();
            } else {
                showMusicCacheDialog();
            }
        } else if (view.getId() == R.id.sidebar_rl_cache_picture_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_cache_picture_lyrics_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_cache_picture_lyrics_clear, Integer.parseInt(0)))
             *  显示音乐缓存值得大小
             *
             */
            if ("0M".equals(mSidebarCachePictureClearTv.getText().toString())) {   // 图片与歌词缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(SettingCacheActivity.this, ResUtil.getString(R.string.str_setting_cache_no_cache), Toast.LENGTH_SHORT).show();
            } else {
                showPictureCacheDialog();
            }
        } else if (view.getId() == R.id.sidebar_rl_setting_cache_video_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_setting_cache_video_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_setting_cache_video_clear, Integer.parseInt(0)))
             *  显示音乐缓存值得大小
             *
             */
            if ("0M".equals(mSidebarSettingCacheVideoClearTv.getText().toString())) {    // 视频缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(SettingCacheActivity.this, ResUtil.getString(R.string.str_setting_cache_no_cache), Toast.LENGTH_SHORT).show();
            } else {
                showVideoCacheDialog();
            }
        } else if (view.getId() == R.id.sidebar_switch_cache_auto_clear) {
            //
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.sidebar_switch_cache_auto_clear) {
            // 此处点击自动清除缓存按钮后，当退出应用后，自动清除所有缓存
            SharedPreferencesUtils.getInstance().saveData(Constant.KEY_SETTING_CACHE_AUTO_CLEAR, isChecked);  //保存自动清除缓存按钮的选中状态
        }
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
