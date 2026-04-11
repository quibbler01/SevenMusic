package com.example.sevenvideoview

import android.R
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.greenrobot.eventbus.EventBus
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * 播放器
 */
class SevenVideoPlayer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), ISevenVideoPlayer, TextureView.SurfaceTextureListener {
    private var mPlayerType: Int = TYPE_IJK
    private var mCurrentState: Int = STATE_IDLE
    private var mCurrentMode: Int = MODE_NORMAL

    private var mContext: Context = context
    private var mAudioManager: AudioManager? = null
    private var mMediaPlayer: IMediaPlayer? = null
    private var mContainer: FrameLayout? = null
    private var mTextureView: SevenTextureView? = null
    private var mController: SevenVideoPlayerController? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null
    private var mUrl: String? = null
    private var mHeaders: MutableMap<String?, String?>? = null
    private var mBufferPercentage = 0
    private var continueFromLastPosition = true
    private var skipToPosition: Long = 0

    fun setContext(context: Context) {
        mContext = context
    }

    private fun init() {
        mContainer = FrameLayout(mContext)
        mContainer!!.setBackgroundColor(Color.BLACK)
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        this.addView(mContainer, params)
    }

    override fun setUp(url: String?, headers: MutableMap<String?, String?>?) {
        mUrl = url
        mHeaders = headers
    }

    fun setController(controller: SevenVideoPlayerController?) {
        mContainer!!.removeView(mController)
        mController = controller
        mController!!.reset()
        mController!!.setSevenVideoPlayer(this)
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        mContainer!!.addView(mController, params)
    }

    /**
     * 设置播放器类型
     *
     * @param playerType IjkPlayer or MediaPlayer.
     */
    fun setPlayerType(playerType: Int) {
        mPlayerType = playerType
    }

    /**
     * 是否从上一次的位置继续播放
     *
     * @param continueFromLastPosition true从上一次的位置继续播放
     */
    override fun continueFromLastPosition(continueFromLastPosition: Boolean) {
        this.continueFromLastPosition = continueFromLastPosition
    }

    override fun setSpeed(speed: Float) {
        if (mMediaPlayer is IjkMediaPlayer) {
            (mMediaPlayer as IjkMediaPlayer).setSpeed(speed)
        } else {
            LogUtil.d("only IjkPlayer can set speed")
        }
    }

    override fun start() {
        EventBus.getDefault().post("mv start")
        if (mCurrentState == STATE_IDLE) {
            SevenVideoPlayerManager.instance.setCurrentSevenVideoPlayer(this)
            initAudioManager()
            initMediaPlayer()
            initTextureView()
            addTextureView()
        } else {
            LogUtil.d("NiceVideoPlayer only when mCurrentState == STATE_IDLE can start")
        }
    }

    override fun start(position: Long) {
        skipToPosition = position
        start()
    }

