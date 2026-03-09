package com.quibbler.sevenmusic

import android.os.Environment

/**
 * Package:        com.quibbler.mymusicdemo
 * ClassName:      Constant
 * Description:    存储App中的常量，比如网页链接
 * Author:         guojinliang
 * CreateDate:     2019/9/6 12:26
 */
object Constant {
    /**
     * 音乐闹钟开关-键值（true为打开，false为关闭）
     */
    const val KEY_MUSIC_ALARM_SWITCH: String = "key_music_alarm_switch"

    /**
     * 音乐闹钟设置的歌曲铃声
     */
    const val KEY_MUSIC_ALARM_SONG_NAME: String = "key_music_alarm_song_name"

    /**
     * 音乐闹钟设置的歌曲sd卡文件路径
     */
    const val KEY_MUSIC_ALARM_SONG_PATH: String = "key_music_alarm_song_path"

    /**
     * 音乐闹钟设置的歌曲的重复频率
     */
    const val KEY_MUSIC_ALARM_SONG_REPEAT: String = "key_music_alarm_song_repeat"

    /**
     * 定时停止播放设置的时间
     */
    const val KEY_TIMING_STOP_PLAY: String = "key_timing_stop_play"

    /**
     * 音乐闹钟设置的时间
     */
    const val KEY_MUSIC_ALARM_TIME: String = "key_music_alarm_time"

    /**
     * 夜间模式开关-键值（true为夜间，false为日间）
     */
    const val KEY_NIGHT_MODE: String = "key_night_mode"

    /**
     * 使用2G/3G/4G网络播放-CheckBox按钮-键值
     */
    const val KEY_SETTING_NETWORK_PLAY: String = "key_setting_network_play"

    /**
     * 使用2G/3G/4G网络下载-键值
     */
    const val KEY_SETTING_NETWORK_DOWNLOAD: String = "key_setting_network_download"

    /**
     * 在线播放音质-文字标识-键值
     */
    const val KEY_SETTING_PLAY_QUALITY: String = "key_setting_play_quality"

    /**
     * 在线播放音质-自动-键值
     */
    const val KEY_SETTING_PLAY_QUALITY_AUTO_SELECT: String = "key_setting_play_quality_auto_select"

    /**
     * 在线播放音质-标准-键值
     */
    const val KEY_SETTING_PLAY_QUALITY_STANDARD: String = "key_setting_play_quality_standard"

    /**
     * 在线播放音质-较高-键值
     */
    const val KEY_SETTING_PLAY_QUALITY_HIGH: String = "key_setting_play_quality_high"

    /**
     * 在线播放音质-极高-键值
     */
    const val KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH: String =
        "key_setting_play_quality_extremely_high"

    /**
     * 下载音质-文字标识-键值
     */
    const val KEY_SETTING_DOWNLOAD_QUALITY: String = "key_setting_download_quality"

    /**
     * 下载音质-标准-键值
     */
    const val KEY_SETTING_DOWNLOAD_QUALITY_STANDARD: String =
        "key_setting_download_quality_standard"

    /**
     * 下载音质-较高-键值
     */
    const val KEY_SETTING_DOWNLOAD_QUALITY_HIGH: String = "key_setting_download_quality_high"

    /**
     * 下载音质-极高-键值
     */
    const val KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH: String =
        "key_setting_download_quality_extremely_high"

    /**
     * 边听边存-文字标识-键值
     */
    const val KEY_SETTING_LISTEN_SAVE: String = "key_setting_listen_save"

    /**
     * 边听边存-免费-键值
     */
    const val KEY_SETTING_LISTEN_SAVE_FREESONG: String = "key_setting_listen_save_freesong"

    /**
     * 边听边存-vip-键值
     */
    const val KEY_SETTING_LISTEN_SAVE_VIPSONG: String = "key_setting_listen_save_vipsong"

    /**
     * 设置音乐缓存上限-键值
     */
    const val KEY_SETTING_CACHE_MUSIC_LIMIT: String = "key_setting_cache_music_limit"

