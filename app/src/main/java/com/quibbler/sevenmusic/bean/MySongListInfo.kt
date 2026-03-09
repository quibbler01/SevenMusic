package com.quibbler.sevenmusic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MySongListInfo
 * Description:    歌单bean数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 14:45
 */
public class MySongListInfo implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(listName);
        dest.writeString(description);
        dest.writeInt(type);
        dest.writeInt(number);
        dest.writeString(songsJsonData);
        dest.writeString(id);
        dest.writeString(imageUrl);
        dest.writeString(creator);
    }

    public static final Parcelable.Creator<MySongListInfo> CREATOR = new Creator<MySongListInfo>() {
        @Override
        public MySongListInfo createFromParcel(Parcel source) {
            MySongListInfo mySongListInfo = new MySongListInfo();
            mySongListInfo.setListName(source.readString());
            mySongListInfo.setDescription(source.readString());
            mySongListInfo.setType(source.readInt());
            mySongListInfo.setNumber(source.readInt());
            mySongListInfo.setSongsJsonData(source.readString());
            mySongListInfo.setId(source.readString());
            mySongListInfo.setImageUrl(source.readString());
            mySongListInfo.setCreator(source.readString());
            return mySongListInfo;
        }

        @Override
        public MySongListInfo[] newArray(int size) {
            return new MySongListInfo[size];
        }
    };

    private String listName = "默认歌单";
    private String description = "";
    private int type = 0;
    private int number = 0;
    private String songsJsonData = "";
    private String id = "-1";
    private String imageUrl = "";
    private String creator;

    public MySongListInfo() {

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MySongListInfo(String listName) {
        this.listName = listName;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSongsJsonData() {
        return songsJsonData;
    }

    public void setSongsJsonData(String songsJsonData) {
        this.songsJsonData = songsJsonData;
    }

}
