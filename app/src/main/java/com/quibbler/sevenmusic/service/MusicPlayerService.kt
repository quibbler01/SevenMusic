package com.quibbler.sevenmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.ActivityCollector
import com.quibbler.sevenmusic.bean.MusicCoverJsonBean
import com.quibbler.sevenmusic.bean.MusicDownloadUrlJsonBean
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.CheckTools
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicIconLoadUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

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
class MusicPlayerService : Service() {
    private val sharedPreferences: SharedPreferences =
        MusicApplication.context.getSharedPreferences(
            TAG, MODE_PRIVATE
        )

    @IntDef(value = [PlayModeType.Companion.PLAY_TYPE_RANDOM, PlayModeType.Companion.PLAY_TYPE_SINGLE_CYCLE, PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE])
    @Retention(AnnotationRetention.SOURCE)
    annotation class PlayModeType {
        companion object {
            const val PLAY_TYPE_RANDOM: Int = 0
            const val PLAY_TYPE_SINGLE_CYCLE: Int = 1
            const val PLAY_TYPE_LIST_CYCLE: Int = 2
        }
    }

    private val mPlayMusicBinder = MusicBinder()
    private var mReceiver: MusicNotificationReceiver? = null

    private var mStateChangeListener: StateReceiver? = null
    private var isFirst = true

