package com.quibbler.sevenmusic.service

import android.os.AsyncTask
import com.quibbler.sevenmusic.listener.MvDownloadListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * 
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MvDownloadAsyncTask
 * Description:    mv下载异步任务
 * Author:         lishijun
 * CreateDate:     2019/9/24 10:45
 */
class MvDownloadAsyncTask(mMvDownloadListener: MvDownloadListener) :
    AsyncTask<String?, Int?, Int?>() {
    private var mIsPaused = false
    private var mIsCanceled = false
    private var mLastProgress = 0

    private var mMvDownloadListener: MvDownloadListener

    init {
        this.mMvDownloadListener = mMvDownloadListener
    }

    fun setMvDownloadListener(mvDownloadListener: MvDownloadListener) {
        this.mMvDownloadListener = mvDownloadListener
    }

    override fun doInBackground(vararg strings: String): Int {
        var inputStream: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null
        var file: File? = null
        var response: Response? = null
        try {
            var downLoadLength: Long = 0 //记录已下载的文件长度
            val downLoadUrl: String = strings[0]
            //获取文件名称
            val fileName = strings[1] + ".mp4"
            //mv保存目录
            val newFile: File = File(SAVE_PATH)
            if (!newFile.exists()) {
                newFile.mkdirs()
            }
            file = File(SAVE_PATH, fileName)
            if (file.exists()) {
                downLoadLength = file.length()
            }
            val contentLength = getContentLength(downLoadUrl)
            if (contentLength == 0L) {
                return TYPE_FAILED
            } else if (contentLength == downLoadLength) {
                //如果已下载的字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS
            }
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("RANGE", "bytes=" + downLoadLength + "-") //断点继续下载
                .url(downLoadUrl)
                .build()
            response = client.newCall(request).execute()
            if (response.body != null) {
                inputStream = response.body!!.byteStream()
                randomAccessFile = RandomAccessFile(file, "rw")
                randomAccessFile.seek(downLoadLength) //跳过已下载的字节
                val bytes = ByteArray(1024)
                var total = 0
                var len: Int
                //inputStream.read(bytes)--读取多个字节写到bytes中S
                while ((inputStream.read(bytes).also { len = it }) != -1) {
                    if (mIsCanceled) {
                        return TYPE_CANCELED
                    } else if (mIsPaused) {
                        return TYPE_PAUSED
                    } else {
                        total += len
                        randomAccessFile.write(bytes, 0, len)
                        val progress = ((total + downLoadLength) * 100 / contentLength).toInt()
                        publishProgress(progress)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close()
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close()
                }
                if (mIsCanceled && file != null) {
                    file.delete()
                }
                if (response != null) {
                    response.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return TYPE_SUCCESS
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val progress: Int = values[0]!!
        if (progress > mLastProgress) {
            mMvDownloadListener.onProgress(progress)
            mLastProgress = progress
        }
    }

    override fun onPostExecute(integer: Int) {
        when (integer) {
            TYPE_SUCCESS -> {
                mMvDownloadListener.onSuccess()
            }

            TYPE_FAILED -> {
                mMvDownloadListener.onFailed()
            }

            TYPE_PAUSED -> {
                mMvDownloadListener.onPaused()
            }

            TYPE_CANCELED -> {
                mMvDownloadListener.onCanceled()
            }

            else -> {}
        }
    }

    /**
     * 暂停下载
     */
    fun pauseDownLoad() {
        this.mIsPaused = true
    }

    /**
     * 取消下载
     */
    fun cancelDownLoad() {
        this.mIsCanceled = true
    }

    /**
     * 获取文件长度
     * 
     * @param downLoadUrl
     * @return
     */
    private fun getContentLength(downLoadUrl: String): Long {
        val client = OkHttpClient()
        val request = Request.Builder().url(downLoadUrl).build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                try {
                    val contentLength = response.body!!.contentLength()
                    return contentLength
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    response.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    companion object {
        const val SAVE_PATH: String = "/storage/emulated/0/sevenMusic/mv"

        const val TYPE_SUCCESS: Int = 0
        const val TYPE_FAILED: Int = 1
        const val TYPE_PAUSED: Int = 2
        const val TYPE_CANCELED: Int = 3
    }
}
