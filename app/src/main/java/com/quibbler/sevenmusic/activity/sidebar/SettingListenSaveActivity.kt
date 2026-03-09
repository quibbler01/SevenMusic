package com.quibbler.sevenmusic.activity.sidebar

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置->边听边存选择功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 */
class SettingListenSaveActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    /**
     * 边听边存界面返回图标实例
     */
    private var mSideBarSettingBackListenSaveToolBar: Toolbar? = null

    /**
     * 边听边存->免费实例
     */
    private var mSideBarListenSaveFreeSongTv: TextView? = null
    private var mSideBarListenSaveFreeSongSwitch: Switch? = null

    /**
     * 边听边存->vip歌曲实例
     */
    private var mSideBarListenSaveVipSongTv: TextView? = null
    private var mSideBarListenSaveVipSongSwitch: Switch? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_setting_listensave_activity)
        initView()
    }

    /**
     * 描述：初始化设置->边听边存界面布局控件
     */
    private fun initView() {
        mSideBarSettingBackListenSaveToolBar =
            findViewById<Toolbar?>(R.id.sidebar_toolbar_setting_back_listensave)
        mSideBarListenSaveFreeSongTv = findViewById<TextView>(R.id.sidebar_tv_listen_save_freesong)
        mSideBarListenSaveFreeSongSwitch =
            findViewById<Switch>(R.id.sidebar_switch_listen_save_free_song)
        mSideBarListenSaveVipSongTv = findViewById<TextView>(R.id.sidebar_tv_listen_save_vipsong)
        mSideBarListenSaveVipSongSwitch =
            findViewById<Switch>(R.id.sidebar_switch_listen_save_vip_song)

        setSupportActionBar(mSideBarSettingBackListenSaveToolBar) // 为边听边存界面ToolBar生成返回图标箭头按钮
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) // 添加返回按钮,同时隐去标题
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        mSideBarListenSaveFreeSongSwitch!!.setOnCheckedChangeListener(this)
        mSideBarListenSaveVipSongSwitch!!.setOnCheckedChangeListener(this)
        saveListenSaveSong() // 首次打开界面时，边听边存默认显示-未开启
    }

    /**
     * 描述：将边听边选选择的类别保存到SharedPreference中
     */
    private fun saveListenSaveSong() {
        SharedPreferencesUtils.Companion.getInstance()
            .saveData(Constant.KEY_SETTING_LISTEN_SAVE, sListenSave)
    }

    override fun onResume() {
        super.onResume()
        recoverySettingListenSaveState()
    }

    /**
     * 描述：恢复边听边存参数状态
     */
    private fun recoverySettingListenSaveState() {
        mSideBarListenSaveFreeSongSwitch!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_LISTEN_SAVE_FREESONG,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复边听边存-免费-选中状态
        mSideBarListenSaveVipSongSwitch!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_LISTEN_SAVE_VIPSONG,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复边听边存-vip专属歌曲-选中状态
    }

    override fun onClick(view: View) {
        when (view.getId()) {
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (compoundButton.getId() == R.id.sidebar_switch_listen_save_free_song) {
            if (isChecked) {
                sListenSave = mSideBarListenSaveFreeSongTv!!.getText().toString()
            } else {
                sListenSave = "未开启"
            }
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_SETTING_LISTEN_SAVE_FREESONG,
                isChecked
            ) // 保存边听边存的选中or未选中状态-免费
        } else if (compoundButton.getId() == R.id.sidebar_switch_listen_save_vip_song) {
            if (isChecked) {
                sListenSave = mSideBarListenSaveVipSongTv!!.getText().toString()
            } else {
                sListenSave = "未开启"
            }
            SharedPreferencesUtils.Companion.getInstance().saveData(
                Constant.KEY_SETTING_LISTEN_SAVE_VIPSONG,
                isChecked
            ) // 保存边听边存的选中or未选中状态-vip专属歌曲
        }
        saveListenSaveSong()
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
        private const val TAG = "SettingListenSaveActivity"

        /**
         * 保存边听边存歌曲种类，默认是未开启
         */
        private var sListenSave = "未开启"
    }
}