    /**
     * 清除音乐缓存-键值
     */
    const val KEY_SETTING_CACHE_MUSIC_CLEAR: String = "key_setting_cache_music_clear"

    /**
     * 清除图片缓存-键值
     */
    const val KEY_SETTING_CACHE_PICTURE_CLEAR: String = "key_setting_cache_picture_clear"

    /**
     * 清除视频缓存-键值
     */
    const val KEY_SETTING_CACHE_VIDEO_CLEAR: String = "key_setting_cache_video_clear"

    /**
     * 自动清除缓存-键值
     */
    const val KEY_SETTING_CACHE_AUTO_CLEAR: String = "key_setting_cache_auto_clear"


    val EXTERNAL: String = Environment.getExternalStorageDirectory().getAbsolutePath()

    /**
     * 音乐存储路径
     */
    val SEVEN_MUSIC: String = EXTERNAL + "/sevenMusic/music"

    /**
     * 音乐封面存储路径
     */
    val SEVEN_MUSIC_IMAGE: String = EXTERNAL + "/sevenMusic/image"

    /**
     * 音乐歌词存储路径
     */
    val SEVEN_MUSIC_LYRICS: String = EXTERNAL + "/sevenMusic/lyrics"

    /**
     * 音乐歌词存储路径
     */
    val SEVEN_MUSIC_SINGER: String = EXTERNAL + "/sevenMusic/singer"

    const val SP_NAME: String = "shift"

    /**
     * 第一次启动登录标识
     */
    const val KEY_IS_FIRST_LOGIN: String = "key_is_first_login"

    /**
     * apk 文件存储路径
     */
    const val KEY_APK_DOWNLOAD_PATH: String = "key_apk_download_path"

    /**
     * 格式：yyyy:MM:dd:HH:mm:ss
     */
    const val KEY_TIME_FORMAT_01: String = "yyyy:MM:dd:HH:mm:ss"

    /**
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    const val KEY_TIME_FORMAT_02: String = "yyyy-MM-dd HH:mm:ss"

    /**
     * 格式：yyyy年MM月dd日HH时mm分ss秒
     */
    const val KEY_TIME_FORMAT_03: String = "yyyy年MM月dd日HH时mm分ss秒"

    /**
     * 单种消息 数据库名
     */
    const val KEY_DB_NAME_MSG: String = "shift.db"

    /**
     * 单种消息 数据库表名（四种消息，四个表）
     */
    const val KEY_DB_TABLE_NAME_MSG_SYSTEM: String = "system_msg"

    /**
     * 四种消息，四个表）创建表的语句
     */
    val CREATE_TBL_MSG_SYSTEM: String = ("create table if not exists "
            + KEY_DB_TABLE_NAME_MSG_SYSTEM
            + "(_id integer primary key autoincrement, title text, time text, invalid text, uri text, content text, msgtype text, unread text) ")

    /**
     * 音乐播放条-歌曲id
     */
    const val KEY_PLAY_BAR_SONG_ID: String = "key_play_bar_song_id"

    /**
     * 音乐播放条-歌曲名字
     */
    const val KEY_PLAY_BAR_SONG_NAME: String = "key_play_bar_song_name"

    /**
     * 音乐播放条-歌曲作家
     */
    const val KEY_PLAY_BAR_SONG_SINGER: String = "key_play_bar_song_singer"

    /**
     * 音乐播放条-歌曲图片url
     */
    const val KEY_PLAY_BAR_SONG_PICTURE_URL: String = "key_play_bar_song_picture_url"

    /**
     * 音乐播放条-歌曲url
     */
    const val KEY_PLAY_BAR_SONG_URL: String = "key_play_bar_song_url"

    /**
     * 音乐播放条-播放列表选中歌曲position
     */
    const val KEY_PLAY_BAR_PLAY_LIST_ITEM_POSITION: String = "key_play_bar_play_list_item_position"

    /**
     * 音乐播放条-歌曲path
     */
    const val KEY_PLAY_BAR_SONG_PATH: String = "key_play_bar_song_path"
}
