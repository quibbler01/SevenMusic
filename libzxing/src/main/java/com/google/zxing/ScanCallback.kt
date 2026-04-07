package com.google.zxing

import android.graphics.Bitmap

/**
 * 二维码扫描工具类
 */
object ScanCallback {
    const val RESULT_TYPE: String = "result_type"
    const val RESULT_STRING: String = "result_string"
    const val RESULT_SUCCESS: Int = 1
    const val RESULT_FAILED: Int = 2

    /**
     * 解析二维码结果的回调，可以自定义扫描成功后行为
     */
    interface AnalyzeCallback {
        fun onAnalyzeSuccess(mBitmap: Bitmap?, result: String?)

        fun onAnalyzeFailed()
    }
}
