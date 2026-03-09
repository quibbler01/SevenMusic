package com.quibbler.sevenmusic.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.NotificationManagerCompat

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      CheckTools
 * Description:    判断各种权限，状态
 * 20191021 增加网络状态检查方法，判断网络状态是WiFi还是2G/3G/4G
 * Author:         zhaopeng
 * CreateDate:     2019/9/30 9:14
 */
object CheckTools {
    /**
     * Unknown network class
     */
    const val NETWORK_CLASS_UNKNOWN: Int = 0

    /**
     * wifi net work
     */
    const val NETWORK_WIFI: Int = 1

    /**
     * "2G" networks
     */
    const val NETWORK_CLASS_2_G: Int = 2

    /**
     * "3G" networks
     */
    const val NETWORK_CLASS_3_G: Int = 3

    /**
     * "4G" networks
     */
    const val NETWORK_CLASS_4_G: Int = 4

    fun getNetWorkStatus(context: Context): Int {
        var netWorkType = NETWORK_CLASS_UNKNOWN

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.getActiveNetworkInfo()

        if (networkInfo != null && networkInfo.isConnected()) {
            val type = networkInfo.getType()

            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = NETWORK_WIFI
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                netWorkType = getNetWorkClass(context)
            }
        }

        return netWorkType
    }

    private fun getNetWorkClass(context: Context): Int {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        when (telephonyManager.getNetworkType()) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return NETWORK_CLASS_2_G

            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> return NETWORK_CLASS_3_G

            TelephonyManager.NETWORK_TYPE_LTE -> return NETWORK_CLASS_4_G

            else -> return NETWORK_CLASS_UNKNOWN
        }
    }

    /**
     * 检查是否拥有某些权限
     * 
     * @param permission
     * @param context
     * @return
     */
    fun hasPermission(permission: String, context: Context): Boolean {
        if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    /**
     * 判断网络是否可可用
     * 
     * @param context
     * @return
     */
    fun isNetWordAvailable(context: Context?): Boolean {
        if (context != null) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.getActiveNetworkInfo()
            if (networkInfo != null) {
                return networkInfo.isAvailable()
            }
        }
        return false
    }

    fun isNotificationPermissionOpen(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return NotificationManagerCompat.from(context)
                .getImportance() != NotificationManager.IMPORTANCE_NONE
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun openNotificationPermissionSetting(context: Context) {
        try {
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //直接跳转到应用通知设置的代码：
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                context.startActivity(localIntent)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                localIntent.putExtra("app_package", context.getPackageName())
                localIntent.putExtra("app_uid", context.getApplicationInfo().uid)
                context.startActivity(localIntent)
                return
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                localIntent.addCategory(Intent.CATEGORY_DEFAULT)
                localIntent.setData(Uri.parse("package:" + context.getPackageName()))
                context.startActivity(localIntent)
                return
            }

            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null))
                context.startActivity(localIntent)
                return
            }

            localIntent.setAction(Intent.ACTION_VIEW)
            localIntent.setClassName(
                "com.android.settings",
                "com.android.setting.InstalledAppDetails"
            )
            localIntent.putExtra(
                "com.android.settings.ApplicationPkgName",
                context.getPackageName()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
