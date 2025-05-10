package com.quibbler.sevenmusic.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.fragment.found.LockScreenLyricFragment;
import com.quibbler.sevenmusic.interfaces.ScrollScreenInterface;
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.BeanConverter;
import com.quibbler.sevenmusic.view.LockScreenUnderView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      LockScreenActivity
 * Description:    锁屏播放页面
 * Author:         yanwuyang
 * CreateDate:     2019/10/9 20:21
 */
public class LockScreenActivity extends AppCompatActivity implements ScrollScreenInterface {
    private static final String TAG = "LockScreenActivity";

    private LockScreenUnderView mLockScreenUnderView;
    //滑动时会移动的主体view，放在这个LinearLayout里
    private LinearLayout mLlScroll;

    private MusicInfo mMusicInfo;
    private MvMusicInfo mMvMusicInfo;

    private TextView mTvName;
    private TextView mTvSinger;
    private ImageView mIvCover;

    private ImageButton mIbPlayOrPause;
    private ImageButton mIbPlayPrevious;
    private ImageButton mIbPlayNext;

    private LockScreenLyricFragment mLyricFragment;
    private int mDuration;   //总时长
    private int mProgress = 0; //当前播放位置
    private int mOldProgress = 0;//上一次播放位置
    private final static String MUSIC_PLAY_URL = "https://music.163.com/song/media/outer/url?id=";
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Timer mProgressUpdateTimer;
    private TimerTask mProgressUpdateTask;

    private static final int PLAY_NEXT_MUSIC = 1;
    private static final int PLAY_PREVIOUS_MUSIC = 2;
    private static final int REFRESH_UI = 3;
    private static final int REFRESH_IMAGE = 4;

    private Thread mThread;
    private LockScreenHandler mHandler = new LockScreenHandler(this);

    private static class LockScreenHandler extends Handler {
        WeakReference<LockScreenActivity> mWeakReference;

        LockScreenHandler(LockScreenActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LockScreenActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case PLAY_NEXT_MUSIC:
                    activity.mThread = new Thread() {
                        @Override
                        public void run() {
                            if (isInterrupted()) {
                                return;
                            }
                            MusicPlayerService.playNextMusic();
                            while (!MusicPlayerService.isPlaying || MusicPlayerService.getMusicInfo() == null) {
                                if (isInterrupted()) {
                                    return;
                                }
                            }
                            Message message = new Message();
                            message.what = REFRESH_UI;
                            sendMessage(message);
                        }
                    };
                    activity.mThread.start();
                    break;

                case PLAY_PREVIOUS_MUSIC:
                    activity.mThread = new Thread() {
                        @Override
                        public void run() {
                            if (isInterrupted()) {
                                return;
                            }
                            MusicPlayerService.playPreviousMusic();
                            while (!MusicPlayerService.isPlaying || MusicPlayerService.getMusicInfo() == null) {
                                if (isInterrupted()) {
                                    return;
                                }
                            }
                            Message message = new Message();
                            message.what = REFRESH_UI;
                            sendMessage(message);
                        }
                    };
                    activity.mThread.start();
                    break;

                case REFRESH_UI:
                    activity.initView();
                    activity.initPlayerDuration();
                    activity.initLyricFragment();
                    break;

                case REFRESH_IMAGE:
                    //设置背景图片变暗，防止白色名字显示不清
                    ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                            .load(activity.mMusicInfo.getAlbumPicUrl())
                            .imageStyle(ImageDownloadPresenter.STYLE_ORIGIN)
                            .into(activity.mIvCover);
                    activity.mIvCover.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY); // 让图片变暗。如果想恢复显示，设置为null即可
                    break;

