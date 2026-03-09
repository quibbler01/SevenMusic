package com.quibbler.sevenmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.bean.MusicCoverJsonBean
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.search.SearchSingerJsonBean
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicIconLoadUtil
 * Description:    封面icon获取
 * Author:         zhaopeng
 * CreateDate:     2019/9/29 17:18
 */
class MusicIconLoadUtil(mContext: Context) {
    private var mContext: Context? = null
    private var mHandler: Handler? = null
    private var mSingerName: String? = null
    private var mID: String? = null
    private var mResource = 0

    private val mBitmap: Bitmap? = null

    init {
        this.mContext = mContext
        mHandler = Handler(mContext.getMainLooper())
    }

    fun found(singerName: String?): MusicIconLoadUtil {
        mSingerName = singerName
        return this
    }

    fun setID(id: String?): MusicIconLoadUtil {
        this.mID = id
        return this
    }

    fun placeHolder(resource: Int): MusicIconLoadUtil {
        this.mResource = resource
        return this
    }

    @MainThread
    fun into(view: ImageView) {
        mHandler!!.post(object : Runnable {
            override fun run() {
                view.setImageResource(mResource)
            }
        })
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var httpURLConnection: HttpURLConnection? = null
                var inputStream: InputStream? = null
                var reader: BufferedReader? = null
                try {
                    val url = URL(MusicURL.SEARCH_SINGER + mSingerName)
                    httpURLConnection = url.openConnection() as HttpURLConnection?
                    httpURLConnection!!.setRequestMethod("GET")
                    httpURLConnection.setConnectTimeout(8000)
                    httpURLConnection.setReadTimeout(8000)
                    inputStream = httpURLConnection.getInputStream()
                    reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonData = StringBuilder()
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        jsonData.append(line)
                    }
                    val gson = Gson()
                    val singerList = gson.fromJson<SearchSingerJsonBean?>(
                        jsonData.toString(),
                        SearchSingerJsonBean::class.java
                    ).getResult().getArtists()
                    if (singerList.size != 0) {
                        mHandler!!.post(object : Runnable {
                            override fun run() {
                                Glide.with(mContext!!).load(singerList.get(0)!!.getImg1v1Url())
                                    .placeholder(mResource).into(view)
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    CloseResourceUtil.closeInputAndOutput(inputStream)
                    CloseResourceUtil.closeReader(reader)
                    CloseResourceUtil.disconnect(httpURLConnection)
                }
            }
        })
    }

    fun icon(view: ImageView) {
        mHandler!!.post(object : Runnable {
            override fun run() {
                view.setImageResource(mResource)
            }
        })
        if (mID == null) {
            return
        }
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var connection: HttpURLConnection? = null
                var inputStream: InputStream? = null
                var bufferedReader: BufferedReader? = null
                try {
                    val musicDetailUrl = URL(MusicURL.API_GET_SONG_DETAIL_AND_IMAGE + mID)
                    connection = musicDetailUrl.openConnection() as HttpURLConnection?
                    connection!!.setConnectTimeout(8000)
                    connection.setReadTimeout(8000)
                    connection.setRequestMethod("GET")
                    inputStream = connection.getInputStream()
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val builder = StringBuilder()
                    var line: String?
                    while ((bufferedReader.readLine().also { line = it }) != null) {
                        builder.append(line)
                    }
                    val jsonData = builder.toString()
                    val musicCoverJsonBean = Gson().fromJson<MusicCoverJsonBean>(
                        jsonData,
                        MusicCoverJsonBean::class.java
                    )
                    val imageUrl = musicCoverJsonBean.getSongs().get(0).getAl().getPicUrl()
                    mHandler!!.post(object : Runnable {
                        override fun run() {
                            Glide.with(mContext!!).load(imageUrl).placeholder(mResource).into(view)
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    CloseResourceUtil.closeInputAndOutput(inputStream)
                    CloseResourceUtil.closeReader(bufferedReader)
                    CloseResourceUtil.disconnect(connection)
                }
            }
        })
    }

