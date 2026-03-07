package com.quibbler.sevenmusic.fragment.song;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.bean.song.impl.DefaultLrcBuilder;
import com.quibbler.sevenmusic.bean.song.impl.LrcRow;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.view.song.ILrcView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
  *
  * Package:        com.quibbler.sevenmusic.fragment.song
  * ClassName:      LyricFragment
  * Description:    播放界面歌词fragment
  * Author:         lishijun
  * CreateDate:     2019/9/27 19:48
 */
public class LyricFragment extends Fragment {

    private static final String TAG = "LyricFragment";

    private static final String SERVER = "http://114.116.128.229:3000";

    private static final String MUSIC_LYRIC_URL = "/lyric?id=";

    private View mView;

    private ILrcView mLrcView;  //自定义歌词view

    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 500;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;

    private boolean mIsUpdatingLrc = false;

    public static Fragment newInstance(boolean isPlaying){
        LyricFragment fragment = new LyricFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("state", isPlaying);
        fragment.setArguments(bundle);
        return fragment;
    }

    public LyricFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_lyric, container, false);
        initView();
        initLyric();
        updateLrc();
        if(getArguments().getBoolean("state")){
            startLrcPlay(0);
        }
        return mView;
    }

    private void initView() {
        mLrcView = mView.findViewById(R.id.music_lyric_view);
    }

    public void initLyric(){
        String lyricStr = ((MusicPlayActivity)getActivity()).getMvMusicInfo().getLyric();
        if(TextUtils.isEmpty(lyricStr)){
            //重新请求歌词并展示
            setMusicLyricOnline(((MusicPlayActivity)getActivity()).getMvMusicInfo());
        }
        else{
            //解析歌词返回LrcRow集合
            List<LrcRow> rows = new DefaultLrcBuilder().getLrcRows(lyricStr);
            mLrcView.setLrc(rows);
        }
    }

    //请求歌词再展示
    public void setMusicLyricOnline(MvMusicInfo mvMusicInfo){
        MusicPresnter.getMusicLyric(mvMusicInfo, new MusicCallBack() {
            @Override
            public void onMusicInfoCompleted() {
                //解析歌词返回LrcRow集合
                List<LrcRow> rows = new DefaultLrcBuilder().getLrcRows(mvMusicInfo.getLyric());
                if(getActivity() != null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLrcView.setLrc(rows);
                        }
                    });
                }
            }
        });
    }

    /**
     * 开始展示歌词
     */
    public void startLrcPlay(int delay) {
        mIsUpdatingLrc = true;
        if (mTimer == null) {
            mTimer = new Timer();
            mTask = new LrcTask();
            mTimer.scheduleAtFixedRate(mTask, delay, mPalyTimerDuration);
        }
    }
    //更新歌词
    public void updateLrc(){
        long timePassed = MusicPlayerService.getPlayProgress();
        if(timePassed == -1){
            mLrcView.seekLrcToTime(0, true);
        }else {
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
        mIsUpdatingLrc = false;
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mIsUpdatingLrc && MusicPlayerService.isPlaying){
            startLrcPlay(0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //停止定时任务
        if(mIsUpdatingLrc){
            stopLrcPlay();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLrcPlay();
    }

    /**
     * 展示歌词的定时任务
     */
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            MusicPlayActivity musicPlayActivity = (MusicPlayActivity)getActivity();
            if(musicPlayActivity != null && !mLrcView.isScrolling()){
                final long timePassed = musicPlayActivity.getProgress();
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
