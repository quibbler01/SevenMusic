package com.quibbler.sevenmusic.bean

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import kotlin.math.min

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicInfo
 * Description:    公共音乐信息类，包含所有用到的音乐基本信息。按需构造。
 * 20191021 新增实现Cloneable，考虑使用原型模式，通过clone来创建MusicInfo对象，避免频繁使用new来创建对象
 * Author:         zhaopeng
 * CreateDate:     2019/9/16 21:34
 */
class MusicInfo : Parcelable, Cloneable {
    @SerializedName("name")
    private var musicSongName: String? = null //歌曲名
    private var musicFileSize = 0L //歌曲大小
    private var duration = 0 //歌曲时长
    private var albumID = 0 //图片ID
    private var album: String? = "" //专辑
    var albumPicUrl: String? = "" //专辑封面url
        get() {
            if (al == null) {
                return field
            }
            if (TextUtils.isEmpty(al.getPicUrl())) {
                return field
            }
            return al.getPicUrl()
        }
    private var musicFilePath: String? = "" //歌曲路径
    private var singer: String? = "" //歌手
    private var id: String? = "-1" //歌曲id
    private var isPlaying = false //音乐播放标志位
    private var url: String? = "" //在线播放使用的URL
    private var lastPlayedTime = 0L //最后播放时间
    private var isSelect = false
    var isCollected: Boolean = false
    var isDownloadFailed: Boolean = false

    //歌曲的歌手信息，歌手数量可能大于1
    val ar: MutableList<SingerInfo>? = null

    //专辑信息
    private val al: AlbumInfo? = null

    constructor()

    constructor(musicSongName: String?, singer: String?) {
        this.musicSongName = musicSongName
        this.singer = singer
    }

    constructor(musicSongName: String?, singer: String?, musicFilePath: String?, id: String?) {
        this.musicSongName = musicSongName
        this.musicFilePath = musicFilePath
        this.singer = singer
        this.id = id
    }

    constructor(
        musicSongName: String?,
        singer: String?,
        musicFilePath: String?,
        id: String?,
        musicFileSize: Long
    ) {
        this.musicSongName = musicSongName
        this.musicFilePath = musicFilePath
        this.singer = singer
        this.id = id
        this.musicFileSize = musicFileSize
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("歌曲:")
        stringBuilder.append(musicSongName)
        stringBuilder.append("\t歌手:")
        stringBuilder.append(singer)
        stringBuilder.append("\t歌曲ID:")
        stringBuilder.append(id)
        stringBuilder.append("\t路径:")
        stringBuilder.append(musicFilePath)
        return stringBuilder.toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(musicSongName)
        parcel.writeLong(musicFileSize)
        parcel.writeInt(duration)
        parcel.writeInt(albumID)
        parcel.writeString(album)
        parcel.writeString(musicFilePath)
        parcel.writeString(singer)
        parcel.writeString(id)
        parcel.writeByte((if (isPlaying) 1 else 0).toByte()) //序列化bool类型的值，技巧
        parcel.writeString(url)
        parcel.writeLong(lastPlayedTime)
        parcel.writeByte((if (isSelect) 1 else 0).toByte())
    }

    fun isSelect(): Boolean {
        return isSelect
    }

    fun setSelect(select: Boolean) {
        isSelect = select
    }

    fun getAlbum(): String? {
        return album
    }

    fun setAlbum(album: String?) {
        this.album = album
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String?) {
        this.url = url
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
    }

    var isMusicMatch: IsMusicMatch? = IsMusicMatch()

    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getMusicSongName(): String? {
        return musicSongName
    }

    fun setMusicSongName(musicSongName: String?) {
        this.musicSongName = musicSongName
    }

    fun getMusicFileSize(): Long {
        return musicFileSize
    }

    fun setMusicFileSize(musicFileSize: Long) {
        this.musicFileSize = musicFileSize
    }

    fun getMusicFilePath(): String? {
        return musicFilePath
    }

    fun setMusicFilePath(musicFilePath: String?) {
        this.musicFilePath = musicFilePath
    }

    fun getLastPlayedTime(): Long {
        return lastPlayedTime
    }

    fun setLastPlayedTime(lastPlayedTime: Long) {
        this.lastPlayedTime = lastPlayedTime
    }

    fun getSinger(): String? {
        return singer
    }

    fun setSinger(singer: String?) {
        this.singer = singer
    }

    fun getDuration(): Int {
        return duration
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    fun getAlbumID(): Int {
        return albumID
    }

    fun setAlbumID(albumID: Int) {
        this.albumID = albumID
    }

    val alName: String?
        //获取专辑名字
        get() {
            if (al != null) {
                return al.getAlbumName()
            }
            return null
        }

    val firstArName: String?
        //获取第一个歌手名
        get() {
            if (ar != null && !ar.isEmpty()) {
                val singerInfo = ar.get(0)
                return singerInfo.getName()
            } else {
                return ""
            }
        }

    /**
     * 获取歌手名，指定显示歌手个数
     * 
     * @param num 显示的歌手个数
     * @param gap 分隔符
     * @return
     */
    fun getArName(num: Int, gap: String?): String {
        val len = min(ar!!.size, num)
        val result = StringBuffer()
        for (i in 0..<len) {
            if (i == 0) {
                result.append(ar.get(i).getName())
            } else {
                result.append(gap + " " + ar.get(i).getName())
            }
        }
        return result.toString()
    }

    val arName: String
        get() = getArName(1)

    fun getArName(num: Int): String {
        return getArName(num, ",")
    }

    val allArName: String
        get() = getArName(ar!!.size)

    /**
     * 成员变量都是基本类型和String类型，是深拷贝。如果是含有复杂类型的成员变量需要手动实现深拷贝
     * 
     * @return
     */
    override fun clone(): MusicInfo {
        var musicInfo: MusicInfo = null
        try {
            musicInfo = super.clone() as MusicInfo
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return musicInfo
    }

    //由于用到indexof，需要重写equals和hashcode
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true //地址相等
        }
        if (obj == null) {
            return false //非空性：对于任意非空引用x，x.equals(null)应该返回false。
        }
        if (obj is MusicInfo) {
            val other = obj
            //id相等，则这两个对象相等
            if (TextUtils.equals(this.id, other.id)) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + (if (id == null) 0 else id.hashCode())
        return result
    }

    companion object {
        val CREATOR: Parcelable.Creator<MusicInfo?> = object : Parcelable.Creator<MusicInfo?> {
            override fun createFromParcel(source: Parcel): MusicInfo {
                val musicInfo = MusicInfo()
                musicInfo.musicSongName = source.readString()
                musicInfo.musicFileSize = source.readLong()
                musicInfo.duration = source.readInt()
                musicInfo.albumID = source.readInt()
                musicInfo.album = source.readString()
                musicInfo.musicFilePath = source.readString()
                musicInfo.singer = source.readString()
                musicInfo.id = source.readString()
                musicInfo.isPlaying = source.readByte().toInt() != 0
                musicInfo.url = source.readString()
                musicInfo.lastPlayedTime = source.readLong()
                musicInfo.isSelect = source.readByte().toInt() != 0
                return musicInfo
            }

            override fun newArray(size: Int): Array<MusicInfo?> {
                return arrayOfNulls<MusicInfo>(size)
            }
        }
    }
}


