package com.example.sevenvideoview

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.sevenvideoview.ChangeClarityDialog.OnClarityChangedListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 仿腾讯视频热点列表页播放器控制器.
 */
class IVideoPlayerController
    (context: Context) : SevenVideoPlayerController(context), View.OnClickListener,
    SeekBar.OnSeekBarChangeListener, OnClarityChangedListener {
    private var mImage: ImageView? = null
    private var mCenterStart: ImageView? = null

    private var mTop: LinearLayout? = null
    private var mBack: ImageView? = null
    private var mTitle: TextView? = null
    private var mBatteryTime: LinearLayout? = null
    private var mBattery: ImageView? = null
    private var mTime: TextView? = null

    private var mBottom: LinearLayout? = null
    private var mRestartPause: ImageView? = null
    private var mPosition: TextView? = null
    private var mDuration: TextView? = null
    private var mSeek: SeekBar? = null
    private var mClarity: TextView? = null
    private var mFullScreen: ImageView? = null

    private var mLength: TextView? = null

    private var mLoading: LinearLayout? = null
    private var mLoadText: TextView? = null

    private var mChangePositon: LinearLayout? = null
    private var mChangePositionCurrent: TextView? = null
    private var mChangePositionProgress: ProgressBar? = null

    private var mChangeBrightness: LinearLayout? = null
    private var mChangeBrightnessProgress: ProgressBar? = null

    private var mChangeVolume: LinearLayout? = null
    private var mChangeVolumeProgress: ProgressBar? = null

    private var mError: LinearLayout? = null
    private var mRetry: TextView? = null

    private var mCompleted: LinearLayout? = null
    private var mReplay: TextView? = null
    private var mShare: TextView? = null

    private var topBottomVisible = false
    private var mDismissTopBottomCountDownTimer: CountDownTimer? = null

    private var clarities: MutableList<Clarity>? = null
    private var defaultClarityIndex = 0

    private var mClarityDialog: ChangeClarityDialog? = null

    private var hasRegisterBatteryReceiver = false // 是否已经注册了电池广播

    private fun init() {
        LayoutInflater.from(mContext!!).inflate(R.layout.i_video_palyer_controller, this, true)

        mCenterStart = findViewById<View?>(R.id.center_start) as ImageView
        mImage = findViewById<View?>(R.id.image) as ImageView

        mTop = findViewById<View?>(R.id.top) as LinearLayout
        mBack = findViewById<View?>(R.id.back) as ImageView
        mTitle = findViewById<View?>(R.id.title) as TextView
        mBatteryTime = findViewById<View?>(R.id.battery_time) as LinearLayout
        mBattery = findViewById<View?>(R.id.battery) as ImageView
        mTime = findViewById<View?>(R.id.time) as TextView

        mBottom = findViewById<View?>(R.id.bottom) as LinearLayout
        mRestartPause = findViewById<View?>(R.id.restart_or_pause) as ImageView
        mPosition = findViewById<View?>(R.id.position) as TextView
        mDuration = findViewById<View?>(R.id.duration) as TextView
        mSeek = findViewById<View?>(R.id.seek) as SeekBar
        mFullScreen = findViewById<View?>(R.id.full_screen) as ImageView
        mClarity = findViewById<View?>(R.id.clarity) as TextView
        mLength = findViewById<View?>(R.id.length) as TextView

        mLoading = findViewById<View?>(R.id.loading) as LinearLayout
        mLoadText = findViewById<View?>(R.id.load_text) as TextView

        mChangePositon = findViewById<View?>(R.id.change_position) as LinearLayout
        mChangePositionCurrent = findViewById<View?>(R.id.change_position_current) as TextView
        mChangePositionProgress = findViewById<View?>(R.id.change_position_progress) as ProgressBar

        mChangeBrightness = findViewById<View?>(R.id.change_brightness) as LinearLayout
        mChangeBrightnessProgress =
            findViewById<View?>(R.id.change_brightness_progress) as ProgressBar

        mChangeVolume = findViewById<View?>(R.id.change_volume) as LinearLayout
        mChangeVolumeProgress = findViewById<View?>(R.id.change_volume_progress) as ProgressBar

        mError = findViewById<View?>(R.id.error) as LinearLayout
        mRetry = findViewById<View?>(R.id.retry) as TextView

        mCompleted = findViewById<View?>(R.id.completed) as LinearLayout
        mReplay = findViewById<View?>(R.id.replay) as TextView
        mShare = findViewById<View?>(R.id.share) as TextView

        mCenterStart!!.setOnClickListener(this)
        mBack!!.setOnClickListener(this)
        mRestartPause!!.setOnClickListener(this)
        mFullScreen!!.setOnClickListener(this)
        mClarity!!.setOnClickListener(this)
        mRetry!!.setOnClickListener(this)
        mReplay!!.setOnClickListener(this)
        mShare!!.setOnClickListener(this)
        mSeek!!.setOnSeekBarChangeListener(this)
        this.setOnClickListener(this)
    }

    override fun setTitle(title: String?) {
        mTitle!!.setText(title)
    }

    override fun imageView(): ImageView {
        return mImage!!
    }

    override fun setImage(@DrawableRes resId: Int) {
        mImage!!.setImageResource(resId)
    }

    override fun setLenght(length: Long) {
        mLength!!.setText(SevenUtil.formatTime(length))
    }

    override fun setSevenVideoPlayer(sevenVideoPlayer: ISevenVideoPlayer) {
        super.setSevenVideoPlayer(sevenVideoPlayer)
        // 给播放器配置视频链接地址
        val cl = clarities
        if (cl != null && cl.size > 1) {
            sevenVideoPlayer.setUp(cl[defaultClarityIndex].videoUrl, null)
        }
    }

    /**
     * 设置清晰度
     *
     * @param clarities 清晰度及链接
     */
    fun setClarity(clarities: MutableList<Clarity>?, defaultClarityIndex: Int) {
        if (clarities != null && clarities.size > 1) {
            this.clarities = clarities
            this.defaultClarityIndex = defaultClarityIndex

            val clarityGrades: MutableList<String?> = ArrayList<String?>()
            for (clarity in clarities) {
                clarityGrades.add(clarity.grade + " " + clarity.p)
            }
            mClarity!!.setText(clarities[defaultClarityIndex].grade)
            // 初始化切换清晰度对话框
            mClarityDialog = ChangeClarityDialog(mContext!!)
            mClarityDialog!!.setClarityGrade(clarityGrades, defaultClarityIndex)
            mClarityDialog!!.setOnClarityCheckedListener(this)
            // 给播放器配置视频链接地址
            val player = mSevenVideoPlayer
            if (player != null) {
                player.setUp(clarities[defaultClarityIndex].videoUrl, null)
            }
        }
    }

    override fun onPlayStateChanged(playState: Int) {
        when (playState) {
            SevenVideoPlayer.STATE_IDLE -> {}
            SevenVideoPlayer.STATE_PREPARING -> {
                mImage!!.setVisibility(GONE)
                mLoading!!.setVisibility(VISIBLE)
                mLoadText!!.setText(R.string.video_prepare)
                mError!!.setVisibility(GONE)
                mCompleted!!.setVisibility(GONE)
                mTop!!.setVisibility(GONE)
                mBottom!!.setVisibility(GONE)
                mRestartPause!!.setVisibility(GONE)
                mCenterStart!!.setVisibility(GONE)
                mLength!!.setVisibility(GONE)
            }

            SevenVideoPlayer.STATE_PREPARED -> startUpdateProgressTimer()
            SevenVideoPlayer.STATE_PLAYING -> {
                mLoading!!.setVisibility(GONE)
                mRestartPause!!.setImageResource(R.drawable.ic_player_pause)
                startDismissTopBottomTimer()
            }

            SevenVideoPlayer.STATE_PAUSED -> {
                mLoading!!.setVisibility(GONE)
                mRestartPause!!.setImageResource(R.drawable.ic_player_start)
                cancelDismissTopBottomTimer()
            }

            SevenVideoPlayer.STATE_BUFFERING_PLAYING -> {
                mLoading!!.setVisibility(VISIBLE)
                mRestartPause!!.setImageResource(R.drawable.ic_player_pause)
                mLoadText!!.setText(R.string.video_prepare)
                startDismissTopBottomTimer()
            }

            SevenVideoPlayer.STATE_BUFFERING_PAUSED -> {
                mLoading!!.setVisibility(VISIBLE)
                mRestartPause!!.setImageResource(R.drawable.ic_player_start)
                mLoadText!!.setText(R.string.video_prepare)
                cancelDismissTopBottomTimer()
            }

            SevenVideoPlayer.STATE_ERROR -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                mTop!!.setVisibility(VISIBLE)
                mError!!.setVisibility(VISIBLE)
            }

            SevenVideoPlayer.STATE_COMPLETED -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                mImage!!.setVisibility(VISIBLE)
                mCompleted!!.setVisibility(VISIBLE)
            }
        }
    }

    override fun onPlayModeChanged(playMode: Int) {
        when (playMode) {
            SevenVideoPlayer.MODE_NORMAL -> {
                mBack!!.setVisibility(GONE)
                mFullScreen!!.setImageResource(R.drawable.mv_full_screen_button)
                mFullScreen!!.setVisibility(VISIBLE)
                mClarity!!.setVisibility(GONE)
                mBatteryTime!!.setVisibility(GONE)
                if (hasRegisterBatteryReceiver) {
                    mContext!!.unregisterReceiver(mBatterReceiver)
                    hasRegisterBatteryReceiver = false
                }
            }

            SevenVideoPlayer.MODE_FULL_SCREEN -> {
                mBack!!.setVisibility(VISIBLE)
                mFullScreen!!.setVisibility(GONE)
                mFullScreen!!.setImageResource(R.drawable.ic_player_shrink)
                val cl = clarities
                if (cl != null && cl.size > 1) {
                    mClarity!!.setVisibility(VISIBLE)
                }
                mBatteryTime!!.setVisibility(VISIBLE)
                if (!hasRegisterBatteryReceiver) {
                    mContext!!.registerReceiver(
                        mBatterReceiver,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    )
                    hasRegisterBatteryReceiver = true
                }
            }

            SevenVideoPlayer.MODE_TINY_WINDOW -> {
                mBack!!.setVisibility(VISIBLE)
                mClarity!!.setVisibility(GONE)
            }
        }
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private val mBatterReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val status = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                mBattery!!.setImageResource(R.drawable.battery_charging)
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                mBattery!!.setImageResource(R.drawable.battery_full)
            } else {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                val percentage = ((level.toFloat() / scale) * 100).toInt()
                if (percentage <= 10) {
                    mBattery!!.setImageResource(R.drawable.battery_10)
                } else if (percentage <= 20) {
                    mBattery!!.setImageResource(R.drawable.battery_20)
                } else if (percentage <= 50) {
                    mBattery!!.setImageResource(R.drawable.battery_50)
                } else if (percentage <= 80) {
                    mBattery!!.setImageResource(R.drawable.battery_80)
                } else if (percentage <= 100) {
                    mBattery!!.setImageResource(R.drawable.battery_100)
                }
            }
        }
    }

    init {
        init()
    }

    override fun reset() {
        topBottomVisible = false
        cancelUpdateProgressTimer()
        cancelDismissTopBottomTimer()
        mSeek!!.setProgress(0)
        mSeek!!.setSecondaryProgress(0)

        mCenterStart!!.setVisibility(VISIBLE)
        mImage!!.setVisibility(VISIBLE)

        mBottom!!.setVisibility(GONE)
        mRestartPause!!.setVisibility(GONE)
        mFullScreen!!.setImageResource(R.drawable.mv_full_screen_button)

        mLength!!.setVisibility(VISIBLE)

        mTop!!.setVisibility(VISIBLE)
        mBack!!.setVisibility(GONE)

        mLoading!!.setVisibility(GONE)
        mError!!.setVisibility(GONE)
        mCompleted!!.setVisibility(GONE)
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到[.onPlayStateChanged]和[.onPlayModeChanged]中处理.
     */
    override fun onClick(v: View?) {
        val player = mSevenVideoPlayer ?: return
        if (v === mCenterStart) {
            if (player.isIdle) {
                player.start()
            }
        } else if (v === mBack) {
            if (player.isFullScreen) {
                player.exitFullScreen()
            } else if (player.isTinyWindow) {
                player.exitTinyWindow()
            }
        } else if (v === mRestartPause) {
            if (player.isPlaying || player.isBufferingPlaying) {
                player.pause()
            } else if (player.isPaused || player.isBufferingPaused) {
                player.restart()
            }
        } else if (v === mFullScreen) {
            if (player.isNormal || player.isTinyWindow) {
                player.enterFullScreen()
            } else if (player.isFullScreen) {
                player.exitFullScreen()
            }
        } else if (v === mClarity) {
            setTopBottomVisible(false) // 隐藏top、bottom
            mClarityDialog!!.show() // 显示清晰度对话框
        } else if (v === mRetry) {
            player.restart()
        } else if (v === mReplay) {
            mRetry!!.performClick()
        } else if (v === mShare) {
            Toast.makeText(mContext!!, "分享", Toast.LENGTH_SHORT).show()
        } else if (v === this) {
            if (player.isPlaying
                || player.isPaused
                || player.isBufferingPlaying
                || player.isBufferingPaused
            ) {
                setTopBottomVisible(!topBottomVisible)
            }
        }
    }

    override fun onClarityChanged(clarityIndex: Int) {
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        val clarity = clarities!![clarityIndex]
        mClarity!!.setText(clarity.grade)
        val player = mSevenVideoPlayer ?: return
        val curPos = player.currentPosition
        player.releasePlayer()
        player.setUp(clarity.videoUrl, null)
        player.start(curPos)
    }

    override fun onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true)
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private fun setTopBottomVisible(visible: Boolean) {
        mTop!!.setVisibility(if (visible) VISIBLE else GONE)
        mBottom!!.setVisibility(if (visible) VISIBLE else GONE)
        mRestartPause!!.setVisibility(if (visible) VISIBLE else GONE)
        topBottomVisible = visible
        if (visible) {
            val player = mSevenVideoPlayer ?: return
            if (!player.isPaused && !player.isBufferingPaused) {
                startDismissTopBottomTimer()
            }
        } else {
            cancelDismissTopBottomTimer()
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private fun startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer()
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = object : CountDownTimer(4000, 4000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    setTopBottomVisible(false)
                }
            }
        }
        mDismissTopBottomCountDownTimer!!.start()
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private fun cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer!!.cancel()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val player = mSevenVideoPlayer ?: return
        if (player.isBufferingPaused || player.isPaused) {
            player.restart()
        }
        val dur = player.duration
        val position = (dur * seekBar.getProgress() / 100f).toLong()
        player.seekTo(position)
        startDismissTopBottomTimer()
    }

    override fun updateProgress() {
        val player = mSevenVideoPlayer ?: return
        val position = player.currentPosition
        val dur = player.duration
        val bufferPct = player.bufferPercentage
        mSeek!!.setSecondaryProgress(bufferPct)
        val progress = (100f * position / dur).toInt()
        mSeek!!.setProgress(progress)
        mPosition!!.setText(SevenUtil.formatTime(position))
        mDuration!!.setText(SevenUtil.formatTime(dur))
        // 更新时间
        mTime!!.setText(SimpleDateFormat("HH:mm", Locale.CHINA).format(Date()))
    }

    override fun showChangePosition(duration: Long, newPositionProgress: Int) {
        mChangePositon!!.setVisibility(VISIBLE)
        val newPosition = (duration * newPositionProgress / 100f).toLong()
        mChangePositionCurrent!!.setText(SevenUtil.formatTime(newPosition))
        mChangePositionProgress!!.setProgress(newPositionProgress)
        mSeek!!.setProgress(newPositionProgress)
        mPosition!!.setText(SevenUtil.formatTime(newPosition))
    }

    override fun hideChangePosition() {
        mChangePositon!!.setVisibility(GONE)
    }

    override fun showChangeVolume(newVolumeProgress: Int) {
        mChangeVolume!!.setVisibility(VISIBLE)
        mChangeVolumeProgress!!.setProgress(newVolumeProgress)
    }

    override fun hideChangeVolume() {
        mChangeVolume!!.setVisibility(GONE)
    }

    override fun showChangeBrightness(newBrightnessProgress: Int) {
        mChangeBrightness!!.setVisibility(VISIBLE)
        mChangeBrightnessProgress!!.setProgress(newBrightnessProgress)
    }

    override fun hideChangeBrightness() {
        mChangeBrightness!!.setVisibility(GONE)
    }
}
