package com.quibbler.sevenmusic.utils

import java.io.Closeable
import java.io.Reader
import java.net.HttpURLConnection

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      CloseResourceUtil
 * Description:    关闭输入输出流工具类
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 18:55
 */
object CloseResourceUtil {
    fun closeInputAndOutput(closeable: Closeable?) {
        try {
            if (closeable != null) {
                closeable.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeReader(reader: Reader?) {
        if (reader != null) {
            try {
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect(urlConnection: HttpURLConnection?) {
        if (urlConnection != null) {
            urlConnection.disconnect()
        }
    }
}
