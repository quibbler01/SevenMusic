package com.quibbler.sevenmusic.utils;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      HttpsUrlConverter
 * Description:    http相关工具类
 * Author:         yanwuyang
 * CreateDate:     2019/9/17 17:14
 */
public class HttpsUrlConverter {
    /**
     * 将http开头的url地址转换成https开头的url。有参数校验。
     *
     * @param httpUrl http开头的url
     * @return https开头的url
     */
    public static String httpToHttps(String httpUrl) {
        if (httpUrl == null) {
            return null;
        }

        //如果是httpsUrl，则返回自身
        if (isHttpsUrl(httpUrl)) {
            return httpUrl;
        }
        //如果是httpUrl，则换成https开头
        if (isHttpUrl(httpUrl)) {
            String httpsUrl = "https" + httpUrl.substring(4);
            return httpsUrl;
        }

        //如果不是合法url，则返回自身
        return httpUrl;
    }

    /**
     * 判断String字符串是否是"http://"开头的地址
     *
     * @param url url地址
     * @return
     */
    public static boolean isHttpUrl(String url) {
        if (url == null) return false;
        return url.startsWith("http://");
    }

    /**
     * 判断String字符串是否是"https://"开头的地址
     *
     * @param url url地址
     * @return
     */
    public static boolean isHttpsUrl(String url) {
        if (url == null) return false;
        return url.startsWith("https://");
    }

    /**
     * 判断String字符串是否是合法的url地址，即以"http://"或"https://"开头
     *
     * @param str url地址
     * @return
     */
    public static boolean isLegalUrl(String str) {
        if (str == null) return false;
        return isHttpUrl(str) || isHttpsUrl(str);
    }
}
