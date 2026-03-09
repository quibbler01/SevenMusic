package com.quibbler.sevenmusic.activity.sidebar

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置->在线播放音质选择功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 * CompoundButton.OnCheckedChangeListener,
 */
class SettingPlayQualityActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    /**
     * 在线播放音质界面返回图标实例
     */
    private var mSidebarSettingBackPlayQualityToolbar: Toolbar? = null

    /**
     * 在线播放音质->自动实例
     */
    private var mSidebarPlayQualityAutoSelectTv: TextView? = null
    private var mSidebarPlayQualityAutoSelectCb: CheckBox? = null

    /**
     * 在线播放音质->标准实例
     */
    private var mSidebarPlayQualityStandardTv: TextView? = null
    private var mSidebarPlayQualityStandardCb: CheckBox? = null

    /**
     * 在线播放音质->较高实例
     */
    private var mSidebarPlayQualityHighTv: TextView? = null
    private var mSidebarPlayQualityHighCb: CheckBox? = null

    /**
     * 在线播放音质->极高实例
     */
    private var mSidebarPlayQualityExtremelyHighTv: TextView? = null
    private var mSidebarPlayQualityExtremelyHighCb: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_setting_play_quality_activity)
        initView()
    }

    /**
     * 描述：初始化设置->在线播放音质界面布局控件
     */
    private fun initView() {
        mSidebarSettingBackPlayQualityToolbar =
            findViewById<Toolbar?>(R.id.sidebar_toolbar_setting_back_play_quality)
        mSidebarPlayQualityAutoSelectTv =
            findViewById<TextView?>(R.id.sidebar_tv_play_quality_auto_select)
        mSidebarPlayQualityAutoSelectCb =
            findViewById<CheckBox>(R.id.sidebar_cb_play_quality_auto_select)
        mSidebarPlayQualityStandardTv =
            findViewById<TextView>(R.id.sidebar_tv_play_quality_standard)
        mSidebarPlayQualityStandardCb =
            findViewById<CheckBox>(R.id.sidebar_cb_play_quality_standard)
        mSidebarPlayQualityHighTv = findViewById<TextView>(R.id.sidebar_tv_play_quality_high)
        mSidebarPlayQualityHighCb = findViewById<CheckBox>(R.id.sidebar_cb_play_quality_high)
        mSidebarPlayQualityExtremelyHighTv =
            findViewById<TextView>(R.id.sidebar_tv_play_quality_extremely_high)
        mSidebarPlayQualityExtremelyHighCb =
            findViewById<CheckBox>(R.id.sidebar_cb_play_quality_extremely_high)

        setSupportActionBar(mSidebarSettingBackPlayQualityToolbar) // 为在线播放音质界面ToolBar生成返回图标箭头按钮
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) // 添加返回按钮,同时隐去标题
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        mSidebarPlayQualityAutoSelectCb!!.setOnCheckedChangeListener(this)
        mSidebarPlayQualityStandardCb!!.setOnCheckedChangeListener(this)
        mSidebarPlayQualityHighCb!!.setOnCheckedChangeListener(this)
        mSidebarPlayQualityExtremelyHighCb!!.setOnCheckedChangeListener(this)
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        recoverySettingPlayQualityState()
    }

    /**
     * 描述：恢复在线播放音质参数状态
     */
    private fun recoverySettingPlayQualityState() {
        mSidebarPlayQualityAutoSelectCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复在线播放音质-自动-选中状态（布尔类型,此处false只表示布尔类型）
        mSidebarPlayQualityStandardCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_PLAY_QUALITY_STANDARD,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复在线播放音质-标准-选中状态
        mSidebarPlayQualityHighCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_PLAY_QUALITY_HIGH,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复在线播放音质-较高-选中状态
        mSidebarPlayQualityExtremelyHighCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance().getData(
                com.quibbler.sevenmusic.Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH,
                false
            ) as kotlin.Boolean?)!!
        ) // 恢复在线播放音质-极高-选中状态
    }

    override fun onClick(view: View) {
        when (view.getId()) {
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            if (compoundButton.getId() == R.id.sidebar_cb_play_quality_auto_select) {
                sPlayQuality = ResUtil.getString(R.string.str_play_quality_automatic)
                mSidebarPlayQualityAutoSelectCb!!.setChecked(true) // 实现CheckBox单选效果
                mSidebarPlayQualityStandardCb!!.setChecked(false)
                mSidebarPlayQualityHighCb!!.setChecked(false)
                mSidebarPlayQualityExtremelyHighCb!!.setChecked(false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT,
                    isChecked
                ) // 保存在线播放音质的选中or未选中状态-自动
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false)
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_standard) {
                sPlayQuality = mSidebarPlayQualityStandardTv!!.getText().toString()
                mSidebarPlayQualityAutoSelectCb!!.setChecked(false)
                mSidebarPlayQualityStandardCb!!.setChecked(true)
                mSidebarPlayQualityHighCb!!.setChecked(false)
                mSidebarPlayQualityExtremelyHighCb!!.setChecked(false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_PLAY_QUALITY_STANDARD,
                    isChecked
                ) // 保存在线播放音质的选中or未选中状态-标准
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false)
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_high) {
                sPlayQuality = mSidebarPlayQualityHighTv!!.getText().toString()
                mSidebarPlayQualityAutoSelectCb!!.setChecked(false)
                mSidebarPlayQualityStandardCb!!.setChecked(false)
                mSidebarPlayQualityHighCb!!.setChecked(true)
                mSidebarPlayQualityExtremelyHighCb!!.setChecked(false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_PLAY_QUALITY_HIGH,
                    isChecked
                ) // 保存在线播放音质的选中or未选中状态-较高
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH, false)
            } else if (compoundButton.getId() == R.id.sidebar_cb_play_quality_extremely_high) {
                sPlayQuality = mSidebarPlayQualityExtremelyHighTv!!.getText().toString()
                mSidebarPlayQualityAutoSelectCb!!.setChecked(false)
                mSidebarPlayQualityStandardCb!!.setChecked(false)
                mSidebarPlayQualityHighCb!!.setChecked(false)
                mSidebarPlayQualityExtremelyHighCb!!.setChecked(true)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_AUTO_SELECT, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_STANDARD, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_PLAY_QUALITY_HIGH, false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH,
                    isChecked
                ) // 保存在线播放音质的选中or未选中状态-极高
            }
            savePlayQuality()
        }
    }

    /**
     * 描述：将在线播放音质选择的类别保存到SharedPreference中
     */
    private fun savePlayQuality() {
        SharedPreferencesUtils.Companion.getInstance()
            .saveData(Constant.KEY_SETTING_PLAY_QUALITY, sPlayQuality)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> //                savePlayQuality();// 返回到设置界面时，首先保存在线播放音质选择类别
                finish()

            else -> {}
        }
        return true
    }


    companion object {
        /**
         * 日志标识符
         */
        private const val TAG = "PlayQualityActivity"

        /**
         * 保存在线播放歌曲的音质选择类别，默认为空，没有选择
         */
        private var sPlayQuality = ""
    }
}
