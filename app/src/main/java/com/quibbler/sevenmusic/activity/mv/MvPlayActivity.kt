package com.quibbler.sevenmusic.activity.mv;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvComment;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.presenter.MusicPresnter;
import com.quibbler.sevenmusic.presenter.MvPresenter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.DateUtil;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.ICallback;
import com.quibbler.sevenmusic.utils.NetUtil;
import com.quibbler.sevenmusic.utils.PageEffectUtil;
import com.quibbler.sevenmusic.view.mv.IMediaController;
import com.quibbler.sevenmusic.view.mv.MediaControlListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
  *
  * Package:        com.quibbler.sevenmusic.activity
  * ClassName:      MvPlayActivity
  * Description:    点击mv跳转到此页播放
  * Author:         lishijun
  * CreateDate:     2019/9/20 11:36
 */
public class MvPlayActivity extends Activity {

    private static final String TAG = "MvPlayActivity";

    private static final String SERVER = "http://114.116.128.229:3000";

    private static final String SIMILAR_URL = "/simi/mv?mvid=";

    private static final String ARTIST_SONG_URL = "/artists?id=";

    private static final String MV_COMMENT_URL = "/comment/mv?id=";

    private static final String MUSIC_CANUSE_URL = "/check/music?id=";

    private static final String MV_URL = "/mv/url?id=";

    private static final int SIMILAR_SONG_NUMBER = 3;

    private static final String MV_DETAIL_URL = "/mv/detail?mvid=";

    private LinearLayout mVideoLayout;

    private VideoView mVideoView;

    private IMediaController mMediaController;

    private MvInfo mMvInfo;

    private List<MvInfo> mSimilarVideoInfoList = new ArrayList<>();

    private List<MvMusicInfo> mSimilarSongList = new ArrayList<>();

    private List<MvComment> mMvCommentList = new ArrayList<>();

    //用于进入后台时videoview的状态保存
    private int mSeekPosition = 0;

    private boolean mIsFullScreen = false;

