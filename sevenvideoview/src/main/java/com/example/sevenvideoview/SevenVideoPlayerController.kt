package com.example.sevenvideoview

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 控制器抽象类
 */
abstract class SevenVideoPlayerController
    (context: Context) : FrameLayout(context), View.OnTouchListener {
    protected val mContext: Context? = context
    protected var mSevenVideoPlayer: ISevenVideoPlayer? = null

    private var mUpdateProgressTimer: Timer? = null
    private var mUpdateProgressTimerTask: TimerTask? = null

    private var mDownX = 0f
    private var mDownY = 0f
    private var mNeedChangePosition = false
    private var mNeedChangeVolume = false
    private var mNeedChangeBrightness = false
    private var mGestureDownPosition: Long = 0
    private var mGestureDownBrightness = 0f
    private var mGestureDownVolume = 0
    private var mNewPosition: Long = 0

    init {
        this.setOnTouchListener(this)
    }

    open fun setSevenVideoPlayer(sevenVideoPlayer: ISevenVideoPlayer) {
        mSevenVideoPlayer = sevenVideoPlayer
    }

    /**
     * 设置播放的视频的标题
     *
     * @param title 视频标题
     */
    abstract fun setTitle(title: String?)

    /**
     * 视频底图
     *
     * @param resId 视频底图资源
     */
    abstract fun setImage(@DrawableRes resId: Int)

    /**
     * 视频底图ImageView控件，提供给外部用图片加载工具来加载网络图片
     *
     * @return 底图ImageView
     */
    abstract fun imageView(): ImageView?

    /**
     * 设置总时长.
     */
    abstract fun setLenght(length: Long)

    /**
     * 当播放器的播放状态发生变化，在此方法中国你更新不同的播放状态的UI
     *
     * @param playState 播放状态
     */
    abstract fun onPlayStateChanged(playState: Int)

    /**
     * 当播放器的播放模式发生变化，在此方法中更新不同模式下的控制器界面。
     *
     * @param playMode 播放器的模式
     */
    abstract fun onPlayModeChanged(playMode: Int)

    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    abstract fun reset()

    /**
     * 开启更新进度的计时器。
     */
    protected fun startUpdateProgressTimer() {
        cancelUpdateProgressTimer()
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = Timer()
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = object : TimerTask() {
                override fun run() {
                    this@SevenVideoPlayerController.post(object : Runnable {
                        override fun run() {
                            updateProgress()
                        }
                    })
                }
            }
        }
        mUpdateProgressTimer!!.schedule(mUpdateProgressTimerTask, 0, 1000)
    }

    /**
     * 取消更新进度的计时器。
     */
    protected fun cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer!!.cancel()
            mUpdateProgressTimer = null
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask!!.cancel()
            mUpdateProgressTimerTask = null
        }
    }

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract fun updateProgress()

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val player = mSevenVideoPlayer ?: return false
        // 只有全屏的时候才能拖动位置、亮度、声音
        if (!player.isFullScreen) {
            return false
        }
        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (player.isIdle
            || player.isError
            || player.isPreparing
            || player.isPrepared
            || player.isCompleted
        ) {
            hideChangePosition()
            hideChangeBrightness()
            hideChangeVolume()
            return false
        }
        val x = event.getX()
        var y = event.getY()
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
                mNeedChangePosition = false
                mNeedChangeVolume = false
                mNeedChangeBrightness = false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mDownX
                var deltaY = y - mDownY
                val absDeltaX = abs(deltaX)
                val absDeltaY = abs(deltaY)
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    if (absDeltaX >= THRESHOLD) {
                        cancelUpdateProgressTimer()
                        mNeedChangePosition = true
                        mGestureDownPosition = player.currentPosition
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < getWidth() * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true
                            mGestureDownBrightness = SevenUtil.scanForActivity(mContext)!!
                                .getWindow().getAttributes().screenBrightness
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true
                            mGestureDownVolume = player.volume
                        }
                    }
                }
                if (mNeedChangePosition) {
                    val duration = player.duration
                    val toPosition =
                        (mGestureDownPosition + duration * deltaX / getWidth()).toLong()
                    mNewPosition = max(0L, min(duration, toPosition))
                    val newPositionProgress = (100f * mNewPosition / duration).toInt()
                    showChangePosition(duration, newPositionProgress)
                }
                if (mNeedChangeBrightness) {
                    deltaY = -deltaY
                    val deltaBrightness = deltaY * 3 / getHeight()
                    var newBrightness = mGestureDownBrightness + deltaBrightness
                    newBrightness = max(0f, min(newBrightness, 1f))
                    val newBrightnessPercentage = newBrightness
                    val params = SevenUtil.scanForActivity(mContext)!!
                        .getWindow().getAttributes()
                    params.screenBrightness = newBrightnessPercentage
                    SevenUtil.scanForActivity(mContext)!!.getWindow().setAttributes(params)
                    val newBrightnessProgress = (100f * newBrightnessPercentage).toInt()
                    showChangeBrightness(newBrightnessProgress)
                }
                if (mNeedChangeVolume) {
                    deltaY = -deltaY
                    val maxVol = player.maxVolume
                    val deltaVolume = (maxVol * deltaY * 3 / getHeight()).toInt()
                    var newVolume = mGestureDownVolume + deltaVolume
                    newVolume = max(0, min(maxVol, newVolume))
                    player.volume = newVolume
                    val newVolumeProgress = (100f * newVolume / maxVol).toInt()
                    showChangeVolume(newVolumeProgress)
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mNeedChangePosition) {
                    player.seekTo(mNewPosition)
                    hideChangePosition()
                    startUpdateProgressTimer()
                    return true
                }
                if (mNeedChangeBrightness) {
                    hideChangeBrightness()
                    return true
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume()
                    return true
                }
            }
        }
        return false
    }

    /**
     * 手势左右滑动改变播放位置时，显示控制器中间的播放位置变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     */
    protected abstract fun showChangePosition(duration: Long, newPositionProgress: Int)

    /**
     * 手势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangePosition()

    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     */
    protected abstract fun showChangeVolume(newVolumeProgress: Int)

    /**
     * 手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeVolume()

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     */
    protected abstract fun showChangeBrightness(newBrightnessProgress: Int)

    /**
     * 手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeBrightness()

    companion object {
        private const val THRESHOLD = 80
    }
}
