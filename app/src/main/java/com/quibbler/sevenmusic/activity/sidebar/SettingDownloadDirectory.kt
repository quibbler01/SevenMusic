package com.quibbler.sevenmusic.activity.sidebar

import android.os.Environment
import android.os.StatFs
import java.text.DecimalFormat

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      SettingDownloadDirectory
 * Description:    实现设置下载目录
 * Author:         11103876
 * CreateDate:     2019/9/19 14:11
 */
object SettingDownloadDirectory {
    /**
     * SD卡总大小
     */
    private var mSDTotalSize: Long = 0

    /**
     * SD卡可用大小
     */
    private var mSDAvailableSize: Long = 0

    val sDTotalSize: String?
        /**
         * 描述：获取SD总容量大小
         * 以字符串数字形式返回
         * 
         * @return
         */
        get() {
            if (existSDCard()) {
                val SDFile = Environment.getExternalStorageDirectory()
                val stat = StatFs(SDFile.getPath())
                val blockSize = stat.getBlockSizeLong()
                val totalBlocks = stat.getBlockCountLong()
                mSDTotalSize = blockSize * totalBlocks
                return FormatFileSize(mSDTotalSize)
            }
            return null
        }

    val sDAvailableSize: String?
        /**
         * 描述：获取SD可用容量大小
         * 以字符串数字形式返回
         * 
         * @return
         */
        get() {
            if (existSDCard()) {
                val SDFile = Environment.getExternalStorageDirectory()
                val stat = StatFs(SDFile.getPath())
                val blockSize = stat.getBlockSizeLong()
                val totalBlocks = stat.getAvailableBlocksLong()
                mSDAvailableSize = blockSize * totalBlocks
                return FormatFileSize(mSDAvailableSize)
            }
            return null
        }

    val sDPath: String
        /**
         * 描述：获取SD路径
         * 
         * @return
         */
        get() {
            val SDFile = Environment.getExternalStorageDirectory()
            val SDPath = SDFile.getPath()
            return SDPath
        }


    /**
     * 描述：格式化SD卡大小显示格式
     * 
     * @param fileS
     * @return
     */
    fun FormatFileSize(fileS: Long): String {
        if (fileS == 0L) {
            return "0M"
        }
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        if (fileS < 1024) {
            fileSizeString = df.format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            fileSizeString = df.format(fileS.toDouble() / 1024) + "K"
        } else if (fileS < 1073741824) {
            fileSizeString = df.format(fileS.toDouble() / 1048576) + "M"
        } else {
            fileSizeString = df.format(fileS.toDouble() / 1073741824) + "G"
        }
        return fileSizeString
    }

    /**
     * 描述：检测SD卡是否存在
     * 
     * @return
     */
    private fun existSDCard(): Boolean {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            return true
        } else {
            return false
        }
    }
}
