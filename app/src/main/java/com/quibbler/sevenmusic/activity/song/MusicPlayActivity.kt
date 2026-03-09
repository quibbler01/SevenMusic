package com.quibbler.sevenmusic.activity.song

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.fragment.song.AlbumFragment
import com.quibbler.sevenmusic.fragment.song.LyricFragment
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.MusicDownloaderService
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.service.MusicPlayerService.PlayModeType
import com.quibbler.sevenmusic.utils.DateUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.view.playbar.PlaybarMusicListDialog
import java.text.ParseException
import java.util.Timer
import java.util.TimerTask

/**
 * Package:        com.quibbler.sevenmusic.activity.song
 * ClassName:      MusicPlayActivity
 * Description:    音乐播放activity
 * Author:         lishijun
 * CreateDate:     2019/9/26 10:59
 */
class MusicPlayActivity : AppCompatActivity(), View.OnClickListener {
    var mainLayout: LinearLayout? = null
        private set

    private var mMusicInfo = MusicInfo()

    var duration: Int = 0 //总时长
        private set

    var progress: Int = 0 //当前时长
        private set

    //歌曲信息
    var mvMusicInfo: MvMusicInfo? = null
        private set

    private var mPlayButton: ImageButton? = null

    private var mPlayBar: SeekBar? = null

    private var mPlayCurrentTimeView: TextView? = null

    private var mPlayMaxTimeView: TextView? = null

    private var mNameView: TextView? = null

    private var mArtistNameView: TextView? = null

    private var mPlayModeView: ImageButton? = null

    private var mCollectView: ImageButton? = null

    private var mTimer: Timer? = null

    private var mTask: TimerTask? = null

    private var mShouldStopUpdateUi = false

    private var mIsUpdatingBar = false

    //更新ui
    private var mReceiver: BroadcastReceiver? = null

    /**
     * 刷新进度条的定时任务
     */
    internal inner class ProgressBarTask : TimerTask() {
        override fun run() {
            //获得歌曲现在播放位置并设置成播放进度条的值
            this.progress = MusicPlayerService.Companion.getPlayProgress()
            if (this.progress >= this.duration || this.progress < 0) {
                this.progress = 0
            }
            runOnUiThread(object : Runnable {
                override fun run() {
                    mPlayBar!!.setProgress(this.progress)
                }
            })
        }
    }

    private val mPlayFragmentList: MutableList<Fragment> = ArrayList<Fragment>()

    private var mPlayPager: ViewPager? = null

    private val mDotIvArray: MutableList<ImageView?> = ArrayList<ImageView?>(2)

    private var mPagerAdapter: FragmentPagerAdapter? = null

    val playBar: SeekBar
        get() = mPlayBar!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        if (Build.VERSION.SDK_INT >= 21) {
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_IMMERSIVE
//                            // Set the content to appear under the system bars so that the
//                            // content doesn't resize when the system bars hide and show.
//                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            );
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        setContentView(R.layout.activity_music_play)
        this.mvMusicInfo = getIntent().getSerializableExtra("musicInfo") as MvMusicInfo?
        //填充歌词
        if (mvMusicInfo!!.getLyric() == null || TextUtils.equals(mvMusicInfo!!.getLyric(), "")) {
            MusicPresnter.getMusicLyric(this.mvMusicInfo)
        }
        initDots()
        initView()
        this.musicDuration
        setListener()
        initFragments()
        initBroadcast()
        mMusicInfo.setId(mvMusicInfo!!.getId().toString())
        mMusicInfo.setMusicSongName(mvMusicInfo!!.getName())
        mMusicInfo.setSinger(mvMusicInfo!!.getArtistList().get(0).getName())
        if (MusicPlayerService.Companion.isPlaying) {
            mPlayButton!!.setBackgroundResource(R.drawable.music_play_button)
            startUpdateProgressBar(0)
        }
        instance = this
    }

