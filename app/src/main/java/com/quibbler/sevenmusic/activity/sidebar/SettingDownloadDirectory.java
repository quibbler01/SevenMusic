package com.quibbler.sevenmusic.activity.sidebar;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Package:        com.quibbler.sevenmusic.activity.sidebar
 * ClassName:      SettingDownloadDirectory
 * Description:    实现设置下载目录
 * Author:         11103876
 * CreateDate:     2019/9/19 14:11
 */
public class SettingDownloadDirectory {
    /**
     * SD卡总大小
     */
    private static long mSDTotalSize = 0;
    /**
     * SD卡可用大小
     */
    private static long mSDAvailableSize = 0;

    /**
     * 描述：获取SD总容量大小
     * 以字符串数字形式返回
     *
     * @return
     */
    public static String getSDTotalSize() {
        if (existSDCard()) {
            File SDFile = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(SDFile.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            mSDTotalSize = blockSize * totalBlocks;
            return FormatFileSize(mSDTotalSize);
        }
        return null;
    }

    /**
     * 描述：获取SD可用容量大小
     * 以字符串数字形式返回
     *
     * @return
     */
    public static String getSDAvailableSize() {
        if (existSDCard()) {
            File SDFile = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(SDFile.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getAvailableBlocksLong();
            mSDAvailableSize = blockSize * totalBlocks;
            return FormatFileSize(mSDAvailableSize);
        }
        return null;
    }

    /**
     * 描述：获取SD路径
     *
     * @return
     */
    public static String getSDPath() {
        File SDFile = Environment.getExternalStorageDirectory();
        String SDPath = SDFile.getPath();
        return SDPath;
    }


    /**
     * 描述：格式化SD卡大小显示格式
     *
     * @param fileS
     * @return
     */
    public static String FormatFileSize(long fileS) {
        if (fileS == 0) {
            return "0M";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 描述：检测SD卡是否存在
     *
     * @return
     */
    private static boolean existSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}
