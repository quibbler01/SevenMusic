package com.quibbler.sevenmusic.service

import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView
import com.google.gson.Gson
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.search.SearchSingerJsonBean
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      LoadSingerThumbnailAsyncTask
 * Description:    异步加载图片，缓存
 * Author:         zhaopeng
 * CreateDate:     2019/10/30 16:16
 */
class LoadSingerThumbnailAsyncTask : AsyncTask<Any?, Void?, Bitmap?> {
    private var mName: String? = null
    private var mImageView: WeakReference<ImageView?>? = null

    constructor(imageView: ImageView?) {
        this.mImageView = WeakReference<ImageView?>(imageView)
    }

    constructor()

    override fun onPreExecute() {
        val path = File(Constant.SEVEN_MUSIC_SINGER)
        if (!path.exists()) {
            path.mkdirs()
        }
    }

    override fun doInBackground(args: Array<Any?>): Bitmap? {
        if (args.size <= 0) {
            return null
        }
        mName = args[0] as String?
        if (File(Constant.SEVEN_MUSIC_SINGER + "/" + mName).exists()) {
            return null
        }

        var bitmap: Bitmap? = null
        bitmap =
            MusicIconLoadUtil.Companion.loadBitmapFromCache(Constant.SEVEN_MUSIC_SINGER + "/" + mName)
        if (bitmap != null) {
            return bitmap
        }
        val url: String? = getSingerCoverUrl(mName)
        bitmap = MusicIconLoadUtil.Companion.getBitmapFromServer(url)
        return bitmap
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }
        if (mImageView != null && mImageView!!.get() != null) {
            mImageView!!.get()!!.setImageBitmap(bitmap)
        }
        MusicIconLoadUtil.Companion.saveBitmapToCache(bitmap, Constant.SEVEN_MUSIC_SINGER, mName)
    }

    companion object {
        fun getSingerCoverUrl(name: String?): String? {
            var httpURLConnection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var reader: BufferedReader? = null
            try {
                val url = URL(MusicURL.SEARCH_SINGER + name)
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
                return singerList.get(0)!!.getImg1v1Url()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                CloseResourceUtil.closeInputAndOutput(inputStream)
                CloseResourceUtil.closeReader(reader)
                CloseResourceUtil.disconnect(httpURLConnection)
            }
            return null
        }
    }
}
