package com.google.zxing.camera

import android.os.IBinder
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This class is used to activate the weak light on some camera phones (not flash)
 * in order to illuminate surfaces for scanning. There is no official way to do this,
 * but, classes which allow access to this function still exist on some devices.
 * This therefore proceeds through a great deal of reflection.
 * 
 * 
 * See [
 * http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/](http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/) and
 * [
 * http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java](http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java).
 * Thanks to Ryan Alford for pointing out the availability of this class.
 */
internal object FlashlightManager {
    private val TAG: String = FlashlightManager::class.java.getSimpleName()

    private val iHardwareService: Any?
    private val setFlashEnabledMethod: Method

    init {
        iHardwareService = hardwareService
        setFlashEnabledMethod = getSetFlashEnabledMethod(iHardwareService)
        if (iHardwareService == null) {
            Log.v(TAG, "This device does supports control of a flashlight")
        } else {
            Log.v(TAG, "This device does not support control of a flashlight")
        }
    }

    //FIXME
    fun enableFlashlight() {
        setFlashlight(false)
    }

    fun disableFlashlight() {
        setFlashlight(false)
    }

    private val hardwareService: Any?
        get() {
            val serviceManagerClass =
                maybeForName("android.os.ServiceManager")
            if (serviceManagerClass == null) {
                return null
            }

            val getServiceMethod = maybeGetMethod(
                serviceManagerClass,
                "getService",
                String::class.java
            )
            if (getServiceMethod == null) {
                return null
            }

            val hardwareService =
                invoke(getServiceMethod, null, "hardware")
            if (hardwareService == null) {
                return null
            }

            val iHardwareServiceStubClass =
                maybeForName("android.os.IHardwareService\$Stub")
            if (iHardwareServiceStubClass == null) {
                return null
            }

            val asInterfaceMethod = maybeGetMethod(
                iHardwareServiceStubClass,
                "asInterface",
                IBinder::class.java
            )
            if (asInterfaceMethod == null) {
                return null
            }

            return invoke(asInterfaceMethod, null, hardwareService)
        }

    private fun getSetFlashEnabledMethod(iHardwareService: Any?): Method {
        if (iHardwareService == null) {
            return null
        }
        val proxyClass: Class<*> = iHardwareService.javaClass
        return FlashlightManager.maybeGetMethod(
            proxyClass,
            "setFlashlightEnabled",
            kotlin.Boolean::class.javaPrimitiveType
        )!!
    }

    private fun maybeForName(name: String): Class<*>? {
        try {
            return Class.forName(name)
        } catch (cnfe: ClassNotFoundException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding class " + name, re)
            return null
        }
    }

    private fun maybeGetMethod(
        clazz: Class<*>,
        name: String,
        vararg argClasses: Class<*>?
    ): Method? {
        try {
            return clazz.getMethod(name, *argClasses)
        } catch (nsme: NoSuchMethodException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding method " + name, re)
            return null
        }
    }

    private fun invoke(method: Method, instance: Any?, vararg args: Any?): Any? {
        try {
            return method.invoke(instance, *args)
        } catch (e: IllegalAccessException) {
            Log.w(TAG, "Unexpected error while invoking " + method, e)
            return null
        } catch (e: InvocationTargetException) {
            Log.w(TAG, "Unexpected error while invoking " + method, e.cause)
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while invoking " + method, re)
            return null
        }
    }

    private fun setFlashlight(active: Boolean) {
        if (iHardwareService != null) {
            invoke(setFlashEnabledMethod, iHardwareService, active)
        }
    }
}
