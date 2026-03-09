package com.quibbler.sevenmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.quibbler.sevenmusic.MainActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.listener.MvDownloadListener
import java.io.File

/**
 * 
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MvDownloadService
 * Description:    mv下载service
 * Author:         lishijun
 * CreateDate:     2019/9/24 11:33
 */
class MvDownloadService : Service() {
    private var mMvDownloadTask: MvDownloadAsyncTask? = null

    private var mDownloadUrl: String? = null

    private var mMvInfo: MvInfo? = null

    private val mDownLoadBinder = DownLoadBinder()

    private val mMvDownloadListener: MvDownloadListener = object : MvDownloadListener {
        override fun onProgress(progress: Int) {
            this.notificationManager.notify(
                1,
                getNotification("正在下载：" + mMvInfo!!.getName(), progress)
            )
        }

        override fun onSuccess() {
            mMvDownloadTask = null
            stopForeground(true)
            this.notificationManager.notify(
                1,
                getNotification("下载完成：" + mMvInfo!!.getName(), -1)
            )
            Toast.makeText(this@MvDownloadService, "下载完成！", Toast.LENGTH_SHORT).show()
        }

        override fun onFailed() {
            mMvDownloadTask = null
            stopForeground(true)
            this.notificationManager.notify(
                1,
                getNotification("下载失败：" + mMvInfo!!.getName(), -1)
            )
            Toast.makeText(this@MvDownloadService, "下载失败！", Toast.LENGTH_SHORT).show()
        }

        override fun onPaused() {
            mMvDownloadTask = null
            Toast.makeText(
                this@MvDownloadService,
                "下载暂停：" + mMvInfo!!.getName(),
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCanceled() {
            mMvDownloadTask = null
            stopForeground(true)
            Toast.makeText(
                this@MvDownloadService,
                "下载取消：" + mMvInfo!!.getName(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 自定义Binder类
     */
    inner class DownLoadBinder : Binder() {
        /**
         * 开始下载
         * 
         * @param mvInfo
         */
        fun startDownLoad(mvInfo: MvInfo) {
            mMvInfo = mvInfo
            if (mMvDownloadTask == null) {
                mDownloadUrl = mvInfo.getUrl()
                mMvDownloadTask = MvDownloadAsyncTask(mMvDownloadListener)
                mMvDownloadTask!!.execute(mDownloadUrl, mvInfo.getName())
                startForeground(1, getNotification("开始下载", 0))
            }
        }

        /**
         * 暂停下载
         */
        fun pauseDownLoad() {
            if (mMvDownloadTask != null) {
                mMvDownloadTask!!.pauseDownLoad()
            }
        }

        /**
         * 取消下载
         */
        fun cancelDownLoad() {
            if (mMvDownloadTask != null) {
                mMvDownloadTask!!.cancelDownLoad()
            }
            //文件删除
            if (mDownloadUrl != null) {
                val fileName = mMvInfo!!.getName() + ".mp4"
                val directory: String = MvDownloadAsyncTask.Companion.SAVE_PATH
                val file = File(directory, fileName)
                if (file.exists()) {
                    file.delete()
                }
                this.notificationManager.cancel(1)
                stopForeground(true)
                Toast.makeText(this@MvDownloadService, "下载取消！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mDownLoadBinder
    }

    /**
     * 显示通知
     * 
     * @param title
     * @param progress
     * @return
     */
    private fun getNotification(title: String?, progress: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            val id = "my_channel_01"
            val name = "channelName"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            this.notificationManager!!.createNotificationChannel(mChannel)
            //设置图片,通知标题,发送时间,提示方式等属性
            val builder = NotificationCompat.Builder(this, id)
            builder.setContentTitle(title) //标题
                .setWhen(System.currentTimeMillis()) //系统显示时间
                .setSmallIcon(R.mipmap.ic_launcher) //收到信息后状态栏显示的小图标
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.ic_launcher
                    )
                ) //大图标
                .setAutoCancel(true) //设置点击后取消Notification
            builder.setContentIntent(pendingIntent) //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress.toString() + "%")
                builder.setProgress(100, progress, false)
            }
            return builder.build()
        } else {
            //设置图片,通知标题,发送时间,提示方式等属性
            val builder = Notification.Builder(this)
            builder.setContentTitle(title) //标题
                .setWhen(System.currentTimeMillis()) //系统显示时间
                .setSmallIcon(R.mipmap.ic_launcher) //收到信息后状态栏显示的小图标
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.ic_launcher
                    )
                ) //大图标
                .setAutoCancel(true) //设置点击后取消Notification
            builder.setContentIntent(pendingIntent) //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress.toString() + "%")
                builder.setProgress(100, progress, false)
            }
            return builder.build()
        }
    }

    private val notificationManager: NotificationManager?
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
}

