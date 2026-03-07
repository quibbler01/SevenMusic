package com.quibbler.sevenmusic.activity.song;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.fragment.song.AlbumFragment;
import com.quibbler.sevenmusic.fragment.song.LyricFragment;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.MusicDownloaderService;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.DateUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.view.playbar.PlaybarMusicListDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PAUSE;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PLAY;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_PLAY_COMPLETION;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.FAVOURITE_URL;

/**
 * Package:        com.quibbler.sevenmusic.activity.song
 * ClassName:      MusicPlayActivity
 * Description:    音乐播放activity
 * Author:         lishijun
 * CreateDate:     2019/9/26 10:59
 */
public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String MUSIC_PLAY_URL = "https://music.163.com/song/media/outer/url?id=";

    private static final String TAG = "MusicPlayActivity";

    private static MusicPlayActivity sInstance;

    private LinearLayout mLinearLayout;

    private MusicInfo mMusicInfo = new MusicInfo();

    private int mDuration;   //总时长

    private int mProgress; //当前时长

    //歌曲信息
    private MvMusicInfo mMvMusicInfo;

    private ImageButton mPlayButton;

    private SeekBar mPlayBar;

    private TextView mPlayCurrentTimeView;

    private TextView mPlayMaxTimeView;

    private TextView mNameView;

    private TextView mArtistNameView;

    private ImageButton mPlayModeView;

    private ImageButton mCollectView;

    private Timer mTimer;

    private TimerTask mTask;

    private boolean mShouldStopUpdateUi = false;

    private boolean mIsUpdatingBar = false;

    public int getDuration() {
        return mDuration;
    }

    public int getProgress() {
        return mProgress;
    }

    //更新ui
    private BroadcastReceiver mReceiver;

    /**
     * 刷新进度条的定时任务
     */
    class ProgressBarTask extends TimerTask {
        @Override
        public void run() {
            //获得歌曲现在播放位置并设置成播放进度条的值
            mProgress = MusicPlayerService.getPlayProgress();
            if (mProgress >= mDuration || mProgress < 0) {
                mProgress = 0;
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    mPlayBar.setProgress(mProgress);
                }
            });
        }
    }

    private List<Fragment> mPlayFragmentList = new ArrayList<>();

    private ViewPager mPlayPager;

    private List<ImageView> mDotIvArray = new ArrayList<>(2);

    private FragmentPagerAdapter mPagerAdapter;

    public static MusicPlayActivity getInstance() {
        return sInstance;
    }

    public SeekBar getPlayBar() {
        return mPlayBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_music_play);
        mMvMusicInfo = (MvMusicInfo) getIntent().getSerializableExtra("musicInfo");
        //填充歌词
        if (mMvMusicInfo.getLyric() == null || TextUtils.equals(mMvMusicInfo.getLyric(), "")) {
            MusicPresnter.getMusicLyric(mMvMusicInfo);
        }
        initDots();
        initView();
        getMusicDuration();
        setListener();
        initFragments();
        initBroadcast();
        mMusicInfo.setId(String.valueOf(mMvMusicInfo.getId()));
        mMusicInfo.setMusicSongName(mMvMusicInfo.getName());
        mMusicInfo.setSinger(mMvMusicInfo.getArtistList().get(0).getName());
        if (MusicPlayerService.isPlaying) {
            mPlayButton.setBackgroundResource(R.drawable.music_play_button);
            startUpdateProgressBar(0);
        }
        sInstance = this;
    }

    private void initBroadcast() {
        mReceiver = new SongBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_GLOBAL_PLAY);
        intentFilter.addAction(MUSIC_GLOBAL_PLAY_COMPLETION);
        intentFilter.addAction(MUSIC_GLOBAL_PAUSE);
        MusicBroadcastManager.registerMusicBroadcastReceiver(mReceiver, intentFilter);
    }

    class SongBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MUSIC_GLOBAL_PLAY.equals(intent.getAction())) {
                mPlayButton.setBackgroundResource(R.drawable.music_play_button);
                //灭屏情况下如果播放歌曲，不要更新界面
                if (!mShouldStopUpdateUi) {
                    if (updatePicAndLyricAndUi()) {
                        getMusicDuration();
                        //如果换了一首歌，歌词之前调至第一句
                        ((LyricFragment) mPlayFragmentList.get(1)).updateLrcTobegin();
                    } else {
                        //没换歌存在两种情况：1.单曲循环，歌曲播完了  2.暂停后播放  由于单曲循环播完切歌会有延时，故延时1S
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((LyricFragment) mPlayFragmentList.get(1)).updateLrc();
                            }
                        }, 1000);
                    }
                    startUpdateProgressBar(0);
                    ((LyricFragment) mPlayFragmentList.get(1)).startLrcPlay(0);
                    ((AlbumFragment) mPlayFragmentList.get(0)).startAlbumAnimator();
                    ((AlbumFragment) mPlayFragmentList.get(0)).startPlayAnimator();
                }
                Log.d(TAG, mMusicInfo.getMusicSongName() + "  " + intent.getAction());
            } else if (MUSIC_GLOBAL_PLAY_COMPLETION.equals(intent.getAction())) {
                mPlayButton.setBackgroundResource(R.drawable.music_pause_button);
                stopUpdateProgressBar();
                ((AlbumFragment) mPlayFragmentList.get(0)).stopAlbumAnimator();
                ((AlbumFragment) mPlayFragmentList.get(0)).startPauseAnimator();
                ((LyricFragment) mPlayFragmentList.get(1)).stopLrcPlay();
            } else if (MUSIC_GLOBAL_PAUSE.equals(intent.getAction())) {
                mPlayButton.setBackgroundResource(R.drawable.music_pause_button);
                stopUpdateProgressBar();
                ((AlbumFragment) mPlayFragmentList.get(0)).pauseAlbumAnimator();
                ((AlbumFragment) mPlayFragmentList.get(0)).startPauseAnimator();
                ((LyricFragment) mPlayFragmentList.get(1)).stopLrcPlay();
            }
        }
    }

    public MvMusicInfo getMvMusicInfo() {
        return mMvMusicInfo;
    }

    private void initFragments() {
        mPlayFragmentList.add(AlbumFragment.newInstance(MusicPlayerService.isPlaying));
        mPlayFragmentList.add(LyricFragment.newInstance(MusicPlayerService.isPlaying));
        mPlayPager = findViewById(R.id.music_play_pager);
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mPlayFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mPlayFragmentList.size();
            }
        };
        mPlayPager.setAdapter(mPagerAdapter);
        mPlayPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mDotIvArray.size(); i++) {
                    if (i == position) {
                        mDotIvArray.get(i).setBackgroundResource(R.drawable.dot_chosen);
                    } else {
                        mDotIvArray.get(i).setBackgroundResource(R.drawable.dot_unchosen);
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void initView() {
        mLinearLayout = findViewById(R.id.music_play_layout);
        mPlayButton = findViewById(R.id.music_iv_play_pause);
        mPlayBar = findViewById(R.id.music_play_seekbar);
        mNameView = findViewById(R.id.music_tv_name);
        mNameView.setSelected(true);
        mArtistNameView = findViewById(R.id.music_artist_tv_name);
        mPlayModeView = findViewById(R.id.music_iv_mode);
        mCollectView = findViewById(R.id.music_ib_collect);
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mMvMusicInfo.getArtistList();
        if (mvArtistList != null) {
            for (int j = 0; j < mvArtistList.size(); j++) {
                if (j == mvArtistList.size() - 1) {
                    artistString.append(mvArtistList.get(j).getName());
                } else {
                    artistString.append(mvArtistList.get(j).getName() + "/");
                }
            }
            mArtistNameView.setText(artistString);
        }
        mNameView.setText(mMvMusicInfo.getName());
        mPlayCurrentTimeView = findViewById(R.id.music_play_current_time);
        mPlayMaxTimeView = findViewById(R.id.music_play_max_time);
        int currentTime = MusicPlayerService.getPlayProgress();
        mPlayCurrentTimeView.setText(timeParse(currentTime));
        //设置当前播放模式的图标
        if (MusicPlayerService.getPlayMode() == MusicPlayerService.PlayModeType.PLAY_TYPE_LIST_CYCLE) {
            mPlayModeView.setBackgroundResource(R.drawable.music_circle_play_button);
        } else if (MusicPlayerService.getPlayMode() == MusicPlayerService.PlayModeType.PLAY_TYPE_RANDOM) {
            mPlayModeView.setBackgroundResource(R.drawable.music_list_play_button);
        } else {
            mPlayModeView.setBackgroundResource(R.drawable.music_single_play_button);
        }
        //设置收藏图标
        initCollectButton();
    }

    public void setListener() {
        mPlayButton.setOnClickListener(this);
        findViewById(R.id.music_iv_back).setOnClickListener(this);
        findViewById(R.id.music_iv_play_next).setOnClickListener(this);
        findViewById(R.id.music_iv_play_last).setOnClickListener(this);
        findViewById(R.id.music_ib_download).setOnClickListener(this);
        findViewById(R.id.music_ib_play_list).setOnClickListener(this);
        mPlayModeView.setOnClickListener(this);
        mCollectView.setOnClickListener(this);
        mPlayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPlayCurrentTimeView.setText(timeParse(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
                stopUpdateProgressBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                MusicPlayerService.setPlayProgress(seekBar.getProgress(), MusicPlayerService.isPlaying);
                mProgress = seekBar.getProgress();
                mPlayCurrentTimeView.setText(timeParse(mProgress));
                ((LyricFragment) mPlayFragmentList.get(1)).updateLrc();
                startUpdateProgressBar(0);
            }
        });
    }

    //仅仅为了获取歌曲的总长度
    private void getMusicDuration() {
        if (MusicPlayerService.getDuration() != -1) {
            mDuration = MusicPlayerService.getDuration();
            mPlayBar.setMax(mDuration);
            mPlayBar.setProgress(MusicPlayerService.getPlayProgress());
            mPlayMaxTimeView.setText(timeParse(mDuration));
        } else {
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    while (MusicPlayerService.getDuration() == -1) {

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDuration = MusicPlayerService.getDuration();
                            mPlayBar.setMax(mDuration);
                            mPlayBar.setProgress(MusicPlayerService.getPlayProgress());
                            mPlayMaxTimeView.setText(timeParse(mDuration));
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.music_iv_play_pause) {
            //如果没在播放中，立刻开始播放。
            if (!MusicPlayerService.isPlaying) {
                MusicPlayerService.playMusic(mMusicInfo);
            } else {
                MusicPlayerService.pauseMusic();
            }
        } else if (v.getId() == R.id.music_iv_back) {
            finish();
        } else if (v.getId() == R.id.music_iv_play_next) {
            playNextMusic();
        } else if (v.getId() == R.id.music_iv_play_last) {
            playPreviousMusic();
        } else if (v.getId() == R.id.music_iv_mode) {
            switchPlayMode();
        } else if (v.getId() == R.id.music_ib_download) {
            downloadMusic();
        } else if (v.getId() == R.id.music_ib_collect) {
            collectMusic();
        } else if (v.getId() == R.id.music_ib_play_list) {
            PlaybarMusicListDialog playBarMusicListDialog = new PlaybarMusicListDialog(this);
            playBarMusicListDialog.show();
        }
    }

    private void switchPlayMode() {
        if (MusicPlayerService.getPlayMode() == MusicPlayerService.PlayModeType.PLAY_TYPE_LIST_CYCLE) {
            MusicPlayerService.setPlayMode(MusicPlayerService.PlayModeType.PLAY_TYPE_RANDOM);
            mPlayModeView.setBackgroundResource(R.drawable.music_list_play_button);
        } else if (MusicPlayerService.getPlayMode() == MusicPlayerService.PlayModeType.PLAY_TYPE_RANDOM) {
            MusicPlayerService.setPlayMode(MusicPlayerService.PlayModeType.PLAY_TYPE_SINGLE_CYCLE);
            mPlayModeView.setBackgroundResource(R.drawable.music_single_play_button);
        } else {
            MusicPlayerService.setPlayMode(MusicPlayerService.PlayModeType.PLAY_TYPE_LIST_CYCLE);
            mPlayModeView.setBackgroundResource(R.drawable.music_circle_play_button);
        }
    }

    public LinearLayout getMainLayout() {
        return mLinearLayout;
    }

    private void initDots() {
        LinearLayout linearLayout = findViewById(R.id.song_ll_carousel_dots);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(20, 20);
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < 2; i++) {
            ImageView imageView = new ImageView(this);
            if (i == 0) {
                imageView.setBackgroundResource(R.drawable.dot_chosen);
            } else {
                imageView.setBackgroundResource(R.drawable.dot_unchosen);
            }
            mDotIvArray.add(imageView);
            linearLayout.addView(mDotIvArray.get(i), layoutParams);
        }
    }

    /**
     * 开始更新进度条
     */
    public void startUpdateProgressBar(int delay) {
        if (!mIsUpdatingBar) {
            mIsUpdatingBar = true;
            if (mTimer == null) {
                mTimer = new Timer();
                mTask = new ProgressBarTask();
                mTimer.scheduleAtFixedRate(mTask, delay, 100);
            }
        }
    }

    /**
     * 停止展示歌词
     */
    public void stopUpdateProgressBar() {
        if (mIsUpdatingBar) {
            mIsUpdatingBar = false;
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShouldStopUpdateUi = false;
        if (updatePicAndLyricAndUi()) {
            getMusicDuration();
        }
        if (!mIsUpdatingBar && MusicPlayerService.isPlaying) {
            startUpdateProgressBar(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShouldStopUpdateUi = true;
        //停止定时任务
        if (mIsUpdatingBar) {
            stopUpdateProgressBar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateProgressBar();
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mReceiver);
        sInstance = null;
    }

    /**
     * Android 音乐播放器应用里，读出的音乐时长为 long 类型以毫秒数为单位
     * 例如：将 234736 转化为分钟和秒应为 03:55 （包含四舍五入）
     *
     * @param duration 音乐时长
     * @return
     */
    public static String timeParse(long duration) {
        String s = null;
        try {
            s = DateUtil.longToString(duration, "mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    private void playNextMusic() {
        stopUpdateProgressBar();
        ((LyricFragment) mPlayFragmentList.get(1)).stopLrcPlay();
        Log.d(TAG, "playNextMusic");
        MusicPlayerService.playNextMusic();
    }

    private void playPreviousMusic() {
        stopUpdateProgressBar();
        ((LyricFragment) mPlayFragmentList.get(1)).stopLrcPlay();
        MusicPlayerService.playPreviousMusic();
        Log.d(TAG, "playPreviousMusic");
    }

    private boolean updatePicAndLyricAndUi() {
        MusicInfo musicInfo = MusicPlayerService.getMusicInfo();
        if (!TextUtils.equals(musicInfo.getId(), mMusicInfo.getId())) {
            mMusicInfo = musicInfo;
            Artist artist = new Artist(0, mMusicInfo.getSinger());
            List<Artist> artistList = new ArrayList<>();
            artistList.add(artist);
            mMvMusicInfo = new MvMusicInfo(Integer.valueOf(mMusicInfo.getId()),
                    mMusicInfo.getMusicSongName(), "", artistList);
            ((AlbumFragment) mPlayFragmentList.get(0)).initPicture();
            ((LyricFragment) mPlayFragmentList.get(1)).initLyric();
            mPagerAdapter.notifyDataSetChanged();
            updateMainUi();
            return true;
        }
        return false;
    }

    private void updateMainUi() {
        mNameView.setText(mMvMusicInfo.getName());
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mMvMusicInfo.getArtistList();
        if (mvArtistList != null) {
            for (int j = 0; j < mvArtistList.size(); j++) {
                if (j == mvArtistList.size() - 1) {
                    artistString.append(mvArtistList.get(j).getName());
                } else {
                    artistString.append(mvArtistList.get(j).getName() + "/");
                }
            }
            mArtistNameView.setText(artistString);
        }
        initCollectButton();
    }

    private void collectMusic() {
        MusicInfo musicInfo = MusicPlayerService.getMusicInfo();
        if (musicInfo == null) {
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(FAVOURITE_URL,
                            null, "id = ?", new String[]{musicInfo.getId()}, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        ContentValues values = new ContentValues();
                        values.put("id", musicInfo.getId());
                        values.put("name", musicInfo.getMusicSongName());
                        values.put("singer", musicInfo.getSinger());
                        values.put("path", musicInfo.getMusicFilePath());
                        MusicApplication.getContext().getContentResolver().insert(FAVOURITE_URL, values);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCollectView.setBackgroundResource(R.drawable.music_collected_button);
                            }
                        });
                    } else {
                        MusicApplication.getContext().getContentResolver().delete(FAVOURITE_URL,
                                "id = ?", new String[]{musicInfo.getId()});
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCollectView.setBackgroundResource(R.drawable.music_collect_button);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    private void downloadMusic() {
        Intent intent = new Intent(this, MusicDownloaderService.class);
        MusicInfo musicInfo = MusicPlayerService.getMusicInfo();
        musicInfo.setUrl("");
        intent.putExtra("music", musicInfo);
        startService(intent);
    }

    private void initCollectButton() {
        MusicInfo musicInfo = MusicPlayerService.getMusicInfo();
        if (musicInfo == null) {
            return;
        }
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(FAVOURITE_URL,
                            null, "id = ?", new String[]{musicInfo.getId()}, null);
                    if (cursor == null || (cursor.getCount() == 0)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCollectView.setBackgroundResource(R.drawable.music_collect_button);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCollectView.setBackgroundResource(R.drawable.music_collected_button);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    //更改默认退出动画
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
    }
}
