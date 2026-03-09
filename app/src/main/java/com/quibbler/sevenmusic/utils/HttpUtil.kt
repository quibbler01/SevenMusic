package com.quibbler.sevenmusic.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Package:        com.quibbler.sevenmusic.util
 * ClassName:      HttpUtil
 * Description:    使用okttp访问网络
 * Author:         lishijun
 * CreateDate:     2019/9/17 17:15
 */
object HttpUtil {
    private const val TAG = "HttpUtil"

    private val client = OkHttpClient()

    //调用自带的callback,回调时需要切换回主线程
    fun sendOkHttpRequest(address: String, callback: Callback) {
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(callback)
    }

    //调用自己封装的callback，回调时已经在主线程
    fun sendOkHttpRequest(address: String, context: Activity?, callback: ICallback) {
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (context == null) return
                context.runOnUiThread(object : Runnable {
                    override fun run() {
                        callback.onFailure()
                    }
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                if (context == null) return
                val responseText = response.body!!.string()
                context.runOnUiThread(object : Runnable {
                    override fun run() {
                        callback.onResponse(responseText)
                    }
                })
            }
        })
    }

    //使用原生的httpconnection
    fun sendHttpRequest(address: String?, callback: ICallback) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(address)
                    connection = url.openConnection() as HttpURLConnection?
                    connection!!.setRequestMethod("GET")
                    connection.setConnectTimeout(8000)
                    connection.setReadTimeout(8000)
                    connection.connect()
                    val responseCode = connection.getResponseCode()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.getInputStream()
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while ((reader.readLine().also { line = it }) != null) {
                            response.append(line)
                        }
                        callback.onResponse(response.toString())
                    } else {
                        callback.onFailure()
                    }
                } catch (e: IOException) {
                    callback.onFailure()
                    e.printStackTrace()
                } finally {
                    if (connection != null) {
                        connection.disconnect()
                    }
                }
            }
        })
    }

    //调用自己封装的callback，回调时已经在主线程。context类型不限于Activity
    fun sendOkHttpRequest(address: String, context: Context, callback: ICallback) {
        val handler = Handler(context.getMainLooper())
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (context == null) return
                handler.post(object : Runnable {
                    override fun run() {
                        callback.onFailure()
                    }
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                if (context == null) return
                val responseText = response.body!!.string()
                handler.post(object : Runnable {
                    override fun run() {
                        callback.onResponse(responseText)
                    }
                })
            }
        })
    }

    //调用自己封装的callback，回调时已经在主线程。context类型不限于Activity。返回的是inputStream。
    fun sendOkHttpRequest(address: String, context: Context, callback: IRequestCallback) {
        val handler = Handler(context.getMainLooper())
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (context == null) return
                handler.post(object : Runnable {
                    override fun run() {
                        callback.onFailure(call, e)
                    }
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                if (context == null) return
                val inputStream = response.body!!.byteStream()
                //                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                val bitmap = BitmapUtils.decodeLowQualityBitmap(inputStream, 400, 200)
                response.close()
                if (bitmap == null) {
                    Log.d(TAG, "bitmap is null!")
                    return
                }
                handler.post(object : Runnable {
                    override fun run() {
                        callback.onResponse(bitmap)
                    }
                })
            }
        })
    }
}
