package com.quibbler.sevenmusic.presenter

import android.content.ContentValues
import android.database.Cursor
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.callback.MvCollectCallback
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * 
 * Package:        com.quibbler.sevenmusic.presenter
 * ClassName:      MvPresenter
 * Description:    关于mv的presenter
 * Author:         lishijun
 * CreateDate:     2019/9/27 10:04
 */
object MvPresenter {
    private const val SERVER = "http://114.116.128.229:3000"

    private const val MV_DETAIL_URL = "/mv/detail?mvid="

    private const val MV_URL_URL = "/mv/url?id="

    //获取mv的详细信息
    fun getMvInfo(mvInfo: MvInfo, musicCallBack: MusicCallBack) {
        HttpUtil.sendOkHttpRequest(SERVER + MV_DETAIL_URL + mvInfo.getId(), object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                if (response.body != null) {
                    try {
                        val jsonString =
                            JSONObject(response.body!!.string()).getJSONObject("data").toString()
                        Log.d("QUIBBLER_TAG", "" + jsonString)
                        val tempMvInfo = Gson().fromJson<MvInfo>(jsonString, MvInfo::class.java)
                        tempMvInfo.setUrl((tempMvInfo.getMvUrl()).getUrl())
                        mvInfo.copy(tempMvInfo)
                        musicCallBack.onMusicInfoCompleted()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("url", "出错")
            }
        })
    }

    fun getMvUrlList(mvInfoList: MutableList<MvInfo?>) {
        getMvUrlListFromDetail(0, mvInfoList)
    }

    //通过detail递归获取mv的url
    private fun getMvUrlListFromDetail(i: Int, mvInfoList: MutableList<MvInfo?>) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MV_DETAIL_URL + mvInfoList.get(i)!!.getId(),
            object : Callback {
                override fun onResponse(call: Call?, response: Response) {
                    if (response.body != null) {
                        try {
                            val jsonObject =
                                JSONObject(response.body!!.string()).getJSONObject("data")
                            var mvUrl = jsonObject.getJSONObject("brs").optString("480")
                            if (TextUtils.equals(mvUrl, "")) {
                                mvUrl = jsonObject.getJSONObject("brs").optString("240")
                            }
                            mvInfoList.get(i)!!.setUrl(mvUrl)
                            //获取下一个视频的url
                            if (i + 1 < mvInfoList.size) {
                                getMvUrlListFromDetail(i + 1, mvInfoList)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    Log.d("url", "出错")
                }
            })
    }

    //通过url递归获取mv的url
    private fun getMvUrlListFromUrl(i: Int, mvInfoList: MutableList<MvInfo?>) {
        HttpUtil.sendOkHttpRequest(
            SERVER + MV_URL_URL + mvInfoList.get(i)!!.getId(),
            object : Callback {
                override fun onResponse(call: Call?, response: Response) {
                    try {
                        val jsonObject = JSONObject(response.body!!.string()).getJSONObject("data")
                        mvInfoList.get(i)!!.setUrl(jsonObject.getString("url"))
                        //获取下一个视频的url
                        if (i + 1 < mvInfoList.size) {
                            getMvUrlListFromUrl(i + 1, mvInfoList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    Log.d("url", "出错")
                }
            })
    }

    fun isMvCollected(mvId: String?, mvCollectCallback: MvCollectCallback) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.MV_URL,
                        null, "id = ?", arrayOf<String?>(mvId), null
                    )
                    if (cursor == null || cursor.getCount() == 0) {
                        mvCollectCallback.notCollected()
                    } else {
                        mvCollectCallback.isCollected()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (cursor != null) {
                        cursor.close()
                    }
                }
            }
        })
    }

    fun collectMv(mvInfo: MvInfo, mvCollectCallback: MvCollectCallback) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    val id = mvInfo.getId()
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.MV_URL,
                        null, "id = ?", arrayOf<String>(id.toString()), null
                    )
                    if (cursor == null || cursor.getCount() == 0) {
                        val values = ContentValues()
                        values.put("id", id)
                        values.put("name", mvInfo.getName())
                        values.put("pictureurl", mvInfo.getPictureUrl())
                        MusicApplication.Companion.getContext().getContentResolver()
                            .insert(MusicContentProvider.Companion.MV_URL, values)
                        mvCollectCallback.isCollected()
                    } else {
                        MusicApplication.Companion.getContext().getContentResolver().delete(
                            MusicContentProvider.Companion.MV_URL,
                            "id = ?", arrayOf<String>(mvInfo.getId().toString())
                        )
                        mvCollectCallback.notCollected()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (cursor != null) {
                        cursor.close()
                    }
                }
            }
        })
    }
}
