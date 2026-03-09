package com.quibbler.sevenmusic.utils;

import java.io.Closeable;
import java.io.Reader;
import java.net.HttpURLConnection;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      CloseResourceUtil
 * Description:    关闭输入输出流工具类
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 18:55
 */
public class CloseResourceUtil {

    public static void closeInputAndOutput(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void disconnect(HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }
}
