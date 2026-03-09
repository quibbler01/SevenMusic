package com.quibbler.sevenmusic.activity.sidebar

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 */
class SettingActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    /**
     * 设置界面返回图标实例
     */
    private var mSidebarSettingBackToolBar: Toolbar? = null

    /**
     * 2G、3G、4G网络播放开关按钮实例
     */
    private var mSidebarSettingNetworkPlaySwitch: Switch? = null

    /**
     * 2G、3G、4G网络下载开关按钮实例
     */
    private var mSidebarSettingNetworkDownloadSwitch: Switch? = null

    /**
     * 在线播放音质布局实例
     */
    private var mSidebarSettingPlayQualityLayout: RelativeLayout? = null

    /**
     * 在线播放音质选择（自动、标准、较高、极高）实例
     */
    private var mSidebarSettingPlayQualityTv: TextView? = null

    /**
     * 下载音质布局实例
     */
    private var mSidebarSettingDownloadQualityLayout: RelativeLayout? = null

    /**
     * 下载音质选择（标准、较高、极高）实例
     */
    private var mSidebarSettingDownloadQualityTv: TextView? = null

    /**
     * 边听边存布局实例
     */
    private var mSidebarSettingListenSaveLayout: RelativeLayout? = null

    /**
     * 边听边存选择（免费歌曲、VIP歌曲）实例
     */
    private var mSidebarSettingListenSaveTv: TextView? = null

    /**
     * 下载目录布局实例
     */
    private var mSidebarSettingDownloadDirectoryLayout: RelativeLayout? = null

    /**
     * 下载目录选择（存储卡1、存储卡2、、、）实例
     */
    private var mSidebarSettingDownloadDirectoryTv: TextView? = null

    /**
     * 缓存设置布局实例
     */
    private var mSidebarSettingCacheLayout: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_setting_activity)
        initView()
    }

    /**
     * 描述：初始化设置界面控件
     */
    private fun initView() {
        mSidebarSettingBackToolBar = findViewById<Toolbar?>(R.id.sidebar_toolbar_setting_back)
        mSidebarSettingNetworkPlaySwitch =
            findViewById<Switch>(R.id.sidebar_switch_setting_network_play)
        mSidebarSettingNetworkDownloadSwitch =
            findViewById<Switch>(R.id.sidebar_switch_setting_network_download)
        mSidebarSettingPlayQualityLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_setting_play_quality)
        mSidebarSettingPlayQualityTv = findViewById<TextView>(R.id.sidebar_tv_setting_play_quality)
        mSidebarSettingDownloadQualityLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_setting_download_quality)
        mSidebarSettingDownloadQualityTv =
            findViewById<TextView>(R.id.sidebar_tv_setting_download_quality)
        mSidebarSettingListenSaveLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_setting_listen_save)
        mSidebarSettingListenSaveTv =
            findViewById<TextView>(R.id.sidebar_tv_setting_cache_video_clear)
        mSidebarSettingDownloadDirectoryLayout =
            findViewById<RelativeLayout>(R.id.sidebar_rl_setting_download_directory)
        mSidebarSettingDownloadDirectoryTv =
            findViewById<TextView?>(R.id.sidebar_tv_setting_download_directory)
        mSidebarSettingCacheLayout = findViewById<RelativeLayout>(R.id.sidebar_rl_setting_cache)

        setSupportActionBar(mSidebarSettingBackToolBar) // 为设置界面ToolBar生成返回图标箭头按钮
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) //添加返回按钮,同时隐去标题
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        mSidebarSettingNetworkPlaySwitch!!.setOnCheckedChangeListener(this) // 2G、3G、4G网络播放单选按钮点击事件
        mSidebarSettingNetworkDownloadSwitch!!.setOnCheckedChangeListener(this) // 2G、3G、4G网络下载单选按钮点击事件
        mSidebarSettingPlayQualityLayout!!.setOnClickListener(this) // 在线播放音质布局点击事件
        mSidebarSettingDownloadQualityLayout!!.setOnClickListener(this) // 下载音质布局点击事件
        mSidebarSettingListenSaveLayout!!.setOnClickListener(this) // 边听边存布局点击事件
        mSidebarSettingDownloadDirectoryLayout!!.setOnClickListener(this) // 设置下载目录布局点击事件
        mSidebarSettingCacheLayout!!.setOnClickListener(this) // 缓存设置布局点击事件
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        recoverySettingState()
    }

    /**
     * 描述：恢复设置界面参数状态
     */
    private fun recoverySettingState() {
        mSidebarSettingNetworkPlaySwitch!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance()
                .getData(
                    com.quibbler.sevenmusic.Constant.KEY_SETTING_NETWORK_PLAY,
                    false
                ) as kotlin.Boolean?)!!
        ) // 恢复2G/3G/4G网络播放的选中状态（布尔类型,此处false只表示布尔类型）
        mSidebarSettingNetworkDownloadSwitch!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance()
                .getData(
                    com.quibbler.sevenmusic.Constant.KEY_SETTING_NETWORK_DOWNLOAD,
                    false
                ) as kotlin.Boolean?)!!
        ) // 恢复2G/3G/4G网络下载的选中状态（布尔类型）
        mSidebarSettingPlayQualityTv!!.setText(
            SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_PLAY_QUALITY, "").toString()
        ) // 恢复在线播放音质的选择类别状态（字符串类型）
        mSidebarSettingDownloadQualityTv!!.setText(
            SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_DOWNLOAD_QUALITY, "").toString()
        ) // 恢复下载音质的选择类别状态（字符串类型）
        mSidebarSettingListenSaveTv!!.setText(
            SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_LISTEN_SAVE, "").toString()
        ) // 恢复边听边存的选择类别状态（字符串类型）
    }

    /**
     * 描述：设置下载目录显示对话框
     */
    private fun showDownloadDirectoryDialog() {
        if (SettingDownloadDirectory.getSDTotalSize() != null && SettingDownloadDirectory.getSDAvailableSize() != null) {  // 存在SD卡
            val SDTotalSize = SettingDownloadDirectory.getSDTotalSize() // SD卡总容量
            val SDAvailable = SettingDownloadDirectory.getSDAvailableSize() // SD卡可用容量
            val showSDSizeMsg = SDAvailable + "可用，共" + SDTotalSize // SD卡总容量和可用容量显示信息
            val SDPath = "存储卡1" + "(" + SettingDownloadDirectory.getSDPath() + ")" // SD卡路径
            val builder = AlertDialog.Builder(this)
            builder.setTitle(ResUtil.getString(R.string.str_setting_download_directory_dialog_title))
                .setMessage(SDPath + "\n" + showSDSizeMsg)
                .setPositiveButton(
                    ResUtil.getString(R.string.str_dialog_btn_confirm),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                            //确定逻辑实现
                        }
                    }).setNegativeButton(ResUtil.getString(R.string.str_dialog_btn_cancel), null)
                .show()
        } else {
            Toast.makeText(
                this@SettingActivity,
                ResUtil.getString(R.string.str_setting_download_directory_no_sd_card),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 描述：网络播放提示对话框
     */
    private fun showNetworkPlayDialog() {
        Log.d(TAG, "showNetworkPlayDialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(ResUtil.getString(R.string.str_setting_network_play_dialog_title))
            .setMessage(ResUtil.getString(R.string.str_setting_network_play_download_dialog_msg))
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_sure),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        mSidebarSettingNetworkPlaySwitch!!.setChecked(true)
                    }
                }).setNegativeButton(
                ResUtil.getString(R.string.str_dialog_btn_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        mSidebarSettingNetworkPlaySwitch!!.setChecked(false)
                    }
                }).show()
    }

    /**
     * 描述：网络下载提示对话框
     */
    private fun showNetworkDownloadDialog() {
        Log.d(TAG, "showNetworkDownloadDialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(ResUtil.getString(R.string.str_setting_network_download_dialog_title))
            .setMessage(ResUtil.getString(R.string.str_setting_network_play_download_dialog_msg))
            .setPositiveButton(
                ResUtil.getString(R.string.str_dialog_btn_sure),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        mSidebarSettingNetworkDownloadSwitch!!.setChecked(true)
                    }
                }).setNegativeButton(
                ResUtil.getString(R.string.str_dialog_btn_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        mSidebarSettingNetworkDownloadSwitch!!.setChecked(false)
                    }
                }).show()
    }

    override fun onClick(view: View) {
        if (view.getId() == R.id.sidebar_rl_setting_play_quality) {
            val settingPlayQualityIntent =
                Intent(this@SettingActivity, SettingPlayQualityActivity::class.java)
            startActivity(settingPlayQualityIntent)
        } else if (view.getId() == R.id.sidebar_rl_setting_download_quality) {
            val settingDownloadQualityIntent =
                Intent(this@SettingActivity, SettingDownloadQualityActivity::class.java)
            startActivity(settingDownloadQualityIntent)
        } else if (view.getId() == R.id.sidebar_rl_setting_listen_save) {
            val settingListenSongIntent =
                Intent(this@SettingActivity, SettingListenSaveActivity::class.java)
            startActivity(settingListenSongIntent)
        } else if (view.getId() == R.id.sidebar_rl_setting_download_directory) {
            showDownloadDirectoryDialog()
        } else if (view.getId() == R.id.sidebar_rl_setting_cache) {
            val settingCacheIntent = Intent(this@SettingActivity, SettingCacheActivity::class.java)
            startActivity(settingCacheIntent)
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (compoundButton.getId() == R.id.sidebar_switch_setting_network_play) {
            Log.d(TAG, "showNetworkPlayDialog,isChecked=" + isChecked)
            val network_play_sp = SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_NETWORK_PLAY, false) as Boolean
            if (isChecked && !network_play_sp) {
                showNetworkPlayDialog()
            }
            SharedPreferencesUtils.Companion.getInstance()
                .saveData(Constant.KEY_SETTING_NETWORK_PLAY, isChecked) // 保存2G/3G/4G网络播放按钮的选中状态
        } else if (compoundButton.getId() == R.id.sidebar_switch_setting_network_download) {
            Log.d(TAG, "showNetworkDownloadDialog,isChecked=" + isChecked)
            val network_download_sp = SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, false) as Boolean
            if (isChecked && !network_download_sp) {
                showNetworkDownloadDialog()
            }
            SharedPreferencesUtils.Companion.getInstance()
                .saveData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, isChecked) //保存2G/3G/4G网络下载按钮的选中状态
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
        private const val TAG = "SettingActivity"
    }
}
