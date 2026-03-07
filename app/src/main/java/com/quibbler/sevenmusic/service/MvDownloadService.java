package com.quibbler.sevenmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.quibbler.sevenmusic.MainActivity;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.listener.MvDownloadListener;

import java.io.File;

/**
 *
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MvDownloadService
 * Description:    mv下载service
 * Author:         lishijun
 * CreateDate:     2019/9/24 11:33
 */
public class MvDownloadService extends Service {

    private MvDownloadAsyncTask mMvDownloadTask;

    private String mDownloadUrl;

    private MvInfo mMvInfo;

    private DownLoadBinder mDownLoadBinder = new DownLoadBinder();

    private MvDownloadListener mMvDownloadListener = new MvDownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("正在下载：" + mMvInfo.getName(), progress));
        }

        @Override
        public void onSuccess() {
            mMvDownloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载完成：" + mMvInfo.getName(), -1));
            Toast.makeText(MvDownloadService.this, "下载完成！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mMvDownloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败：" + mMvInfo.getName(), -1));
            Toast.makeText(MvDownloadService.this, "下载失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            mMvDownloadTask = null;
            Toast.makeText(MvDownloadService.this, "下载暂停：" + mMvInfo.getName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            mMvDownloadTask = null;
            stopForeground(true);
            Toast.makeText(MvDownloadService.this, "下载取消：" + mMvInfo.getName(), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 自定义Binder类
     */
    public class DownLoadBinder extends Binder {

        /**
         * 开始下载
         *
         * @param mvInfo
         */
        public void startDownLoad(MvInfo mvInfo) {
            mMvInfo = mvInfo;
            if (mMvDownloadTask == null) {
                mDownloadUrl = mvInfo.getUrl();
                mMvDownloadTask = new MvDownloadAsyncTask(mMvDownloadListener);
                mMvDownloadTask.execute(mDownloadUrl, mvInfo.getName());
                startForeground(1, getNotification("开始下载", 0));
            }
        }

        /**
         * 暂停下载
         */
        public void pauseDownLoad() {
            if (mMvDownloadTask != null) {
                mMvDownloadTask.pauseDownLoad();
            }
        }

        /**
         * 取消下载
         */
        public void cancelDownLoad() {
            if (mMvDownloadTask != null) {
                mMvDownloadTask.cancelDownLoad();
            }
            //文件删除
            if (mDownloadUrl != null) {
                String fileName = mMvInfo.getName() + ".mp4";
                String directory = MvDownloadAsyncTask.SAVE_PATH;
                File file = new File(directory, fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(MvDownloadService.this, "下载取消！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mDownLoadBinder;
    }

    /**
     * 显示通知
     *
     * @param title
     * @param progress
     * @return
     */
    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String id = "my_channel_01";
            String name="channelName";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(mChannel);
            //设置图片,通知标题,发送时间,提示方式等属性
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.mipmap.ic_launcher)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        } else {
            //设置图片,通知标题,发送时间,提示方式等属性
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.mipmap.ic_launcher)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
}

