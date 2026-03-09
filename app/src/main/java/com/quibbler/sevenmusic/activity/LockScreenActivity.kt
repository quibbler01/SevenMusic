package com.quibbler.sevenmusic.activity

import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.fragment.found.LockScreenLyricFragment
import com.quibbler.sevenmusic.interfaces.ScrollScreenInterface
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.BeanConverter
import com.quibbler.sevenmusic.view.LockScreenUnderView
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      LockScreenActivity
 * Description:    锁屏播放页面
 * Author:         yanwuyang
 * CreateDate:     2019/10/9 20:21
 */
class LockScreenActivity : AppCompatActivity(), ScrollScreenInterface {
    private var mLockScreenUnderView: LockScreenUnderView? = null

    //滑动时会移动的主体view，放在这个LinearLayout里
    private var mLlScroll: LinearLayout? = null

    private var mMusicInfo: MusicInfo? = null
    private var mMvMusicInfo: MvMusicInfo? = null

    private var mTvName: TextView? = null
    private var mTvSinger: TextView? = null
    private var mIvCover: ImageView? = null

    private var mIbPlayOrPause: ImageButton? = null
    private var mIbPlayPrevious: ImageButton? = null
    private var mIbPlayNext: ImageButton? = null

    private var mLyricFragment: LockScreenLyricFragment? = null
    private var mDuration = 0 //总时长
    val progress: Int = 0 //当前播放位置
    private val mOldProgress = 0 //上一次播放位置
    private var mMediaPlayer: MediaPlayer? = MediaPlayer()
    private val mProgressUpdateTimer: Timer? = null
    private val mProgressUpdateTask: TimerTask? = null

    private var mThread: Thread? = null
    private val mHandler: LockScreenHandler? = LockScreenHandler(this)

    private class LockScreenHandler(activity: LockScreenActivity?) : Handler() {
        var mWeakReference: WeakReference<LockScreenActivity?>

        init {
            mWeakReference = WeakReference<LockScreenActivity?>(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mWeakReference.get()
            if (activity == null) {
                return
            }

            when (msg.what) {
                PLAY_NEXT_MUSIC -> {
                    activity.mThread = object : Thread() {
                        override fun run() {
                            if (isInterrupted()) {
                                return
                            }
                            MusicPlayerService.Companion.playNextMusic()
                            while (!MusicPlayerService.Companion.isPlaying || MusicPlayerService.Companion.getMusicInfo() == null) {
                                if (isInterrupted()) {
                                    return
                                }
                            }
                            val message = Message()
                            message.what = REFRESH_UI
                            sendMessage(message)
                        }
                    }
                    activity.mThread!!.start()
                }

                PLAY_PREVIOUS_MUSIC -> {
                    activity.mThread = object : Thread() {
                        override fun run() {
                            if (isInterrupted()) {
                                return
                            }
                            MusicPlayerService.Companion.playPreviousMusic()
                            while (!MusicPlayerService.Companion.isPlaying || MusicPlayerService.Companion.getMusicInfo() == null) {
                                if (isInterrupted()) {
                                    return
                                }
                            }
                            val message = Message()
                            message.what = REFRESH_UI
                            sendMessage(message)
                        }
                    }
                    activity.mThread!!.start()
                }

                REFRESH_UI -> {
                    activity.initView()
                    activity.initPlayerDuration()
                    activity.initLyricFragment()
                }

                REFRESH_IMAGE -> {
                    //设置背景图片变暗，防止白色名字显示不清
                    ImageDownloadPresenter.Companion.getInstance()
                        .with(MusicApplication.Companion.getContext())
                        .load(activity.mMusicInfo!!.getAlbumPicUrl())
                        .imageStyle(ImageDownloadPresenter.Companion.STYLE_ORIGIN)
                        .into(activity.mIvCover)
                    activity.mIvCover!!.setColorFilter(
                        Color.GRAY,
                        PorterDuff.Mode.MULTIPLY
                    ) // 让图片变暗。如果想恢复显示，设置为null即可
                }

                else -> {}
            }
        }
    }

    private val mMusicBroadcastReceiver =
        MusicBroadcastReceiver(object : BroadcastMusicStateChangeListener {
            override fun onNoCopyright() {
            }

            override fun onSomethingWrong() {
            }

            override fun handBroadcast() {
            }

            override fun onMusicPlay() {
            }

            override fun onMusicPause() {
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        val window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        init()
        initView()
        initListener()
        initPlayerDuration()
        initLyricFragment()

        //注册本地音乐广播接收器
        MusicBroadcastManager.registerMusicBroadcastReceiverForMusicIntent(mMusicBroadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mLockScreenUnderView!!.reset()
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mMusicBroadcastReceiver)
        if (mLyricFragment != null) {
            mLyricFragment!!.stopLrcPlay()
        }
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.cancel()
        }
        if (mProgressUpdateTask != null) {
            mProgressUpdateTask.cancel()
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null)
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
        }
    }