    private int mVideoHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransparentToolbar();
        setContentView(R.layout.activity_mv_play);
        if(NetUtil.getNetWorkStart(this) == NetUtil.NETWORK_NONE){
            showNoNetPage();
        }else{
            hideNoNetPage();
            initView();
        }
    }

    private void initView(){
        Intent intent = getIntent();
        mMvInfo = (MvInfo) intent.getSerializableExtra("mvInfo");
        mVideoLayout = findViewById(R.id.mv_video_layout);
        mVideoView = findViewById(R.id.mv_vv_video);
        mMediaController = new IMediaController(MvPlayActivity.this);
        mVideoView.setMediaController(mMediaController);
        mMediaController.setControlListener(new MediaControlListener() {
            @Override
            public void actionForFullScreen() {
                if(!mIsFullScreen){
                    changeToFullScreen();
                }
                else{
                    exitFullScreen();
                }
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
                            mVideoView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        return true;
                    }
                });
            }
        });
        ViewTreeObserver vto = mVideoView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mVideoHeight = mVideoView.getHeight();
                mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mVideoHeight));
            }
        });
        ScrollView scrollView = findViewById(R.id.mv_sv_relative);
        PageEffectUtil.setScrollViewSpringback(scrollView);
        showMvPicture(mMvInfo);
        MvPresenter.getMvInfo(mMvInfo, new MusicCallBack() {
            @Override
            public void onMusicInfoCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!TextUtils.isEmpty(mMvInfo.getUrl())){
                            mVideoView.setVideoPath(mMvInfo.getUrl());
                        }
                        TextView nameView = findViewById(R.id.mv_tv_palyname);
                        TextView palyCountView = findViewById(R.id.mv_tv_count);
                        TextView descriptionView = findViewById(R.id.mv_tv_description);
                        nameView.setText(mMvInfo.getName());
                        palyCountView.setText(mMvInfo.getPlayCount() + "次播放");
                        descriptionView.setText(mMvInfo.getCopyWriter());
                        //折叠按钮的监听事件
                        ImageButton collapseButton = findViewById(R.id.mv_btn_collapse);
                        collapseButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(descriptionView.getVisibility() == View.GONE){
                                    descriptionView.setVisibility(View.VISIBLE);
                                    collapseButton.setBackgroundResource(R.drawable.mv_btn_collapse_ing);
                                }else{
                                    descriptionView.setVisibility(View.GONE);
                                    collapseButton.setBackgroundResource(R.drawable.mv_btn_collapse);
                                }
                            }
                        });
                        //获取相似歌曲
                        if(mMvInfo.getArtists().size() > 0){
                            getSimilarSong(mMvInfo.getArtists().get(0).getId());
                        }
                    }
                });
            }
        });
        //获取相似mv
        getSimilarMv(mMvInfo.getId());
        getMvComment(mMvInfo.getId());
    }

    private void setTransparentToolbar() {
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //设置让应用主题内容占据状态栏和导航栏
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏和导航栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void changeToFullScreen(){
        mIsFullScreen = true;
        mVideoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置activity横屏
        getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);//隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void exitFullScreen(){
        mIsFullScreen = false;
        mVideoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mVideoHeight));
        mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mVideoHeight));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置activity竖屏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//显示状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTransparentToolbar();
    }
    //无网络，展示提示页
    private void showNoNetPage(){
        findViewById(R.id.mv_nonet_tip).setVisibility(View.VISIBLE);
    }
    //有网络，关闭提示页
    private void hideNoNetPage(){
        findViewById(R.id.mv_nonet_tip).setVisibility(View.GONE);
    }
    //显示mv的缩略图
    private void showMvPicture(MvInfo mvInfo){
        Glide.with(this)
                .load(mvInfo.getPictureUrl())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        mVideoView.setBackground(resource);
                    }
                });
    }

    private void getSimilarMv(int mvId){
        HttpUtil.sendHttpRequest(SERVER + SIMILAR_URL + mvId, new ICallback() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray("mvs");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject mvObject = jsonArray.getJSONObject(i);
                        int mvId = mvObject.getInt("id");
                        String name = mvObject.getString("name");
                        String copyWriter = mvObject.getString("briefDesc");
                        String pictureUrl = mvObject.getString("cover");
                        int playCount = mvObject.getInt("playCount");
                        JSONArray artistArray = mvObject.getJSONArray("artists");
                        List<Artist> artistList = new ArrayList<>();
                        for(int j = 0; j < artistArray.length(); j++){
                            int artistId = artistArray.getJSONObject(j).getInt("id");
                            String artistName = artistArray.getJSONObject(j).getString("name");
                            artistList.add(new Artist(artistId, artistName));
                        }
                        MvInfo mvInfo = new MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl);
                        mSimilarVideoInfoList.add(mvInfo);
                        //在主线程更新
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSimilarMv(mvInfo);
                            }
                        });
                    }
                    //按顺序获取video的url
                    if(mSimilarVideoInfoList.size() > 0){
                        MvPresenter.getMvUrlList(mSimilarVideoInfoList);
                    }
                } catch (Exception e) {
                    Log.d(TAG,"mvPath:get error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "mvList:get error");
            }
        });
    }

    //获取相似的MV
