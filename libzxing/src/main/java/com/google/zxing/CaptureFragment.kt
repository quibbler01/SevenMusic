package com.google.zxing

import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.ScanCallback.AnalyzeCallback
import com.google.zxing.camera.CameraManager
import com.google.zxing.decoding.CaptureActivityHandler
import com.google.zxing.decoding.InactivityTimer
import com.google.zxing.view.ViewfinderView
import com.google.zxing.view.ViewfinderView.netChangeListener
import java.util.Vector

/**
 * 扫描Fragment
 */
class CaptureFragment : Fragment(), SurfaceHolder.Callback, netChangeListener {
    private var handler: CaptureActivityHandler? = null
    private var viewfinderView: ViewfinderView? = null
    private var warnView: View? = null
    private var scanTips: TextView? = null
    private var coverView: View? = null

    private var hasSurface = false
    private var decodeFormats: Vector<BarcodeFormat?>? = null
    private var characterSet: String? = null
    private var inactivityTimer: InactivityTimer? = null
    private var surfaceView: SurfaceView? = null
    private var surfaceHolder: SurfaceHolder? = null
    var analyzeCallback: AnalyzeCallback? = null
    private var cameraManager: CameraManager? = null
    private var camera: Camera? = null
    private var mFramingRect: Rect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CameraManager.init(activity!!.application)
        hasSurface = false
        inactivityTimer = InactivityTimer(this.activity!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_capture, null)
        viewfinderView = view.findViewById<ViewfinderView>(R.id.viewfinder_view)
        viewfinderView!!.setNetChangeListener(this)
        warnView = view.findViewById<View>(R.id.warn_info)
        scanTips = view.findViewById<TextView>(R.id.scan_tips)
        coverView = view.findViewById<View>(R.id.cover_view)

        surfaceView = view.findViewById<SurfaceView>(R.id.preview_view)
        surfaceHolder = surfaceView!!.getHolder()
        return view
    }

    override fun onResume() {
        super.onResume()
        //startScan();
        cameraManager = CameraManager.get()
        viewfinderView!!.setCameraManager(cameraManager)
        if (hasSurface) {
            initCamera(surfaceHolder)
        } else {
            surfaceHolder!!.addCallback(this)
            surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
        decodeFormats = null
        characterSet = null
    }

    override fun onPause() {
        super.onPause()
        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }

        //stopScan();
        CameraManager.get()!!.closeDriver()
    }


    override fun onDestroy() {
        super.onDestroy()
        inactivityTimer!!.shutdown()
    }

    /**
     * Handler scan result
     * 
     * @param result
     * @param barcode
     */
    fun handleDecode(result: Result?, barcode: Bitmap?) {
        inactivityTimer!!.onActivity()

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (analyzeCallback != null) {
                analyzeCallback!!.onAnalyzeFailed()
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback!!.onAnalyzeSuccess(barcode, result.getText())
            }
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        try {
            CameraManager.get()!!.openDriver(surfaceHolder)
            camera = CameraManager.get()!!.camera
            mFramingRect = cameraManager!!.framingRect
        } catch (ioe: Exception) {
            //部分手机授权失败
            Toast.makeText(
                getActivity(),
                R.string.permission_tips, Toast.LENGTH_LONG
            ).show()
            warnView!!.setVisibility(View.GONE)
            return
        }
        if (handler == null) {
            handler = CaptureActivityHandler(this, decodeFormats, characterSet, viewfinderView)
        }
    }

    private fun showRemindTips() {
        val rect = cameraManager!!.framingRect
        if (rect == null) {
            return
        }
        val tipsParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tipsParams.topMargin = rect.bottom + UiUtil.dp2px(context!!, 10)
        scanTips!!.setLayoutParams(tipsParams)
        scanTips!!.setGravity(Gravity.CENTER)
    }

    /**
     * 检查当前网络是否可用
     */
    private fun checkoutNet(isAvailable: Boolean) {
        if (mFramingRect == null) {
            return
        }

        val warnParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (isAvailable) {
            warnView!!.setVisibility(View.GONE)
            coverView!!.setVisibility(View.GONE)
        } else {
            warnParams.topMargin = mFramingRect!!.bottom - UiUtil.dp2px(context!!, 100)
            warnView!!.setLayoutParams(warnParams)
            warnView!!.setVisibility(View.VISIBLE)
            coverView!!.setVisibility(View.VISIBLE)
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int,
        height: Int
    ) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
        }
        try {
            if (!hasSurface) {
                hasSurface = true
                initCamera(holder)
            }
            showRemindTips()
        } catch (e: Exception) {
            //没有权限，弹出提示用户打开权限
            UiUtil.showToast(getActivity(), getString(R.string.permission_tips))
            getActivity()!!.getSupportFragmentManager().beginTransaction().remove(this)
                .commitAllowingStateLoss()
            //            mflashLightContainer.setVisibility(View.GONE);
//            preview.setVisibility(View.VISIBLE);
            Toast.makeText(
                getActivity(),
                R.string.permission_tips, Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
        if (camera != null) {
            if (camera != null && CameraManager.get()!!.isPreviewing) {
                if (!CameraManager.get()!!.isUseOneShotPreviewCallback) {
                    camera!!.setPreviewCallback(null)
                }
                camera!!.stopPreview()
                CameraManager.get()!!.previewCallback.setHandler(null, 0)
                CameraManager.get()!!.autoFocusCallback.setHandler(null, 0)
                CameraManager.get()!!.isPreviewing = false
            }
        }
    }

    /**
     * 切换闪光灯
     */
    fun switchFlashlight() {
        cameraManager!!.switchFlashlight()
    }

    /**
     * 当前闪光灯状态
     * 
     * @return
     */
    fun flashIsOpen(): Boolean {
        return cameraManager!!.flashIsOpen()
    }

    fun getHandler(): Handler? {
        return handler
    }

    fun drawViewfinder() {
        viewfinderView!!.drawViewfinder()
    }

    override fun netChange(isAvailable: Boolean) {
        checkoutNet(isAvailable)
    }

    companion object {
        private val TAG: String = CaptureFragment::class.java.getSimpleName()
    }
}
