package com.quibbler.sevenmusic;

import android.os.Environment;

/**
 * Package:        com.quibbler.mymusicdemo
 * ClassName:      Constant
 * Description:    存储App中的常量，比如网页链接
 * Author:         guojinliang
 * CreateDate:     2019/9/6 12:26
 */
public class Constant {

    /**
     * 音乐闹钟开关-键值（true为打开，false为关闭）
     */
    public static final String KEY_MUSIC_ALARM_SWITCH = "key_music_alarm_switch";
    /**
     * 音乐闹钟设置的歌曲铃声
     */
    public static final String KEY_MUSIC_ALARM_SONG_NAME = "key_music_alarm_song_name";
    /**
     * 音乐闹钟设置的歌曲sd卡文件路径
     */
    public static final String KEY_MUSIC_ALARM_SONG_PATH = "key_music_alarm_song_path";
    /**
     * 音乐闹钟设置的歌曲的重复频率
     */
    public static final String KEY_MUSIC_ALARM_SONG_REPEAT = "key_music_alarm_song_repeat";
    /**
     * 定时停止播放设置的时间
     */
    public static final String KEY_TIMING_STOP_PLAY = "key_timing_stop_play";

    /**
     * 音乐闹钟设置的时间
     */
    public static final String KEY_MUSIC_ALARM_TIME = "key_music_alarm_time";
    /**
     * 夜间模式开关-键值（true为夜间，false为日间）
     */
    public static final String KEY_NIGHT_MODE = "key_night_mode";

    /**
     * 使用2G/3G/4G网络播放-CheckBox按钮-键值
     */
    public static final String KEY_SETTING_NETWORK_PLAY = "key_setting_network_play";
    /**
     * 使用2G/3G/4G网络下载-键值
     */
    public static final String KEY_SETTING_NETWORK_DOWNLOAD = "key_setting_network_download";
    /**
     * 在线播放音质-文字标识-键值
     */
    public static final String KEY_SETTING_PLAY_QUALITY = "key_setting_play_quality";
    /**
     * 在线播放音质-自动-键值
     */
    public static final String KEY_SETTING_PLAY_QUALITY_AUTO_SELECT = "key_setting_play_quality_auto_select";
    /**
     * 在线播放音质-标准-键值
     */
    public static final String KEY_SETTING_PLAY_QUALITY_STANDARD = "key_setting_play_quality_standard";
    /**
     * 在线播放音质-较高-键值
     */
    public static final String KEY_SETTING_PLAY_QUALITY_HIGH = "key_setting_play_quality_high";
    /**
     * 在线播放音质-极高-键值
     */
    public static final String KEY_SETTING_PLAY_QUALITY_EXTREMELY_HIGH = "key_setting_play_quality_extremely_high";
    /**
     * 下载音质-文字标识-键值
     */
    public static final String KEY_SETTING_DOWNLOAD_QUALITY = "key_setting_download_quality";
    /**
     * 下载音质-标准-键值
     */
    public static final String KEY_SETTING_DOWNLOAD_QUALITY_STANDARD = "key_setting_download_quality_standard";
    /**
     * 下载音质-较高-键值
     */
    public static final String KEY_SETTING_DOWNLOAD_QUALITY_HIGH = "key_setting_download_quality_high";
    /**
     * 下载音质-极高-键值
     */
    public static final String KEY_SETTING_DOWNLOAD_QUALITY_EXTREMELY_HIGH = "key_setting_download_quality_extremely_high";
    /**
     * 边听边存-文字标识-键值
     */
    public static final String KEY_SETTING_LISTEN_SAVE = "key_setting_listen_save";
    /**
     * 边听边存-免费-键值
     */
    public static final String KEY_SETTING_LISTEN_SAVE_FREESONG = "key_setting_listen_save_freesong";
    /**
     * 边听边存-vip-键值
     */
    public static final String KEY_SETTING_LISTEN_SAVE_VIPSONG = "key_setting_listen_save_vipsong";
    /**
     * 设置音乐缓存上限-键值
     */
    public static final String KEY_SETTING_CACHE_MUSIC_LIMIT = "key_setting_cache_music_limit";
    /**
     * 清除音乐缓存-键值
     */
    public static final String KEY_SETTING_CACHE_MUSIC_CLEAR = "key_setting_cache_music_clear";
    /**
     * 清除图片缓存-键值
     */
    public static final String KEY_SETTING_CACHE_PICTURE_CLEAR = "key_setting_cache_picture_clear";
    /**
     * 清除视频缓存-键值
     */
    public static final String KEY_SETTING_CACHE_VIDEO_CLEAR = "key_setting_cache_video_clear";
    /**
     * 自动清除缓存-键值
     */
    public static final String KEY_SETTING_CACHE_AUTO_CLEAR = "key_setting_cache_auto_clear";


