package com.quibbler.sevenmusic.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      NetUtil
  * Description:    网络判断util
  * Author:         lishijun
  * CreateDate:     2019/9/30 11:04
 */
public class NetUtil {
    //没有网络
    public static final int NETWORK_NONE = 1;
    //移动网络
    public static final int NETWORK_MOBILE = 0;
    //无线网络
    public static final int NETWORW_WIFI = 2;
    //获取网络启动
    public static int getNetWorkStart(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                //连接服务 CONNECTIVITY_SERVICE
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //网络信息 NetworkInfo
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo!=null&&activeNetworkInfo.isConnected()){
            //判断是否是wifi
            if (activeNetworkInfo.getType()==(ConnectivityManager.TYPE_WIFI)){
                return NETWORW_WIFI;
                //判断是否移动网络
            }else if (activeNetworkInfo.getType()==(ConnectivityManager.TYPE_MOBILE)){
                //返回移动网络
                return NETWORK_MOBILE;
            }
        }else {
            return NETWORK_NONE;
        }
        //默认返回  没有网络
        return NETWORK_NONE;
    }
}
