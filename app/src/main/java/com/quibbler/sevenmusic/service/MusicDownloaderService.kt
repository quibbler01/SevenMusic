package com.quibbler.sevenmusic.service

import android.Manifest.permission
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.Parcelable
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.listener.MusicDownloadListener
import com.quibbler.sevenmusic.utils.CheckTools
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils
import java.io.File

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MusicDownloaderService
 * Description:    下载音乐的Service服务的基本框架，使用AsyncTask任务执行 下载状态通知，下载完成更新前台 广播
 * 20191021 下载网络状态检测，通过设置来判断是否使用移动网络进行下载
 * Author:         zhaopeng
 * CreateDate:     2019/10/11 21:39
 */
class MusicDownloaderService : Service() {
    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private var mDownloadList: MutableList<MusicInfo>? = ArrayList<MusicInfo>()

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.createNotificationChannel(this.notificationChannel)
        builder = NotificationCompat.Builder(this, "download")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.download_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.download_notification
                )
            )
            .setProgress(100, 0, false)
            .setAutoCancel(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY_COMPATIBILITY
        }
        if (!CheckTools.hasPermission(
                permission.WRITE_EXTERNAL_STORAGE,
                MusicApplication.Companion.getContext()
            )
        ) {
            return super.onStartCommand(intent, flags, startId)
        }
        //非WiFi情况下下载
        if (CheckTools.getNetWorkStatus(MusicApplication.Companion.getContext()) != CheckTools.NETWORK_WIFI) {
            //从郭金良那里获取设置，是否使用移动网络下载
            val setting: Any? = SharedPreferencesUtils.Companion.getInstance()
                .getData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, false)
            if (setting != null && !(setting as Boolean)) {
                Toast.makeText(
                    MusicApplication.Companion.getContext(),
                    MusicApplication.Companion.getContext()
                        .getString(R.string.service_download_toast_network_setting),
                    Toast.LENGTH_SHORT
                ).show()
                return START_STICKY_COMPATIBILITY
            }
        }
        val musicLists = intent.getSerializableExtra("musics") as MutableList<MusicInfo>?
        if (musicLists != null && musicLists.size != 0) {
            mDownloadList = musicLists
            for (musicInfo in musicLists) {
                startDownAsyncTask(musicInfo)
            }
        } else {
            val musicInfo = intent.getParcelableExtra<Parcelable?>("music") as MusicInfo?
            if (musicInfo != null && "" != musicInfo.getId()) {
                mDownloadList!!.add(musicInfo)
                startDownAsyncTask(musicInfo)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotificationBuilder(title: String?, progress: Int): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, "download")
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.download_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.download_notification
                )
            )
            .setAutoCancel(true)
        if (progress > 0) {
            builder.setContentText("已下载:" + progress + "%")
            builder.setProgress(100, progress, false)
        }
        return builder
    }

    @Deprecated("")
    private fun getNotificationBuilder(
        title: String?,
        result: Boolean
    ): NotificationCompat.Builder {
        if (result) {
            val builder = NotificationCompat.Builder(this, "download")
                .setContentTitle(title).setContentText("下载成功")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.download_notification)
                .setAutoCancel(true)
            return builder
        } else {
            val builder = NotificationCompat.Builder(this, "download")
                .setContentTitle(title).setContentText("下载失败")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.download_notification)
                .setAutoCancel(true)
            return builder
        }
    }

    private val notificationChannel: NotificationChannel
        get() {
            val channel =
                NotificationChannel("download", "下载", NotificationManager.IMPORTANCE_LOW)
            channel.setDescription("歌曲下载")
            return channel
        }

    private fun startDownAsyncTask(musicInfo: MusicInfo) {
        if (isExistAlready(MusicDownloadAsyncTask.Companion.SAVE_PATH + "/" + musicInfo.getId() + ".mp3") || isExistAlready(
                musicInfo.getMusicFilePath()
            )
        ) {
            //这里文件已经下载过，本地以存在。前台应该做处理，不应该执行到这里。如果歌曲已经下载了，就不需要点亮下载按钮执行重复下载
            return
        }
        val downloadMusicAsyncTask =
            MusicDownloadAsyncTask(MusicDownloaderService.DownloadListener(musicInfo))
        downloadMusicAsyncTask.execute(musicInfo)
        addToDownloadHistory(musicInfo)
        MusicIconLoadUtil.Companion.getMusicIcon(musicInfo.getId())
    }

    private fun isExistAlready(path: String): Boolean {
        val musicFile = File(path)
        return musicFile.exists()
    }

    private fun addToDownloadHistory(musicInfo: MusicInfo) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val contentValues = ContentValues()
                contentValues.put("id", musicInfo.getId())
                contentValues.put("name", musicInfo.getMusicSongName())
                contentValues.put("singer", musicInfo.getSinger())
                contentValues.put("is_download", 1)
                getContentResolver().insert(
                    MusicContentProvider.Companion.DOWNLOAD_URL,
                    contentValues
                )
            }
        })
    }

    private fun changeDownloadHistoryState(musicInfo: MusicInfo, isSuccess: Boolean) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val contentValues = ContentValues()
                contentValues.put("path", musicInfo.getMusicFilePath())
                if (isSuccess) {
                    contentValues.put("is_download", 0)
                    contentValues.put("success", 0)
                } else {
                    contentValues.put("is_download", 1)
                    contentValues.put("success", 1)
                }
                getContentResolver().update(
                    MusicContentProvider.Companion.DOWNLOAD_URL,
                    contentValues,
                    "id = ?",
                    arrayOf<String?>(musicInfo.getId())
                )
            }
        })
    }

    private inner class DownloadListener(musicInfo: MusicInfo) : MusicDownloadListener {
        private val musicInfo: MusicInfo

        init {
            this.musicInfo = musicInfo
        }

        override fun onProgress(progress: Int, name: String?) {
            builder!!.setContentTitle("正在下载:" + name)
            builder!!.setContentText("已下载:" + progress + "%")
            builder!!.setProgress(100, progress, false)
            notificationManager!!.notify(DOWNLOADNOTIFY, builder!!.build())
        }

        override fun isSuccess(result: Boolean) {
            //重新发送下载状态的通知
            notificationManager!!.cancelAll()
            val intent: Intent?
            if (result) {
                notificationManager!!.notify(
                    DOWNLOADNOTIFY,
                    getNotificationBuilder("下载完成", -1).build()
                )
                musicInfo.setMusicFilePath(MusicDownloadAsyncTask.Companion.SAVE_PATH + "/" + musicInfo.getId() + ".mp3")
                intent = Intent(MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS)
            } else {
                notificationManager!!.notify(
                    DOWNLOADNOTIFY,
                    getNotificationBuilder("下载失败:" + musicInfo.getMusicSongName(), -1).build()
                )
                intent = Intent(MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED)
            }
            MusicBroadcastManager.sendBroadcast(intent)
            changeDownloadHistoryState(musicInfo, result)
            removeFromDownloadList(musicInfo)
        }
    }

    private fun removeFromDownloadList(musicInfo: MusicInfo?) {
        mDownloadList!!.remove(musicInfo!!)
    }

    companion object {
        private const val DOWNLOADNOTIFY = 1
    }
}

