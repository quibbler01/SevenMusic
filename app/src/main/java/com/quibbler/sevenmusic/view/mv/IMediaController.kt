package com.quibbler.sevenmusic.view.mv

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.quibbler.sevenmusic.R
import java.util.Formatter
import java.util.Locale

/**
 * Package:        com.quibbler.sevenmusic.view.mv
 * ClassName:      IMediaController
 * Description:    重写MediaController类，自定义样式，增加全屏
 * Author:         lishijun
 * CreateDate:     2019/10/14 21:38
 */
class IMediaController : MediaController {
    private var mPlayer: MediaPlayerControl? = null
    private val mContext: Context
    var root: View? = null
        private set
    private var mAnchor: View? = null
    private var mProgress: ProgressBar? = null
    private var mEndTime: TextView? = null
    private var mCurrentTime: TextView? = null
    private var mDragging = false
    private var mShowing = false
    private var mFromXml = false
    private var mNextListener: OnClickListener? = null
    private var mPrevListener: OnClickListener? = null
    private var mControlListener: MediaControlListener? = null
    var mFormatBuilder: StringBuilder? = null
    var mFormatter: Formatter? = null
    private var mPauseButton: ImageButton? = null
    private var mNextButton: ImageButton? = null
    private var mPrevButton: ImageButton? = null
    private var mFullScreenButton: ImageButton? = null

    //是否强制不显示一次
    private var mIsToHideOnce = false

    fun setToHideOnce(toHideOnce: Boolean) {
        mIsToHideOnce = toHideOnce
    }

    fun setControlListener(controlListener: MediaControlListener?) {
        mControlListener = controlListener
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        mFromXml = true
    }

    //在这里设置是否使用FastForward而不是Next;useFastForward=false时使用
    // Next/Prevouse按钮所以我们在实例化MediaControl是调用这个构造函数，并且 useFastForward=false×/
    constructor(context: Context, useFastForward: Boolean) : super(context) {
        mContext = context
    }

    constructor(context: Context) : super(context) {
        mContext = context
    }

    override fun setMediaPlayer(player: MediaPlayerControl?) {
        super.setMediaPlayer(player)
        mPlayer = player
        updatePausePlay()
    }

    override fun setAnchorView(view: View?) {
        super.setAnchorView(view)
        mAnchor = view
        val frameParams = LayoutParams(
            LayoutParams.FILL_PARENT,
            LayoutParams.FILL_PARENT
        )
        removeAllViews()
        val v = makeControllerView()
        addView(v, frameParams)
    }


    protected fun makeControllerView(): View? {
        val inflate = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.root = inflate.inflate(R.layout.my_media_controller, null)
        initControllerView(this.root!!)
        return this.root
    }

    private fun initControllerView(v: View) {
        mPauseButton = v.findViewById<View?>(R.id.pause) as ImageButton?
        if (mPauseButton != null) {
            mPauseButton!!.requestFocus()
            mPauseButton!!.setOnClickListener(mPauseListener)
        }

        mNextButton = v.findViewById<View?>(R.id.next) as ImageButton?
        mPrevButton = v.findViewById<View?>(R.id.prev) as ImageButton?
        mFullScreenButton = v.findViewById<View?>(R.id.full_screen) as ImageButton?
        mProgress = v.findViewById<View?>(R.id.mediacontroller_progress) as ProgressBar?
        if (mProgress != null) {
            if (mProgress is SeekBar) {
                val seeker = mProgress as SeekBar
                seeker.setOnSeekBarChangeListener(mSeekListener)
            }
            mProgress!!.setMax(1000)
        }

        mEndTime = v.findViewById<View?>(R.id.time) as TextView?
        mCurrentTime = v.findViewById<View?>(R.id.time_current) as TextView?
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        installPrevNextListeners()
    }

