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
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.mymusicdemo.activity
 * ClassName:      SystemSettingActivity
 * Description:    实现系统设置->下载音质选择功能
 * Author:         guojinliang
 * CreateDate:     2019/9/11 17:38
 */
class SettingDownloadQualityActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    /**
     * 下载音质界面返回图标实例
     */
    private var mSidebarSettingBackDownloadQualityToolBar: Toolbar? = null

    /**
     * 下载音质->标准实例
     */
    private var mSidebarDownloadQualityStandardTv: TextView? = null
    private var mSidebarDownloadQualityStandardCb: CheckBox? = null

    /**
     * 下载音质->较高实例
     */
    private var mSidebarDownloadQualityHighTv: TextView? = null
    private var mSidebarDownloadQualityHighCb: CheckBox? = null

    /**
     * 下载音质->极高实例
     */
    private var mSidebarDownloadQualityExtremelyHighTv: TextView? = null
    private var mSidebarDownloadQualityExtremelyHighCb: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_setting_download_quality_activity)
        initView()
    }

    /**
     * 描述：初始化设置->下载音质界面控件
     */
    private fun initView() {
        mSidebarSettingBackDownloadQualityToolBar =
            findViewById<Toolbar?>(R.id.sidebar_toolbar_setting_back_download_quality)
        mSidebarDownloadQualityStandardTv =
            findViewById<TextView>(R.id.sidebar_tv_download_quality_standard)
        mSidebarDownloadQualityStandardCb =
            findViewById<CheckBox>(R.id.sidebar_cb_download_quality_standard)
        mSidebarDownloadQualityHighTv =
            findViewById<TextView>(R.id.sidebar_tv_download_quality_high)
        mSidebarDownloadQualityHighCb =
            findViewById<CheckBox>(R.id.sidebar_cb_download_quality_high)
        mSidebarDownloadQualityExtremelyHighTv =
            findViewById<TextView>(R.id.sidebar_tv_download_quality_extremely_high)
        mSidebarDownloadQualityExtremelyHighCb =
            findViewById<CheckBox>(R.id.sidebar_cb_download_quality_extremely_high)

        setSupportActionBar(mSidebarSettingBackDownloadQualityToolBar) // 为下载音质界面ToolBar生成返回图标箭头按钮
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true) // 添加返回按钮,同时隐去标题
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        mSidebarDownloadQualityStandardCb!!.setOnCheckedChangeListener(this)
        mSidebarDownloadQualityHighCb!!.setOnCheckedChangeListener(this)
        mSidebarDownloadQualityExtremelyHighCb!!.setOnCheckedChangeListener(this)
    }


    override fun onResume() {
        super.onResume()
        recoverySettingDownloadQualityState()
    }

    /**
     * 描述：恢复下载音质参数状态
     */
    private fun recoverySettingDownloadQualityState() {
        mSidebarDownloadQualityStandardCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance()
                .getData(
                    com.quibbler.sevenmusic.Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD,
                    false
                ) as kotlin.Boolean?)!!
        ) // 恢复下载音质-标准-选中状态
        mSidebarDownloadQualityHighCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance()
                .getData(
                    com.quibbler.sevenmusic.Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH,
                    false
                ) as kotlin.Boolean?)!!
        ) // 恢复下载音质-较高-选中状态
        mSidebarDownloadQualityExtremelyHighCb!!.setChecked(
            (SharedPreferencesUtils.Companion.getInstance()
                .getData(
                    com.quibbler.sevenmusic.Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH,
                    false
                ) as kotlin.Boolean?)!!
        ) // 恢复下载音质-极高-选中状态
    }

    override fun onClick(view: View) {
        when (view.getId()) {
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            if (compoundButton.getId() == R.id.sidebar_cb_download_quality_standard) {
                sDownloadQuality = mSidebarDownloadQualityStandardTv!!.getText().toString()
                mSidebarDownloadQualityStandardCb!!.setChecked(true) // 实现CheckBox的单选按钮效果
                mSidebarDownloadQualityHighCb!!.setChecked(false)
                mSidebarDownloadQualityExtremelyHighCb!!.setChecked(false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD,
                    isChecked
                ) // 保存下载音质的选中or未选中状态-标准
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, false)
            } else if (compoundButton.getId() == R.id.sidebar_cb_download_quality_high) {
                sDownloadQuality = mSidebarDownloadQualityHighTv!!.getText().toString()
                mSidebarDownloadQualityStandardCb!!.setChecked(false)
                mSidebarDownloadQualityHighCb!!.setChecked(true)
                mSidebarDownloadQualityExtremelyHighCb!!.setChecked(false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH,
                    isChecked
                ) // 保存下载音质的选中or未选中状态-较高
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH, false)
            } else if (compoundButton.getId() == R.id.sidebar_cb_download_quality_extremely_high) {
                sDownloadQuality = mSidebarDownloadQualityExtremelyHighTv!!.getText().toString()
                mSidebarDownloadQualityStandardCb!!.setChecked(false)
                mSidebarDownloadQualityHighCb!!.setChecked(false)
                mSidebarDownloadQualityExtremelyHighCb!!.setChecked(true)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_STANDARD, false)
                SharedPreferencesUtils.Companion.getInstance()
                    .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY_HIGH, false)
                SharedPreferencesUtils.Companion.getInstance().saveData(
                    Constant.KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH,
                    isChecked
                ) // 保存下载音质的选中or未选中状态-极高
            }
            saveDownloadQuality()
        }
    }

    /**
     * 描述：将下载音质选择的类别保存到SharedPreference中
     */
    private fun saveDownloadQuality() {
        SharedPreferencesUtils.Companion.getInstance()
            .saveData(Constant.KEY_SETTING_DOWNLOAD_QUALITY, sDownloadQuality)
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
        private const val TAG = "DownloadQualityActivity"

        /**
         * 保存在下载音质选择类别，默认为空，没有选择
         */
        private var sDownloadQuality = "未开启"
    }
}
