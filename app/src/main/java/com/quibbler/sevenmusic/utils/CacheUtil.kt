package com.quibbler.sevenmusic.utils

import android.os.Environment
import android.util.Log
import java.io.File
import java.text.DecimalFormat

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      CacheUtil
 * Description:    缓存辅助工具类
 * Author:         11103876
 * CreateDate:     2019/11/25 17:42
 */
object CacheUtil {
    private const val TAG = "CacheUtil"

    /**
     * 缓存路径
     */
    private val cachePath = Environment.getExternalStorageDirectory().toString() + "/sevenMusic"

    val pictureCache: String
        /**
         * 描述：获取图片缓存
         * 图片缓存也包括歌手图片的缓存、图片下载的缓存
         * 
         * @return
         */
        get() {
            try {
                val file = File(cachePath + "/image")
                if (file.exists()) {
                    val files = file.listFiles()
                    var fileTotalSize: Long = 0
                    for (i in files!!.indices) {
                        fileTotalSize += files[i]!!.length()
                    }
                    fileTotalSize =
                        fileTotalSize + singerCache + imageCache // 获取总的图片缓存
                    Log.d(
                        TAG,
                        "getPictureCache total size = " + FormatFileSize(fileTotalSize)
                    )
                    return FormatFileSize(fileTotalSize)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return "0"
        }

    val musicCache: String
        /**
         * 描述：获取音乐缓存
         * 
         * @return
         */
        get() {
            try {
                val file = File(cachePath + "/music")
                if (file.exists()) {
                    val files = file.listFiles()
                    var fileTotalSize: Long = 0
                    for (i in files!!.indices) {
                        fileTotalSize += files[i]!!.length()
                    }
                    Log.d(
                        TAG,
                        "getMusicCache total size = " + FormatFileSize(fileTotalSize)
                    )
                    return FormatFileSize(fileTotalSize)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return "0"
        }

    val singerCache: Long
        /**
         * 描述：获取歌手缓存
         * 
         * @return
         */
        get() {
            try {
                val file = File(cachePath + "/singer")
                if (file.exists()) {
                    val files = file.listFiles()
                    var fileTotalSize: Long = 0
                    for (i in files!!.indices) {
                        fileTotalSize += files[i]!!.length()
                    }
                    return fileTotalSize
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return 0
        }

    val imageCache: Long
        /**
         * 描述：获取图片下载缓存
         * 
         * @return
         */
        get() {
            try {
                val file = File(cachePath + "/imgCache")
                if (file.exists()) {
                    val files = file.listFiles()
                    var fileTotalSize: Long = 0
                    for (i in files!!.indices) {
                        fileTotalSize += files[i]!!.length()
                    }
                    return fileTotalSize
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return 0
        }

    /**
     * 计算单个文件的大小 ，单位M
     * 
     * @param fileS
     * @return
     */
    private fun FormatFileSize(fileS: Long): String {
        if (fileS == 0L) {
            return "0"
        }
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        fileSizeString = df.format(fileS.toDouble() / 1048576) + "M"
        return fileSizeString
    }

    /**
     * 描述：删除图片缓存
     * 
     * @return
     */
    fun deletePictureCache(): Boolean {
        val b = deleteImageCache()
        try {
            val file = File(cachePath + "/image")
            var flag = true //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deletePictureCache file is not exist or directory")
                return false
            }
            val files = file.listFiles()
            for (i in files!!.indices) {
                if (files[i]!!.isFile()) { // 删除子文件
                    flag = files[i]!!.delete()
                    if (!flag) {
                        break
                    }
                }
            }
            return b
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteImageCache(): Boolean {
        try {
            val file = File(cachePath + "/imgCache")
            var flag = true //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deletePictureCache file is not exist or directory")
                return false
            }
            val files = file.listFiles()
            for (i in files!!.indices) {
                if (files[i]!!.isFile()) { // 删除子文件
                    flag = files[i]!!.delete()
                    if (!flag) {
                        break
                    }
                }
            }
            return true
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 描述：删除音乐缓存
     * @return
     */
    fun deleteMusicCache(): Boolean {
        try {
            val file = File(cachePath + "/music")
            var flag = true //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deleteMusicCache file is not exist or directory")
                return false
            }
            val files = file.listFiles()
            for (i in files!!.indices) {
                if (files[i]!!.isFile()) { // 删除子文件
                    flag = files[i]!!.delete()
                    if (!flag) {
                        break
                    }
                }
            }
            return true
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 描述：删除歌手缓存
     * @return
     */
    fun deleteSingerCache(): Boolean {
        try {
            val file = File(cachePath + "/singer")
            var flag = true //用于标识是否删除成功
            if (!file.exists() || !file.isDirectory()) { // 如果对应的文件不存在，或者不是一个目录，则退出
                Log.d(TAG, "deleteSingerCache file is not exist or directory")
                return false
            }
            val files = file.listFiles()
            for (i in files!!.indices) {
                if (files[i]!!.isFile()) { // 删除子文件
                    flag = files[i]!!.delete()
                    if (!flag) {
                        break
                    }
                }
            }
            return true
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return false
    }
}
