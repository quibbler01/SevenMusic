package com.google.zxing;

import android.graphics.Bitmap;

/**
 * 二维码扫描工具类
 */
public class ScanCallback {

    public static final String RESULT_TYPE = "result_type";
    public static final String RESULT_STRING = "result_string";
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = 2;

    /**
     * 解析二维码结果的回调，可以自定义扫描成功后行为
     */
    public interface AnalyzeCallback{

        public void onAnalyzeSuccess(Bitmap mBitmap, String result);

        public void onAnalyzeFailed();
    }
}
