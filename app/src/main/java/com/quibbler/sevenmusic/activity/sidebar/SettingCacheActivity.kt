package com.quibbler.sevenmusic.activity.sidebar

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.CacheUtil
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingCacheActivity
 * Description:    实现系统设置->缓存功能
 * Author:         guojinliang
 * CreateDate:     2019/9/16 21:31
 */
class SettingCacheActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    /**
     * 音乐缓存
     */
    private val mMusicCacheValue = "0M"

    /**
     * 图片歌词缓存
     */
    private val mPictureLyricsCacheValue = "0M"

    /**
     * 视频歌词缓存
     */
    private val mVideoCacheValue = "0M"

    /**
     * 缓存设置界面返回图标实例
     */
    private var mSidebarSettingBackCacheToolBar: Toolbar? = null

    /**
     * 缓存设置->设置音乐缓存上限布局实例
     */
    private var mSidebarCacheMusicLimitLayout: RelativeLayout? = null
    private var mSidebarCacheMusicLimitTv: TextView? = null

    /**
     * 缓存设置->清除音乐缓存布局实例
     */
    private var mSidebarCacheMusicClearLayout: RelativeLayout? = null
    private var mSidebarCacheMusicClearTv: TextView? = null

    /**
     * 缓存设置->清除图片与歌词缓存布局实例
     */
    private var mSidebarCachePictureClearLayout: RelativeLayout? = null
    private var mSidebarCachePictureClearTv: TextView? = null

    /**
     * 缓存设置->清除视频缓存
     */
    private var mSidebarSettingCacheVideoClearLayout: RelativeLayout? = null
    private var mSidebarSettingCacheVideoClearTv: TextView? = null

    /**
     * 缓存设置->自动清除缓存开关实例
     */
    private var mSidebarCacheAutoClearSwitch: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_setting_cache_activity)
        initView()
    }

    /**
     * 描述：初始化设置->缓存设置界面控件
     */
    private fun initView() {
        mSidebarSettingBackCacheToolBar =
            findViewById<Toolbar?>(R.id.sidebar_toolbar_setting_back_cache)
        mSidebarCacheMusicLimitLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_cache_music_limit)
        mSidebarCacheMusicLimitTv = findViewById<TextView>(R.id.sidebar_tv_cache_music_limit)
        mSidebarCacheMusicClearLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_cache_music_clear)
        mSidebarCacheMusicClearTv = findViewById<TextView>(R.id.sidebar_tv_cache_music_clear)
        mSidebarCachePictureClearLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_cache_picture_clear)
        mSidebarCachePictureClearTv = findViewById<TextView>(R.id.sidebar_tv_cache_picture_clear)
        mSidebarSettingCacheVideoClearLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_setting_cache_video_clear)
        mSidebarSettingCacheVideoClearTv =
            findViewById<TextView>(R.id.sidebar_tv_setting_cache_video_clear)
        mSidebarCacheAutoClearSwitch = findViewById<Switch>(R.id.sidebar_switch_cache_auto_clear)

        setSupportActionBar(mSidebarSettingBackCacheToolBar) // 为缓存设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) // 添加返回按钮,同时隐去标题
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        mSidebarCacheMusicLimitLayout!!.setOnClickListener(this) // 自定义缓存点击事件，弹出输入框，可设置缓存值大小
        mSidebarCacheMusicClearLayout!!.setOnClickListener(this) // 清除音乐缓存点击事件，清空显示的音乐缓存值
        mSidebarCachePictureClearLayout!!.setOnClickListener(this) // 清除图片与歌词缓存点击事件，清空显示的缓存值
        mSidebarSettingCacheVideoClearLayout!!.setOnClickListener(this) // 清除视频缓存点击事件，清空显示的缓存值
        mSidebarCacheAutoClearSwitch!!.setOnCheckedChangeListener(this) // 自动清除缓存点击事件，清空所有显示的缓存值
    }

    /**
     * 描述：设置自定义缓存值
     */
    private fun showCustomCacheDialog() {
        val CustomCacheEt = EditText(this)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(ResUtil.getString(R.string.str_setting_cache_dialog_custom_cache_title))
            .setView(CustomCacheEt)
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_confirm),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        mSidebarCacheMusicLimitTv!!.setText(
                            CustomCacheEt.getText().toString() + "M"
                        ) // 让自定义缓存文本框显示设置的缓存值
                        SharedPreferencesUtils.Companion.getInstance().saveData(
                            Constant.KEY_SETTING_CACHE_MUSIC_LIMIT,
                            CustomCacheEt.getText().toString().toInt()
                        ) // 以整数形式保存自定义缓存值到SharedPerference中
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show()
    }

    /**
     * 描述：清除音乐缓存
     */
    private fun showMusicCacheDialog() {
        val build = AlertDialog.Builder(this)
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_music_cache_msg))
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_eliminate),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        CacheUtil.deleteMusicCache()
                        mSidebarCacheMusicClearTv!!.setText("0M")
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show()
    }

    /**
     * 描述：清除图片缓存
     */
    private fun showPictureCacheDialog() {
        val build = AlertDialog.Builder(this)
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_picture_lyrice_msg))
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_eliminate),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        CacheUtil.deletePictureCache()
                        mSidebarCachePictureClearTv!!.setText("0M")
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show()
    }

    /**
     * 描述：清除视频缓存值
     */
    private fun showVideoCacheDialog() {
        val build = AlertDialog.Builder(this)
        build.setMessage(ResUtil.getString(R.string.str_setting_cache_dialog_clear_video_msg))
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_eliminate),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        mSidebarCachePictureClearTv!!.setText("0M")
                    }
                }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null).show()
    }

    override fun onResume() {
        super.onResume()
        if ("0" != CacheUtil.getPictureCache()) { // 显示图片缓存
            mSidebarCachePictureClearTv!!.setText(CacheUtil.getPictureCache())
        }
        if ("0" != CacheUtil.getMusicCache()) { // 显示音乐缓存
            Log.d(TAG, "音乐缓存数值：" + CacheUtil.getMusicCache())
            mSidebarCacheMusicClearTv!!.setText(CacheUtil.getMusicCache())
        }
        recoverySettingCacheState()
    }

    /**
     * 描述：恢复缓存设置界面参数状态
     */
    private fun recoverySettingCacheState() {
        mSidebarCacheMusicLimitTv!!.setText(
            SharedPreferencesUtils.Companion.getInstance().getData(
                Constant.KEY_SETTING_CACHE_MUSIC_LIMIT, 0
            ).toString() + "M"
        ) // 恢复自定义缓存设置的音乐缓存上限(字符串整数类型，此处""表示字符串类型)
        mSidebarCacheAutoClearSwitch!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_CACHE_AUTO_CLEAR,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复自动清除缓存选中状态(布尔类型，此处false只表示布尔类型)
    }

    override fun onClick(view: View) {
        if (view.getId() == R.id.sidebar_rl_cache_music_limit) {
            showCustomCacheDialog()
        } else if (view.getId() == R.id.sidebar_rl_cache_music_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_cache_music_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_cache_music_limit, Integer.parseInt(0)))
             * 显示音乐缓存值得大小
             * 
             */
            if ("0M" == mSidebarCacheMusicClearTv!!.getText()
                    .toString()
            ) {   // 音乐缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(
                    this@SettingCacheActivity,
                    ResUtil.getString(R.string.str_setting_cache_no_cache),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showMusicCacheDialog()
            }
        } else if (view.getId() == R.id.sidebar_rl_cache_picture_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_cache_picture_lyrics_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_cache_picture_lyrics_clear, Integer.parseInt(0)))
             * 显示音乐缓存值得大小
             * 
             */
            if ("0M" == mSidebarCachePictureClearTv!!.getText()
                    .toString()
            ) {   // 图片与歌词缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(
                    this@SettingCacheActivity,
                    ResUtil.getString(R.string.str_setting_cache_no_cache),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showPictureCacheDialog()
            }
        } else if (view.getId() == R.id.sidebar_rl_setting_cache_video_clear) {
            /**
             * 在该类初始化时，要通过
             * sidebar_tv_setting_cache_video_clear.setText(SharedPreferencesUtils.getInstance().getData(Constant.key_setting_cache_video_clear, Integer.parseInt(0)))
             * 显示音乐缓存值得大小
             * 
             */
            if ("0M" == mSidebarSettingCacheVideoClearTv!!.getText()
                    .toString()
            ) {    // 视频缓存值为0M时，弹出提示，否则进行清空
                Toast.makeText(
                    this@SettingCacheActivity,
                    ResUtil.getString(R.string.str_setting_cache_no_cache),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showVideoCacheDialog()
            }
        } else if (view.getId() == R.id.sidebar_switch_cache_auto_clear) {
            //
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (compoundButton.getId() == R.id.sidebar_switch_cache_auto_clear) {
            // 此处点击自动清除缓存按钮后，当退出应用后，自动清除所有缓存
            SharedPreferencesUtils.Companion.getInstance()
                .saveData(Constant.KEY_SETTING_CACHE_AUTO_CLEAR, isChecked) //保存自动清除缓存按钮的选中状态
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }

    companion object {
        /**
         * 日志标识符
         */
        private const val TAG = "SettingCacheActivity"
    }
}
