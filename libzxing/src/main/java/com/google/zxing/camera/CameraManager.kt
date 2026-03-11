package com.google.zxing.camera

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.view.SurfaceHolder
import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
class CameraManager private constructor(val context: Context?) {
    private val configManager: CameraConfigurationManager
    var camera: Camera? = null
        private set

    @get:Synchronized
    var framingRect: Rect? = null
        /**
         * Calculates the framing rect which the UI should draw to show the user where to place the
         * barcode. This target helps with alignment as well as forces the user to hold the device
         * far enough away to ensure the image will be in focus.
         * 
         * @return The rectangle to draw on screen in window coordinates.
         */
        get() {
            val screenResolution = configManager.getScreenResolution()
            // if (framingRect == null) {
            if (camera == null) {
                return null
            }

            val leftOffset: Int =
                (screenResolution.x - FRAME_WIDTH) / 2

            var topOffset = 0
            if (FRAME_MARGINTOP != -1) {
                topOffset = FRAME_MARGINTOP
            } else {
                topOffset =
                    (screenResolution.y - FRAME_HEIGHT) / 3
            }
            field = Rect(
                leftOffset,
                topOffset,
                leftOffset + FRAME_WIDTH,
                topOffset + FRAME_HEIGHT
            )
            // }
            return field
        }
        private set

    @get:Synchronized
    var framingRectInPreview: Rect? = null
        /**
         * Like [.getFramingRect] but coordinates are in terms of the preview frame,
         * not UI / screen.
         */
        get() {
            if (field == null) {
                val framingRect = this.framingRect
                if (framingRect == null) {
                    return null
                }
                val rect = Rect(this.framingRect)
                val cameraResolution = configManager.getCameraResolution()
                val screenResolution = configManager.getScreenResolution()
                if (cameraResolution == null || screenResolution == null) {
                    // Called early, before init even finished
                    return null
                }
                rect.left = rect.left * cameraResolution.y / screenResolution.x
                rect.right = rect.right * cameraResolution.y / screenResolution.x
                rect.top = rect.top * cameraResolution.x / screenResolution.y
                rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y
                field = rect
            }
            return field
        }
        private set
    private var initialized = false
    var isPreviewing: Boolean = false
    val isUseOneShotPreviewCallback: Boolean

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    val previewCallback: PreviewCallback

    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which requested them.
     */
    val autoFocusCallback: AutoFocusCallback

    init {
        this.configManager = CameraConfigurationManager(context)

        // Camera.setOneShotPreviewCallback() has a race condition in Cupcake, so we use the older
        // Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later, we need to use
        // the more efficient one shot callback, as the older one can swamp the system and cause it
        // to run out of memory. We can't use SDK_INT because it was introduced in the Donut SDK.
        //useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.CUPCAKE;
        this.isUseOneShotPreviewCallback = Build.VERSION.SDK.toInt() > 3 // 3 = Cupcake

        previewCallback = PreviewCallback(
            configManager,
            this.isUseOneShotPreviewCallback
        )
        autoFocusCallback = AutoFocusCallback()
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     * 
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder?) {
        if (camera == null) {
            camera = Camera.open()
            if (camera == null) {
                throw IOException()
            }
            camera!!.setPreviewDisplay(holder)

            if (!initialized) {
                initialized = true
                configManager.initFromCameraParameters(camera)
            }
            configManager.setDesiredCameraParameters(camera)

            //FIXME
            //     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//      if (prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false)) {
//        FlashlightManager.enableFlashlight();
//      }
            FlashlightManager.enableFlashlight()
        }
    }

    val cameraResolution: Point?
        get() = configManager.getCameraResolution()

