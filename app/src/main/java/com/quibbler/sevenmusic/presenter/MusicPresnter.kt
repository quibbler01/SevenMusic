package com.quibbler.sevenmusic.presenter

import android.text.TextUtils
import android.util.Log
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.utils.HttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * 
 * Package:        com.quibbler.sevenmusic.presenter
 * ClassName:      MusicPresnter
 * Description:    关于音乐的presenter
 * Author:         lishijun
 * CreateDate:     2019/9/27 11:27
 */
object MusicPresnter {
    private const val MUSIC_CANUSE_URL = "/check/music?id="

    private const val SERVER = "http://114.116.128.229:3000"

    private const val MUSIC_LYRIC_URL = "/lyric?id="

    private const val MUSIC_DETAIL_URL = "/song/detail?ids="

    fun getMusicCanUse(mvMusicInfo: MutableList<MvMusicInfo?>) {
        getMusicCanUseFromId(0, mvMusicInfo)
    }

    //填充歌词
    fun getMusicLyric(mvMusicInfo: MvMusicInfo) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MUSIC_LYRIC_URL + mvMusicInfo.getId(),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("url", "出错")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.body != null) {
                            val lyric = JSONObject(response.body!!.string()).getJSONObject("lrc")
                                .getString("lyric")
                            mvMusicInfo.setLyric(lyric)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    //填充歌曲图片，带callback
    fun getMusicPicture(mvMusicInfo: MvMusicInfo, musicCallBack: MusicCallBack) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MUSIC_DETAIL_URL + mvMusicInfo.getId(),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("url", "出错")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.body != null) {
                            val jsonArray =
                                JSONObject(response.body!!.string()).getJSONArray("songs")
                            //如果没有，直接返回
                            if (jsonArray.length() == 0) {
                                return
                            }
                            val songObject = jsonArray.getJSONObject(0)
                            val pictureUrl = songObject.getJSONObject("al").getString("picUrl")
                            mvMusicInfo.setPictureUrl(pictureUrl)
                            musicCallBack.onMusicInfoCompleted()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    //填充歌词，带callback
    fun getMusicLyric(mvMusicInfo: MvMusicInfo, musicCallBack: MusicCallBack) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MUSIC_LYRIC_URL + mvMusicInfo.getId(),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("url", "出错")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.body != null) {
                            val lyric = JSONObject(response.body!!.string()).getJSONObject("lrc")
                                .getString("lyric")
                            mvMusicInfo.setLyric(lyric)
                            musicCallBack.onMusicInfoCompleted()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    //填充歌曲的版权信息
    private fun getMusicCanUseFromId(i: Int, mvMusicInfoList: MutableList<MvMusicInfo?>) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MUSIC_CANUSE_URL + mvMusicInfoList.get(i)!!.getId(),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("url", "出错")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.body != null) {
                            val success = JSONObject(response.body!!.string()).getString("success")
                            if (TextUtils.equals(success, "true")) {
                                mvMusicInfoList.get(i)!!.setCanUse(true)
                            }
                            //获取下一个视频的url
                            if (i + 1 < mvMusicInfoList.size) {
                                getMusicCanUseFromId(i + 1, mvMusicInfoList)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }
}
