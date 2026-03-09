package com.quibbler.sevenmusic.service

import android.os.AsyncTask
import com.google.gson.Gson
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.bean.MusicDownloadUrlJsonBean
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.listener.MusicDownloadListener
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MusicPlayerService
 * Description:    下载音乐任务: MusicInfo:传入对象作为参数   Integer  :作为参数显示执行进度    Boolean  :反馈执行结果
 * Author:         zhaopeng
 * CreateDate:     2019/10/11 21:39
 */
class MusicDownloadAsyncTask(listener: MusicDownloadListener) :
    AsyncTask<MusicInfo?, Int?, Boolean?>() {
    private val mListener: MusicDownloadListener

    init {
        this.mListener = listener
    }

    /**
     * 该方法中所有代码在子线程中执行，耗时任务
     * 
     * @param musicInfos 传入的参数类型就是定义的第一个类型
     * @return 返回值就是定义的Boolean类型
     */
    override fun doInBackground(musicInfos: Array<MusicInfo>): Boolean {
        for (musicInfo in musicInfos) {
            var musicOnlineData: MusicDownloadUrlJsonBean.Data? = null
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(MusicURL.API_MUSIC_DOWNLOAD_URL + musicInfo.getId() + "&br=128000").build()
                val response = client.newCall(request).execute()
                if (response.code != 200 || response.body == null) {
                    return false
                }
                val json = response.body!!.string()
                musicOnlineData = (Gson().fromJson<MusicDownloadUrlJsonBean?>(
                    json,
                    MusicDownloadUrlJsonBean::class.java
                )).getData().get(0)
                if (musicOnlineData == null || musicOnlineData.getUrl() == null || "" == musicOnlineData.getUrl()) {
                    return false
                }
            } catch (e: Exception) {
                return false
            }
            val client = OkHttpClient()
            val request = Request.Builder().url(musicOnlineData.getUrl()).build()
            var inputMusicStream: InputStream? = null
            var musicFileOutputStream: FileOutputStream? = null
            try {
                val response = client.newCall(request).execute()
                val body = response.body
                if (body == null) {
                    return false
                }
                val total = body.contentLength()
                inputMusicStream = body.byteStream()

                val musicFile: File = File(SAVE_PATH, musicInfo.getId() + ".mp3")
                musicFileOutputStream = FileOutputStream(musicFile)

                val buffer = ByteArray(1024 * 4)
                var sum: Long = 0
                var current = 0
                while ((inputMusicStream.read(buffer).also { current = it }) != -1) {
                    musicFileOutputStream.write(buffer, 0, current)
                    sum += current.toLong()
                    mListener.onProgress(
                        (sum * 100.00 / total).toInt(),
                        musicInfo.getMusicSongName()
                    )
                }
                musicFileOutputStream.flush()
            } catch (e: Exception) {
                return false
            } finally {
                CloseResourceUtil.closeInputAndOutput(inputMusicStream)
                CloseResourceUtil.closeInputAndOutput(musicFileOutputStream)
            }
        }
        return true
    }

    /**
     * 在后台任务开始执行前调用
     * 检查储存下载歌曲文件的目录是否存在
     */
    override fun onPreExecute() {
        val path: File = File(SAVE_PATH)
        if (!path.exists()) {
            path.mkdir()
        }
    }

    /**
     * @param result
     */
    override fun onPostExecute(result: Boolean) {
        mListener.isSuccess(result)
    }

    /**
     * 后台任务调用publishProgress(Progress...)方法后，该方法会被调用
     * 不建议使用这个方法，更新进度，有延迟
     * 
     * @param values
     */
    override fun onProgressUpdate(values: Array<Int?>?) {
    }

    companion object {
        var SAVE_PATH: String = Constant.EXTERNAL + "/sevenMusic/music"
    }
}