    private fun init() {
        mTvName = findViewById<TextView>(R.id.lock_activity_tv_name)
        mTvSinger = findViewById<TextView>(R.id.lock_activity_tv_singer)
        mIvCover = findViewById<ImageView>(R.id.lock_activity_iv_cover)

        mLockScreenUnderView = findViewById<LockScreenUnderView>(R.id.activity_lock_under_view)
        mLlScroll = findViewById<LinearLayout?>(R.id.activity_lock_ll_move_view)

        mLockScreenUnderView!!.setMoveView(mLlScroll)
        mLockScreenUnderView!!.setScrollInterface(this)

        mIbPlayOrPause = findViewById<ImageButton>(R.id.lock_activity_ib_play_or_pause)
        mIbPlayPrevious = findViewById<ImageButton>(R.id.lock_activity_ib_play_last)
        mIbPlayNext = findViewById<ImageButton>(R.id.lock_activity_ib_play_next)
    }

    private fun initListener() {
        mIbPlayOrPause!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //如果没在播放中，立刻开始播放。
                if (!MusicPlayerService.Companion.isPlaying) {
                    MusicPlayerService.Companion.playMusic(mMusicInfo)
                    mIbPlayOrPause!!.setImageResource(R.drawable.music_play_button)
                    mLyricFragment!!.startLrcPlay()
                    //                    startUpdateProgress();
                } else {
                    MusicPlayerService.Companion.pauseMusic()
                    mIbPlayOrPause!!.setImageResource(R.drawable.music_pause_button)
                    mLyricFragment!!.stopLrcPlay()
                }
            }
        })

        mIbPlayNext!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mThread != null) {
                    mThread!!.interrupt()
                }
                mHandler!!.removeCallbacksAndMessages(null)

                val message = Message()
                message.what = PLAY_NEXT_MUSIC
                mHandler.sendMessage(message)
            }
        })
        mIbPlayPrevious!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mThread != null) {
                    mThread!!.interrupt()
                }
                mHandler!!.removeCallbacksAndMessages(null)

                val message = Message()
                message.what = PLAY_PREVIOUS_MUSIC
                mHandler.sendMessage(message)
            }
        })
    }

    private fun initView() {
        if (MusicPlayerService.Companion.isPlaying) {
            mIbPlayOrPause!!.setImageResource(R.drawable.music_play_button)
        } else {
            mIbPlayOrPause!!.setImageResource(R.drawable.music_pause_button)
        }

        mMusicInfo = MusicPlayerService.Companion.getMusicInfo()
        mMvMusicInfo = BeanConverter.convertMusicInfo2MvMusicInfo(mMusicInfo)
        val name = mMusicInfo!!.getMusicSongName()
        val singer = mMusicInfo!!.getSinger()

        mTvName!!.setText(name)
        mTvSinger!!.setText(singer)

        //没有专辑封面时的处理
        if (TextUtils.isEmpty(mMusicInfo!!.getAlbumPicUrl())) {
            MusicPresnter.getMusicPicture(mMvMusicInfo, object : MusicCallBack {
                override fun onMusicInfoCompleted() {
                    mMusicInfo!!.setAlbumPicUrl(mMvMusicInfo!!.getPictureUrl())
                    val message = Message()
                    message.what = REFRESH_IMAGE
                    mHandler!!.sendMessage(message)
                }
            })
        } else {
            val message = Message()
            message.what = REFRESH_IMAGE
            mHandler!!.sendMessage(message)
        }
    }

    private fun initLyricFragment() {
        val fragmentManager = getSupportFragmentManager()
        val transaction = fragmentManager.beginTransaction()
        mLyricFragment =
            LockScreenLyricFragment.Companion.newInstance(MusicPlayerService.Companion.isPlaying) as LockScreenLyricFragment
        transaction.replace(R.id.lock_activity_fl_lyric, mLyricFragment!!)
        transaction.commit()
    }

    override fun onScreenScrolledToEnd() {
        finish()
    }

    val mvMusicInfo: MvMusicInfo
        get() = mMvMusicInfo!!

    //仅仅为了获取歌曲的总长度
    private fun initPlayerDuration() {
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(MUSIC_PLAY_URL + mMvMusicInfo!!.getId() + ".mp3")
            mMediaPlayer!!.prepare()
            mDuration = mMediaPlayer!!.getDuration()
            mMediaPlayer!!.release()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "LockScreenActivity"

        private const val MUSIC_PLAY_URL = "https://music.163.com/song/media/outer/url?id="
        private const val PLAY_NEXT_MUSIC = 1
        private const val PLAY_PREVIOUS_MUSIC = 2
        private const val REFRESH_UI = 3
        private const val REFRESH_IMAGE = 4
    }
}