    override fun restart() {
        EventBus.getDefault().post("mv start")
        if (mCurrentState == STATE_PAUSED) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("STATE_PLAYING")
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_BUFFERING_PLAYING
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("STATE_BUFFERING_PLAYING")
        } else if (mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR) {
            mMediaPlayer!!.reset()
            openMediaPlayer()
        } else {
            LogUtil.d("NiceVideoPlayer when mCurrentState == " + mCurrentState + "cannot restart")
        }
    }

    override fun pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaPlayer!!.pause()
            mCurrentState = STATE_PAUSED
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("STATE_PAUSED")
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaPlayer!!.pause()
            mCurrentState = STATE_BUFFERING_PAUSED
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("STATE_BUFFERING_PAUSED")
        }
    }

    override fun seekTo(pos: Long) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.seekTo(pos)
        }
    }

    override var volume: Int
        get() {
            return if (mAudioManager != null) {
                mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            } else 0
        }
        set(volume) {
            if (mAudioManager != null) {
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }
        }

    override val isIdle: Boolean get() = mCurrentState == STATE_IDLE
    override val isPreparing: Boolean get() = mCurrentState == STATE_PREPARING
    override val isPrepared: Boolean get() = mCurrentState == STATE_PREPARED
    override val isBufferingPlaying: Boolean get() = mCurrentState == STATE_BUFFERING_PLAYING
    override val isBufferingPaused: Boolean get() = mCurrentState == STATE_BUFFERING_PAUSED
    override val isPlaying: Boolean get() = mCurrentState == STATE_PLAYING
    override val isPaused: Boolean get() = mCurrentState == STATE_PAUSED
    override val isError: Boolean get() = mCurrentState == STATE_ERROR
    override val isCompleted: Boolean get() = mCurrentState == STATE_COMPLETED
    override val isFullScreen: Boolean get() = mCurrentMode == MODE_FULL_SCREEN
    override val isTinyWindow: Boolean get() = mCurrentMode == MODE_TINY_WINDOW
    override val isNormal: Boolean get() = mCurrentMode == MODE_NORMAL

    override val maxVolume: Int
        get() {
            return if (mAudioManager != null) {
                mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            } else 0
        }

    override val duration: Long
        get() = if (mMediaPlayer != null) mMediaPlayer!!.duration else 0

    override val currentPosition: Long
        get() = if (mMediaPlayer != null) mMediaPlayer!!.currentPosition else 0

    override val bufferPercentage: Int
        get() = mBufferPercentage

    override fun getSpeed(speed: Float): Float {
        if (mMediaPlayer is IjkMediaPlayer) {
            return (mMediaPlayer as IjkMediaPlayer).getSpeed(speed)
        }
        return 0f
    }

    override val tcpSpeed: Long
        get() {
            if (mMediaPlayer is IjkMediaPlayer) {
                return (mMediaPlayer as IjkMediaPlayer).getTcpSpeed()
            }
            return 0
        }

    private fun initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            mAudioManager!!.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun initMediaPlayer() {
        if (mMediaPlayer == null) {
            when (mPlayerType) {
                TYPE_NATIVE -> mMediaPlayer = AndroidMediaPlayer()
                TYPE_IJK -> mMediaPlayer = AndroidMediaPlayer()
                else -> mMediaPlayer = AndroidMediaPlayer()
            }
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
    }

    private fun initTextureView() {
        if (mTextureView == null) {
            mTextureView = SevenTextureView(mContext)
            mTextureView!!.setSurfaceTextureListener(this)
        }
    }

    private fun addTextureView() {
        mContainer!!.removeView(mTextureView)
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        mContainer!!.addView(mTextureView, 0, params)
    }

    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture
            openMediaPlayer()
        } else {
            mTextureView!!.setSurfaceTexture(mSurfaceTexture!!)
        }
    }

    private fun openMediaPlayer() {
        // 屏幕常亮
        mContainer!!.setKeepScreenOn(true)
        // 设置监听
        mMediaPlayer!!.setOnPreparedListener(mOnPreparedListener)
        mMediaPlayer!!.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
        mMediaPlayer!!.setOnCompletionListener(mOnCompletionListener)
        mMediaPlayer!!.setOnErrorListener(mOnErrorListener)
        mMediaPlayer!!.setOnInfoListener(mOnInfoListener)
        mMediaPlayer!!.setOnBufferingUpdateListener(mOnBufferingUpdateListener)
        // 设置dataSource
        try {
            mMediaPlayer!!.setDataSource(
                mContext.getApplicationContext(),
                Uri.parse(mUrl),
                mHeaders
            )
            if (mSurface == null) {
                mSurface = Surface(mSurfaceTexture)
            }
            mMediaPlayer!!.setSurface(mSurface)
            mMediaPlayer!!.prepareAsync()
            mCurrentState = STATE_PREPARING
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("STATE_PREPARING")
        } catch (e: Exception) {
            LogUtil.e("open player error", e)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return mSurfaceTexture == null
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    private val mOnPreparedListener
            : IMediaPlayer.OnPreparedListener = object : IMediaPlayer.OnPreparedListener {
        override fun onPrepared(mp: IMediaPlayer) {
            mCurrentState = STATE_PREPARED
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("onPrepared -> STATE_PREPARED")
            mp.start()
            // 从上次的保存位置播放
            if (continueFromLastPosition) {
                val savedPlayPosition = SevenUtil.getSavedPlayPosition(mContext, mUrl)
                mp.seekTo(savedPlayPosition)
            }
            // 跳到指定位置播放
            if (skipToPosition != 0L) {
                mp.seekTo(skipToPosition)
            }
        }
    }

    private val mOnVideoSizeChangedListener
            : IMediaPlayer.OnVideoSizeChangedListener =
        object : IMediaPlayer.OnVideoSizeChangedListener {
            override fun onVideoSizeChanged(
                mp: IMediaPlayer?,
                width: Int,
                height: Int,
                sar_num: Int,
                sar_den: Int
            ) {
                mTextureView!!.adaptVideoSize(width, height)
                LogUtil.d("onVideoSizeChanged -> width: $width, height: $height")
            }
        }

    private val mOnCompletionListener
            : IMediaPlayer.OnCompletionListener = object : IMediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: IMediaPlayer?) {
            mCurrentState = STATE_COMPLETED
            mController!!.onPlayStateChanged(mCurrentState)
            LogUtil.d("onCompletion -> STATE_COMPLETED")
            // 清除屏幕常亮
            mContainer!!.setKeepScreenOn(false)
        }
    }

    private val mOnErrorListener
            : IMediaPlayer.OnErrorListener = object : IMediaPlayer.OnErrorListener {
        override fun onError(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
            // 直播流播放时去调用mediaPlayer.duration会导致-38和-2147483648错误，忽略该错误
            if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
                mCurrentState = STATE_ERROR
                mController!!.onPlayStateChanged(mCurrentState)
                LogUtil.d("onError -> STATE_ERROR -- what: $what, extra: $extra")
            }
            return true
        }
    }

    private val mOnInfoListener
            : IMediaPlayer.OnInfoListener = object : IMediaPlayer.OnInfoListener {
        override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    // 播放器开始渲染
                    mCurrentState = STATE_PLAYING
                    mController!!.onPlayStateChanged(mCurrentState)
                    LogUtil.d("onInfo -> MEDIA_INFO_VIDEO_RENDERING_START: STATE_PLAYING")
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    // MediaPlayer暂时不播放，以缓冲更多的数据
                    if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_BUFFERING_PAUSED
                        LogUtil.d("onInfo -> MEDIA_INFO_BUFFERING_START: STATE_BUFFERING_PAUSED")
                    } else {
                        mCurrentState = STATE_BUFFERING_PLAYING
                        LogUtil.d("onInfo -> MEDIA_INFO_BUFFERING_START: STATE_BUFFERING_PLAYING")
                    }
                    mController!!.onPlayStateChanged(mCurrentState)
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    // 填充缓冲区后，MediaPlayer恢复播放/暂停
                    if (mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_PLAYING
                        mController!!.onPlayStateChanged(mCurrentState)
                        LogUtil.d("onInfo -> MEDIA_INFO_BUFFERING_END: STATE_PLAYING")
                    }
                    if (mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_PAUSED
                        mController!!.onPlayStateChanged(mCurrentState)
                        LogUtil.d("onInfo -> MEDIA_INFO_BUFFERING_END: STATE_PAUSED")
                    }
                }
                IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                    // 视频旋转了extra度，需要恢复
                    if (mTextureView != null) {
                        mTextureView!!.setRotation(extra.toFloat())
                        LogUtil.d("video rotate angle: $extra")
                    }
                }
                IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> {
                    LogUtil.d("video cannot seekTo, is living video")
                }
                else -> {
                    LogUtil.d("onInfo -> what: $what")
                }
            }
            return true
        }
    }

    private val mOnBufferingUpdateListener
            : IMediaPlayer.OnBufferingUpdateListener =
        object : IMediaPlayer.OnBufferingUpdateListener {
            override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {
                mBufferPercentage = percent
            }
        }

    init {
        init()
    }

    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     * 切换横屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期
     */
    override fun enterFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) return

        // 隐藏ActionBar、状态栏，并横屏
        SevenUtil.hideActionBar(mContext)
        SevenUtil.scanForActivity(mContext)!!
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        val contentView = SevenUtil.scanForActivity(mContext)!!
            .findViewById<View?>(R.id.content) as ViewGroup
        if (mCurrentMode == MODE_TINY_WINDOW) {
            contentView.removeView(mContainer)
        } else {
            this.removeView(mContainer)
        }
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        contentView.addView(mContainer, params)

        mCurrentMode = MODE_FULL_SCREEN
        mController!!.onPlayModeChanged(mCurrentMode)
        LogUtil.d("MODE_FULL_SCREEN")
    }

    /**
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     * 切换竖屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期.
     *
     * @return true退出全屏.
     */
    override fun exitFullScreen(): Boolean {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            SevenUtil.showActionBar(mContext)
            SevenUtil.scanForActivity(mContext)!!
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            val contentView = SevenUtil.scanForActivity(mContext)!!
                .findViewById<View?>(R.id.content) as ViewGroup
            contentView.removeView(mContainer)
            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            this.addView(mContainer, params)

            mCurrentMode = MODE_NORMAL
            mController!!.onPlayModeChanged(mCurrentMode)
            LogUtil.d("MODE_NORMAL")
            return true
        }
        return false
    }

    /**
     * 进入小窗口播放，小窗口播放的实现原理与全屏播放类似。
     */
    override fun enterTinyWindow() {
        if (mCurrentMode == MODE_TINY_WINDOW) return
        this.removeView(mContainer)

        val contentView = SevenUtil.scanForActivity(mContext)!!
            .findViewById<View?>(R.id.content) as ViewGroup
        // 小窗口的宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp。
        val params = LayoutParams(
            (SevenUtil.getScreenWidth(mContext) * 0.6f).toInt(),
            (SevenUtil.getScreenWidth(mContext) * 0.6f * 9f / 16f).toInt()
        )
        params.gravity = Gravity.BOTTOM or Gravity.END
        params.rightMargin = SevenUtil.dp2px(mContext, 8f)
        params.bottomMargin = SevenUtil.dp2px(mContext, 8f)

        contentView.addView(mContainer, params)

        mCurrentMode = MODE_TINY_WINDOW
        mController!!.onPlayModeChanged(mCurrentMode)
        LogUtil.d("MODE_TINY_WINDOW")
    }

    /**
     * 退出小窗口播放
     */
    override fun exitTinyWindow(): Boolean {
        if (mCurrentMode == MODE_TINY_WINDOW) {
            val contentView = SevenUtil.scanForActivity(mContext)!!
                .findViewById<View?>(R.id.content) as ViewGroup
            contentView.removeView(mContainer)
            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            this.addView(mContainer, params)

            mCurrentMode = MODE_NORMAL
            mController!!.onPlayModeChanged(mCurrentMode)
            LogUtil.d("MODE_NORMAL")
            return true
        }
        return false
    }

    override fun releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager!!.abandonAudioFocus(null)
            mAudioManager = null
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        mContainer!!.removeView(mTextureView)
        if (mSurface != null) {
            mSurface!!.release()
            mSurface = null
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.release()
            mSurfaceTexture = null
        }
        mCurrentState = STATE_IDLE
    }

    override fun release() {
        // 保存播放位置
        if (isPlaying || isBufferingPlaying || isBufferingPaused || isPaused) {
            SevenUtil.savePlayPosition(mContext, mUrl, currentPosition)
        } else if (isCompleted) {
            SevenUtil.savePlayPosition(mContext, mUrl, 0)
        }
        // 退出全屏或小窗口
        if (isFullScreen) {
            exitFullScreen()
        }
        if (isTinyWindow) {
            exitTinyWindow()
        }
        mCurrentMode = MODE_NORMAL

        // 释放播放器
        releasePlayer()

        // 恢复控制器
        if (mController != null) {
            mController!!.reset()
        }
        Runtime.getRuntime().gc()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isPlaying) {
            SevenVideoPlayerManager.instance.releaseSevenVideoPlayer()
        }
    }

    companion object {
        /**
         * 播放错误
         */
        val STATE_ERROR: Int = -1

        /**
         * 播放未开始
         */
        const val STATE_IDLE: Int = 0

        /**
         * 播放准备中
         */
        const val STATE_PREPARING: Int = 1

        /**
         * 播放准备就绪
         */
        const val STATE_PREPARED: Int = 2

        /**
         * 正在播放
         */
        const val STATE_PLAYING: Int = 3

        /**
         * 暂停播放
         */
        const val STATE_PAUSED: Int = 4

        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
         */
        const val STATE_BUFFERING_PLAYING: Int = 5

        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
         */
        const val STATE_BUFFERING_PAUSED: Int = 6

        /**
         * 播放完成
         */
        const val STATE_COMPLETED: Int = 7

        /**
         * 普通模式
         */
        const val MODE_NORMAL: Int = 10

        /**
         * 全屏模式
         */
        const val MODE_FULL_SCREEN: Int = 11

        /**
         * 小窗口模式
         */
        const val MODE_TINY_WINDOW: Int = 12

        /**
         * IjkPlayer
         */
        const val TYPE_IJK: Int = 111

        /**
         * MediaPlayer
         */
        const val TYPE_NATIVE: Int = 222
    }
}
