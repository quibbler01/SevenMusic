package com.quibbler.sevenmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.ActivityCollector;
import com.quibbler.sevenmusic.bean.MusicCoverJsonBean;
import com.quibbler.sevenmusic.bean.MusicDownloadUrlJsonBean;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.utils.CheckTools;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;
import static com.quibbler.sevenmusic.bean.MusicURL.API_GET_SONG_DETAIL_AND_IMAGE;
import static com.quibbler.sevenmusic.bean.MusicURL.API_MUSIC_DOWNLOAD_URL;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.AUDIO_BECOMING_NOISY;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_NO_COPYRIGHT;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PLAY_COMPLETION;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.FAVOURITE_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.PLAYED_URL;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.PLAYLIST_URL;
import static com.quibbler.sevenmusic.utils.CheckTools.isNotificationPermissionOpen;
import static com.quibbler.sevenmusic.utils.CheckTools.openNotificationPermissionSetting;
import static com.quibbler.sevenmusic.utils.CloseResourceUtil.closeInputAndOutput;

/**
 * Package:        com.quibbler.sevenmusic.service
 * ClassName:      MusicPlayerService
 * Description:    播放音乐的Service服务 2.1版 需要在AndroidManifest.xml中注册.通过绑定的方式启动Service，借助Binder对象，传入MusicInfo对象进行播放。两种播放方式，另一种是提供音乐id和路径path.
 * 播放网络检查，断网自动暂停，网络恢复自动恢复播放。逻辑。
 * 20191009 提高健壮性，断网不加载在线歌单，网络恢复自动初始化推荐歌单
 * 20191015 播放列表保存和加载，播放记录点；修改无版权播放的bug
 * 20191021 判断网络类型，根据设置是否在2G/3G/4G网络下进行播放
 * 20191022 修复id为空写入历史记录null空指针的bug，原因是播放逻辑错误，先更新id再播放写入历史记录
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 12:39
 */
public class MusicPlayerService extends Service {
    public static final String TAG = "MusicPlayerService";
    private SharedPreferences sharedPreferences = MusicApplication.getContext().getSharedPreferences(TAG, MODE_PRIVATE);