    override fun onCreate() {
        super.onCreate()

        loadMusicPlayInfo()
        mMusicPlayer = MediaPlayer()
        mMusicPlayer!!.setOnPreparedListener(object : OnPreparedListener {
            //异步加载,非阻塞主线程
            override fun onPrepared(mediaPlayer: MediaPlayer) {
                sPrepared = true
                mediaPlayer.start()
            }
        })
        mMusicPlayer!!.setOnCompletionListener(object : OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                isPlaying = false //必须置位
                when (playMode) {
                    PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE -> if (sPlayMusicLists.size <= 1) {
                        playMusic(musicInfo)
                    } else {
                        val position: Int = sPlayMusicLists.indexOf(musicInfo)
                        if (position == -1) {
                            return
                        } else if (position < sPlayMusicLists.size - 1) {
                            musicInfo = sPlayMusicLists.get(position + 1)
                            playMusic(musicInfo)
                        } else {
                            musicInfo = sPlayMusicLists.get(0)
                            playMusic(musicInfo)
                        }
                    }

                    PlayModeType.Companion.PLAY_TYPE_SINGLE_CYCLE -> playMusic(musicInfo)
                    PlayModeType.Companion.PLAY_TYPE_RANDOM -> if (sPlayMusicLists.size == 0) {
                        return
                    } else {
                        musicInfo = sPlayMusicLists.get(sRandom.nextInt(sPlayMusicLists.size))
                        playMusic(musicInfo)
                    }

                    else -> {
                        val intent = Intent(MusicBroadcastManager.MUSIC_GLOBAL_PLAY_COMPLETION)
                        intent.putExtra("id", sMusicID)
                        MusicBroadcastManager.sendBroadcast(intent)
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                    }
                }
            }
        })
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(
                MUSIC_PLAY_NOTIFICATION_CHANNEL,
                "音乐播放通知栏",
                NotificationManager.IMPORTANCE_LOW
            )
            mNotificationManager!!.createNotificationChannel(mChannel!!)
        }

        initAudioFocus()

        initNotificationFunction()

        initPlayListData()

        val intentFilter = IntentFilter()
        intentFilter.addAction(MUSIC_PREVIOUS_ACTION)
        intentFilter.addAction(MUSIC_NEXT_ACTION)
        intentFilter.addAction(MUSIC_PLAY_ACTION)
        intentFilter.addAction(MUSIC_CLOSE_ACTION)
        intentFilter.addAction(MUSIC_FAVOURITE_ACTION)
        mReceiver = MusicNotificationReceiver()
        registerReceiver(mReceiver, intentFilter)

        val filter = IntentFilter()
        filter.addAction(MusicBroadcastManager.AUDIO_BECOMING_NOISY)
        filter.addAction(MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE)
        mStateChangeListener = StateReceiver()
        registerReceiver(mStateChangeListener, filter)
    }

    private fun initAudioFocus() {
        sMusicAudioFocusChangeListener = MusicAudioFocusChangeListener()
        sAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
        sAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        sAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(sAudioAttributes!!)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(sMusicAudioFocusChangeListener!!).build()

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

    private fun initNotificationFunction() {
        mRemoteViews = RemoteViews(getPackageName(), R.layout.music_play_notification_layout)

        val previousIntent: Intent = Intent(MUSIC_PREVIOUS_ACTION)
        val previousPendingIntent = PendingIntent.getBroadcast(
            MusicApplication.Companion.getContext(),
            MUSIC_PREVIOUS_CODE,
            previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        mRemoteViews!!.setOnClickPendingIntent(
            R.id.music_notification_previous,
            previousPendingIntent
        )

        val nextIntent: Intent = Intent(MUSIC_NEXT_ACTION)
        val nextPendingIntent = PendingIntent.getBroadcast(
            MusicApplication.Companion.getContext(),
            MUSIC_NEXT_CODE,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        mRemoteViews!!.setOnClickPendingIntent(R.id.music_notification_next, nextPendingIntent)

        val playIntent: Intent = Intent(MUSIC_PLAY_ACTION)
        val playPendingIntent = PendingIntent.getBroadcast(
            MusicApplication.Companion.getContext(),
            MUSIC_PLAY_CODE,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        mRemoteViews!!.setOnClickPendingIntent(R.id.music_notification_play, playPendingIntent)


        val closeIntent: Intent = Intent(MUSIC_CLOSE_ACTION)
        val closePendingIntent = PendingIntent.getBroadcast(
            MusicApplication.Companion.getContext(),
            MUSIC_CLOSE_CODE,
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        mRemoteViews!!.setOnClickPendingIntent(R.id.music_notification_close, closePendingIntent)

        val favouriteIntent: Intent = Intent(MUSIC_FAVOURITE_ACTION)
        val favouritePendingIntent = PendingIntent.getBroadcast(
            MusicApplication.Companion.getContext(),
            MUSIC_FAVOURITE_CODE,
            favouriteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        mRemoteViews!!.setOnClickPendingIntent(
            R.id.music_notification_favourite,
            favouritePendingIntent
        )
    }

    @MainThread
    private fun initPlayListData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    cursor = getContentResolver().query(
                        MusicContentProvider.Companion.PLAYLIST_URL,
                        null,
                        null,
                        null,
                        null
                    )
                    if (cursor != null) {
                        val temp: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                        while (cursor.moveToNext()) {
                            val musicInfo = MusicInfo()
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")))
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")))
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")))
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")))
                            temp.add(musicInfo)
                        }
                        mHandler.post(object : Runnable {
                            override fun run() {
                                sPlayMusicLists.clear()
                                sPlayMusicLists.addAll(temp)
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor)
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mPlayMusicBinder
    }

    class MusicBinder : Binder() {
        fun playMusic(musicInfo: MusicInfo?) {
            Companion.playMusic(musicInfo)
        }

        fun playMusic(musicID: String, path: String?) {
            Companion.playMusic(musicID, path)
        }

        val duration: Int
            get() = Companion.duration

        var playProgress: Int
            get() {
                if (mMusicPlayer != null) {
                    return mMusicPlayer!!.getCurrentPosition()
                } else {
                    return -1
                }
            }
            set(progress) {
                if (musicInfo != null && mMusicPlayer != null) {
                    mMusicPlayer!!.seekTo(progress)
                    if (isPlaying) {
                        continuePlay()
                    } else {
                        mMusicPlayer!!.pause()
                    }
                }
            }

        fun stopPlayMusic() {
            Companion.stopPlayMusic()
        }

        fun pauseMusic() {
            Companion.pauseMusic()
        }

        fun pausePlayMusic() {
            mMusicPlayer!!.pause()
            isPlaying = false
            showPlayNotification(
                musicInfo!!.getId(),
                musicInfo!!.getMusicSongName(),
                musicInfo!!.getSinger()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveCurrentPlayMusicInfo(musicInfo)

        mNotificationManager!!.cancelAll()

        mMusicPlayer!!.release()
        mMusicPlayer = null

        isPlaying = false
        sMusicID = null
        musicInfo = null
        sPosition = -1

        unregisterReceiver(mReceiver)
        mRemoteViews = null
        unregisterReceiver(mStateChangeListener)
        mStateChangeListener = null

        mHandler.removeCallbacksAndMessages(null)

        if (sAudioManager != null) {
            sAudioManager!!.abandonAudioFocus(sMusicAudioFocusChangeListener)
            sAudioManager = null
            sMusicAudioFocusChangeListener = null
        }
    }

    inner class MusicNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.getAction()
            if (action == null) {
                return
            }
            when (action) {
                MUSIC_PREVIOUS_ACTION -> playPreviousMusic()
                MUSIC_NEXT_ACTION -> playNextMusic()
                MUSIC_PLAY_ACTION -> {
                    if (isPlaying) {
                        MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PAUSE)
                        mMusicPlayer!!.pause()
                    } else {
                        MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY)
                        mMusicPlayer!!.start()
                    }
                    isPlaying = !isPlaying
                }

                MUSIC_CLOSE_ACTION -> {
                    mNotificationManager!!.cancelAll()
                    ActivityCollector.finishAllActivity()
                }

                MUSIC_FAVOURITE_ACTION -> if (musicInfo == null) {
                    return
                } else {
                    MusicThreadPool.postRunnable(object : Runnable {
                        override fun run() {
                            var cursor: Cursor? = null
                            try {
                                cursor =
                                    MusicApplication.Companion.getContext().getContentResolver()
                                        .query(
                                            MusicContentProvider.Companion.FAVOURITE_URL,
                                            null,
                                            "id = ?",
                                            arrayOf<String?>(
                                                musicInfo!!.getId()
                                            ),
                                            null
                                        )
                                if (cursor != null && cursor.getCount() == 0) {
                                    val values = ContentValues()
                                    values.put("id", musicInfo!!.getId())
                                    values.put("name", musicInfo!!.getMusicSongName())
                                    values.put("singer", musicInfo!!.getSinger())
                                    values.put("path", musicInfo!!.getMusicFilePath())
                                    MusicApplication.Companion.getContext().getContentResolver()
                                        .insert(
                                            MusicContentProvider.Companion.FAVOURITE_URL,
                                            values
                                        )
                                } else {
                                    MusicApplication.Companion.getContext().getContentResolver()
                                        .delete(
                                            MusicContentProvider.Companion.FAVOURITE_URL,
                                            "id = ?",
                                            arrayOf<String?>(
                                                musicInfo!!.getId()
                                            )
                                        )
                                }
                                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE)
                                val message = Message()
                                message.what = HANDLER_CODE_DATABASE_DOWN
                                mHandler.sendMessage(message)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                if (cursor != null) {
                                    cursor.close()
                                }
                            }
                        }
                    })
                }

                else -> {}
            }
            showPlayNotification(
                musicInfo!!.getId(),
                musicInfo!!.getMusicSongName(),
                musicInfo!!.getSinger()
            )
        }
    }

    private inner class StateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.getAction()
            if (action == null) {
                return
            }
            when (action) {
                MusicBroadcastManager.SYSTEM_BROADCAST_NETWORK_CHANGE -> {
                    Log.d(TAG, "StateReceiver  " + intent.getAction())
                    if (isFirst) {
                        isFirst = false
                        return
                    } else {
                        if ((musicInfo != null && "" == musicInfo!!.getMusicFilePath()) || (musicInfo != null && musicInfo!!.getMusicFilePath() == null)) {
                            if (!CheckTools.isNetWordAvailable(getApplicationContext())) {
                                if (mMusicPlayer != null && isPlaying) {
                                    sendBroadcast(Intent(MUSIC_PLAY_ACTION))
                                }
                            } else {
                                if (mMusicPlayer != null && musicInfo != null && !isPlaying) {
                                    sendBroadcast(Intent(MUSIC_PLAY_ACTION))
                                }
                            }
                        }
                    }
                }

                MusicBroadcastManager.AUDIO_BECOMING_NOISY -> {
                    isInterrupted = true
                    pauseMusic()
                }

                else -> {}
            }
        }
    }

    private fun loadMusicPlayInfo() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val string = sharedPreferences.getString(TAG, "")
                if ("" == string || string == null) {
                    return
                }
                val gson = Gson()
                musicInfo = gson.fromJson<MusicInfo?>(string, MusicInfo::class.java)
            }
        })
    }

    private fun saveCurrentPlayMusicInfo(musicInfo: MusicInfo?) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val gson = Gson()
                val string = gson.toJson(musicInfo)
                val editor = sharedPreferences.edit()
                editor.putString(TAG, string)
                editor.apply()
            }
        })
    }

    private class MusicAudioFocusChangeListener : OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            Log.e(TAG, "focusChange  " + focusChange)
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.e(TAG, "你已经完全获得了音频焦点  ")
                    //获得了 Audio Focus.
                    if (isInterrupted) {
                        isInterrupted = false
                        playMusic(musicInfo)
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.e(TAG, "你会长时间的失去焦点，所以不要指望在短时间内能获得  ")
                    sAudioManager!!.abandonAudioFocusRequest(sAudioFocusRequest!!)
                    isInterrupted = true
                    pauseMusic()
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.e(TAG, "暂时失去 Audio Focus，但是很快就会重新获得。")
                    //暂时失去 Audio Focus，但是很快就会重新获得。应该停止所有的音频播放，但是可以不清里资源，因为可能很快就会再次获取 Audio Focus.
                    isInterrupted = true
                    pauseMusic()
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Log.e(
                    TAG,
                    "暂时失去 Audio Focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。  "
                )

                else -> {}
            }
        }
    }

    companion object {
        const val TAG: String = "MusicPlayerService"
        private var mMusicPlayer: MediaPlayer? = null

        private var mNotificationManager: NotificationManager? = null
        private var mChannel: NotificationChannel? = null
        private const val MUSIC_PLAY_NOTIFICATION_CHANNEL = "0"
        private var mRemoteViews: RemoteViews? = null
        private var mBitmap: Bitmap? = null

        const val MUSIC_PREVIOUS_ACTION: String = "quibbler.com.sevenmusic.nitofication.previous"
        const val MUSIC_NEXT_ACTION: String = "quibbler.com.sevenmusic.nitofication.next"
        const val MUSIC_PLAY_ACTION: String = "quibbler.com.sevenmusic.nitofication.play"
        const val MUSIC_FAVOURITE_ACTION: String = "quibbler.com.sevenmusic.nitofication.favourite"
        const val MUSIC_CLOSE_ACTION: String = "quibbler.com.sevenmusic.nitofication.close"

        const val MUSIC_PREVIOUS_CODE: Int = 0
        const val MUSIC_NEXT_CODE: Int = 1
        const val MUSIC_PLAY_CODE: Int = 2
        const val MUSIC_CLOSE_CODE: Int = 3
        const val MUSIC_FAVOURITE_CODE: Int = 4
        private var sAudioManager: AudioManager? = null
        private var sMusicAudioFocusChangeListener: MusicAudioFocusChangeListener? = null
        private var isInterrupted = false
        private var sAudioAttributes: AudioAttributes? = null
        private var sAudioFocusRequest: AudioFocusRequest? = null

        private const val HANDLER_CODE_DATABASE_DOWN = 0
        private const val HANDLER_CODE_IMAGE_GET = 1
        private const val HANDLER_CODE_URL_GOT = 2
        private val HANDLER_CODE_IMAGE_FAILED = -1
        private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    HANDLER_CODE_DATABASE_DOWN -> showPlayNotification(
                        musicInfo!!.getId(),
                        musicInfo!!.getMusicSongName(),
                        musicInfo!!.getSinger()
                    )

                    HANDLER_CODE_IMAGE_GET -> {
                        mRemoteViews!!.setImageViewBitmap(R.id.music_notification_icon, mBitmap)
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                    }

                    HANDLER_CODE_URL_GOT -> {
                        initMusicIcon(musicInfo!!.getId())
                        playMusic(musicInfo!!.getId(), musicInfo!!.getUrl())
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                    }

                    HANDLER_CODE_IMAGE_FAILED -> {}
                    else -> {}
                }
            }
        }

        private var sPrepared = false //播放器有无准备好
        var isPlaying: Boolean = false
        var sMusicID: String? = null

        /**
         * get getInstance of current music
         */
        var musicInfo: MusicInfo? = null
        private val sPlayMusicLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()
        /**
         * get play mode
         * 
         * @return
         */
        /**
         * @param mode
         */
        var playMode: Int = PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE
        var sPosition: Int = -1
        private val sRandom = Random(47)

        private fun requestAudioFocus() {
            if (isInterrupted) {
            }
            val focusRequest: Int = sAudioManager!!.requestAudioFocus(sAudioFocusRequest!!)
            when (focusRequest) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> Log.e(
                    TAG,
                    "AUDIOFOCUS_REQUEST_FAILED" + AudioManager.AUDIOFOCUS_REQUEST_FAILED
                )

                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> Log.e(
                    TAG,
                    "AUDIOFOCUS_REQUEST_GRANTED" + AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                )

                else -> {}
            }
        }

        fun savePlayList() {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    var cursor: Cursor? = null
                    MusicApplication.Companion.getContext().getContentResolver()
                        .delete(MusicContentProvider.Companion.PLAYLIST_URL, null, null)
                    try {
                        for (musicInfo in sPlayMusicLists) {
                            val values = ContentValues()
                            values.put("id", musicInfo.getId())
                            values.put("name", musicInfo.getMusicSongName())
                            values.put("singer", musicInfo.getSinger())
                            values.put("path", musicInfo.getMusicFilePath())
                            cursor = MusicApplication.Companion.getContext().getContentResolver()
                                .query(
                                    MusicContentProvider.Companion.PLAYLIST_URL,
                                    null,
                                    "id = ?",
                                    arrayOf<String?>(musicInfo.getId()),
                                    null
                                )
                            if (cursor != null) {
                                if (cursor.getCount() == 0) {
                                    MusicApplication.Companion.getContext().getContentResolver()
                                        .insert(MusicContentProvider.Companion.PLAYLIST_URL, values)
                                }
                                cursor.close()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }
                }
            })
        }

        /**
         * Play Music Service Entry
         * 
         * @param musicInfo
         */
        fun playMusic(musicInfo: MusicInfo?) {
            if (musicInfo == null || mMusicPlayer == null) {
                return
            }

            if (musicInfo.getMusicFilePath() != null && ("" != musicInfo.getMusicFilePath()) && isExistAlready(
                    musicInfo.getMusicFilePath()
                )
            ) {
                Companion.musicInfo = musicInfo
                initMusicIcon(Companion.musicInfo!!.getId())
                playMusic(musicInfo.getId(), musicInfo.getMusicFilePath())
                showPlayNotification(
                    musicInfo.getId(),
                    musicInfo.getMusicSongName(),
                    musicInfo.getSinger()
                )
                Companion.addToPlayList(Companion.musicInfo!!)
                addToPlayHistory(musicInfo)
            } else {
                if (!CheckTools.isNetWordAvailable(MusicApplication.Companion.getContext())) {
                    Toast.makeText(
                        MusicApplication.Companion.getContext(),
                        MusicApplication.Companion.getContext()
                            .getString(R.string.service_play_toast_check_network),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                //非WiFi情况下播放
                if (CheckTools.getNetWorkStatus(MusicApplication.Companion.getContext()) != CheckTools.NETWORK_WIFI) {
                    //从郭金良那里获取设置，默认播放打开移动网络开关
                    val setting: Any? = SharedPreferencesUtils.Companion.getInstance().getData(
                        Constant.KEY_SETTING_NETWORK_PLAY, true
                    )
                    if (setting != null && !(setting as Boolean)) {
                        Toast.makeText(
                            MusicApplication.Companion.getContext(),
                            MusicApplication.Companion.getContext()
                                .getString(R.string.service_play_toast_network_setting),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                MusicThreadPool.postRunnable(object : Runnable {
                    override fun run() {
                        var musicOnlineData: MusicDownloadUrlJsonBean.Data? = null
                        try {
                            val client = OkHttpClient()
                            val request = Request.Builder()
                                .url(MusicURL.API_MUSIC_DOWNLOAD_URL + musicInfo.getId() + "&br=128000")
                                .build()
                            val response = client.newCall(request).execute()
                            val json = response.body!!.string()
                            musicOnlineData = (Gson().fromJson<MusicDownloadUrlJsonBean?>(
                                json,
                                MusicDownloadUrlJsonBean::class.java
                            )).getData().get(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (musicOnlineData == null || musicOnlineData.getUrl() == null || "" == musicOnlineData.getUrl()) {
                            mHandler.post(object : Runnable {
                                override fun run() {
                                    Toast.makeText(
                                        MusicApplication.Companion.getContext(),
                                        "暂无版权",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_NO_COPYRIGHT)
                                    playNextMusic()
                                }
                            })
                        } else {
                            Companion.musicInfo = musicInfo
                            Companion.addToPlayList(Companion.musicInfo!!)
                            Companion.musicInfo!!.setUrl(musicOnlineData.getUrl())
                            val message = Message()
                            message.what = HANDLER_CODE_URL_GOT
                            mHandler.sendMessage(message)
                        }
                    }
                })
            }
        }

        /**
         * play a song:new one or current one:file path or url
         */
        fun playMusic(musicID: String, path: String?) {
            if (!CheckTools.isNotificationPermissionOpen(MusicApplication.Companion.getContext())) {
                CheckTools.openNotificationPermissionSetting(MusicApplication.Companion.getContext())
                Toast.makeText(
                    MusicApplication.Companion.getContext(),
                    "请打开播放通知权限",
                    Toast.LENGTH_SHORT
                ).show()
            }
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    try {
                        Thread.sleep(1000)
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                        Thread.sleep(2000)
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                        Thread.sleep(2000)
                        showPlayNotification(
                            musicInfo!!.getId(),
                            musicInfo!!.getMusicSongName(),
                            musicInfo!!.getSinger()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            requestAudioFocus()
            Companion.addToPlayHistory(musicInfo!!)
            try {
                if (isPlaying) {
                    if (musicID == sMusicID) {
                        return
                    } else {
                        sPrepared = false
                        mMusicPlayer!!.reset()
                        mMusicPlayer!!.setDataSource(path)
                        mMusicPlayer!!.prepareAsync() //改用异步加载,非阻塞主线程
                        sMusicID = musicID
                        isPlaying = true
                    }
                } else {
                    if (musicID != "-1" && musicID == sMusicID) {
                        continuePlay()
                        return
                    } else {
                        sPrepared = false
                        mMusicPlayer!!.reset()
                        mMusicPlayer!!.setDataSource(path)
                        mMusicPlayer!!.prepareAsync() //异步加载,非阻塞主线程
                        sMusicID = musicID
                        isPlaying = true
                    }
                }
                Log.d(TAG, musicInfo!!.getMusicSongName())
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * continue playing music
         */
        @Deprecated("")
        fun continuePlay() {
            if (mMusicPlayer != null && !isPlaying) {
                mMusicPlayer!!.start()
                isPlaying = true
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PLAY)
            }
        }

        /**
         * pause current played music
         */
        fun pauseMusic() {
            if (mMusicPlayer != null && isPlaying) {
                mMusicPlayer!!.pause()
                isPlaying = false
                showPlayNotification(
                    musicInfo!!.getId(),
                    musicInfo!!.getMusicSongName(),
                    musicInfo!!.getSinger()
                )
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_PAUSE)
            }
        }

        /**
         * stop play current music reset all status include bottom status bar
         */
        fun stopPlayMusic() {
            if (mMusicPlayer == null || musicInfo == null) {
                return
            }
            mMusicPlayer!!.stop()
            mMusicPlayer!!.release()
            mMusicPlayer = null
            isPlaying = false
            sPosition = -1
            sMusicID = null
            musicInfo = null
            mNotificationManager!!.cancelAll()
        }

        /**
         * set progress and play or not
         * 
         * @param progress
         * @param play
         */
        fun setPlayProgress(progress: Int, play: Boolean) {
            if (musicInfo != null && mMusicPlayer != null) {
                mMusicPlayer!!.seekTo(progress)
                if (play) {
                    continuePlay()
                } else {
                    mMusicPlayer!!.pause()
                }
            }
        }

        var playProgress: Int
            /**
             * get play progress
             * 
             * @return
             */
            get() {
                if (mMusicPlayer != null && sPrepared) {
                    return mMusicPlayer!!.getCurrentPosition()
                } else {
                    return -1
                }
            }
            /**
             * @param progress
             */
            set(progress) {
                if (musicInfo != null && mMusicPlayer != null) {
                    mMusicPlayer!!.seekTo(progress)
                    if (isPlaying) {
                        continuePlay()
                    } else {
                        mMusicPlayer!!.pause()
                    }
                }
            }

        val duration: Int
            /**
             * get song duration
             */
            get() {
                if (mMusicPlayer != null && sPrepared) {
                    return mMusicPlayer!!.getDuration()
                } else {
                    return -1
                }
            }

        /**
         * add one single song to play music list
         * 
         * @param musicInfo
         */
        fun addToPlayerList(musicInfo: MusicInfo?) {
            sPlayMusicLists.add(musicInfo!!)
        }

        /**
         * add two or more music to play list
         * 
         * @param musicInfos
         */
        fun addToPlayerList(musicInfos: MutableList<MusicInfo?>) {
            sPlayMusicLists.clear()
            sPlayMusicLists.addAll(musicInfos)
            savePlayList()
        }

        val playMusicLists: MutableList<MusicInfo>
            /**
             * @return get currently play music list
             */
            get() {
                Log.d(
                    TAG,
                    "getPlayMusicLists   " + sPlayMusicLists.size
                )
                return sPlayMusicLists
            }

        /**
         * remove from play list
         * 
         * @param musicInfo
         */
        fun removeFromPlayList(musicInfo: MusicInfo?) {
            if (musicInfo == null || musicInfo.getId() == null || "" == musicInfo.getId()) {
                return
            }
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    for (musicInfo1 in sPlayMusicLists) {
                        if (musicInfo.getId() == musicInfo1.getId()) {
                            sPlayMusicLists.remove(musicInfo1)
                            MusicApplication.Companion.getContext().getContentResolver().delete(
                                MusicContentProvider.Companion.PLAYLIST_URL,
                                "id = ?",
                                arrayOf<String?>(musicInfo.getId())
                            )
                            return
                        }
                    }
                }
            })
        }

        /**
         * clear current music play list
         */
        fun clearPlayMusicList() {
            pauseMusic()
            sPlayMusicLists.clear()
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    MusicApplication.Companion.getContext().getContentResolver()
                        .delete(MusicContentProvider.Companion.PLAYLIST_URL, null, null)
                }
            })
        }

        /**
         * play previous music
         */
        fun playPreviousMusic() {
            --sPosition
            if (musicInfo == null || mMusicPlayer == null) {
                return
            }
            val position: Int = sPlayMusicLists.indexOf(musicInfo)
            if (position == -1) {
                return
            } else if (position == 0) {
                musicInfo = sPlayMusicLists.get(sPlayMusicLists.size - 1)
                playMusic(musicInfo)
            } else {
                musicInfo = sPlayMusicLists.get(position - 1)
                playMusic(musicInfo)
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
        fun playNextMusic() {
            if (musicInfo == null || mMusicPlayer == null) {
                return
            }
            val position: Int = sPlayMusicLists.indexOf(musicInfo)
            if (position == -1) {
                return
            } else if (position == sPlayMusicLists.size - 1) {
                musicInfo = sPlayMusicLists.get(0)
                playMusic(musicInfo)
            } else {
                musicInfo = sPlayMusicLists.get(position + 1)
                playMusic(musicInfo)
            }
        }

        @WorkerThread
        private fun addToPlayList(musicInfo: MusicInfo) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    for (mi in sPlayMusicLists) {
                        if (mi.getId() == musicInfo.getId()) {
                            return
                        }
                    }
                    sPlayMusicLists.add(musicInfo)
                    //实时保存至数据库
                    try {
                        val values = ContentValues()
                        values.put("id", musicInfo.getId())
                        values.put("name", musicInfo.getMusicSongName())
                        values.put("singer", musicInfo.getSinger())
                        values.put("path", musicInfo.getMusicFilePath())
                        val cursor: Cursor? =
                            MusicApplication.Companion.getContext().getContentResolver().query(
                                MusicContentProvider.Companion.PLAYLIST_URL,
                                null,
                                "id = ?",
                                arrayOf<String?>(musicInfo.getId()),
                                null
                            )
                        if (cursor != null) {
                            if (cursor.getCount() == 0) {
                                MusicApplication.Companion.getContext().getContentResolver()
                                    .insert(MusicContentProvider.Companion.PLAYLIST_URL, values)
                            }
                            cursor.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }

        @WorkerThread
        fun addToPlayHistory(musicInfo: MusicInfo) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    val playRecord = ContentValues()
                    playRecord.put("id", musicInfo.getId())
                    playRecord.put("name", musicInfo.getMusicSongName())
                    playRecord.put("path", musicInfo.getMusicFilePath())
                    playRecord.put("last_played", System.currentTimeMillis())
                    playRecord.put("singer", musicInfo.getSinger())
                    playRecord.put("singer", musicInfo.getSinger())
                    var cursor: Cursor? = null
                    try {
                        cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                            MusicContentProvider.Companion.PLAYED_URL,
                            null,
                            "id = ?",
                            arrayOf<String?>(musicInfo.getId()),
                            null
                        )
                        if (cursor != null) {
                            if (cursor.getCount() != 0) {
                                MusicApplication.Companion.getContext().getContentResolver().update(
                                    MusicContentProvider.Companion.PLAYED_URL,
                                    playRecord,
                                    "id = ?",
                                    arrayOf<String?>(musicInfo.getId())
                                )
                            } else {
                                MusicApplication.Companion.getContext().getContentResolver()
                                    .insert(MusicContentProvider.Companion.PLAYED_URL, playRecord)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        CloseResourceUtil.closeInputAndOutput(cursor)
                    }
                }
            })
        }

        private fun showPlayNotification(id: String?, name: String?, singer: String?) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    val builder = Notification.Builder(
                        MusicApplication.Companion.getContext(),
                        MUSIC_PLAY_NOTIFICATION_CHANNEL
                    )
                    mRemoteViews!!.setTextViewText(R.id.music_notification_song_name, name)
                    mRemoteViews!!.setTextViewText(R.id.music_notification_singer, singer)
                    if (isPlaying) {
                        mRemoteViews!!.setImageViewResource(
                            R.id.music_notification_play,
                            R.drawable.music_notification_pause
                        )
                    } else {
                        mRemoteViews!!.setImageViewResource(
                            R.id.music_notification_play,
                            R.drawable.music_notification_play
                        )
                    }
                    var cursor: Cursor? = null
                    try {
                        cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                            MusicContentProvider.Companion.FAVOURITE_URL,
                            null,
                            "id = ?",
                            arrayOf<String?>(
                                musicInfo!!.getId()
                            ),
                            null
                        )
                        if (cursor != null && cursor.getCount() != 0) {
                            mRemoteViews!!.setImageViewResource(
                                R.id.music_notification_favourite,
                                R.drawable.music_notification_un_favourite
                            )
                        } else {
                            mRemoteViews!!.setImageViewResource(
                                R.id.music_notification_favourite,
                                R.drawable.music_notification_favourite
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }
                    builder.setCustomContentView(mRemoteViews)
                        .setSmallIcon(R.drawable.music_notification_small_icon)
                        .setContentTitle("正在播放:" + name).setAutoCancel(false)
                    val notification = builder.build()
                    if (isPlaying) {
                        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
                    }
                    mNotificationManager!!.notify(1, notification)
                }
            })
        }

        private fun initMusicIcon(musicID: String?) {
            if (musicID == null || "" == musicID) {
                return
            }
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    val file = File(Constant.SEVEN_MUSIC_IMAGE + "/" + musicInfo!!.getId())
                    if (file.exists()) {
                        mBitmap =
                            BitmapFactory.decodeFile(Constant.SEVEN_MUSIC_IMAGE + "/" + musicInfo!!.getId())
                        val message = Message()
                        message.what = HANDLER_CODE_IMAGE_GET
                        mHandler.sendMessage(message)
                        return
                    }

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
                        mBitmap = BitmapFactory.decodeStream(inputStream)
                        val message = Message()
                        message.what = HANDLER_CODE_IMAGE_GET
                        mHandler.sendMessage(message)
                        MusicIconLoadUtil.Companion.saveBitmapToCache(
                            mBitmap,
                            Constant.SEVEN_MUSIC_IMAGE,
                            musicInfo!!.getId()
                        )
                    } catch (e: Exception) {
                        val message = Message()
                        message.what = -1
                        mHandler.sendMessage(message)
                        e.printStackTrace()
                    } finally {
                        try {
                            CloseResourceUtil.closeInputAndOutput(inputStream)
                            CloseResourceUtil.disconnect(connection)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }

        private fun isExistAlready(path: String): Boolean {
            val musicFile = File(path)
            return musicFile.exists()
        }
    }
}
