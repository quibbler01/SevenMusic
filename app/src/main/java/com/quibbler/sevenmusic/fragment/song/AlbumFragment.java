package com.quibbler.sevenmusic.fragment.song;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.song.MusicPlayActivity;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.BlurTransformation;
import com.quibbler.sevenmusic.utils.ColorFilterTransformation;

/**
  *
  * Package:        com.quibbler.sevenmusic.fragment.song
  * ClassName:      AlbumFragment
  * Description:    播放界面专辑页面fragment
  * Author:         lishijun
  * CreateDate:     2019/9/27 19:48
 */
public class AlbumFragment extends Fragment {

    private View mView;

    //歌曲信息
    private MvMusicInfo mMvMusicInfo;

    private ObjectAnimator mAlbumAnimator;

    private ObjectAnimator mPlayAnimator;

    private ObjectAnimator mPauseAnimator;

    private ImageView mMusicPictureView;

    //留声机view
    private ImageView mGramoView;

    private boolean mIsPlayingAlbumAnim = false;

    private boolean mGramoViewStoped = true;

    public static Fragment newInstance(boolean isPlaying){
        AlbumFragment fragment = new AlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("state", isPlaying);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_album, container, false);
        initView();
        initPicture();
        initAnimator();
        if(getArguments().getBoolean("state")){
            startPlayAnimator();
            startAlbumAnimator();
        }
        return mView;
    }

    public void initPicture(){
        mMvMusicInfo = ((MusicPlayActivity)getActivity()).getMvMusicInfo();
        if(TextUtils.isEmpty(mMvMusicInfo.getPictureUrl())){
            MusicPresnter.getMusicPicture(mMvMusicInfo, new MusicCallBack() {
                @Override
                public void onMusicInfoCompleted() {
                    if(TextUtils.isEmpty(mMvMusicInfo.getPictureUrl())){
                        return;
                    }
                    if(getActivity() != null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initPictureByGlide();
                            }
                        });
                    }
                }
            });
        }else{
            initPictureByGlide();
        }
    }

    private void initPictureByGlide(){
        //先用glide加载
        Glide.with(AlbumFragment.this)
                .load(mMvMusicInfo.getPictureUrl())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(mMusicPictureView);
        Glide.with(AlbumFragment.this)
                .load(mMvMusicInfo.getPictureUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(22,35)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        if(getActivity() != null){
                            LinearLayout linearLayout = ((MusicPlayActivity)getActivity()).getMainLayout();
                            linearLayout.setBackground(resource);
                        }
                    }
                });
    }

    private void initView(){
        mMusicPictureView = mView.findViewById(R.id.music_iv_picture);
        mGramoView = mView.findViewById(R.id.music_iv_gramo);
    }

    //初始化旋转动画
    private void initAnimator() {
        //专辑旋转动画
        FrameLayout frameLayout = mView.findViewById(R.id.music_fl_album);
        mAlbumAnimator = ObjectAnimator.ofFloat(frameLayout, "rotation", 0.0f, 360.0f);
        mAlbumAnimator.setDuration(50000);//设定转一圈的时间
        mAlbumAnimator.setRepeatCount(Animation.INFINITE);//设定无限循环
        mAlbumAnimator.setRepeatMode(ObjectAnimator.RESTART);// 循环模式
        mAlbumAnimator.setInterpolator(new LinearInterpolator());// 匀速

        //留声机开始动画
        mPlayAnimator = ObjectAnimator.ofFloat(mGramoView, "rotation", 0.0f, 20.0f);
        mPlayAnimator.setDuration(400);//设定转一圈的时间
        mPlayAnimator.setInterpolator(new AccelerateInterpolator());// 加速

        //留声机结束动画
        mPauseAnimator = ObjectAnimator.ofFloat(mGramoView, "rotation", 20.0f, 0f);
        mPauseAnimator.setDuration(400);//设定转一圈的时间
        mPauseAnimator.setInterpolator(new AccelerateInterpolator());// 加速
        mGramoView.setPivotX(55);
        mGramoView.setPivotY(90);
        mGramoView.bringToFront();
    }

    public void startAlbumAnimator(){
        mIsPlayingAlbumAnim = true;
        if(mAlbumAnimator.getCurrentPlayTime() != 0){
            mAlbumAnimator.resume();
        }else{
            mAlbumAnimator.start();
        }
    }

    public void stopAlbumAnimator(){
        mIsPlayingAlbumAnim = false;
        mAlbumAnimator.cancel();
        mAlbumAnimator.setCurrentPlayTime(0);
    }

    public void resumeAlbumAnimator(){
        mAlbumAnimator.resume();
    }

    public void pauseAlbumAnimator(){
        mAlbumAnimator.pause();
    }

    public void startPlayAnimator(){
        long playBeginTime = mPauseAnimator.getCurrentPlayTime();
        mPlayAnimator.setCurrentPlayTime(playBeginTime);
        mPlayAnimator.start();
        mGramoViewStoped = false;
    }

    public boolean isPlayAnimatorStoped(){
        return mPlayAnimator.getDuration() == mPlayAnimator.getCurrentPlayTime();
    }

    public void stopPlayAnimator(){
        mPlayAnimator.cancel();
        mPlayAnimator.setCurrentPlayTime(0);
    }

    public void startPauseAnimator(){
        long playBeginTime = mPlayAnimator.getCurrentPlayTime();
        mPauseAnimator.setCurrentPlayTime(playBeginTime);
        mPauseAnimator.start();
        mGramoViewStoped = true;
    }

    public void stopPauseAnimator(){
        mPauseAnimator.cancel();
        mPauseAnimator.setCurrentPlayTime(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        //需要停止循环动画
        if(mIsPlayingAlbumAnim){
            stopAlbumAnimator();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //开启循环动画
        if(!mIsPlayingAlbumAnim && MusicPlayerService.isPlaying){
            startAlbumAnimator();
            if(mGramoViewStoped){
                startPlayAnimator();
            }
        }
    }
}
