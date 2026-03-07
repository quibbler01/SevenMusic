package com.quibbler.sevenmusic.fragment.found;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.LockScreenActivity;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.bean.song.impl.DefaultLrcBuilder;
import com.quibbler.sevenmusic.bean.song.impl.LrcRow;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.ThreadDispatcher;
import com.quibbler.sevenmusic.view.song.impl.LrcView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LockScreenLyricFragment extends Fragment {
    private View mView;

    private LrcView mLrcView;  //自定义歌词view

    //更新歌词的频率，每0.5秒更新一次
    private int mPlayTimerDuration = 500;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;

    public static Fragment newInstance(boolean isPlaying) {
        LockScreenLyricFragment fragment = new LockScreenLyricFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("state", isPlaying);
        fragment.setArguments(bundle);
        return fragment;
    }

    public LockScreenLyricFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_lock_screen_lyric, container, false);
        if (getArguments().getBoolean("state")) {
            startLrcPlay();
        }
        initView();
        initLyric();
        return mView;
    }

    private void initView() {
        mLrcView = mView.findViewById(R.id.fragment_lock_lyric_view);
        mLrcView.setCanScroll(false);
    }

    public void initLyric() {
        //重新请求歌词并展示
        setMusicLyricOnline(((LockScreenActivity) getActivity()).getMvMusicInfo());
    }
    //请求歌词再展示
    public void setMusicLyricOnline (MvMusicInfo mvMusicInfo){
        MusicPresnter.getMusicLyric(mvMusicInfo, new MusicCallBack() {
            @Override
            public void onMusicInfoCompleted() {
                //解析歌词返回LrcRow集合
                List<LrcRow> rows = new DefaultLrcBuilder().getLrcRows(mvMusicInfo.getLyric());
                ThreadDispatcher.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLrcView.setLrc(rows);
                    }
                });
            }
        });
    }

    /**
     * 开始展示歌词
     */
    public void startLrcPlay() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTask = new LrcTask();
            mTimer.scheduleAtFixedRate(mTask, 0, mPlayTimerDuration);
        }
    }
    //更新歌词
    public void updateLrc(){
        if(getActivity() != null){
            final long timePassed = ((LockScreenActivity)getActivity()).getProgress();
            mLrcView.seekLrcToTime(timePassed, true);
        }
    }

    //更新歌词至第一句
    public void updateLrcTobegin(){
        mLrcView.seekLrcToTime(0, true);
    }
    /**
     * 停止展示歌词
     */
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
    /**
     * 展示歌词的定时任务
     */
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            if(getActivity() != null){
                final long timePassed = MusicPlayerService.getPlayProgress();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        //滚动歌词
                        mLrcView.seekLrcToTime(timePassed, false);
                    }
                });
            }
        }
    }
}
