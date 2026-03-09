package com.quibbler.sevenmusic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.listener.MusicDownloadListener;
import com.quibbler.sevenmusic.utils.CheckTools;
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.DOWNLOAD_URL;
import static com.quibbler.sevenmusic.service.MusicDownloadAsyncTask.SAVE_PATH;

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MusicDownloaderService
 * Description:    下载音乐的Service服务的基本框架，使用AsyncTask任务执行 下载状态通知，下载完成更新前台 广播
 * 20191021 下载网络状态检测，通过设置来判断是否使用移动网络进行下载
 * Author:         zhaopeng
 * CreateDate:     2019/10/11 21:39
 */
public class MusicDownloaderService extends Service {
    private static final int DOWNLOADNOTIFY = 1;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private List<MusicInfo> mDownloadList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(getNotificationChannel());
        builder = new NotificationCompat.Builder(this, "download")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.download_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download_notification))
                .setProgress(100, 0, false)
                .setAutoCancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY_COMPATIBILITY;
        }
        if (!CheckTools.hasPermission(WRITE_EXTERNAL_STORAGE, MusicApplication.getContext())) {
            return super.onStartCommand(intent, flags, startId);
        }
        //非WiFi情况下下载
        if (CheckTools.getNetWorkStatus(MusicApplication.getContext()) != CheckTools.NETWORK_WIFI) {
            //从郭金良那里获取设置，是否使用移动网络下载
            Object setting = SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_NETWORK_DOWNLOAD, false);
            if (setting != null && !((boolean) setting)) {
                Toast.makeText(MusicApplication.getContext(), MusicApplication.getContext().getString(R.string.service_download_toast_network_setting), Toast.LENGTH_SHORT).show();
                return START_STICKY_COMPATIBILITY;
            }
        }
        List<MusicInfo> musicLists = (List<MusicInfo>) intent.getSerializableExtra("musics");
        if (musicLists != null && musicLists.size() != 0) {
            mDownloadList = musicLists;
            for (MusicInfo musicInfo : musicLists) {
                startDownAsyncTask(musicInfo);
            }
        } else {
            MusicInfo musicInfo = (MusicInfo) intent.getParcelableExtra("music");
            if (musicInfo != null && !"".equals(musicInfo.getId())) {
                mDownloadList.add(musicInfo);
                startDownAsyncTask(musicInfo);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationCompat.Builder getNotificationBuilder(String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "download")
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.download_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download_notification))
                .setAutoCancel(true);
        if (progress > 0) {
            builder.setContentText("已下载:" + progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder;
    }

    @Deprecated
    private NotificationCompat.Builder getNotificationBuilder(String title, boolean result) {
        if (result) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "download")
                    .setContentTitle(title).setContentText("下载成功").setPriority(NotificationCompat.PRIORITY_LOW).setSmallIcon(R.drawable.download_notification)
                    .setAutoCancel(true);
            return builder;
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "download")
                    .setContentTitle(title).setContentText("下载失败").setPriority(NotificationCompat.PRIORITY_LOW).setSmallIcon(R.drawable.download_notification)
                    .setAutoCancel(true);
            return builder;
        }
    }

    private NotificationChannel getNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("download", "下载", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("歌曲下载");
        return channel;
    }

    private void startDownAsyncTask(MusicInfo musicInfo) {
        if (isExistAlready(SAVE_PATH + "/" + musicInfo.getId() + ".mp3") || isExistAlready(musicInfo.getMusicFilePath())) {
            //这里文件已经下载过，本地以存在。前台应该做处理，不应该执行到这里。如果歌曲已经下载了，就不需要点亮下载按钮执行重复下载
            return;
        }
        MusicDownloadAsyncTask downloadMusicAsyncTask = new MusicDownloadAsyncTask(new DownloadListener(musicInfo));
        downloadMusicAsyncTask.execute(musicInfo);
        addToDownloadHistory(musicInfo);
        MusicIconLoadUtil.getMusicIcon(musicInfo.getId());
    }

    private boolean isExistAlready(String path) {
        File musicFile = new File(path);
        return musicFile.exists();
    }

    private void addToDownloadHistory(MusicInfo musicInfo) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", musicInfo.getId());
                contentValues.put("name", musicInfo.getMusicSongName());
                contentValues.put("singer", musicInfo.getSinger());
                contentValues.put("is_download", 1);
                getContentResolver().insert(DOWNLOAD_URL, contentValues);
            }
        });
    }

    private void changeDownloadHistoryState(MusicInfo musicInfo, boolean isSuccess) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                ContentValues contentValues = new ContentValues();
                contentValues.put("path", musicInfo.getMusicFilePath());
                if (isSuccess) {
                    contentValues.put("is_download", 0);
                    contentValues.put("success", 0);
                } else {
                    contentValues.put("is_download", 1);
                    contentValues.put("success", 1);
                }
                getContentResolver().update(DOWNLOAD_URL, contentValues, "id = ?", new String[]{musicInfo.getId()});
            }
        });
    }

    private class DownloadListener implements MusicDownloadListener {
        private MusicInfo musicInfo;

        public DownloadListener(MusicInfo musicInfo) {
            this.musicInfo = musicInfo;
        }

        @Override
        public void onProgress(int progress, String name) {
            builder.setContentTitle("正在下载:" + name);
            builder.setContentText("已下载:" + progress + "%");
            builder.setProgress(100, progress, false);
            notificationManager.notify(DOWNLOADNOTIFY, builder.build());
        }

        @Override
        public void isSuccess(boolean result) {
            //重新发送下载状态的通知
            notificationManager.cancelAll();
            Intent intent;
            if (result) {
                notificationManager.notify(DOWNLOADNOTIFY, getNotificationBuilder("下载完成", -1).build());
                musicInfo.setMusicFilePath(SAVE_PATH + "/" + musicInfo.getId() + ".mp3");
                intent = new Intent(MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS);
            } else {
                notificationManager.notify(DOWNLOADNOTIFY, getNotificationBuilder("下载失败:" + musicInfo.getMusicSongName(), -1).build());
                intent = new Intent(MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED);
            }
            MusicBroadcastManager.sendBroadcast(intent);
            changeDownloadHistoryState(musicInfo, result);
            removeFromDownloadList(musicInfo);
        }
    }

    private void removeFromDownloadList(MusicInfo musicInfo) {
        mDownloadList.remove(musicInfo);
    }
}

