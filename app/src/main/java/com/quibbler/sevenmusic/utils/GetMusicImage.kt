package com.quibbler.sevenmusic.utils;

import android.content.Context;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      GetMusicImage
 * Description:    获取歌手头像
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 17:40
 */
public class GetMusicImage {
    public static MusicIconLoadUtil with(Context context) {
        return new MusicIconLoadUtil(context);
    }
}
