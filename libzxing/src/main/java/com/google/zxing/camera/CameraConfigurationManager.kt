package com.google.zxing.camera

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.WindowManager
import java.util.regex.Pattern
import kotlin.math.abs

internal class CameraConfigurationManager(private val context: Context) {
    private var screenResolution: Point? = null
    private var cameraResolution: Point? = null
    var previewFormat: Int = 0
        private set
    var previewFormatString: String? = null
        private set

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.getParameters()
        previewFormat = parameters.getPreviewFormat()
        previewFormatString = parameters.get("preview-format")
        Log.d(TAG, "Default preview format: " + previewFormat + '/' + previewFormatString)
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.getDefaultDisplay()
        val theScreenResolution = Point()
        display.getSize(theScreenResolution)
        screenResolution = theScreenResolution
        Log.d(TAG, "Screen resolution: " + screenResolution)

        val screenResolutionForCamera = Point()
        screenResolutionForCamera.x = screenResolution!!.x
        screenResolutionForCamera.y = screenResolution!!.y
        // preview size is always something like 480*320, other 320*480
        if (screenResolution!!.x < screenResolution!!.y) {
            screenResolutionForCamera.x = screenResolution!!.y
            screenResolutionForCamera.y = screenResolution!!.x
        }
        Log.d(
            TAG,
            "screenX:" + screenResolutionForCamera.x + "   screenY:" + screenResolutionForCamera.y
        )
        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera)

        // cameraResolution = getCameraResolution(parameters, screenResolution);
        Log.d(TAG, "Camera resolution: " + screenResolution)
    }

    /**
     * Sets the camera up to take preview images which are used for both preview and decoding.
     * We detect the preview format here so that buildLuminanceSource() can build an appropriate
     * LuminanceSource subclass. In the future we may want to force YUV420SP as it's the smallest,
     * and the planar Y can be used for barcode scanning without a copy in some cases.
     */
    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.getParameters()
        Log.d(TAG, "Setting preview size: " + cameraResolution)
        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        Log.d(TAG, "cameraResolution.x=" + cameraResolution!!.x)
        setFlash(parameters)
        setZoom(parameters)
        //setSharpness(parameters);
        //modify here
        camera.setDisplayOrientation(90)
        camera.setParameters(parameters)
    }

    fun switchFlashLight(camera: Camera?) {
        if (camera == null) {
            return
        }
        val mode = flashIsOpen(camera)
        if (mode) {
            doSetTorch(camera, false)
        } else {
            doSetTorch(camera, true)
        }
    }

    fun flashIsOpen(camera: Camera?): Boolean {
        if (camera == null) {
            return false
        }
        val p = camera.getParameters()
        if (p.getFlashMode() == Camera.Parameters.FLASH_MODE_TORCH) {
            return true
        }
        return false
    }

    private fun doSetTorch(camera: Camera, newSetting: Boolean) {
        val parameters = camera.getParameters()
        val flashMode: String?
        /** 是否支持闪光灯  */
        if (newSetting) {
            flashMode = findSettableValue(
                parameters.getSupportedFlashModes(),
                Camera.Parameters.FLASH_MODE_TORCH,
                Camera.Parameters.FLASH_MODE_ON
            )
        } else {
            flashMode = findSettableValue(
                parameters.getSupportedFlashModes(),
                Camera.Parameters.FLASH_MODE_OFF
            )
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode)
        }
        camera.setParameters(parameters)
    }


    fun getCameraResolution(): Point {
        return cameraResolution!!
    }

    fun getScreenResolution(): Point {
        return screenResolution!!
    }

    private fun setFlash(parameters: Camera.Parameters) {
        // FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
        // And this is a hack-hack to work around a different value on the Behold II
        // Restrict Behold II check to Cupcake, per Samsung's advice
        //if (Build.MODEL.contains("Behold II") &&
        //    CameraManager.SDK_INT == Build.VERSION_CODES.CUPCAKE) {
        if (Build.MODEL.contains("Behold II") && CameraManager.Companion.SDK_INT == 3) { // 3 = Cupcake
            parameters.set("flash-value", 1)
        } else {
            parameters.set("flash-value", 2)
        }
        // This is the standard setting to turn the flash off that all devices should honor.
        parameters.set("flash-mode", "off")
    }

    private fun setZoom(parameters: Camera.Parameters) {
        val zoomSupportedString = parameters.get("zoom-supported")
        if (zoomSupportedString != null && !zoomSupportedString.toBoolean()) {
            return
        }

        var tenDesiredZoom: Int = TEN_DESIRED_ZOOM

        val maxZoomString = parameters.get("max-zoom")
        if (maxZoomString != null) {
            try {
                val tenMaxZoom = (10.0 * maxZoomString.toDouble()).toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
                Log.w(TAG, "Bad max-zoom: " + maxZoomString)
            }
        }

        val takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max")
        if (takingPictureZoomMaxString != null) {
            try {
                val tenMaxZoom = takingPictureZoomMaxString.toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
                Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString)
            }
        }

        val motZoomValuesString = parameters.get("mot-zoom-values")
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom)
        }

        val motZoomStepString = parameters.get("mot-zoom-step")
        if (motZoomStepString != null) {
            try {
                val motZoomStep = motZoomStepString.trim { it <= ' ' }.toDouble()
                val tenZoomStep = (10.0 * motZoomStep).toInt()
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep
                }
            } catch (nfe: NumberFormatException) {
                // continue
            }
        }

        // Set zoom. This helps encourage the user to pull back.
        // Some devices like the Behold have a zoom parameter
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", (tenDesiredZoom / 10.0).toString())
        }

        // Most devices, like the Hero, appear to expose this zoom parameter.
        // It takes on values like "27" which appears to mean 2.7x zoom
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom)
        }
    }

    companion object {
        private val TAG: String = CameraConfigurationManager::class.java.getSimpleName()

        private const val TEN_DESIRED_ZOOM = 27
        const val desiredSharpness: Int = 30

        private val COMMA_PATTERN: Pattern = Pattern.compile(",")

        private fun findSettableValue(
            supportedValues: MutableCollection<String?>?,
            vararg desiredValues: String?
        ): String? {
            var result: String? = null
            if (supportedValues != null) {
                for (desiredValue in desiredValues) {
                    if (supportedValues.contains(desiredValue)) {
                        result = desiredValue
                        break
                    }
                }
            }
            return result
        }

        private fun getCameraResolution(
            parameters: Camera.Parameters,
            screenResolution: Point
        ): Point {
            var previewSizeValueString = parameters.get("preview-size-values")
            // saw this on Xperia
            if (previewSizeValueString == null) {
                previewSizeValueString = parameters.get("preview-size-value")
            }

            var cameraResolution: Point? = null

            if (previewSizeValueString != null) {
                Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString)
                cameraResolution =
                    findBestPreviewSizeValue(previewSizeValueString, screenResolution)
            }

            if (cameraResolution == null) {
                // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
                cameraResolution = Point(
                    (screenResolution.x shr 3) shl 3,
                    (screenResolution.y shr 3) shl 3
                )
            }

            return cameraResolution
        }

        private fun findBestPreviewSizeValue(
            previewSizeValueString: CharSequence,
            screenResolution: Point
        ): Point? {
            var bestX = 0
            var bestY = 0
            var diff = Int.Companion.MAX_VALUE
            for (previewSize in COMMA_PATTERN.split(previewSizeValueString)) {
                var previewSize = previewSize
                previewSize = previewSize.trim { it <= ' ' }
                val dimPosition = previewSize.indexOf('x')
                if (dimPosition < 0) {
                    Log.w(TAG, "Bad preview-size: " + previewSize)
                    continue
                }

                val newX: Int
                val newY: Int
                try {
                    newX = previewSize.substring(0, dimPosition).toInt()
                    newY = previewSize.substring(dimPosition + 1).toInt()
                } catch (nfe: NumberFormatException) {
                    Log.w(TAG, "Bad preview-size: " + previewSize)
                    continue
                }

                val newDiff = abs(newX - screenResolution.x) + abs(newY - screenResolution.y)
                if (newDiff == 0) {
                    bestX = newX
                    bestY = newY
                    break
                } else if (newDiff < diff) {
                    bestX = newX
                    bestY = newY
                    diff = newDiff
                }
            }

            if (bestX > 0 && bestY > 0) {
                return Point(bestX, bestY)
            }
            return null
        }

        private fun findBestMotZoomValue(stringValues: CharSequence, tenDesiredZoom: Int): Int {
            var tenBestValue = 0
            for (stringValue in COMMA_PATTERN.split(stringValues)) {
                var stringValue = stringValue
                stringValue = stringValue.trim { it <= ' ' }
                val value: Double
                try {
                    value = stringValue.toDouble()
                } catch (nfe: NumberFormatException) {
                    return tenDesiredZoom
                }
                val tenValue = (10.0 * value).toInt()
                if (abs(tenDesiredZoom - value) < abs(tenDesiredZoom - tenBestValue)) {
                    tenBestValue = tenValue
                }
            }
            return tenBestValue
        }
    }
}