//    private void getSimilarMv(int mvId){
//        HttpUtil.sendOkHttpRequest(SERVER + SIMILAR_URL + mvId, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) {
//                if(response.body() == null){
//                    return;
//                }
//                try {
//                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("mvs");
//                    for(int i = 0; i < jsonArray.length(); i++){
//                        JSONObject mvObject = jsonArray.getJSONObject(i);
//                        int mvId = mvObject.getInt("id");
//                        String name = mvObject.getString("name");
//                        String copyWriter = mvObject.getString("briefDesc");
//                        String pictureUrl = mvObject.getString("cover");
//                        int playCount = mvObject.getInt("playCount");
//                        JSONArray artistArray = mvObject.getJSONArray("artists");
//                        List<Artist> artistList = new ArrayList<>();
//                        for(int j = 0; j < artistArray.length(); j++){
//                            int artistId = artistArray.getJSONObject(j).getInt("id");
//                            String artistName = artistArray.getJSONObject(j).getString("name");
//                            artistList.add(new Artist(artistId, artistName));
//                        }
//                        MvInfo mvInfo = new MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl);
//                        mSimilarVideoInfoList.add(mvInfo);
//                        //在主线程更新
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showSimilarMv(mvInfo);
//                            }
//                        });
//                    }
//                    //按顺序获取video的url
//                    if(mSimilarVideoInfoList.size() > 0){
//                        MvPresenter.getMvUrlList(mSimilarVideoInfoList);
//                    }
//                } catch (Exception e) {
//                    Log.d("mvPath", "出错");
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("mvList", "获取失败");
//            }
//        });
//    }

    //显示相似的mv
    private void showSimilarMv(MvInfo mvInfo){
        LinearLayout fatherView = findViewById(R.id.mv_similar_video);
        View view = LayoutInflater.from(this).inflate(R.layout.similar_mv_item,
                null, false);
        ImageView videoView = view.findViewById(R.id.mv_iv_picture);
        TextView videoDescView = view.findViewById(R.id.mv_tv_description);
        if(!TextUtils.equals("", mvInfo.getCopyWriter()) && !TextUtils.equals("null", mvInfo.getCopyWriter())){
            videoDescView.setText(mvInfo.getName() + "，" + mvInfo.getCopyWriter());
        }else{
            videoDescView.setText(mvInfo.getName());
        }
        fatherView.addView(view);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转进入播放activity
                if(mvInfo.getUrl() != null){
                    ActivityStart.startMvPlayActivity(MvPlayActivity.this, mvInfo);
                }
            }
        });
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        //先用glide加载
        if(!this.isFinishing()){
            Glide.with(this)
                    .load(mvInfo.getPictureUrl())
                    .apply(options)
                    .into(videoView);
        }
    }

    //根据歌手id获取相似歌曲
    private void getSimilarSong(int id){
        HttpUtil.sendOkHttpRequest(SERVER + ARTIST_SONG_URL + id, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.body() == null){
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("hotSongs");
                    //如果没有，直接返回
                    if(jsonArray.length() == 0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                removeSimilarText();
                            }
                        });
                        return;
                    }
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject songObject = jsonArray.getJSONObject(i);
                        int songId = songObject.getInt("id");
                        String name = songObject.getString("name");
                        JSONArray artistArray = songObject.getJSONArray("ar");
                        List<Artist> artistList = new ArrayList<>();
                        for(int j = 0; j < artistArray.length(); j++){
                            int artistId = artistArray.getJSONObject(j).getInt("id");
                            String artistName = artistArray.getJSONObject(j).getString("name");
                            artistList.add(new Artist(artistId, artistName));
                        }
                        String pictureUrl = songObject.getJSONObject("al").getString("picUrl");
                        MvMusicInfo mvMusicInfo = new MvMusicInfo(songId, name, pictureUrl, artistList);
                        mSimilarSongList.add(mvMusicInfo);
                        //显示
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSimilarSong(mvMusicInfo);
                            }
                        });
                        if(mSimilarSongList.size() >= SIMILAR_SONG_NUMBER){
                            break;
                        }
                    }
                    //获取歌曲的可用性
                    MusicPresnter.getMusicCanUse(mSimilarSongList);
                } catch (Exception e) {
                    Log.d("mvPath", "出错");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("songList", "获取失败");
            }
        });
    }

    //移除相关歌曲这个view
    private void removeSimilarText(){
        LinearLayout fatherView = findViewById(R.id.mv_similar_song);
        fatherView.removeAllViews();
    }

    //显示某个相关歌曲
    private void showSimilarSong(MvMusicInfo mvMusicInfo){
        LinearLayout fatherView = findViewById(R.id.mv_similar_song);
        View view = LayoutInflater.from(this).inflate(R.layout.similar_song_item,
                    null, false);
        ImageView albumView = view.findViewById(R.id.mv_iv_albumpic);
        TextView songNameView = view.findViewById(R.id.mv_tv_song_name);
        TextView artistNameView = view.findViewById(R.id.mv_tv_artist_name);
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        //先用glide加载
        if(!this.isFinishing()){
            Glide.with(this)
                    .load(mvMusicInfo.getPictureUrl())
                    .apply(options)
                    .into(albumView);
        }
        songNameView.setText(mvMusicInfo.getName());
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mvMusicInfo.getArtistList();
        if(mvArtistList != null){
            for(int j = 0; j < mvArtistList.size(); j++){
                if(j == mvArtistList.size() - 1){
                    artistString.append(mvArtistList.get(j).getName());
                }else{
                    artistString.append(mvArtistList.get(j).getName() + "/");
                }
            }
            artistNameView.setText(artistString + "-" + mvMusicInfo.getName());
        }
        fatherView.addView(view);
        //点击播放响应
        ImageButton playButton = view.findViewById(R.id.mv_iv_music_play);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mvMusicInfo.isCanUse()){
                    if(MusicPlayerService.isPlaying && MusicPlayerService.getMusicInfo() != null
                            && TextUtils.equals(mvMusicInfo.getId() + "",
                            MusicPlayerService.getMusicInfo().getId())){
                        ActivityStart.startMusicPlayActivity(MvPlayActivity.this, mvMusicInfo);
                    }else{
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setId(String.valueOf(mvMusicInfo.getId()));
                        musicInfo.setMusicSongName(mvMusicInfo.getName());
                        if(mvMusicInfo.getArtistList().size() > 0){
                            musicInfo.setSinger(mvMusicInfo.getArtistList().get(0).getName());
                        }
                        MusicPlayerService.playMusic(musicInfo);
                    }
                }
                else{
                    Toast.makeText(MvPlayActivity.this, "暂无版权！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        playButton.setOnClickListener(clickListener);
        songNameView.setOnClickListener(clickListener);
        artistNameView.setOnClickListener(clickListener);
    }

    private void getMvComment(int mvId){
        HttpUtil.sendOkHttpRequest(SERVER + MV_COMMENT_URL + mvId, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.body() == null){
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("hotComments");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject commentObject = jsonArray.getJSONObject(i);
                        int mvId = commentObject.getInt("commentId");
                        String username = commentObject.getJSONObject("user").getString("nickname");
                        String userheadUrl = commentObject.getJSONObject("user").getString("avatarUrl");
                        String content = commentObject.getString("content");
                        long date = commentObject.getLong("time");
                        int likeCount = commentObject.getInt("likedCount");
                        MvComment mvComment = new MvComment(mvId, content, username, userheadUrl, date, likeCount);
                        mMvCommentList.add(mvComment);
                        //在主线程更新
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMvComment(mvComment);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d("mvPath", "出错");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("mvList", "获取失败");
            }
        });
    }

    //显示某个评论
    private void showMvComment(MvComment mvComment){
        LinearLayout fatherView = findViewById(R.id.mv_comment);
        View view = LayoutInflater.from(this).inflate(R.layout.mv_comment_item,
                null, false);
        ImageView userheadView = view.findViewById(R.id.mv_comment_user_picture);
        TextView userameView = view.findViewById(R.id.mv_comment_username);
        TextView timeView = view.findViewById(R.id.mv_comment_time);
        TextView contentView = view.findViewById(R.id.mv_comment_body);
        TextView likeCountView = view.findViewById(R.id.mv_comment_likecount);
        //先用glide加载
        if (this != null) {
            Glide.with(this)
                    .load(mvComment.getUerHeadUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(userheadView);
        }
        userameView.setText(mvComment.getUserName());
        try {
            String time = DateUtil.longToString(mvComment.getDate(), "yyyy-MM-dd HH:mm:ss");
            timeView.setText(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        contentView.setText(mvComment.getContent());
        likeCountView.setText(mvComment.getLikeCount() + "");
        fatherView.addView(view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVideoView != null){
            mSeekPosition = mVideoView.getCurrentPosition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recoveryMvState();
    }

    private void recoveryMvState() {
        MediaMetadataRetriever metadataRetriever = null;
        try {
            if (mVideoView != null && mSeekPosition > 0 && !mVideoView.isPlaying()) {
                if (mMvInfo.getUrl() != null) {
                    metadataRetriever = new MediaMetadataRetriever();
                    //mPath视频地址
                    metadataRetriever.setDataSource(mMvInfo.getUrl(), new HashMap<>());
                    //获取当前视频某一时刻(毫秒*1000)的一帧
                    Bitmap bitmap = metadataRetriever.getFrameAtTime(mSeekPosition * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST);
                    mVideoView.setBackground(new BitmapDrawable(bitmap));
                }
                mVideoView.seekTo(mSeekPosition);
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG,"recoveryMvState:"+e);
        } finally {
            if (metadataRetriever != null) {
                try {
                    metadataRetriever.release();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
