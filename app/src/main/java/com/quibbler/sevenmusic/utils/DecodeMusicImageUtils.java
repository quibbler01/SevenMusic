package com.quibbler.sevenmusic.utils;

import android.content.Context;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      DecodeMusicImageUtils
 * Description:    音乐专辑封面解析工具类调用入口
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 9:21
 */
public class DecodeMusicImageUtils {

    public static DecodeMusicManager with(Context context) {
        return new DecodeMusicManager(context);
    }

}
