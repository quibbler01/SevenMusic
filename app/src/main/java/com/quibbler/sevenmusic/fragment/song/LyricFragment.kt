package com.quibbler.sevenmusic.fragment.song

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.bean.song.impl.DefaultLrcBuilder
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.view.song.ILrcView
import java.util.Timer
import java.util.TimerTask

/**
 * 
 * Package:        com.quibbler.sevenmusic.fragment.song
 * ClassName:      LyricFragment
 * Description:    播放界面歌词fragment
 * Author:         lishijun
 * CreateDate:     2019/9/27 19:48
 */
class LyricFragment : Fragment() {
    private var mView: View? = null

    private var mLrcView: ILrcView? = null //自定义歌词view

    //更新歌词的频率，每秒更新一次
    private val mPalyTimerDuration = 500

    //更新歌词的定时器
    private var mTimer: Timer? = null

    //更新歌词的定时任务
    private var mTask: TimerTask? = null

    private var mIsUpdatingLrc = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_lyric, container, false)
        initView()
        initLyric()
        updateLrc()
        if (getArguments()!!.getBoolean("state")) {
            startLrcPlay(0)
        }
        return mView!!
    }

    private fun initView() {
        mLrcView = mView!!.findViewById(R.id.music_lyric_view)
    }

    fun initLyric() {
        val lyricStr = (getActivity() as MusicPlayActivity).getMvMusicInfo().getLyric()
        if (TextUtils.isEmpty(lyricStr)) {
            //重新请求歌词并展示
            setMusicLyricOnline((getActivity() as MusicPlayActivity).getMvMusicInfo())
        } else {
            //解析歌词返回LrcRow集合
            val rows = DefaultLrcBuilder().getLrcRows(lyricStr)
            mLrcView!!.setLrc(rows)
        }
    }

    //请求歌词再展示
    fun setMusicLyricOnline(mvMusicInfo: MvMusicInfo) {
        MusicPresnter.getMusicLyric(mvMusicInfo, object : MusicCallBack {
            override fun onMusicInfoCompleted() {
                //解析歌词返回LrcRow集合
                val rows = DefaultLrcBuilder().getLrcRows(mvMusicInfo.getLyric())
                if (getActivity() != null) {
                    getActivity()!!.runOnUiThread(object : Runnable {
                        override fun run() {
                            mLrcView!!.setLrc(rows)
                        }
                    })
                }
            }
        })
    }

    /**
     * 开始展示歌词
     */
    fun startLrcPlay(delay: Int) {
        mIsUpdatingLrc = true
        if (mTimer == null) {
            mTimer = Timer()
            mTask = LyricFragment.LrcTask()
            mTimer!!.scheduleAtFixedRate(mTask, delay.toLong(), mPalyTimerDuration.toLong())
        }
    }

    //更新歌词
    fun updateLrc() {
        val timePassed = MusicPlayerService.Companion.getPlayProgress().toLong()
        if (timePassed == -1L) {
            mLrcView!!.seekLrcToTime(0, true)
        } else {
            mLrcView!!.seekLrcToTime(timePassed, true)
        }
    }

    //更新歌词至第一句
    fun updateLrcTobegin() {
        mLrcView!!.seekLrcToTime(0, true)
    }

    /**
     * 停止展示歌词
     */
    fun stopLrcPlay() {
        mIsUpdatingLrc = false
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
    }

    override fun onResume() {
        super.onResume()
        if (!mIsUpdatingLrc && MusicPlayerService.Companion.isPlaying) {
            startLrcPlay(0)
        }
    }

    override fun onPause() {
        super.onPause()
        //停止定时任务
        if (mIsUpdatingLrc) {
            stopLrcPlay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLrcPlay()
    }

    /**
     * 展示歌词的定时任务
     */
    internal inner class LrcTask : TimerTask() {
        override fun run() {
            //获取歌曲播放的位置
            val musicPlayActivity = getActivity() as MusicPlayActivity?
            if (musicPlayActivity != null && !mLrcView!!.isScrolling()) {
                val timePassed = musicPlayActivity.getProgress().toLong()
                getActivity()!!.runOnUiThread(object : Runnable {
                    override fun run() {
                        //滚动歌词
                        mLrcView!!.seekLrcToTime(timePassed, false)
                    }
                })
            }
        }
    }

    companion object {
        private const val TAG = "LyricFragment"

        private const val SERVER = "http://114.116.128.229:3000"

        private const val MUSIC_LYRIC_URL = "/lyric?id="

        fun newInstance(isPlaying: Boolean): Fragment {
            val fragment = LyricFragment()
            val bundle = Bundle()
            bundle.putBoolean("state", isPlaying)
            fragment.setArguments(bundle)
            return fragment
        }
    }
}
