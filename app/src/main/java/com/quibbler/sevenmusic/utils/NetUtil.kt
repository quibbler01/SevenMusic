package com.quibbler.sevenmusic.utils

import android.content.Context
import android.net.ConnectivityManager


/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      NetUtil
 * Description:    网络判断util
 * Author:         lishijun
 * CreateDate:     2019/9/30 11:04
 */
object NetUtil {
    //没有网络
    const val NETWORK_NONE: Int = 1

    //移动网络
    const val NETWORK_MOBILE: Int = 0

    //无线网络
    const val NETWORW_WIFI: Int = 2

    //获取网络启动
    fun getNetWorkStart(context: Context): Int {
        val connectivityManager = context //连接服务 CONNECTIVITY_SERVICE
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //网络信息 NetworkInfo
        val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            //判断是否是wifi
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORW_WIFI
                //判断是否移动网络
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                //返回移动网络
                return NETWORK_MOBILE
            }
        } else {
            return NETWORK_NONE
        }
        //默认返回  没有网络
        return NETWORK_NONE
    }
}
