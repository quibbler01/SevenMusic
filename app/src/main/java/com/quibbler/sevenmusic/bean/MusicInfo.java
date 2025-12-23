package com.quibbler.sevenmusic.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MusicInfo
 * Description:    公共音乐信息类，包含所有用到的音乐基本信息。按需构造。
 * 20191021 新增实现Cloneable，考虑使用原型模式，通过clone来创建MusicInfo对象，避免频繁使用new来创建对象
 * Author:         zhaopeng
 * CreateDate:     2019/9/16 21:34
 */
public class MusicInfo implements Parcelable, Cloneable {
    @SerializedName("name")
    private String musicSongName;                   //歌曲名
    private long musicFileSize = 0L;                //歌曲大小
    private int duration = 0;                       //歌曲时长
    private int albumID = 0;                        //图片ID
    private String album = "";                      //专辑
    private String albumPicUrl = "";                   //专辑封面url
    private String musicFilePath = "";              //歌曲路径
    private String singer = "";                     //歌手
    private String id = "-1";                       //歌曲id
    private boolean isPlaying = false;              //音乐播放标志位
    private String url = "";                        //在线播放使用的URL
    private long lastPlayedTime = 0L;               //最后播放时间
    private boolean isSelect = false;
    private boolean isCollected = false;
    private boolean isDownloadFailed = false;

    //歌曲的歌手信息，歌手数量可能大于1
    private List<SingerInfo> ar;
    //专辑信息
    private AlbumInfo al;

    public MusicInfo() {
    }

    public MusicInfo(String musicSongName, String singer) {
        this.musicSongName = musicSongName;
        this.singer = singer;
    }

    public MusicInfo(String musicSongName, String singer, String musicFilePath, String id) {
        this.musicSongName = musicSongName;
        this.musicFilePath = musicFilePath;
        this.singer = singer;
        this.id = id;
    }

    public MusicInfo(String musicSongName, String singer, String musicFilePath, String id, long musicFileSize) {
        this.musicSongName = musicSongName;
        this.musicFilePath = musicFilePath;
        this.singer = singer;
        this.id = id;
        this.musicFileSize = musicFileSize;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("歌曲:");
        stringBuilder.append(musicSongName);
        stringBuilder.append("\t歌手:");
        stringBuilder.append(singer);
        stringBuilder.append("\t歌曲ID:");
        stringBuilder.append(id);
        stringBuilder.append("\t路径:");
        stringBuilder.append(musicFilePath);
        return stringBuilder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(musicSongName);
        parcel.writeLong(musicFileSize);
        parcel.writeInt(duration);
        parcel.writeInt(albumID);
        parcel.writeString(album);
        parcel.writeString(musicFilePath);
        parcel.writeString(singer);
        parcel.writeString(id);
        parcel.writeByte((byte) (isPlaying ? 1 : 0));   //序列化bool类型的值，技巧
        parcel.writeString(url);
        parcel.writeLong(lastPlayedTime);
        parcel.writeByte((byte) (isSelect ? 1 : 0));
    }

    public static final Parcelable.Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel source) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.musicSongName = source.readString();
            musicInfo.musicFileSize = source.readLong();
            musicInfo.duration = source.readInt();
            musicInfo.albumID = source.readInt();
            musicInfo.album = source.readString();
            musicInfo.musicFilePath = source.readString();
            musicInfo.singer = source.readString();
            musicInfo.id = source.readString();
            musicInfo.isPlaying = source.readByte() != 0;
            musicInfo.url = source.readString();
            musicInfo.lastPlayedTime = source.readLong();
            musicInfo.isSelect = source.readByte() != 0;
            return musicInfo;
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    public boolean isDownloadFailed() {
        return isDownloadFailed;
    }

    public void setDownloadFailed(boolean downloadFailed) {
        isDownloadFailed = downloadFailed;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private IsMusicMatch isMusicMatch = new IsMusicMatch();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IsMusicMatch getIsMusicMatch() {
        return isMusicMatch;
    }

    public void setIsMusicMatch(IsMusicMatch isMusicMatch) {
        this.isMusicMatch = isMusicMatch;
    }

    public String getMusicSongName() {
        return musicSongName;
    }

    public void setMusicSongName(String musicSongName) {
        this.musicSongName = musicSongName;
    }

    public long getMusicFileSize() {
        return musicFileSize;
    }

    public void setMusicFileSize(long musicFileSize) {
        this.musicFileSize = musicFileSize;
    }

    public String getMusicFilePath() {
        return musicFilePath;
    }

    public void setMusicFilePath(String musicFilePath) {
        this.musicFilePath = musicFilePath;
    }

    public long getLastPlayedTime() {
        return lastPlayedTime;
    }

    public void setLastPlayedTime(long lastPlayedTime) {
        this.lastPlayedTime = lastPlayedTime;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAlbumID() {
        return albumID;
    }

    public void setAlbumID(int albumID) {
        this.albumID = albumID;
    }

    //获取专辑名字
    public String getAlName() {
        if (al != null) {
            return al.getAlbumName();
        }
        return null;
    }

    //获取第一个歌手名
    public String getFirstArName() {
        if (ar != null && !ar.isEmpty()) {
            SingerInfo singerInfo = ar.get(0);
            return singerInfo.getName();
        } else {
            return "";
        }
    }

    /**
     * 获取歌手名，指定显示歌手个数
     *
     * @param num 显示的歌手个数
     * @param gap 分隔符
     * @return
     */
    public String getArName(int num, String gap) {
        int len = Math.min(ar.size(), num);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                result.append(ar.get(i).getName());
            } else {
                result.append(gap + " " + ar.get(i).getName());
            }
        }
        return result.toString();
    }

    public String getArName() {
        return getArName(1);
    }

    public String getArName(int num) {
        return getArName(num, ",");
    }

    public String getAllArName() {
        return getArName(ar.size());
    }

    public List<SingerInfo> getAr() {
        return ar;
    }

    public String getAlbumPicUrl() {
        if (al == null) {
            return albumPicUrl;
        }
        if (TextUtils.isEmpty(al.getPicUrl())) {
            return albumPicUrl;
        }
        return al.getPicUrl();
    }

    public void setAlbumPicUrl(String albumPicUrl) {
        this.albumPicUrl = albumPicUrl;
    }

    /**
     * 成员变量都是基本类型和String类型，是深拷贝。如果是含有复杂类型的成员变量需要手动实现深拷贝
     *
     * @return
     */
    @Override
    protected MusicInfo clone() {
        MusicInfo musicInfo = null;
        try {
            musicInfo = (MusicInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return musicInfo;
    }

    //由于用到indexof，需要重写equals和hashcode
    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;//地址相等
        }
        if(obj == null){
            return false;//非空性：对于任意非空引用x，x.equals(null)应该返回false。
        }
        if(obj instanceof MusicInfo){
            MusicInfo other = (MusicInfo) obj;
            //id相等，则这两个对象相等
            if(TextUtils.equals(this.id, other.id)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (id == null ? 0 : id.hashCode());
        return result;
    }
}