    @IntDef(value = {PlayModeType.PLAY_TYPE_RANDOM, PlayModeType.PLAY_TYPE_SINGLE_CYCLE, PlayModeType.PLAY_TYPE_LIST_CYCLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayModeType {
        int PLAY_TYPE_RANDOM = 0;
        int PLAY_TYPE_SINGLE_CYCLE = 1;
        int PLAY_TYPE_LIST_CYCLE = 2;
    }

    private MusicBinder mPlayMusicBinder = new MusicBinder();
    private static MediaPlayer mMusicPlayer = null;

    private static NotificationManager mNotificationManager = null;
    private static NotificationChannel mChannel = null;
    private static final String MUSIC_PLAY_NOTIFICATION_CHANNEL = "0";
    private static RemoteViews mRemoteViews;
    private static Bitmap mBitmap = null;

    public static final String MUSIC_PREVIOUS_ACTION = "quibbler.com.sevenmusic.nitofication.previous";
    public static final String MUSIC_NEXT_ACTION = "quibbler.com.sevenmusic.nitofication.next";
    public static final String MUSIC_PLAY_ACTION = "quibbler.com.sevenmusic.nitofication.play";
    public static final String MUSIC_FAVOURITE_ACTION = "quibbler.com.sevenmusic.nitofication.favourite";
    public static final String MUSIC_CLOSE_ACTION = "quibbler.com.sevenmusic.nitofication.close";

    public static final int MUSIC_PREVIOUS_CODE = 0;
    public static final int MUSIC_NEXT_CODE = 1;
    public static final int MUSIC_PLAY_CODE = 2;
    public static final int MUSIC_CLOSE_CODE = 3;
    public static final int MUSIC_FAVOURITE_CODE = 4;
    private MusicNotificationReceiver mReceiver = null;

    private StateReceiver mStateChangeListener;
    private boolean isFirst = true;

    private static AudioManager sAudioManager;
    private static MusicAudioFocusChangeListener sMusicAudioFocusChangeListener;
    private static boolean isInterrupted = false;
    private static AudioAttributes sAudioAttributes;
    private static AudioFocusRequest sAudioFocusRequest;

    private static final int HANDLER_CODE_DATABASE_DOWN = 0;
    private static final int HANDLER_CODE_IMAGE_GET = 1;
    private static final int HANDLER_CODE_URL_GOT = 2;
    private static final int HANDLER_CODE_IMAGE_FAILED = -1;
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case HANDLER_CODE_DATABASE_DOWN:
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                    break;
                case HANDLER_CODE_IMAGE_GET:
                    mRemoteViews.setImageViewBitmap(R.id.music_notification_icon, mBitmap);
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                    break;
                case HANDLER_CODE_URL_GOT:
                    initMusicIcon(sMusicInfo.getId());
                    MusicPlayerService.playMusic(sMusicInfo.getId(), sMusicInfo.getUrl());
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                    break;
                case HANDLER_CODE_IMAGE_FAILED:
                default:
                    break;
            }
        }
    };

    private static boolean sPrepared = false;  //播放器有无准备好
    public static boolean isPlaying = false;
    public static String sMusicID = null;
    public static MusicInfo sMusicInfo = null;
    private static List<MusicInfo> sPlayMusicLists = new ArrayList<>();
    private static int sPlayMode = PlayModeType.PLAY_TYPE_LIST_CYCLE;
    public static int sPosition = -1;
    private static Random sRandom = new Random(47);

    public MusicPlayerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        loadMusicPlayInfo();
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {                                       //异步加载,非阻塞主线程
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                sPrepared = true;
                mediaPlayer.start();
            }
        });
        mMusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;                                                                                       //必须置位
                switch (sPlayMode) {
                    case PlayModeType.PLAY_TYPE_LIST_CYCLE:
                        if (sPlayMusicLists.size() <= 1) {
                            MusicPlayerService.playMusic(sMusicInfo);
                        } else {
                            int position = sPlayMusicLists.indexOf(sMusicInfo);
                            if (position == -1) {
                                return;
                            } else if (position < sPlayMusicLists.size() - 1) {
                                sMusicInfo = sPlayMusicLists.get(position + 1);
                                MusicPlayerService.playMusic(sMusicInfo);
                            } else {
                                sMusicInfo = sPlayMusicLists.get(0);
                                MusicPlayerService.playMusic(sMusicInfo);
                            }
                        }
                        break;
                    case PlayModeType.PLAY_TYPE_SINGLE_CYCLE:                                                            //单曲循环
                        MusicPlayerService.playMusic(sMusicInfo);
                        break;
                    case PlayModeType.PLAY_TYPE_RANDOM:                                                                  //列表随机
                        if (sPlayMusicLists.size() == 0) {
                            return;
                        } else {
                            sMusicInfo = sPlayMusicLists.get(sRandom.nextInt(sPlayMusicLists.size()));
                            MusicPlayerService.playMusic(sMusicInfo);
                        }
                        break;
                    default:
                        Intent intent = new Intent(MUSIC_GLOBAL_PLAY_COMPLETION);
                        intent.putExtra("id", sMusicID);
                        MusicBroadcastManager.sendBroadcast(intent);
                        showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                        break;
                }
            }
        });
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(MUSIC_PLAY_NOTIFICATION_CHANNEL, "音乐播放通知栏", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        initAudioFocus();

        initNotificationFunction();

        initPlayListData();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_PREVIOUS_ACTION);
        intentFilter.addAction(MUSIC_NEXT_ACTION);
        intentFilter.addAction(MUSIC_PLAY_ACTION);
        intentFilter.addAction(MUSIC_CLOSE_ACTION);
        intentFilter.addAction(MUSIC_FAVOURITE_ACTION);
        mReceiver = new MusicNotificationReceiver();
        registerReceiver(mReceiver, intentFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AUDIO_BECOMING_NOISY);
        filter.addAction(SYSTEM_BROADCAST_NETWORK_CHANGE);
        mStateChangeListener = new StateReceiver();
        registerReceiver(mStateChangeListener, filter);
    }

    private void initAudioFocus() {
        sMusicAudioFocusChangeListener = new MusicAudioFocusChangeListener();
        sAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        sAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(sAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(sMusicAudioFocusChangeListener).build();

//        int focusRequest = sAudioManager.requestAudioFocus(sAudioFocusRequest);
//        switch (focusRequest) {
//            case AUDIOFOCUS_REQUEST_FAILED:
//                Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED" + AUDIOFOCUS_REQUEST_FAILED);
//                break;
//            case AUDIOFOCUS_REQUEST_GRANTED:
//                Log.e(TAG, "AUDIOFOCUS_REQUEST_GRANTED" + AUDIOFOCUS_REQUEST_GRANTED);
//                break;
//            default:
//                break;
//        }
    }

    private static void requestAudioFocus() {
        if (isInterrupted) {
        }
        int focusRequest = sAudioManager.requestAudioFocus(sAudioFocusRequest);
        switch (focusRequest) {
            case AUDIOFOCUS_REQUEST_FAILED:
                Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED" + AUDIOFOCUS_REQUEST_FAILED);
                break;
            case AUDIOFOCUS_REQUEST_GRANTED:
                Log.e(TAG, "AUDIOFOCUS_REQUEST_GRANTED" + AUDIOFOCUS_REQUEST_GRANTED);
                break;
            default:
                break;
        }
    }

    private void initNotificationFunction() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.music_play_notification_layout);

        Intent previousIntent = new Intent(MUSIC_PREVIOUS_ACTION);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(MusicApplication.getContext(), MUSIC_PREVIOUS_CODE, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mRemoteViews.setOnClickPendingIntent(R.id.music_notification_previous, previousPendingIntent);

        Intent nextIntent = new Intent(MUSIC_NEXT_ACTION);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(MusicApplication.getContext(), MUSIC_NEXT_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mRemoteViews.setOnClickPendingIntent(R.id.music_notification_next, nextPendingIntent);

        Intent playIntent = new Intent(MUSIC_PLAY_ACTION);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(MusicApplication.getContext(), MUSIC_PLAY_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mRemoteViews.setOnClickPendingIntent(R.id.music_notification_play, playPendingIntent);


        Intent closeIntent = new Intent(MUSIC_CLOSE_ACTION);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(MusicApplication.getContext(), MUSIC_CLOSE_CODE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mRemoteViews.setOnClickPendingIntent(R.id.music_notification_close, closePendingIntent);

        Intent favouriteIntent = new Intent(MUSIC_FAVOURITE_ACTION);
        PendingIntent favouritePendingIntent = PendingIntent.getBroadcast(MusicApplication.getContext(), MUSIC_FAVOURITE_CODE, favouriteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        mRemoteViews.setOnClickPendingIntent(R.id.music_notification_favourite, favouritePendingIntent);
    }

    @MainThread
    private void initPlayListData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(PLAYLIST_URL, null, null, null, null);
                    if (cursor != null) {
                        List<MusicInfo> temp = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            MusicInfo musicInfo = new MusicInfo();
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")));
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")));
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")));
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")));
                            temp.add(musicInfo);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                sPlayMusicLists.clear();
                                sPlayMusicLists.addAll(temp);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeInputAndOutput(cursor);
                }
            }
        });
    }

    public static void savePlayList() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                MusicApplication.getContext().getContentResolver().delete(PLAYLIST_URL, null, null);
                try {
                    for (MusicInfo musicInfo : sPlayMusicLists) {
                        ContentValues values = new ContentValues();
                        values.put("id", musicInfo.getId());
                        values.put("name", musicInfo.getMusicSongName());
                        values.put("singer", musicInfo.getSinger());
                        values.put("path", musicInfo.getMusicFilePath());
                        cursor = MusicApplication.getContext().getContentResolver().query(PLAYLIST_URL, null, "id = ?", new String[]{musicInfo.getId()}, null);
                        if (cursor != null) {
                            if (cursor.getCount() == 0) {
                                MusicApplication.getContext().getContentResolver().insert(PLAYLIST_URL, values);
                            }
                            cursor.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayMusicBinder;
    }

    public static class MusicBinder extends Binder {

        public void playMusic(MusicInfo musicInfo) {
            MusicPlayerService.playMusic(musicInfo);
        }

        public void playMusic(String musicID, String path) {
            MusicPlayerService.playMusic(musicID, path);
        }

        public int getDuration() {
            return MusicPlayerService.getDuration();
        }

        public int getPlayProgress() {
            if (mMusicPlayer != null) {
                return mMusicPlayer.getCurrentPosition();
            } else {
                return -1;
            }
        }

        public void setPlayProgress(int progress) {
            if (sMusicInfo != null && mMusicPlayer != null) {
                mMusicPlayer.seekTo(progress);
                if (isPlaying) {
                    MusicPlayerService.continuePlay();
                } else {
                    mMusicPlayer.pause();
                }
            }
        }

        public void stopPlayMusic() {
            MusicPlayerService.stopPlayMusic();
        }

        public void pauseMusic() {
            MusicPlayerService.pauseMusic();
        }

        public void pausePlayMusic() {
            mMusicPlayer.pause();
            isPlaying = false;
            showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveCurrentPlayMusicInfo(sMusicInfo);

        mNotificationManager.cancelAll();

        mMusicPlayer.release();
        mMusicPlayer = null;

        isPlaying = false;
        sMusicID = null;
        sMusicInfo = null;
        sPosition = -1;

        unregisterReceiver(mReceiver);
        mRemoteViews = null;
        unregisterReceiver(mStateChangeListener);
        mStateChangeListener = null;

        mHandler.removeCallbacksAndMessages(null);

        if (sAudioManager != null) {
            sAudioManager.abandonAudioFocus(sMusicAudioFocusChangeListener);
            sAudioManager = null;
            sMusicAudioFocusChangeListener = null;
        }
    }

    /**
     * Play Music Service Entry
     *
     * @param musicInfo
     */
    public static void playMusic(MusicInfo musicInfo) {
        if (musicInfo == null || mMusicPlayer == null) {
            return;
        }

        if (musicInfo.getMusicFilePath() != null && !"".equals(musicInfo.getMusicFilePath()) && isExistAlready(musicInfo.getMusicFilePath())) {
            sMusicInfo = musicInfo;
            initMusicIcon(sMusicInfo.getId());
            playMusic(musicInfo.getId(), musicInfo.getMusicFilePath());
            showPlayNotification(musicInfo.getId(), musicInfo.getMusicSongName(), musicInfo.getSinger());
            addToPlayList(sMusicInfo);
            addToPlayHistory(musicInfo);
        } else {
            if (!CheckTools.isNetWordAvailable(MusicApplication.getContext())) {
                Toast.makeText(MusicApplication.getContext(), MusicApplication.getContext().getString(R.string.service_play_toast_check_network), Toast.LENGTH_SHORT).show();
                return;
            }
            //非WiFi情况下播放
            if (CheckTools.getNetWorkStatus(MusicApplication.getContext()) != CheckTools.NETWORK_WIFI) {
                //从郭金良那里获取设置，默认播放打开移动网络开关
                Object setting = SharedPreferencesUtils.getInstance().getData(Constant.KEY_SETTING_NETWORK_PLAY, true);
                if (setting != null && !((boolean) setting)) {
                    Toast.makeText(MusicApplication.getContext(), MusicApplication.getContext().getString(R.string.service_play_toast_network_setting), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MusicDownloadUrlJsonBean.Data musicOnlineData = null;
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(API_MUSIC_DOWNLOAD_URL + musicInfo.getId() + "&br=128000").build();
                        Response response = client.newCall(request).execute();
                        String json = response.body().string();
                        musicOnlineData = (new Gson().fromJson(json, MusicDownloadUrlJsonBean.class)).getData().get(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (musicOnlineData == null || musicOnlineData.getUrl() == null || "".equals(musicOnlineData.getUrl())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MusicApplication.getContext(), "暂无版权", Toast.LENGTH_SHORT).show();
                                MusicBroadcastManager.sendBroadcast(MUSIC_GLOBAL_NO_COPYRIGHT);
                                playNextMusic();
                            }
                        });
                    } else {
                        sMusicInfo = musicInfo;
                        addToPlayList(sMusicInfo);
                        sMusicInfo.setUrl(musicOnlineData.getUrl());
                        Message message = new Message();
                        message.what = HANDLER_CODE_URL_GOT;
                        mHandler.sendMessage(message);
                    }
                }
            });
        }
    }

    /**
     * play a song:new one or current one:file path or url
     */
    public static void playMusic(String musicID, String path) {
        if (!isNotificationPermissionOpen(MusicApplication.getContext())) {
            openNotificationPermissionSetting(MusicApplication.getContext());
            Toast.makeText(MusicApplication.getContext(), "请打开播放通知权限", Toast.LENGTH_SHORT).show();
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                    Thread.sleep(2000);
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                    Thread.sleep(2000);
                    showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        requestAudioFocus();
        addToPlayHistory(sMusicInfo);
        try {
            if (isPlaying) {
                if (musicID.equals(sMusicID)) {
                    return;
                } else {
                    sPrepared = false;
                    mMusicPlayer.reset();
                    mMusicPlayer.setDataSource(path);
                    mMusicPlayer.prepareAsync();                                                    //改用异步加载,非阻塞主线程
                    sMusicID = musicID;
                    isPlaying = true;
                }
            } else {
                if (!musicID.equals("-1") && musicID.equals(sMusicID)) {
                    MusicPlayerService.continuePlay();
                    return;
                } else {
                    sPrepared = false;
                    mMusicPlayer.reset();
                    mMusicPlayer.setDataSource(path);
                    mMusicPlayer.prepareAsync();                                                    //异步加载,非阻塞主线程
                    sMusicID = musicID;
                    isPlaying = true;
                }
            }
            Log.d(TAG, sMusicInfo.getMusicSongName());
            MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * continue playing music
     */
    @Deprecated
    public static void continuePlay() {
        if (mMusicPlayer != null && !isPlaying) {
            mMusicPlayer.start();
            isPlaying = true;
            MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY);
        }
    }

    /**
     * pause current played music
     */
    public static void pauseMusic() {
        if (mMusicPlayer != null && isPlaying) {
            mMusicPlayer.pause();
            isPlaying = false;
            showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
            MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PAUSE);
        }
    }

    /**
     * stop play current music reset all status include bottom status bar
     */
    public static void stopPlayMusic() {
        if (mMusicPlayer == null || sMusicInfo == null) {
            return;
        }
        mMusicPlayer.stop();
        mMusicPlayer.release();
        mMusicPlayer = null;
        isPlaying = false;
        sPosition = -1;
        sMusicID = null;
        sMusicInfo = null;
        mNotificationManager.cancelAll();
    }

    /**
     * @param progress
     */
    public static void setPlayProgress(int progress) {
        if (sMusicInfo != null && mMusicPlayer != null) {
            mMusicPlayer.seekTo(progress);
            if (isPlaying) {
                MusicPlayerService.continuePlay();
            } else {
                mMusicPlayer.pause();
            }
        }
    }

    /**
     * set progress and play or not
     *
     * @param progress
     * @param play
     */
    public static void setPlayProgress(int progress, boolean play) {
        if (sMusicInfo != null && mMusicPlayer != null) {
            mMusicPlayer.seekTo(progress);
            if (play) {
                MusicPlayerService.continuePlay();
            } else {
                mMusicPlayer.pause();
            }
        }
    }

    /**
     * get play progress
     *
     * @return
     */
    public static int getPlayProgress() {
        if (mMusicPlayer != null && sPrepared) {
            return mMusicPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    /**
     * get song duration
     */
    public static int getDuration() {
        if (mMusicPlayer != null && sPrepared) {
            return mMusicPlayer.getDuration();
        } else {
            return -1;
        }
    }

    /**
     * @param mode
     */
    public static void setPlayMode(@PlayModeType int mode) {
        sPlayMode = mode;
    }

    /**
     * get play mode
     *
     * @return
     */
    public static int getPlayMode() {
        return sPlayMode;
    }

    /**
     * get getInstance of current music
     */
    public static MusicInfo getMusicInfo() {
        return sMusicInfo;
    }

    /**
     * add one single song to play music list
     *
     * @param musicInfo
     */
    public static void addToPlayerList(MusicInfo musicInfo) {
        sPlayMusicLists.add(musicInfo);
    }

    /**
     * add two or more music to play list
     *
     * @param musicInfos
     */
    public static void addToPlayerList(List<MusicInfo> musicInfos) {
        sPlayMusicLists.clear();
        sPlayMusicLists.addAll(musicInfos);
        savePlayList();
    }

    /**
     * @return get currently play music list
     */
    public static List<MusicInfo> getPlayMusicLists() {
        Log.d(TAG, "getPlayMusicLists   " + sPlayMusicLists.size());
        return sPlayMusicLists;
    }

    /**
     * remove from play list
     *
     * @param musicInfo
     */
    public static void removeFromPlayList(MusicInfo musicInfo) {
        if (musicInfo == null || musicInfo.getId() == null || "".equals(musicInfo.getId())) {
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo musicInfo1 : sPlayMusicLists) {
                    if (musicInfo.getId().equals(musicInfo1.getId())) {
                        sPlayMusicLists.remove(musicInfo1);
                        MusicApplication.getContext().getContentResolver().delete(PLAYLIST_URL, "id = ?", new String[]{musicInfo.getId()});
                        return;
                    }
                }
            }
        });
    }

    /**
     * clear current music play list
     */
    public static void clearPlayMusicList() {
        pauseMusic();
        sPlayMusicLists.clear();
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                MusicApplication.getContext().getContentResolver().delete(PLAYLIST_URL, null, null);
            }
        });
    }

    /**
     * play previous music
     */
    public static void playPreviousMusic() {
        --sPosition;
        if (sMusicInfo == null || mMusicPlayer == null) {
            return;
        }
        int position = sPlayMusicLists.indexOf(sMusicInfo);
        if (position == -1) {
            return;
        } else if (position == 0) {
            sMusicInfo = sPlayMusicLists.get(sPlayMusicLists.size() - 1);
            MusicPlayerService.playMusic(sMusicInfo);
        } else {
            sMusicInfo = sPlayMusicLists.get(position - 1);
            MusicPlayerService.playMusic(sMusicInfo);
        }

//        待验证
//        if (sPosition >= 0) {
//            sMusicInfo = sPlayMusicLists.get(sPosition);
//
//        } else {
//            sPosition = sPlayMusicLists.size() - 1;
//            sMusicInfo = sPlayMusicLists.get(sPosition);
//
//        }
    }

    /**
     * play next music
     */
    public static void playNextMusic() {
        if (sMusicInfo == null || mMusicPlayer == null) {
            return;
        }
        int position = sPlayMusicLists.indexOf(sMusicInfo);
        if (position == -1) {
            return;
        } else if (position == sPlayMusicLists.size() - 1) {
            sMusicInfo = sPlayMusicLists.get(0);
            MusicPlayerService.playMusic(sMusicInfo);
        } else {
            sMusicInfo = sPlayMusicLists.get(position + 1);
            MusicPlayerService.playMusic(sMusicInfo);
        }
    }

    @WorkerThread
    private static void addToPlayList(MusicInfo musicInfo) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo mi : sPlayMusicLists) {
                    if (mi.getId().equals(musicInfo.getId())) {
                        return;
                    }
                }
                sPlayMusicLists.add(musicInfo);
                //实时保存至数据库
                try {
                    ContentValues values = new ContentValues();
                    values.put("id", musicInfo.getId());
                    values.put("name", musicInfo.getMusicSongName());
                    values.put("singer", musicInfo.getSinger());
                    values.put("path", musicInfo.getMusicFilePath());
                    Cursor cursor = MusicApplication.getContext().getContentResolver().query(PLAYLIST_URL, null, "id = ?", new String[]{musicInfo.getId()}, null);
                    if (cursor != null) {
                        if (cursor.getCount() == 0) {
                            MusicApplication.getContext().getContentResolver().insert(PLAYLIST_URL, values);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @WorkerThread
    public static void addToPlayHistory(MusicInfo musicInfo) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                ContentValues playRecord = new ContentValues();
                playRecord.put("id", musicInfo.getId());
                playRecord.put("name", musicInfo.getMusicSongName());
                playRecord.put("path", musicInfo.getMusicFilePath());
                playRecord.put("last_played", System.currentTimeMillis());
                playRecord.put("singer", musicInfo.getSinger());
                playRecord.put("singer", musicInfo.getSinger());
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(PLAYED_URL, null, "id = ?", new String[]{musicInfo.getId()}, null);
                    if (cursor != null) {
                        if (cursor.getCount() != 0) {
                            MusicApplication.getContext().getContentResolver().update(PLAYED_URL, playRecord, "id = ?", new String[]{musicInfo.getId()});
                        } else {
                            MusicApplication.getContext().getContentResolver().insert(PLAYED_URL, playRecord);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor);
                }
            }
        });
    }

    private static void showPlayNotification(String id, String name, String singer) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Notification.Builder builder = new Notification.Builder(MusicApplication.getContext(), MUSIC_PLAY_NOTIFICATION_CHANNEL);
                mRemoteViews.setTextViewText(R.id.music_notification_song_name, name);
                mRemoteViews.setTextViewText(R.id.music_notification_singer, singer);
                if (isPlaying) {
                    mRemoteViews.setImageViewResource(R.id.music_notification_play, R.drawable.music_notification_pause);
                } else {
                    mRemoteViews.setImageViewResource(R.id.music_notification_play, R.drawable.music_notification_play);
                }
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(FAVOURITE_URL, null, "id = ?", new String[]{sMusicInfo.getId()}, null);
                    if (cursor != null && cursor.getCount() != 0) {
                        mRemoteViews.setImageViewResource(R.id.music_notification_favourite, R.drawable.music_notification_un_favourite);
                    } else {
                        mRemoteViews.setImageViewResource(R.id.music_notification_favourite, R.drawable.music_notification_favourite);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                builder.setCustomContentView(mRemoteViews).setSmallIcon(R.drawable.music_notification_small_icon).setContentTitle("正在播放:" + name).setAutoCancel(false);
                Notification notification = builder.build();
                if (isPlaying) {
                    notification.flags |= Notification.FLAG_NO_CLEAR;
                }
                mNotificationManager.notify(1, notification);
            }
        });
    }

    public class MusicNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case MUSIC_PREVIOUS_ACTION:
                    MusicPlayerService.playPreviousMusic();
                    break;
                case MUSIC_NEXT_ACTION:
                    MusicPlayerService.playNextMusic();
                    break;
                case MUSIC_PLAY_ACTION:
                    if (isPlaying) {
                        MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PAUSE);
                        mMusicPlayer.pause();
                    } else {
                        MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY);
                        mMusicPlayer.start();
                    }
                    isPlaying = !isPlaying;
                    break;
                case MUSIC_CLOSE_ACTION:
                    mNotificationManager.cancelAll();
                    ActivityCollector.finishAllActivity();
                    break;
                case MUSIC_FAVOURITE_ACTION:
                    if (sMusicInfo == null) {
                        return;
                    } else {
                        MusicThreadPool.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Cursor cursor = null;
                                try {
                                    cursor = MusicApplication.getContext().getContentResolver().query(FAVOURITE_URL, null, "id = ?", new String[]{sMusicInfo.getId()}, null);
                                    if (cursor != null && cursor.getCount() == 0) {
                                        ContentValues values = new ContentValues();
                                        values.put("id", sMusicInfo.getId());
                                        values.put("name", sMusicInfo.getMusicSongName());
                                        values.put("singer", sMusicInfo.getSinger());
                                        values.put("path", sMusicInfo.getMusicFilePath());
                                        MusicApplication.getContext().getContentResolver().insert(FAVOURITE_URL, values);
                                    } else {
                                        MusicApplication.getContext().getContentResolver().delete(FAVOURITE_URL, "id = ?", new String[]{sMusicInfo.getId()});
                                    }
                                    MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE);
                                    Message message = new Message();
                                    message.what = HANDLER_CODE_DATABASE_DOWN;
                                    mHandler.sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                }
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
            showPlayNotification(sMusicInfo.getId(), sMusicInfo.getMusicSongName(), sMusicInfo.getSinger());
        }
    }

    private static void initMusicIcon(String musicID) {
        if (musicID == null || "".equals(musicID)) {
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                File file = new File(SEVEN_MUSIC_IMAGE + "/" + sMusicInfo.getId());
                if (file.exists()) {
                    mBitmap = BitmapFactory.decodeFile(SEVEN_MUSIC_IMAGE + "/" + sMusicInfo.getId());
                    Message message = new Message();
                    message.what = HANDLER_CODE_IMAGE_GET;
                    mHandler.sendMessage(message);
                    return;
                }

                //先解析歌曲详细详细
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                try {
                    URL musicDetailUrl = new URL(API_GET_SONG_DETAIL_AND_IMAGE + musicID);
                    connection = (HttpURLConnection) musicDetailUrl.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestMethod("GET");
                    if (connection.getResponseCode() != 200) {
                        return;
                    }
                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                    //拿到歌曲封面地址，去获取歌曲封面
                    String jsonData = builder.toString();
                    MusicCoverJsonBean musicCoverJsonBean = new Gson().fromJson(jsonData, MusicCoverJsonBean.class);
                    if (musicCoverJsonBean.getSongs().size() == 0) {
                        return;
                    }
                    String imageUrl = musicCoverJsonBean.getSongs().get(0).getAl().getPicUrl();
                    if ("".equals(imageUrl) || imageUrl == null) {
                        return;
                    }
                    connection.disconnect();
                    inputStream.close();
                    URL musicImageURL = new URL(imageUrl);
                    connection = (HttpURLConnection) musicImageURL.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestMethod("GET");
                    if (connection.getResponseCode() != 200) {
                        return;
                    }
                    inputStream = connection.getInputStream();
                    mBitmap = BitmapFactory.decodeStream(inputStream);
                    Message message = new Message();
                    message.what = HANDLER_CODE_IMAGE_GET;
                    mHandler.sendMessage(message);
                    MusicIconLoadUtil.saveBitmapToCache(mBitmap, SEVEN_MUSIC_IMAGE, sMusicInfo.getId());

                } catch (Exception e) {
                    Message message = new Message();
                    message.what = -1;
                    mHandler.sendMessage(message);
                    e.printStackTrace();
                } finally {
                    try {
                        CloseResourceUtil.closeInputAndOutput(inputStream);
                        CloseResourceUtil.disconnect(connection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private class StateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case SYSTEM_BROADCAST_NETWORK_CHANGE:
                    Log.d(TAG, "StateReceiver  " + intent.getAction());
                    if (isFirst) {
                        isFirst = false;
                        return;
                    } else {
                        if ((sMusicInfo != null && "".equals(sMusicInfo.getMusicFilePath())) || (sMusicInfo != null && sMusicInfo.getMusicFilePath() == null)) {
                            if (!CheckTools.isNetWordAvailable(getApplicationContext())) {
                                if (mMusicPlayer != null && isPlaying) {
                                    sendBroadcast(new Intent(MUSIC_PLAY_ACTION));
                                }
                            } else {
                                if (mMusicPlayer != null && sMusicInfo != null && !isPlaying) {
                                    sendBroadcast(new Intent(MUSIC_PLAY_ACTION));
                                }
                            }
                        }
                    }
                    break;
                case AUDIO_BECOMING_NOISY:
                    isInterrupted = true;
                    pauseMusic();
                    break;
                default:
                    break;
            }

        }
    }

    private void loadMusicPlayInfo() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                String string = sharedPreferences.getString(TAG, "");
                if ("".equals(string) || string == null) {
                    return;
                }
                Gson gson = new Gson();
                sMusicInfo = gson.fromJson(string, MusicInfo.class);
            }
        });
    }

    private void saveCurrentPlayMusicInfo(MusicInfo musicInfo) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                String string = gson.toJson(musicInfo);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TAG, string);
                editor.apply();
            }
        });
    }

    private static boolean isExistAlready(String path) {
        File musicFile = new File(path);
        return musicFile.exists();
    }

    private static class MusicAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.e(TAG, "focusChange  " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.e(TAG, "你已经完全获得了音频焦点  ");
                    //获得了 Audio Focus.
                    if (isInterrupted) {
                        isInterrupted = false;
                        playMusic(sMusicInfo);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.e(TAG, "你会长时间的失去焦点，所以不要指望在短时间内能获得  ");
                    sAudioManager.abandonAudioFocusRequest(sAudioFocusRequest);
                    isInterrupted = true;
                    pauseMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.e(TAG, "暂时失去 Audio Focus，但是很快就会重新获得。");
                    //暂时失去 Audio Focus，但是很快就会重新获得。应该停止所有的音频播放，但是可以不清里资源，因为可能很快就会再次获取 Audio Focus.
                    isInterrupted = true;
                    pauseMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.e(TAG, "暂时失去 Audio Focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。  ");
                    //暂时失去 Audio Focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                    break;
                default:
                    break;
            }
        }
    }
}