                default:
                    break;
            }
        }
    }

    private MusicBroadcastReceiver mMusicBroadcastReceiver = new MusicBroadcastReceiver(new BroadcastMusicStateChangeListener() {
        @Override
        public void onNoCopyright() {

        }

        @Override
        public void onSomethingWrong() {

        }

        @Override
        public void handBroadcast() {

        }

        @Override
        public void onMusicPlay() {

        }

        @Override
        public void onMusicPause() {

        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        init();
        initView();
        initListener();
        initPlayerDuration();
        initLyricFragment();

        //注册本地音乐广播接收器
        MusicBroadcastManager.registerMusicBroadcastReceiverForMusicIntent(mMusicBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLockScreenUnderView.reset();
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mMusicBroadcastReceiver);
        if (mLyricFragment != null) {
            mLyricFragment.stopLrcPlay();
        }
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.cancel();
        }
        if (mProgressUpdateTask != null) {
            mProgressUpdateTask.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

    }

    private void init() {
        mTvName = findViewById(R.id.lock_activity_tv_name);
        mTvSinger = findViewById(R.id.lock_activity_tv_singer);
        mIvCover = findViewById(R.id.lock_activity_iv_cover);

        mLockScreenUnderView = findViewById(R.id.activity_lock_under_view);
        mLlScroll = findViewById(R.id.activity_lock_ll_move_view);

        mLockScreenUnderView.setMoveView(mLlScroll);
        mLockScreenUnderView.setScrollInterface(this);

        mIbPlayOrPause = findViewById(R.id.lock_activity_ib_play_or_pause);
        mIbPlayPrevious = findViewById(R.id.lock_activity_ib_play_last);
        mIbPlayNext = findViewById(R.id.lock_activity_ib_play_next);
    }

    private void initListener() {
        mIbPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果没在播放中，立刻开始播放。
                if (!MusicPlayerService.isPlaying) {
                    MusicPlayerService.playMusic(mMusicInfo);
                    mIbPlayOrPause.setImageResource(R.drawable.music_play_button);
                    mLyricFragment.startLrcPlay();
//                    startUpdateProgress();
                } else {
                    MusicPlayerService.pauseMusic();
                    mIbPlayOrPause.setImageResource(R.drawable.music_pause_button);
                    mLyricFragment.stopLrcPlay();
                }
            }
        });

        mIbPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThread != null) {
                    mThread.interrupt();
                }
                mHandler.removeCallbacksAndMessages(null);

                Message message = new Message();
                message.what = PLAY_NEXT_MUSIC;
                mHandler.sendMessage(message);
            }
        });
        mIbPlayPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThread != null) {
                    mThread.interrupt();
                }
                mHandler.removeCallbacksAndMessages(null);

                Message message = new Message();
                message.what = PLAY_PREVIOUS_MUSIC;
                mHandler.sendMessage(message);
            }
        });
    }

    private void initView() {
        if (MusicPlayerService.isPlaying) {
            mIbPlayOrPause.setImageResource(R.drawable.music_play_button);
        } else {
            mIbPlayOrPause.setImageResource(R.drawable.music_pause_button);
        }

        mMusicInfo = MusicPlayerService.getMusicInfo();
        mMvMusicInfo = BeanConverter.convertMusicInfo2MvMusicInfo(mMusicInfo);
        String name = mMusicInfo.getMusicSongName();
        String singer = mMusicInfo.getSinger();

        mTvName.setText(name);
        mTvSinger.setText(singer);

        //没有专辑封面时的处理
        if (TextUtils.isEmpty(mMusicInfo.getAlbumPicUrl())) {
            MusicPresnter.getMusicPicture(mMvMusicInfo, new MusicCallBack() {
                @Override
                public void onMusicInfoCompleted() {
                    mMusicInfo.setAlbumPicUrl(mMvMusicInfo.getPictureUrl());
                    Message message = new Message();
                    message.what = REFRESH_IMAGE;
                    mHandler.sendMessage(message);
                }
            });
        } else {
            Message message = new Message();
            message.what = REFRESH_IMAGE;
            mHandler.sendMessage(message);
        }
    }

    private void initLyricFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mLyricFragment = (LockScreenLyricFragment) LockScreenLyricFragment.newInstance(MusicPlayerService.isPlaying);
        transaction.replace(R.id.lock_activity_fl_lyric, mLyricFragment);
        transaction.commit();
    }

    @Override
    public void onScreenScrolledToEnd() {
        finish();
    }

    public MvMusicInfo getMvMusicInfo() {
        return mMvMusicInfo;
    }

    public int getProgress() {
        return mProgress;
    }

    //仅仅为了获取歌曲的总长度
    private void initPlayerDuration() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(MUSIC_PLAY_URL + mMvMusicInfo.getId() + ".mp3");
            mMediaPlayer.prepare();
            mDuration = mMediaPlayer.getDuration();
            mMediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
