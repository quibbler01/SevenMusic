package com.quibbler.sevenmusic.activity.sidebar

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.CaptureFragment
import com.google.zxing.ScanCallback.AnalyzeCallback
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.UiUtil

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      ScanCaptureActivity
 * Description:    扫一扫界面主类
 * Author:         11103876
 * CreateDate:     2019/10/16 20:38
 */
class ScanCaptureActivity : AppCompatActivity(), View.OnClickListener {
    /**
     * CaptureFragment实例
     */
    var mCaptureFragment: CaptureFragment? = null

    /**
     * 闪光灯布局实例
     */
    private var mSidebarScanFlashLightLayout: LinearLayout? = null

    /**
     * 扫一扫标题布局实例
     */
    private var mSidebarScanTitleLayout: RelativeLayout? = null

    /**
     * 闪光灯图片实例
     */
    private var mSidebarScanFlashLightIv: ImageView? = null

    /**
     * 返回图标实例
     */
    private var mmSidebarScanBackIv: ImageView? = null

    /**
     * 闪光灯提示信息实例
     */
    private var mmSidebarScanLightTipTv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_scan_capture_activity)
        checkPermission()
        initView()
    }

    /**
     * 描述：初始化组件
     */
    private fun initView() {
        mmSidebarScanBackIv = findViewById<ImageView>(R.id.sidebar_iv_scan_back)
        mSidebarScanFlashLightIv = findViewById<ImageView>(R.id.sidebar_iv_scan_open_light)
        mSidebarScanFlashLightLayout =
            findViewById<LinearLayout?>(R.id.sidebar_ll_scan_light_layout)
        mmSidebarScanLightTipTv = findViewById<TextView>(R.id.sidebar_tv_scan_light_tip)
        mSidebarScanTitleLayout = findViewById<RelativeLayout>(R.id.sidebar_fl_scan_title_layout)

        mmSidebarScanBackIv!!.setOnClickListener(this)
        mSidebarScanFlashLightIv!!.setOnClickListener(this)

        val params =
            RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtil.dp2px(this, 44))
        val offset: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            offset = UiUtil.dp2px(this, 24)
            params.topMargin = offset
        }
        mSidebarScanTitleLayout!!.setLayoutParams(params)

        mCaptureFragment = CaptureFragment()
        mCaptureFragment!!.setAnalyzeCallback(analyzeCallback)
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.sidebar_fl_scan_zxing_layout, mCaptureFragment!!).commit()
    }

    /**
     * 描述：二维码解析回调函数
     * result:二维码解析字符信息
     */
    var analyzeCallback: AnalyzeCallback = object : AnalyzeCallback {
        override fun onAnalyzeSuccess(mBitmap: Bitmap?, result: String?) {
            if (mCaptureFragment!!.flashIsOpen()) {
                mmSidebarScanLightTipTv!!.setText(ResUtil.getString(R.string.str_scan_flash_light_open))
                mSidebarScanFlashLightIv!!.setImageResource(R.drawable.sidebar_scan_flash_light_close)
            }
            ScanTransferActivity.Companion.startActivity(this@ScanCaptureActivity, result)
        }

        override fun onAnalyzeFailed() {
        }
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.sidebar_iv_scan_open_light) {
            mCaptureFragment!!.switchFlashlight()
            if (mCaptureFragment!!.flashIsOpen()) {
                mmSidebarScanLightTipTv!!.setText(ResUtil.getString(R.string.str_scan_flash_light_close))
                mSidebarScanFlashLightIv!!.setImageResource(R.drawable.sidebar_scan_flash_light_open)
            } else {
                mmSidebarScanLightTipTv!!.setText(ResUtil.getString(R.string.str_scan_flash_light_open))
                mSidebarScanFlashLightIv!!.setImageResource(R.drawable.sidebar_scan_flash_light_close)
            }
        } else if (v.getId() == R.id.sidebar_iv_scan_back) {
            finish()
        }
    }

    /**
     * 描述：动态权限申请
     * 此处注意一定注意RECORD_AUDIO权限的申请，否则报错
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission.CAMERA
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this, ("" + ResUtil.getString(R.string.str_toast_permission)
                                + permissions[i] + ResUtil.getString(R.string.str_toast_permission_success)),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this, ("" + ResUtil.getString(R.string.str_toast_permission)
                                + permissions[i] + ResUtil.getString(R.string.str_toast_permission_fail)),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        /**
         * 权限申请数组
         */
        private val PERMISSIONS_STORAGE = arrayOf<String?>(permission.CAMERA)

        /**
         * 权限请求状态码
         */
        private const val REQUEST_PERMISSION_CODE = 1


        /**
         * 描述：启动Activity跳转
         * 
         * @param context
         */
        fun startActivity(context: Context) {
            val intent = Intent(context, ScanCaptureActivity::class.java)
            context.startActivity(intent)
        }
    }
}
