package com.quibbler.sevenmusic.activity.sidebar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.CaptureFragment;
import com.google.zxing.ScanCallback;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.UiUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      ScanCaptureActivity
 * Description:    扫一扫界面主类
 * Author:         11103876
 * CreateDate:     2019/10/16 20:38
 */
public class ScanCaptureActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * CaptureFragment实例
     */
    public CaptureFragment mCaptureFragment;
    /**
     * 闪光灯布局实例
     */
    private LinearLayout mSidebarScanFlashLightLayout;
    /**
     * 扫一扫标题布局实例
     */
    private RelativeLayout mSidebarScanTitleLayout;
    /**
     * 闪光灯图片实例
     */
    private ImageView mSidebarScanFlashLightIv;
    /**
     * 返回图标实例
     */
    private ImageView mmSidebarScanBackIv;
    /**
     * 闪光灯提示信息实例
     */
    private TextView mmSidebarScanLightTipTv;

    /**
     * 权限申请数组
     */
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.CAMERA};
    /**
     * 权限请求状态码
     */
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar_scan_capture_activity);
        checkPermission();
        initView();
    }

    /**
     * 描述：初始化组件
     */
    private void initView() {
        mmSidebarScanBackIv = findViewById(R.id.sidebar_iv_scan_back);
        mSidebarScanFlashLightIv = findViewById(R.id.sidebar_iv_scan_open_light);
        mSidebarScanFlashLightLayout = findViewById(R.id.sidebar_ll_scan_light_layout);
        mmSidebarScanLightTipTv = findViewById(R.id.sidebar_tv_scan_light_tip);
        mSidebarScanTitleLayout = findViewById(R.id.sidebar_fl_scan_title_layout);

        mmSidebarScanBackIv.setOnClickListener(this);
        mSidebarScanFlashLightIv.setOnClickListener(this);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtil.dp2px(this, 44));
        int offset;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            offset = UiUtil.dp2px(this, 24);
            params.topMargin = offset;
        }
        mSidebarScanTitleLayout.setLayoutParams(params);

        mCaptureFragment = new CaptureFragment();
        mCaptureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.sidebar_fl_scan_zxing_layout, mCaptureFragment).commit();

    }

    /**
     * 描述：启动Activity跳转
     *
     * @param context
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ScanCaptureActivity.class);
        context.startActivity(intent);
    }


    /**
     * 描述：二维码解析回调函数
     * result:二维码解析字符信息
     */
    ScanCallback.AnalyzeCallback analyzeCallback = new ScanCallback.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            if (mCaptureFragment.flashIsOpen()) {
                mmSidebarScanLightTipTv.setText(ResUtil.getString(R.string.str_scan_flash_light_open));
                mSidebarScanFlashLightIv.setImageResource(R.drawable.sidebar_scan_flash_light_close);
            }
            ScanTransferActivity.startActivity(ScanCaptureActivity.this, result);
        }

        @Override
        public void onAnalyzeFailed() {

        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sidebar_iv_scan_open_light) {
            mCaptureFragment.switchFlashlight();
            if (mCaptureFragment.flashIsOpen()) {
                mmSidebarScanLightTipTv.setText(ResUtil.getString(R.string.str_scan_flash_light_close));
                mSidebarScanFlashLightIv.setImageResource(R.drawable.sidebar_scan_flash_light_open);
            } else {
                mmSidebarScanLightTipTv.setText(ResUtil.getString(R.string.str_scan_flash_light_open));
                mSidebarScanFlashLightIv.setImageResource(R.drawable.sidebar_scan_flash_light_close);
            }
        } else if (v.getId() == R.id.sidebar_iv_scan_back) {
            finish();
        }
    }

    /**
     * 描述：动态权限申请
     * 此处注意一定注意RECORD_AUDIO权限的申请，否则报错
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + ResUtil.getString(R.string.str_toast_permission)
                                    + permissions[i] + ResUtil.getString(R.string.str_toast_permission_success),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + ResUtil.getString(R.string.str_toast_permission)
                                    + permissions[i] + ResUtil.getString(R.string.str_toast_permission_fail),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
