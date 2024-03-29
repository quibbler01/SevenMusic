package com.example.sevenvideoview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 仿腾讯视频热点列表页播放器控制器.
 */
public class IVideoPlayerController
        extends SevenVideoPlayerController
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        ChangeClarityDialog.OnClarityChangedListener {

    private Context mContext;
    private ImageView mImage;
    private ImageView mCenterStart;

    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;
    private LinearLayout mBatteryTime;
    private ImageView mBattery;
    private TextView mTime;

    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mPosition;
    private TextView mDuration;
    private SeekBar mSeek;
    private TextView mClarity;
    private ImageView mFullScreen;

    private TextView mLength;

    private LinearLayout mLoading;
    private TextView mLoadText;

    private LinearLayout mChangePositon;
    private TextView mChangePositionCurrent;
    private ProgressBar mChangePositionProgress;

    private LinearLayout mChangeBrightness;
    private ProgressBar mChangeBrightnessProgress;

    private LinearLayout mChangeVolume;
    private ProgressBar mChangeVolumeProgress;

    private LinearLayout mError;
    private TextView mRetry;

    private LinearLayout mCompleted;
    private TextView mReplay;
    private TextView mShare;

    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;

    private List<Clarity> clarities;
    private int defaultClarityIndex;

    private ChangeClarityDialog mClarityDialog;

    private boolean hasRegisterBatteryReceiver; // 是否已经注册了电池广播

    public IVideoPlayerController(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.i_video_palyer_controller, this, true);

        mCenterStart = (ImageView) findViewById(R.id.center_start);
        mImage = (ImageView) findViewById(R.id.image);

        mTop = (LinearLayout) findViewById(R.id.top);
        mBack = (ImageView) findViewById(R.id.back);
        mTitle = (TextView) findViewById(R.id.title);
        mBatteryTime = (LinearLayout) findViewById(R.id.battery_time);
        mBattery = (ImageView) findViewById(R.id.battery);
        mTime = (TextView) findViewById(R.id.time);

        mBottom = (LinearLayout) findViewById(R.id.bottom);
        mRestartPause = (ImageView) findViewById(R.id.restart_or_pause);
        mPosition = (TextView) findViewById(R.id.position);
        mDuration = (TextView) findViewById(R.id.duration);
        mSeek = (SeekBar) findViewById(R.id.seek);
        mFullScreen = (ImageView) findViewById(R.id.full_screen);
        mClarity = (TextView) findViewById(R.id.clarity);
        mLength = (TextView) findViewById(R.id.length);

        mLoading = (LinearLayout) findViewById(R.id.loading);
        mLoadText = (TextView) findViewById(R.id.load_text);

        mChangePositon = (LinearLayout) findViewById(R.id.change_position);
        mChangePositionCurrent = (TextView) findViewById(R.id.change_position_current);
        mChangePositionProgress = (ProgressBar) findViewById(R.id.change_position_progress);

        mChangeBrightness = (LinearLayout) findViewById(R.id.change_brightness);
        mChangeBrightnessProgress = (ProgressBar) findViewById(R.id.change_brightness_progress);

        mChangeVolume = (LinearLayout) findViewById(R.id.change_volume);
        mChangeVolumeProgress = (ProgressBar) findViewById(R.id.change_volume_progress);

        mError = (LinearLayout) findViewById(R.id.error);
        mRetry = (TextView) findViewById(R.id.retry);

        mCompleted = (LinearLayout) findViewById(R.id.completed);
        mReplay = (TextView) findViewById(R.id.replay);
        mShare = (TextView) findViewById(R.id.share);

        mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRestartPause.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mClarity.setOnClickListener(this);
        mRetry.setOnClickListener(this);
        mReplay.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mSeek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public ImageView imageView() {
        return mImage;
    }

    @Override
    public void setImage(@DrawableRes int resId) {
        mImage.setImageResource(resId);
    }

    @Override
    public void setLenght(long length) {
        mLength.setText(SevenUtil.formatTime(length));
    }

    @Override
    public void setSevenVideoPlayer(ISevenVideoPlayer sevenVideoPlayer) {
        super.setSevenVideoPlayer(sevenVideoPlayer);
        // 给播放器配置视频链接地址
        if (clarities != null && clarities.size() > 1) {
            mSevenVideoPlayer.setUp(clarities.get(defaultClarityIndex).videoUrl, null);
        }
    }

    /**
     * 设置清晰度
     *
     * @param clarities 清晰度及链接
     */
    public void setClarity(List<Clarity> clarities, int defaultClarityIndex) {
        if (clarities != null && clarities.size() > 1) {
            this.clarities = clarities;
            this.defaultClarityIndex = defaultClarityIndex;

            List<String> clarityGrades = new ArrayList<>();
            for (Clarity clarity : clarities) {
                clarityGrades.add(clarity.grade + " " + clarity.p);
            }
            mClarity.setText(clarities.get(defaultClarityIndex).grade);
            // 初始化切换清晰度对话框
            mClarityDialog = new ChangeClarityDialog(mContext);
            mClarityDialog.setClarityGrade(clarityGrades, defaultClarityIndex);
            mClarityDialog.setOnClarityCheckedListener(this);
            // 给播放器配置视频链接地址
            if (mSevenVideoPlayer != null) {
                mSevenVideoPlayer.setUp(clarities.get(defaultClarityIndex).videoUrl, null);
            }
        }
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        switch (playState) {
            case SevenVideoPlayer.STATE_IDLE:
                break;
            case SevenVideoPlayer.STATE_PREPARING:
                mImage.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                mLoadText.setText(R.string.video_prepare);
                mError.setVisibility(View.GONE);
                mCompleted.setVisibility(View.GONE);
                mTop.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mRestartPause.setVisibility(View.GONE);
                mCenterStart.setVisibility(View.GONE);
                mLength.setVisibility(View.GONE);
                break;
            case SevenVideoPlayer.STATE_PREPARED:
                startUpdateProgressTimer();
                break;
            case SevenVideoPlayer.STATE_PLAYING:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                startDismissTopBottomTimer();
                break;
            case SevenVideoPlayer.STATE_PAUSED:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                cancelDismissTopBottomTimer();
                break;
            case SevenVideoPlayer.STATE_BUFFERING_PLAYING:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                mLoadText.setText(R.string.video_prepare);
                startDismissTopBottomTimer();
                break;
            case SevenVideoPlayer.STATE_BUFFERING_PAUSED:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                mLoadText.setText(R.string.video_prepare);
                cancelDismissTopBottomTimer();
                break;
            case SevenVideoPlayer.STATE_ERROR:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mTop.setVisibility(View.VISIBLE);
                mError.setVisibility(View.VISIBLE);
                break;
            case SevenVideoPlayer.STATE_COMPLETED:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mImage.setVisibility(View.VISIBLE);
                mCompleted.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onPlayModeChanged(int playMode) {
        switch (playMode) {
            case SevenVideoPlayer.MODE_NORMAL:
                mBack.setVisibility(View.GONE);
                mFullScreen.setImageResource(R.drawable.mv_full_screen_button);
                mFullScreen.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                mBatteryTime.setVisibility(View.GONE);
                if (hasRegisterBatteryReceiver) {
                    mContext.unregisterReceiver(mBatterReceiver);
                    hasRegisterBatteryReceiver = false;
                }
                break;
            case SevenVideoPlayer.MODE_FULL_SCREEN:
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setVisibility(View.GONE);
                mFullScreen.setImageResource(R.drawable.ic_player_shrink);
                if (clarities != null && clarities.size() > 1) {
                    mClarity.setVisibility(View.VISIBLE);
                }
                mBatteryTime.setVisibility(View.VISIBLE);
                if (!hasRegisterBatteryReceiver) {
                    mContext.registerReceiver(mBatterReceiver,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    hasRegisterBatteryReceiver = true;
                }
                break;
            case SevenVideoPlayer.MODE_TINY_WINDOW:
                mBack.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                mBattery.setImageResource(R.drawable.battery_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                mBattery.setImageResource(R.drawable.battery_full);
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (percentage <= 10) {
                    mBattery.setImageResource(R.drawable.battery_10);
                } else if (percentage <= 20) {
                    mBattery.setImageResource(R.drawable.battery_20);
                } else if (percentage <= 50) {
                    mBattery.setImageResource(R.drawable.battery_50);
                } else if (percentage <= 80) {
                    mBattery.setImageResource(R.drawable.battery_80);
                } else if (percentage <= 100) {
                    mBattery.setImageResource(R.drawable.battery_100);
                }
            }
        }
    };

    @Override
    protected void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);

        mCenterStart.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.VISIBLE);

        mBottom.setVisibility(View.GONE);
        mRestartPause.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.mv_full_screen_button);

        mLength.setVisibility(View.VISIBLE);

        mTop.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.GONE);

        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到{@link #onPlayStateChanged}和{@link #onPlayModeChanged}中处理.
     */
    @Override
    public void onClick(View v) {
        if (v == mCenterStart) {
            if (mSevenVideoPlayer.isIdle()) {
                mSevenVideoPlayer.start();
            }
        } else if (v == mBack) {
            if (mSevenVideoPlayer.isFullScreen()) {
                mSevenVideoPlayer.exitFullScreen();
            } else if (mSevenVideoPlayer.isTinyWindow()) {
                mSevenVideoPlayer.exitTinyWindow();
            }
        } else if (v == mRestartPause) {
            if (mSevenVideoPlayer.isPlaying() || mSevenVideoPlayer.isBufferingPlaying()) {
                mSevenVideoPlayer.pause();
            } else if (mSevenVideoPlayer.isPaused() || mSevenVideoPlayer.isBufferingPaused()) {
                mSevenVideoPlayer.restart();
            }
        } else if (v == mFullScreen) {
            if (mSevenVideoPlayer.isNormal() || mSevenVideoPlayer.isTinyWindow()) {
                mSevenVideoPlayer.enterFullScreen();
            } else if (mSevenVideoPlayer.isFullScreen()) {
                mSevenVideoPlayer.exitFullScreen();
            }
        } else if (v == mClarity) {
            setTopBottomVisible(false); // 隐藏top、bottom
            mClarityDialog.show();     // 显示清晰度对话框
        } else if (v == mRetry) {
            mSevenVideoPlayer.restart();
        } else if (v == mReplay) {
            mRetry.performClick();
        } else if (v == mShare) {
            Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
        } else if (v == this) {
            if (mSevenVideoPlayer.isPlaying()
                    || mSevenVideoPlayer.isPaused()
                    || mSevenVideoPlayer.isBufferingPlaying()
                    || mSevenVideoPlayer.isBufferingPaused()) {
                setTopBottomVisible(!topBottomVisible);
            }
        }
    }

    @Override
    public void onClarityChanged(int clarityIndex) {
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        Clarity clarity = clarities.get(clarityIndex);
        mClarity.setText(clarity.grade);
        long currentPosition = mSevenVideoPlayer.getCurrentPosition();
        mSevenVideoPlayer.releasePlayer();
        mSevenVideoPlayer.setUp(clarity.videoUrl, null);
        mSevenVideoPlayer.start(currentPosition);
    }

    @Override
    public void onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true);
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        mRestartPause.setVisibility(visible ? View.VISIBLE : View.GONE);
        topBottomVisible = visible;
        if (visible) {
            if (!mSevenVideoPlayer.isPaused() && !mSevenVideoPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(4000, 4000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mSevenVideoPlayer.isBufferingPaused() || mSevenVideoPlayer.isPaused()) {
            mSevenVideoPlayer.restart();
        }
        long position = (long) (mSevenVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mSevenVideoPlayer.seekTo(position);
        startDismissTopBottomTimer();
    }

    @Override
    protected void updateProgress() {
        long position = mSevenVideoPlayer.getCurrentPosition();
        long duration = mSevenVideoPlayer.getDuration();
        int bufferPercentage = mSevenVideoPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeek.setProgress(progress);
        mPosition.setText(SevenUtil.formatTime(position));
        mDuration.setText(SevenUtil.formatTime(duration));
        // 更新时间
        mTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
    }

    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
        mChangePositon.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mChangePositionCurrent.setText(SevenUtil.formatTime(newPosition));
        mChangePositionProgress.setProgress(newPositionProgress);
        mSeek.setProgress(newPositionProgress);
        mPosition.setText(SevenUtil.formatTime(newPosition));
    }

    @Override
    protected void hideChangePosition() {
        mChangePositon.setVisibility(View.GONE);
    }

    @Override
    protected void showChangeVolume(int newVolumeProgress) {
        mChangeVolume.setVisibility(View.VISIBLE);
        mChangeVolumeProgress.setProgress(newVolumeProgress);
    }

    @Override
    protected void hideChangeVolume() {
        mChangeVolume.setVisibility(View.GONE);
    }

    @Override
    protected void showChangeBrightness(int newBrightnessProgress) {
        mChangeBrightness.setVisibility(View.VISIBLE);
        mChangeBrightnessProgress.setProgress(newBrightnessProgress);
    }

    @Override
    protected void hideChangeBrightness() {
        mChangeBrightness.setVisibility(View.GONE);
    }
}
