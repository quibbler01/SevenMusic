package com.quibbler.sevenmusic.view.mv;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.quibbler.sevenmusic.R;

import java.util.Formatter;
import java.util.Locale;

/**
 * Package:        com.quibbler.sevenmusic.view.mv
 * ClassName:      IMediaController
 * Description:    重写MediaController类，自定义样式，增加全屏
 * Author:         lishijun
 * CreateDate:     2019/10/14 21:38
 */
public class IMediaController extends MediaController {
    private static final String TAG = "IMediaController";
    private MediaPlayerControl mPlayer;
    private Context mContext;
    private View mRoot;
    private View mAnchor;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mDragging;
    private boolean mShowing;
    private static final int sDefaultTimeout = 5000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mFromXml;
    private OnClickListener mNextListener, mPrevListener;
    private MediaControlListener mControlListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ImageButton mFullScreenButton;

    //是否强制不显示一次
    private boolean mIsToHideOnce = false;

    public View getRoot() {
        return mRoot;
    }

    public void setToHideOnce(boolean toHideOnce) {
        mIsToHideOnce = toHideOnce;
    }

    public void setControlListener(MediaControlListener controlListener) {
        mControlListener = controlListener;
    }

    public IMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFromXml = true;
    }

    //在这里设置是否使用FastForward而不是Next;useFastForward=false时使用
// Next/Prevouse按钮所以我们在实例化MediaControl是调用这个构造函数，并且 useFastForward=false×/
    public IMediaController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
    }

    public IMediaController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        mPlayer = player;
        updatePausePlay();
    }

    public void setAnchorView(View view) {
        super.setAnchorView(view);
        mAnchor = view;
        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }


    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.my_media_controller, null);
        initControllerView(mRoot);
        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mNextButton = (ImageButton) v.findViewById(R.id.next);
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        mFullScreenButton = (ImageButton) v.findViewById(R.id.full_screen);
        mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        installPrevNextListeners();
    }

    public void hide() {
        super.hide();
        if (mAnchor == null)
            return;
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = mHandler.obtainMessage(SHOW_PROGRESS);
                        mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
            return false;
        }
    });

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void show(int timeout) {
        if (mIsToHideOnce) {
            mIsToHideOnce = false;
            return;
        }
        super.show(timeout);
        if (!mShowing && mAnchor != null) {
            setProgress();
            mShowing = true;
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }
        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));
        return position;
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFullScreenListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControlListener != null) {
                mControlListener.actionForFullScreen();
            }
        }
    };


    private void updatePausePlay() {
        if (mRoot == null)
            return;

        ImageButton button = (ImageButton) mRoot.findViewById(R.id.pause);
        if (button == null)
            return;

        if (mPlayer.isPlaying()) {
            button.setBackgroundResource(R.drawable.music_play_button);
        } else {
            button.setBackgroundResource(R.drawable.music_pause_button);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);
            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime((int) newposition));
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }

        if (mFullScreenButton != null) {
            mFullScreenButton.setOnClickListener(mFullScreenListener);
            mFullScreenButton.setEnabled(mFullScreenListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev, OnClickListener fullScreen) {
        mNextListener = next;
        mPrevListener = prev;
        mFullScreenListener = fullScreen;

        if (mRoot != null) {
            installPrevNextListeners();
            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
            if (mFullScreenButton != null && !mFromXml) {
                mFullScreenButton.setVisibility(View.VISIBLE);
            }
        }
    }


}