    public static final String EXTERNAL = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * 音乐存储路径
     */
    public final static String SEVEN_MUSIC = EXTERNAL + "/sevenMusic/music";
    /**
     * 音乐封面存储路径
     */
    public final static String SEVEN_MUSIC_IMAGE = EXTERNAL + "/sevenMusic/image";
    /**
     * 音乐歌词存储路径
     */
    public final static String SEVEN_MUSIC_LYRICS = EXTERNAL + "/sevenMusic/lyrics";
    /**
     * 音乐歌词存储路径
     */
    public final static String SEVEN_MUSIC_SINGER = EXTERNAL + "/sevenMusic/singer";

    public static final String SP_NAME = "shift";
    /**
     * 第一次启动登录标识
     */
    public static final String KEY_IS_FIRST_LOGIN = "key_is_first_login";
    /**
     * apk 文件存储路径
     */
    public static final String KEY_APK_DOWNLOAD_PATH = "key_apk_download_path";
    /**
     * 格式：yyyy:MM:dd:HH:mm:ss
     */
    public static final String KEY_TIME_FORMAT_01 = "yyyy:MM:dd:HH:mm:ss";
    /**
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String KEY_TIME_FORMAT_02 = "yyyy-MM-dd HH:mm:ss";
    /**
     * 格式：yyyy年MM月dd日HH时mm分ss秒
     */
    public static final String KEY_TIME_FORMAT_03 = "yyyy年MM月dd日HH时mm分ss秒";

    /**
     * 单种消息 数据库名
     */
    public static final String KEY_DB_NAME_MSG = "shift.db";
    /**
     * 单种消息 数据库表名（四种消息，四个表）
     */
    public static final String KEY_DB_TABLE_NAME_MSG_SYSTEM = "system_msg";
    /**
     * 四种消息，四个表）创建表的语句
     */
    public static final String CREATE_TBL_MSG_SYSTEM = "create table if not exists "
            + KEY_DB_TABLE_NAME_MSG_SYSTEM
            + "(_id integer primary key autoincrement, title text, time text, invalid text, uri text, content text, msgtype text, unread text) ";

    /**
     * 音乐播放条-歌曲id
     */
    public static final String KEY_PLAY_BAR_SONG_ID = "key_play_bar_song_id";
    /**
     * 音乐播放条-歌曲名字
     */
    public static final String KEY_PLAY_BAR_SONG_NAME = "key_play_bar_song_name";
    /**
     * 音乐播放条-歌曲作家
     */
    public static final String KEY_PLAY_BAR_SONG_SINGER = "key_play_bar_song_singer";
    /**
     * 音乐播放条-歌曲图片url
     */
    public static final String KEY_PLAY_BAR_SONG_PICTURE_URL = "key_play_bar_song_picture_url";
    /**
     * 音乐播放条-歌曲url
     */
    public static final String KEY_PLAY_BAR_SONG_URL = "key_play_bar_song_url";
    /**
     * 音乐播放条-播放列表选中歌曲position
     */
    public static final String KEY_PLAY_BAR_PLAY_LIST_ITEM_POSITION = "key_play_bar_play_list_item_position";
    /**
     * 音乐播放条-歌曲path
     */
    public static final String KEY_PLAY_BAR_SONG_PATH = "key_play_bar_song_path";
}
