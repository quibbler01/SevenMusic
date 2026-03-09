package com.quibbler.sevenmusic.activity.sidebar

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.utils.ResUtil
import java.util.regex.Pattern

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      ScanTransferActivity
 * Description:    二维码扫描结果处理Activity
 * Author:         11103876
 * CreateDate:     2019/10/16 20:39
 */
class ScanTransferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sidebar_scan_transfer_activity)
        dealScanInfo()
    }

    /**
     * 描述：通过对话框弹出扫描结果信息
     */
    private fun dealScanInfo() {
        val resultInfo =
            getIntent().getStringExtra("code_info") // 获取ScanCaptureActivity保存的二维码解析字符信息
        if (resultInfo == null) {
            return
        }
        //利用正则表达式判断内容是否是URL，是的话则打开网页
        val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") // 设置正则表达式
        val pattern = Pattern.compile(regex.trim { it <= ' ' }) // 网址url比对
        val matcher = pattern.matcher(resultInfo)
        if (matcher.matches()) { // 若是网址，则打开浏览器
            val uri = Uri.parse(resultInfo)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {  // 若不是网址，对话框弹出扫描结果信息
            AlertDialog.Builder(this)
                .setTitle(ResUtil.getString(R.string.str_dialog_scan_title_tips))
                .setMessage(resultInfo)
                .setNegativeButton(
                    ResUtil.getString(R.string.str_dialog_btn_continue_scan),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            finish()
                        }
                    }).setPositiveButton(
                    ResUtil.getString(R.string.str_dialog_btn_sure),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            finish()
                        }
                    }).create().show()
        }
    }

    override fun finish() {
        super.finish()
    }

    companion object {
        /**
         * 描述：跳转到其他Activity
         * 
         * @param context
         * @param codeInfo
         */
        fun startActivity(context: Context, codeInfo: String?) {
            val intent = Intent(context, ScanTransferActivity::class.java)
            intent.putExtra("code_info", codeInfo)
            context.startActivity(intent)
        }
    }
}