    private fun initBroadcast() {
        mReceiver = SongBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_PLAY)
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_PLAY_COMPLETION)
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_PAUSE)
        MusicBroadcastManager.registerMusicBroadcastReceiver(mReceiver, intentFilter)
    }

    internal inner class SongBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (MusicBroadcastManager.MUSIC_GLOBAL_PLAY == intent.getAction()) {
                mPlayButton!!.setBackgroundResource(R.drawable.music_play_button)
                //灭屏情况下如果播放歌曲，不要更新界面
                if (!mShouldStopUpdateUi) {
                    if (updatePicAndLyricAndUi()) {
                        this.musicDuration
                        //如果换了一首歌，歌词之前调至第一句
                        (mPlayFragmentList.get(1) as LyricFragment).updateLrcTobegin()
                    } else {
                        //没换歌存在两种情况：1.单曲循环，歌曲播完了  2.暂停后播放  由于单曲循环播完切歌会有延时，故延时1S
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                (mPlayFragmentList.get(1) as LyricFragment).updateLrc()
                            }
                        }, 1000)
                    }
                    startUpdateProgressBar(0)
                    (mPlayFragmentList.get(1) as LyricFragment).startLrcPlay(0)
                    (mPlayFragmentList.get(0) as AlbumFragment).startAlbumAnimator()
                    (mPlayFragmentList.get(0) as AlbumFragment).startPlayAnimator()
                }
                Log.d(TAG, mMusicInfo.getMusicSongName() + "  " + intent.getAction())
            } else if (MusicBroadcastManager.MUSIC_GLOBAL_PLAY_COMPLETION == intent.getAction()) {
                mPlayButton!!.setBackgroundResource(R.drawable.music_pause_button)
                stopUpdateProgressBar()
                (mPlayFragmentList.get(0) as AlbumFragment).stopAlbumAnimator()
                (mPlayFragmentList.get(0) as AlbumFragment).startPauseAnimator()
                (mPlayFragmentList.get(1) as LyricFragment).stopLrcPlay()
            } else if (MusicBroadcastManager.MUSIC_GLOBAL_PAUSE == intent.getAction()) {
                mPlayButton!!.setBackgroundResource(R.drawable.music_pause_button)
                stopUpdateProgressBar()
                (mPlayFragmentList.get(0) as AlbumFragment).pauseAlbumAnimator()
                (mPlayFragmentList.get(0) as AlbumFragment).startPauseAnimator()
                (mPlayFragmentList.get(1) as LyricFragment).stopLrcPlay()
            }
        }
    }

    private fun initFragments() {
        mPlayFragmentList.add(AlbumFragment.Companion.newInstance(MusicPlayerService.Companion.isPlaying))
        mPlayFragmentList.add(LyricFragment.Companion.newInstance(MusicPlayerService.Companion.isPlaying))
        mPlayPager = findViewById<ViewPager>(R.id.music_play_pager)
        mPagerAdapter = object : FragmentPagerAdapter(getSupportFragmentManager()) {
            override fun getItem(position: Int): Fragment {
                return mPlayFragmentList.get(position)
            }

            override fun getCount(): Int {
                return mPlayFragmentList.size
            }
        }
        mPlayPager!!.setAdapter(mPagerAdapter)
        mPlayPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                for (i in mDotIvArray.indices) {
                    if (i == position) {
                        mDotIvArray.get(i)!!.setBackgroundResource(R.drawable.dot_chosen)
                    } else {
                        mDotIvArray.get(i)!!.setBackgroundResource(R.drawable.dot_unchosen)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }


    private fun initView() {
        this.mainLayout = findViewById<LinearLayout?>(R.id.music_play_layout)
        mPlayButton = findViewById<ImageButton>(R.id.music_iv_play_pause)
        mPlayBar = findViewById<SeekBar>(R.id.music_play_seekbar)
        mNameView = findViewById<TextView>(R.id.music_tv_name)
        mNameView!!.setSelected(true)
        mArtistNameView = findViewById<TextView>(R.id.music_artist_tv_name)
        mPlayModeView = findViewById<ImageButton>(R.id.music_iv_mode)
        mCollectView = findViewById<ImageButton>(R.id.music_ib_collect)
        val artistString = StringBuffer()
        val mvArtistList = mvMusicInfo!!.getArtistList()
        if (mvArtistList != null) {
            for (j in mvArtistList.indices) {
                if (j == mvArtistList.size - 1) {
                    artistString.append(mvArtistList.get(j)!!.getName())
                } else {
                    artistString.append(mvArtistList.get(j)!!.getName() + "/")
                }
            }
            mArtistNameView!!.setText(artistString)
        }
        mNameView!!.setText(mvMusicInfo!!.getName())
        mPlayCurrentTimeView = findViewById<TextView>(R.id.music_play_current_time)
        mPlayMaxTimeView = findViewById<TextView>(R.id.music_play_max_time)
        val currentTime: Int = MusicPlayerService.Companion.getPlayProgress()
        mPlayCurrentTimeView!!.setText(timeParse(currentTime.toLong()))
        //设置当前播放模式的图标
        if (MusicPlayerService.Companion.getPlayMode() == PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE) {
            mPlayModeView!!.setBackgroundResource(R.drawable.music_circle_play_button)
        } else if (MusicPlayerService.Companion.getPlayMode() == PlayModeType.Companion.PLAY_TYPE_RANDOM) {
            mPlayModeView!!.setBackgroundResource(R.drawable.music_list_play_button)
        } else {
            mPlayModeView!!.setBackgroundResource(R.drawable.music_single_play_button)
        }
        //设置收藏图标
        initCollectButton()
    }

    fun setListener() {
        mPlayButton!!.setOnClickListener(this)
        findViewById<View?>(R.id.music_iv_back).setOnClickListener(this)
        findViewById<View?>(R.id.music_iv_play_next).setOnClickListener(this)
        findViewById<View?>(R.id.music_iv_play_last).setOnClickListener(this)
        findViewById<View?>(R.id.music_ib_download).setOnClickListener(this)
        findViewById<View?>(R.id.music_ib_play_list).setOnClickListener(this)
        mPlayModeView!!.setOnClickListener(this)
        mCollectView!!.setOnClickListener(this)
        mPlayBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mPlayCurrentTimeView!!.setText(timeParse(progress.toLong()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG, "onStartTrackingTouch")
                stopUpdateProgressBar()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.d(TAG, "onStopTrackingTouch")
                MusicPlayerService.Companion.setPlayProgress(
                    seekBar.getProgress(),
                    MusicPlayerService.Companion.isPlaying
                )
                this.progress = seekBar.getProgress()
                mPlayCurrentTimeView!!.setText(timeParse(progress.toLong()))
                (mPlayFragmentList.get(1) as LyricFragment).updateLrc()
                startUpdateProgressBar(0)
            }
        })
    }

    private val musicDuration: Unit
        //仅仅为了获取歌曲的总长度
        get() {
            if (MusicPlayerService.Companion.getDuration() != -1) {
                this.duration = MusicPlayerService.Companion.getDuration()
                mPlayBar!!.setMax(this.duration)
                mPlayBar!!.setProgress(MusicPlayerService.Companion.getPlayProgress())
                mPlayMaxTimeView!!.setText(timeParse(duration.toLong()))
            } else {
                MusicThreadPool.postRunnable(object : Runnable {
                    override fun run() {
                        while (MusicPlayerService.Companion.getDuration() == -1) {
                        }
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                this.duration = MusicPlayerService.Companion.getDuration()
                                mPlayBar!!.setMax(this.duration)
                                mPlayBar!!.setProgress(MusicPlayerService.Companion.getPlayProgress())
                                mPlayMaxTimeView!!.setText(
                                    timeParse(
                                        duration.toLong()
                                    )
                                )
                            }
                        })
                    }
                })
            }
        }

    override fun onClick(v: View) {
        if (v.getId() == R.id.music_iv_play_pause) {
            //如果没在播放中，立刻开始播放。
            if (!MusicPlayerService.Companion.isPlaying) {
                MusicPlayerService.Companion.playMusic(mMusicInfo)
            } else {
                MusicPlayerService.Companion.pauseMusic()
            }
        } else if (v.getId() == R.id.music_iv_back) {
            finish()
        } else if (v.getId() == R.id.music_iv_play_next) {
            playNextMusic()
        } else if (v.getId() == R.id.music_iv_play_last) {
            playPreviousMusic()
        } else if (v.getId() == R.id.music_iv_mode) {
            switchPlayMode()
        } else if (v.getId() == R.id.music_ib_download) {
            downloadMusic()
        } else if (v.getId() == R.id.music_ib_collect) {
            collectMusic()
        } else if (v.getId() == R.id.music_ib_play_list) {
            val playBarMusicListDialog = PlaybarMusicListDialog(this)
            playBarMusicListDialog.show()
        }
    }

    private fun switchPlayMode() {
        if (MusicPlayerService.Companion.getPlayMode() == PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE) {
            MusicPlayerService.Companion.setPlayMode(PlayModeType.Companion.PLAY_TYPE_RANDOM)
            mPlayModeView!!.setBackgroundResource(R.drawable.music_list_play_button)
        } else if (MusicPlayerService.Companion.getPlayMode() == PlayModeType.Companion.PLAY_TYPE_RANDOM) {
            MusicPlayerService.Companion.setPlayMode(PlayModeType.Companion.PLAY_TYPE_SINGLE_CYCLE)
            mPlayModeView!!.setBackgroundResource(R.drawable.music_single_play_button)
        } else {
            MusicPlayerService.Companion.setPlayMode(PlayModeType.Companion.PLAY_TYPE_LIST_CYCLE)
            mPlayModeView!!.setBackgroundResource(R.drawable.music_circle_play_button)
        }
    }

    private fun initDots() {
        val linearLayout = findViewById<LinearLayout>(R.id.song_ll_carousel_dots)
        val layoutParams = LinearLayout.LayoutParams(20, 20)
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in 0..1) {
            val imageView = ImageView(this)
            if (i == 0) {
                imageView.setBackgroundResource(R.drawable.dot_chosen)
            } else {
                imageView.setBackgroundResource(R.drawable.dot_unchosen)
            }
            mDotIvArray.add(imageView)
            linearLayout.addView(mDotIvArray.get(i), layoutParams)
        }
    }

    /**
     * 开始更新进度条
     */
    fun startUpdateProgressBar(delay: Int) {
        if (!mIsUpdatingBar) {
            mIsUpdatingBar = true
            if (mTimer == null) {
                mTimer = Timer()
                mTask = ProgressBarTask()
                mTimer!!.scheduleAtFixedRate(mTask, delay.toLong(), 100)
            }
        }
    }

    /**
     * 停止展示歌词
     */
    fun stopUpdateProgressBar() {
        if (mIsUpdatingBar) {
            mIsUpdatingBar = false
            if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mShouldStopUpdateUi = false
        if (updatePicAndLyricAndUi()) {
            this.musicDuration
        }
        if (!mIsUpdatingBar && MusicPlayerService.Companion.isPlaying) {
            startUpdateProgressBar(0)
        }
    }

    override fun onPause() {
        super.onPause()
        mShouldStopUpdateUi = true
        //停止定时任务
        if (mIsUpdatingBar) {
            stopUpdateProgressBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdateProgressBar()
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mReceiver)
        instance = null
    }

    private fun playNextMusic() {
        stopUpdateProgressBar()
        (mPlayFragmentList.get(1) as LyricFragment).stopLrcPlay()
        Log.d(TAG, "playNextMusic")
        MusicPlayerService.Companion.playNextMusic()
    }

    private fun playPreviousMusic() {
        stopUpdateProgressBar()
        (mPlayFragmentList.get(1) as LyricFragment).stopLrcPlay()
        MusicPlayerService.Companion.playPreviousMusic()
        Log.d(TAG, "playPreviousMusic")
    }

    private fun updatePicAndLyricAndUi(): Boolean {
        val musicInfo: MusicInfo = MusicPlayerService.Companion.getMusicInfo()
        if (!TextUtils.equals(musicInfo.getId(), mMusicInfo.getId())) {
            mMusicInfo = musicInfo
            val artist = Artist(0, mMusicInfo.getSinger())
            val artistList: MutableList<Artist?> = ArrayList<Artist?>()
            artistList.add(artist)
            this.mvMusicInfo = MvMusicInfo(
                mMusicInfo.getId().toInt(),
                mMusicInfo.getMusicSongName(), "", artistList
            )
            (mPlayFragmentList.get(0) as AlbumFragment).initPicture()
            (mPlayFragmentList.get(1) as LyricFragment).initLyric()
            mPagerAdapter!!.notifyDataSetChanged()
            updateMainUi()
            return true
        }
        return false
    }

    private fun updateMainUi() {
        mNameView!!.setText(mvMusicInfo!!.getName())
        val artistString = StringBuffer()
        val mvArtistList = mvMusicInfo!!.getArtistList()
        if (mvArtistList != null) {
            for (j in mvArtistList.indices) {
                if (j == mvArtistList.size - 1) {
                    artistString.append(mvArtistList.get(j)!!.getName())
                } else {
                    artistString.append(mvArtistList.get(j)!!.getName() + "/")
                }
            }
            mArtistNameView!!.setText(artistString)
        }
        initCollectButton()
    }

    private fun collectMusic() {
        val musicInfo: MusicInfo? = MusicPlayerService.Companion.getMusicInfo()
        if (musicInfo == null) {
            return
        }
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.FAVOURITE_URL,
                        null, "id = ?", arrayOf<String?>(musicInfo.getId()), null
                    )
                    if (cursor == null || cursor.getCount() == 0) {
                        val values = ContentValues()
                        values.put("id", musicInfo.getId())
                        values.put("name", musicInfo.getMusicSongName())
                        values.put("singer", musicInfo.getSinger())
                        values.put("path", musicInfo.getMusicFilePath())
                        MusicApplication.Companion.getContext().getContentResolver()
                            .insert(MusicContentProvider.Companion.FAVOURITE_URL, values)
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                mCollectView!!.setBackgroundResource(R.drawable.music_collected_button)
                            }
                        })
                    } else {
                        MusicApplication.Companion.getContext().getContentResolver().delete(
                            MusicContentProvider.Companion.FAVOURITE_URL,
                            "id = ?", arrayOf<String?>(musicInfo.getId())
                        )
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                mCollectView!!.setBackgroundResource(R.drawable.music_collect_button)
                            }
                        })
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

    private fun downloadMusic() {
        val intent = Intent(this, MusicDownloaderService::class.java)
        val musicInfo: MusicInfo = MusicPlayerService.Companion.getMusicInfo()
        musicInfo.setUrl("")
        intent.putExtra("music", musicInfo)
        startService(intent)
    }

    private fun initCollectButton() {
        val musicInfo: MusicInfo? = MusicPlayerService.Companion.getMusicInfo()
        if (musicInfo == null) {
            return
        }
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.FAVOURITE_URL,
                        null, "id = ?", arrayOf<String?>(musicInfo.getId()), null
                    )
                    if (cursor == null || (cursor.getCount() == 0)) {
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                mCollectView!!.setBackgroundResource(R.drawable.music_collect_button)
                            }
                        })
                    } else {
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                mCollectView!!.setBackgroundResource(R.drawable.music_collected_button)
                            }
                        })
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

    //更改默认退出动画
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
    }

    companion object {
        private const val MUSIC_PLAY_URL = "https://music.163.com/song/media/outer/url?id="

        private const val TAG = "MusicPlayActivity"

        var instance: MusicPlayActivity? = null
            private set

        /**
         * Android 音乐播放器应用里，读出的音乐时长为 long 类型以毫秒数为单位
         * 例如：将 234736 转化为分钟和秒应为 03:55 （包含四舍五入）
         * 
         * @param duration 音乐时长
         * @return
         */
        fun timeParse(duration: Long): String? {
            var s: String? = null
            try {
                s = DateUtil.longToString(duration, "mm:ss")
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return s
        }
    }
}
