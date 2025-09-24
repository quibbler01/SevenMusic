package com.google.zxing;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.camera.CameraManager;
import com.google.zxing.decoding.CaptureActivityHandler;
import com.google.zxing.decoding.InactivityTimer;
import com.google.zxing.view.ViewfinderView;

import java.util.Vector;


/**
 * 扫描Fragment
 */
public class CaptureFragment extends Fragment implements SurfaceHolder.Callback, ViewfinderView.netChangeListener {

    private static final String TAG = CaptureFragment.class.getSimpleName();

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private View warnView;
    private TextView scanTips;
    private View coverView;

    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ScanCallback.AnalyzeCallback analyzeCallback;
    private CameraManager cameraManager;
    private Camera camera;
    private Rect mFramingRect;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getActivity().getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture, null);
        viewfinderView = view.findViewById(R.id.viewfinder_view);
        viewfinderView.setNetChangeListener(this);
        warnView = view.findViewById(R.id.warn_info);
        scanTips = view.findViewById(R.id.scan_tips);
        coverView = view.findViewById(R.id.cover_view);

        surfaceView = view.findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //startScan();
        cameraManager = CameraManager.get();
        viewfinderView.setCameraManager(cameraManager);
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        //stopScan();

        CameraManager.get().closeDriver();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeFailed();
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeSuccess(barcode, result.getText());
            }
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
            mFramingRect = cameraManager.getFramingRect();
        } catch (Exception ioe) {
            //部分手机授权失败
            Toast.makeText(getActivity(),
                    R.string.permission_tips, Toast.LENGTH_LONG).show();
            warnView.setVisibility(View.GONE);
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet, viewfinderView);
        }
    }

    private void showRemindTips() {
        Rect rect = cameraManager.getFramingRect();
        if (rect == null) {
            return;
        }
        FrameLayout.LayoutParams tipsParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tipsParams.topMargin = rect.bottom + UiUtil.dp2px(getContext(), 10);
        scanTips.setLayoutParams(tipsParams);
        scanTips.setGravity(Gravity.CENTER);
    }

    /**
     * 检查当前网络是否可用
     */
    private void checkoutNet(boolean isAvailable) {
        if (mFramingRect == null) {
            return;
        }

        FrameLayout.LayoutParams warnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isAvailable) {
            warnView.setVisibility(View.GONE);
            coverView.setVisibility(View.GONE);
        } else {
            warnParams.topMargin = mFramingRect.bottom - UiUtil.dp2px(getContext(), 100);
            warnView.setLayoutParams(warnParams);
            warnView.setVisibility(View.VISIBLE);
            coverView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        try {
            if (!hasSurface) {
                hasSurface = true;
                initCamera(holder);
            }
            showRemindTips();
        } catch (Exception e) {
            //没有权限，弹出提示用户打开权限
            UiUtil.showToast(getActivity(), getString(R.string.permission_tips));
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
//            mflashLightContainer.setVisibility(View.GONE);
//            preview.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(),
                    R.string.permission_tips, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (camera != null) {
            if (camera != null && CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }

    /**
     * 切换闪光灯
     */
    public void switchFlashlight() {
        cameraManager.switchFlashlight();
    }

    /**
     * 当前闪光灯状态
     *
     * @return
     */
    public boolean flashIsOpen() {
        return cameraManager.flashIsOpen();
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    public ScanCallback.AnalyzeCallback getAnalyzeCallback() {
        return analyzeCallback;
    }

    public void setAnalyzeCallback(ScanCallback.AnalyzeCallback analyzeCallback) {
        this.analyzeCallback = analyzeCallback;
    }

    @Override
    public void netChange(boolean isAvailable) {
        checkoutNet(isAvailable);
    }
}