    override fun hide() {
        super.hide()
        if (mAnchor == null) return
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS)
            } catch (ex: IllegalArgumentException) {
                Log.w("MediaController", "already removed")
            }
            mShowing = false
        }
    }

    private val mHandler = Handler(object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            val pos: Int
            when (msg.what) {
                FADE_OUT -> hide()
                SHOW_PROGRESS -> {
                    pos = setProgress()
                    if (!mDragging && mShowing && mPlayer!!.isPlaying()) {
                        val newMsg = mHandler.obtainMessage(SHOW_PROGRESS)
                        mHandler.sendMessageDelayed(newMsg, (1000 - (pos % 1000)).toLong())
                    }
                }
            }
            return false
        }
    })

    private fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        mFormatBuilder!!.setLength(0)
        if (hours > 0) {
            return mFormatter!!.format("%02d:%02d:%02d", hours, minutes, seconds)
                .toString()
        } else {
            return mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    override fun show(timeout: Int) {
        if (mIsToHideOnce) {
            mIsToHideOnce = false
            return
        }
        super.show(timeout)
        if (!mShowing && mAnchor != null) {
            setProgress()
            mShowing = true
        }
        updatePausePlay()
        mHandler.sendEmptyMessage(SHOW_PROGRESS)
        val msg = mHandler.obtainMessage(FADE_OUT)
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT)
            mHandler.sendMessageDelayed(msg, timeout.toLong())
        }
    }

    private fun setProgress(): Int {
        if (mPlayer == null || mDragging) {
            return 0
        }
        val position = mPlayer!!.getCurrentPosition()
        val duration = mPlayer!!.getDuration()
        if (mProgress != null) {
            if (duration > 0) {
                val pos = 1000L * position / duration
                mProgress!!.setProgress(pos.toInt())
            }
            val percent = mPlayer!!.getBufferPercentage()
            mProgress!!.setSecondaryProgress(percent * 10)
        }
        if (mEndTime != null) mEndTime!!.setText(stringForTime(duration))
        if (mCurrentTime != null) mCurrentTime!!.setText(stringForTime(position))
        return position
    }

    private val mPauseListener: OnClickListener = object : OnClickListener {
        override fun onClick(v: View?) {
            doPauseResume()
            show(sDefaultTimeout)
        }
    }

    private var mFullScreenListener: OnClickListener? = object : OnClickListener {
        override fun onClick(v: View?) {
            if (mControlListener != null) {
                mControlListener!!.actionForFullScreen()
            }
        }
    }


    private fun updatePausePlay() {
        if (this.root == null) return

        val button = root!!.findViewById<View?>(R.id.pause) as ImageButton?
        if (button == null) return

        if (mPlayer!!.isPlaying()) {
            button.setBackgroundResource(R.drawable.music_play_button)
        } else {
            button.setBackgroundResource(R.drawable.music_pause_button)
        }
    }

    private fun doPauseResume() {
        if (mPlayer!!.isPlaying()) {
            mPlayer!!.pause()
        } else {
            mPlayer!!.start()
        }
        updatePausePlay()
    }

    private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStartTrackingTouch(bar: SeekBar?) {
            show(3600000)
            mDragging = true
            mHandler.removeMessages(SHOW_PROGRESS)
        }

        override fun onProgressChanged(bar: SeekBar?, progress: Int, fromuser: Boolean) {
            if (!fromuser) {
                return
            }
            val duration = mPlayer!!.getDuration().toLong()
            val newposition = (duration * progress) / 1000L
            mPlayer!!.seekTo(newposition.toInt())
            if (mCurrentTime != null) {
                mCurrentTime!!.setText(stringForTime(newposition.toInt()))
            }
        }

        override fun onStopTrackingTouch(bar: SeekBar?) {
            mDragging = false
            setProgress()
            updatePausePlay()
            show(sDefaultTimeout)
            mHandler.sendEmptyMessage(SHOW_PROGRESS)
        }
    }

    private fun installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton!!.setOnClickListener(mNextListener)
            mNextButton!!.setEnabled(mNextListener != null)
        }

        if (mPrevButton != null) {
            mPrevButton!!.setOnClickListener(mPrevListener)
            mPrevButton!!.setEnabled(mPrevListener != null)
        }

        if (mFullScreenButton != null) {
            mFullScreenButton!!.setOnClickListener(mFullScreenListener)
            mFullScreenButton!!.setEnabled(mFullScreenListener != null)
        }
    }

    fun setPrevNextListeners(
        next: OnClickListener?,
        prev: OnClickListener?,
        fullScreen: OnClickListener?
    ) {
        mNextListener = next
        mPrevListener = prev
        mFullScreenListener = fullScreen

        if (this.root != null) {
            installPrevNextListeners()
            if (mNextButton != null && !mFromXml) {
                mNextButton!!.setVisibility(VISIBLE)
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton!!.setVisibility(VISIBLE)
            }
            if (mFullScreenButton != null && !mFromXml) {
                mFullScreenButton!!.setVisibility(VISIBLE)
            }
        }
    }


    companion object {
        private const val TAG = "IMediaController"
        private const val sDefaultTimeout = 5000
        private const val FADE_OUT = 1
        private const val SHOW_PROGRESS = 2
    }
}