    companion object {
        @WorkerThread
        fun getBitmapFromServer(uri: String?): Bitmap? {
            var bitmap: Bitmap? = null
            var httpURLConnection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            try {
                val url = URL(uri)
                httpURLConnection = url.openConnection() as HttpURLConnection?
                httpURLConnection!!.setConnectTimeout(8000)
                httpURLConnection.setReadTimeout(8000)
                httpURLConnection.setRequestMethod("GET")
                if (httpURLConnection.getResponseCode() != 200) {
                    return null
                }
                inputStream = httpURLConnection.getInputStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
                return bitmap
            } catch (e: Exception) {
            } finally {
                CloseResourceUtil.closeInputAndOutput(inputStream)
                CloseResourceUtil.disconnect(httpURLConnection)
            }

            return null
        }

        @WorkerThread
        fun loadBitmapFromCache(path: String): Bitmap? {
            val file = File(path)
            if (!file.exists()) {
                return null
            }
            val bitmap = BitmapFactory.decodeFile(path)
            return bitmap
        }

        fun saveBitmapToCache(bitmap: Bitmap, dirPath: String, name: String?) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    val dir = File(dirPath)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(dirPath + "/" + name)
                    if (file.exists()) {
                        return
                    }
                    var fileOutputStream: FileOutputStream? = null
                    try {
                        fileOutputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, fileOutputStream)
                        fileOutputStream.flush()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        CloseResourceUtil.closeInputAndOutput(fileOutputStream)
                    }
                }
            })
        }

        fun getMusicIcon(musicID: String?) {
            if (musicID == null || "" == musicID) {
                return
            }
            val file = File(Constant.SEVEN_MUSIC_IMAGE + "/" + musicID)
            if (file.exists()) {
                return
            }

            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    //先解析歌曲详细详细
                    var connection: HttpURLConnection? = null
                    var inputStream: InputStream? = null
                    var bufferedReader: BufferedReader? = null
                    try {
                        val musicDetailUrl = URL(MusicURL.API_GET_SONG_DETAIL_AND_IMAGE + musicID)
                        connection = musicDetailUrl.openConnection() as HttpURLConnection?
                        connection!!.setConnectTimeout(8000)
                        connection.setReadTimeout(8000)
                        connection.setRequestMethod("GET")
                        if (connection.getResponseCode() != 200) {
                            return
                        }
                        inputStream = connection.getInputStream()
                        bufferedReader = BufferedReader(InputStreamReader(inputStream))
                        val builder = StringBuilder()
                        var line: String?
                        while ((bufferedReader.readLine().also { line = it }) != null) {
                            builder.append(line)
                        }
                        //拿到歌曲封面地址，去获取歌曲封面
                        val jsonData = builder.toString()
                        val musicCoverJsonBean = Gson().fromJson<MusicCoverJsonBean>(
                            jsonData,
                            MusicCoverJsonBean::class.java
                        )
                        if (musicCoverJsonBean.getSongs().size == 0) {
                            return
                        }
                        val imageUrl = musicCoverJsonBean.getSongs().get(0).getAl().getPicUrl()
                        if ("" == imageUrl || imageUrl == null) {
                            return
                        }
                        connection.disconnect()
                        inputStream.close()
                        val musicImageURL = URL(imageUrl)
                        connection = musicImageURL.openConnection() as HttpURLConnection?
                        connection!!.setConnectTimeout(8000)
                        connection.setReadTimeout(8000)
                        connection.setRequestMethod("GET")
                        if (connection.getResponseCode() != 200) {
                            return
                        }
                        inputStream = connection.getInputStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        saveBitmapToCache(bitmap, Constant.SEVEN_MUSIC_IMAGE, musicID)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        CloseResourceUtil.closeInputAndOutput(inputStream)
                        CloseResourceUtil.disconnect(connection)
                    }
                }
            })
        }
    }
}