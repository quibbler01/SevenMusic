package com.quibbler.sevenmusic.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      CacheUtil
 * Description:    缓存辅助工具类
 * Author:         11103876
 * CreateDate:     2019/11/25 17:42
 */
public class CacheUtil {
    private static String TAG = "CacheUtil";
    /**
     * 缓存路径
     */
    private static String cachePath = Environment.getExternalStorageDirectory() + "/sevenMusic";

    /**
     * 描述：获取图片缓存
     * 图片缓存也包括歌手图片的缓存、图片下载的缓存
     *
     * @return
     */
    public static String getPictureCache() {
        try {
            File file = new File(cachePath + "/image");
            if (file.exists()) {
                File[] files = file.listFiles();
                long fileTotalSize = 0;
                for (int i = 0; i < files.length; i++) {
                    fileTotalSize += files[i].length();
                }
                fileTotalSize = fileTotalSize + getSingerCache() + getImageCache(); // 获取总的图片缓存
                Log.d(TAG, "getPictureCache total size = "+FormatFileSize(fileTotalSize));
                return FormatFileSize(fileTotalSize);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * 描述：获取音乐缓存
     *
     * @return
     */
    public static String getMusicCache() {
        try {
            File file = new File(cachePath + "/music");
            if (file.exists()) {
                File[] files = file.listFiles();
                long fileTotalSize = 0;
                for (int i = 0; i < files.length; i++) {
                    fileTotalSize += files[i].length();
                }
                Log.d(TAG, "getMusicCache total size = "+FormatFileSize(fileTotalSize));
                return FormatFileSize(fileTotalSize);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * 描述：获取歌手缓存
     *
     * @return
     */
    public static long getSingerCache() {
        try {
            File file = new File(cachePath + "/singer");
            if (file.exists()) {
                File[] files = file.listFiles();
                long fileTotalSize = 0;
                for (int i = 0; i < files.length; i++) {
                    fileTotalSize += files[i].length();
                }
                return fileTotalSize;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 描述：获取图片下载缓存
     *
     * @return
     */
    public static long getImageCache() {
        try {
            File file = new File(cachePath + "/imgCache");
            if (file.exists()) {
                File[] files = file.listFiles();
                long fileTotalSize = 0;
                for (int i = 0; i < files.length; i++) {
                    fileTotalSize += files[i].length();
                }
                return fileTotalSize;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 计算单个文件的大小 ，单位M
     *
     * @param fileS
     * @return
     */
    private static String FormatFileSize(long fileS) {
        if (fileS == 0) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        fileSizeString = df.format((double) fileS / 1048576) + "M";
        return fileSizeString;
    }

    /**
     * 描述：删除图片缓存
     *
     * @return
     */
    public static boolean deletePictureCache() {
        boolean b = deleteImageCache();
        try {
            File file = new File(cachePath + "/image");
            boolean flag = true; //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deletePictureCache file is not exist or directory");
                return false;
            }
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) { // 删除子文件
                    flag = files[i].delete();
                    if (!flag) {
                        break;
                    }
                }
            }
            return b;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteImageCache() {
        try {
            File file = new File(cachePath + "/imgCache");
            boolean flag = true; //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deletePictureCache file is not exist or directory");
                return false;
            }
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) { // 删除子文件
                    flag = files[i].delete();
                    if (!flag) {
                        break;
                    }
                }
            }
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 描述：删除音乐缓存
     * @return
     */
    public static boolean deleteMusicCache() {
        try {
            File file = new File(cachePath + "/music");
            boolean flag = true; //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deleteMusicCache file is not exist or directory");
                return false;
            }
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) { // 删除子文件
                    flag = files[i].delete();
                    if (!flag) {
                        break;
                    }
                }
            }
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 描述：删除歌手缓存
     * @return
     */
    public static boolean deleteSingerCache() {
        try {
            File file = new File(cachePath + "/singer");
            boolean flag = true; //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deleteSingerCache file is not exist or directory");
                return false;
            }
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) { // 删除子文件
                    flag = files[i].delete();
                    if (!flag) {
                        break;
                    }
                }
            }
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }


}
