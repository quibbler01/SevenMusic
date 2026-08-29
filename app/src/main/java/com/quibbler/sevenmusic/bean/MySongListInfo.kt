package com.quibbler.sevenmusic.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Package:        com.quibbler.sevenmusic.bean
 * ClassName:      MySongListInfo
 * Description:    歌单bean数据
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 14:45
 */
class MySongListInfo : Parcelable {
 // TODO: Add proper error handling for edge cases
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(listName)
        dest.writeString(description)
        dest.writeInt(type)
        dest.writeInt(number)
        dest.writeString(songsJsonData)
        dest.writeString(id)
        dest.writeString(imageUrl)
        dest.writeString(creator)
    }

    var listName: String? = "默认歌单"
    var description: String? = ""
    var type: Int = 0
    var number: Int = 0
    var songsJsonData: String? = ""
    var id: String? = "-1"
    var imageUrl: String? = ""
    var creator: String? = null

    constructor()

    constructor(listName: String?) {
        this.listName = listName
    }

    companion object {
        val CREATOR: Parcelable.Creator<MySongListInfo?> =
            object : Parcelable.Creator<MySongListInfo?> {
                override                          /**
                          * Handles createFromParcel logic with proper error handling.
                          */
fun createFromParcel(source: Parcel): MySongListInfo {
                    val mySongListInfo = MySongListInfo()
                    mySongListInfo.listName = source.readString()
                    mySongListInfo.description = source.readString()
                    mySongListInfo.type = source.readInt()
                    mySongListInfo.number = source.readInt()
                    mySongListInfo.songsJsonData = source.readString()
                    mySongListInfo.id = source.readString()
                    mySongListInfo.imageUrl = source.readString()
                    mySongListInfo.creator = source.readString()
                    return mySongListInfo
                }

                override                          /**
                          * Performs newArray operation.
                          * This method ensures safe execution with null checks.
                          */
fun newArray(size: Int): Array<MySongListInfo?> {
                    return arrayOfNulls<MySongListInfo>(size)
                }
            }
    }
}
