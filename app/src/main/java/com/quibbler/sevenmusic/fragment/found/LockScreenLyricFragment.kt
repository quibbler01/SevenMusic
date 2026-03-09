package com.quibbler.sevenmusic.fragment.found

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.LockScreenActivity
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.bean.song.impl.DefaultLrcBuilder
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.ThreadDispatcher
import com.quibbler.sevenmusic.view.song.impl.LrcView
import java.util.Timer
import java.util.TimerTask

class LockScreenLyricFragment : Fragment() {
    private var mView: View? = null

    private var mLrcView: LrcView? = null //自定义歌词view

    //更新歌词的频率，每0.5秒更新一次
    private val mPlayTimerDuration = 500

    //更新歌词的定时器
    private var mTimer: Timer? = null

    //更新歌词的定时任务
    private var mTask: TimerTask? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = inflater.inflate(R.layout.fragment_lock_screen_lyric, container, false)
        if (getArguments()!!.getBoolean("state")) {
            startLrcPlay()
        }
        initView()
        initLyric()
        return mView!!
    }

    private fun initView() {
        mLrcView = mView!!.findViewById<LrcView>(R.id.fragment_lock_lyric_view)
        mLrcView!!.setCanScroll(false)
    }

    fun initLyric() {
        //重新请求歌词并展示
        setMusicLyricOnline((getActivity() as LockScreenActivity).getMvMusicInfo())
    }

    //请求歌词再展示
    fun setMusicLyricOnline(mvMusicInfo: MvMusicInfo) {
        MusicPresnter.getMusicLyric(mvMusicInfo, object : MusicCallBack {
            override fun onMusicInfoCompleted() {
                //解析歌词返回LrcRow集合
                val rows = DefaultLrcBuilder().getLrcRows(mvMusicInfo.getLyric())
                ThreadDispatcher.Companion.getInstance().runOnUiThread(object : Runnable {
                    override fun run() {
                        mLrcView!!.setLrc(rows)
                    }
                })
            }
        })
    }

    /**
     * 开始展示歌词
     */
    fun startLrcPlay() {
        if (mTimer == null) {
            mTimer = Timer()
            mTask = LockScreenLyricFragment.LrcTask()
            mTimer!!.scheduleAtFixedRate(mTask, 0, mPlayTimerDuration.toLong())
        }
    }

    //更新歌词
    fun updateLrc() {
        if (getActivity() != null) {
            val timePassed = (getActivity() as LockScreenActivity).getProgress().toLong()
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
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
    }

    /**
     * 展示歌词的定时任务
     */
    internal inner class LrcTask : TimerTask() {
        override fun run() {
            //获取歌曲播放的位置
            if (getActivity() != null) {
                val timePassed = MusicPlayerService.Companion.getPlayProgress().toLong()
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
        fun newInstance(isPlaying: Boolean): Fragment {
            val fragment = LockScreenLyricFragment()
            val bundle = Bundle()
            bundle.putBoolean("state", isPlaying)
            fragment.setArguments(bundle)
            return fragment
        }
    }
}