    /**
     * Closes the camera driver if still in use.
     */
    fun closeDriver() {
        if (camera != null) {
            FlashlightManager.disableFlashlight()
            camera!!.release()
            camera = null
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    fun startPreview() {
        if (camera != null && !this.isPreviewing) {
            camera!!.startPreview()
            this.isPreviewing = true
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    fun stopPreview() {
        if (camera != null && this.isPreviewing) {
            if (!this.isUseOneShotPreviewCallback) {
                camera!!.setPreviewCallback(null)
            }
            camera!!.stopPreview()
            previewCallback.setHandler(null, 0)
            autoFocusCallback.setHandler(null, 0)
            this.isPreviewing = false
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     * 
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    fun requestPreviewFrame(handler: Handler?, message: Int) {
        if (camera != null && this.isPreviewing) {
            previewCallback.setHandler(handler, message)
            if (this.isUseOneShotPreviewCallback) {
                camera!!.setOneShotPreviewCallback(previewCallback)
            } else {
                camera!!.setPreviewCallback(previewCallback)
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     * 
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    fun requestAutoFocus(handler: Handler?, message: Int) {
        if (camera != null && this.isPreviewing) {
            autoFocusCallback.setHandler(handler, message)
            //Log.d(TAG, "Requesting auto-focus callback");
            camera!!.autoFocus(autoFocusCallback)
        }
    }

    /**
     * Converts the result points from still resolution coordinates to screen coordinates.
     * 
     * @param points The points returned by the Reader subclass through Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and offset appropriately
     * so they can be drawn in screen coordinates.
     */
    /*
  public Point[] convertResultPoints(ResultPoint[] points) {
    Rect frame = getFramingRectInPreview();
    int count = points.length;
    Point[] output = new Point[count];
    for (int x = 0; x < count; x++) {
      output[x] = new Point();
      output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
      output[x].y = frame.top + (int) (points[x].getY() + 0.5f);
    }
    return output;
  }
   */
    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     * 
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    fun buildLuminanceSource(data: ByteArray?, width: Int, height: Int): PlanarYUVLuminanceSource {
        val rect = this.framingRectInPreview
        val previewFormat = configManager.getPreviewFormat()
        val previewFormatString = configManager.getPreviewFormatString()
        when (previewFormat) {
            PixelFormat.YCbCr_420_SP, PixelFormat.YCbCr_422_SP -> return PlanarYUVLuminanceSource(
                data, width, height, rect!!.left, rect.top,
                rect.width(), rect.height()
            )

            else ->                 // The Samsung Moment incorrectly uses this variant instead of the 'sp' version.
                // Fortunately, it too has all the Y data up front, so we can read it.
                if ("yuv420p" == previewFormatString) {
                    return PlanarYUVLuminanceSource(
                        data, width, height, rect!!.left, rect.top,
                        rect.width(), rect.height()
                    )
                }
        }
        throw IllegalArgumentException(
            "Unsupported picture format: " +
                    previewFormat + '/' + previewFormatString
        )
    }

    private fun flashLightAvailable(): Boolean {
        return camera != null && this.isPreviewing && this.context!!.getPackageManager()
            .hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH
            )
    }


    fun switchFlashlight() {
        configManager.switchFlashLight(camera)
    }

    fun flashIsOpen(): Boolean {
        return configManager.flashIsOpen(camera)
    }


    companion object {
        private val TAG: String = CameraManager::class.java.getSimpleName()

        private const val MIN_FRAME_WIDTH = 240
        private const val MAX_FRAME_WIDTH = 1200 // = 5/8 * 1920
        private const val MIN_FRAME_HEIGHT = 240
        private const val MAX_FRAME_HEIGHT = 675 // = 5/8 * 1080


        var FRAME_WIDTH: Int = -1
        var FRAME_HEIGHT: Int = -1
        var FRAME_MARGINTOP: Int = -1

        private var cameraManager: CameraManager? = null

        val SDK_INT: Int // Later we can use Build.VERSION.SDK_INT

        init {
            var sdkInt: Int
            try {
                sdkInt = Build.VERSION.SDK.toInt()
            } catch (nfe: NumberFormatException) {
                // Just to be safe
                sdkInt = 10000
            }
            SDK_INT = sdkInt
        }

        /**
         * Initializes this static object with the Context of the calling Activity.
         * 
         * @param context The Activity which wants to use the camera.
         */
        fun init(context: Context?) {
            if (cameraManager == null) {
                cameraManager = CameraManager(context)
            }
        }

        /**
         * Gets the CameraManager singleton instance.
         * 
         * @return A reference to the CameraManager singleton.
         */
        fun get(): CameraManager? {
            return cameraManager
        }

        private fun findDesiredDimensionInRange(resolution: Int, hardMin: Int, hardMax: Int): Int {
            val dim = 5 * resolution / 8 // Target 5/8 of each dimension
            if (dim < hardMin) {
                return hardMin
            }
            if (dim > hardMax) {
                return hardMax
            }
            return dim
        }
    }
}
