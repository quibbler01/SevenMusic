package com.quibbler.sevenmusic.fragment.mv;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.callback.MvCollectCallback;
import com.quibbler.sevenmusic.presenter.MvPresenter;
import com.quibbler.sevenmusic.service.MvDownloadService;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.PageEffectUtil;
import com.quibbler.sevenmusic.utils.TextureVideoViewOutlineProvider;
import com.quibbler.sevenmusic.view.mv.IMediaController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Package:        com.quibbler.sevenmusic.fragment.mv
 * ClassName:      ChildMvFragment
 * Description:    mv模块下推荐fragment
 * Author:         lishijun
 * CreateDate:     2019/9/24 17:22
 */
public class ChildMvFragment extends Fragment {

    private static final String TAG = "ChildMvFragment";

    private static final String SERVER = "http://114.116.128.229:3000";

    private static final String MV_ARTIST = "/artist/mv?id=6452";

    private static final String MV_RECOMMEND = "/personalized/mv";

    private static final String MV_DETAIL_URL = "/mv/detail?mvid=";

    private List<MvInfo> mVideoInfoList = new ArrayList<>();

    private View mView;

    private List<VideoView> mVideoViewList = new ArrayList<>();

    private List<IMediaController> mMediaControllerList = new ArrayList<>();

    private List<Integer> mVideoProgressList = new ArrayList<>();

    //弹出的下载收藏等popview
    private PopupWindow mDownloadPopWindow;

    private MvDownloadService.DownLoadBinder mDownLoadBinder;

    ScrollView mScrollView;
    private IMediaController mediaController;


    private ServiceConnection mDownloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownLoadBinder = (MvDownloadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载fragment_friend布局文件
        //LinearLayout layout = R.layout.fragment_child_mv;
        mView = inflater.inflate(R.layout.fragment_child_mv, null);
        mScrollView = mView.findViewById(R.id.mv_sv_mvs);
        mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //滑动时隐藏播放条
                hideAllMediaController();
            }
        });
        PageEffectUtil.setScrollViewSpringback(mScrollView);
        getMvInfoListFromRecommend();
        Intent intent = new Intent(MusicApplication.getContext(), MvDownloadService.class);
        MusicApplication.getContext().startService(intent);
        MusicApplication.getContext().bindService(intent, mDownloadConnection, BIND_AUTO_CREATE);
        return mView;
    }

    public void hideAllMediaController() {
        for (MediaController mediaController : mMediaControllerList) {
            mediaController.hide();
        }
    }

    private void getMvInfoListFromRecommend() {
        HttpUtil.sendOkHttpRequest(SERVER + MV_RECOMMEND, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.body() == null) {
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("result");
                    Log.d(TAG, "json length is : " + jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject mvObject = jsonArray.getJSONObject(i);
                        int mvId = mvObject.getInt("id");
                        String name = mvObject.getString("name");
                        String copyWriter = mvObject.getString("copywriter");
                        String pictureUrl = mvObject.getString("picUrl");
                        int playCount = mvObject.getInt("playCount");
                        JSONArray artistArray = mvObject.getJSONArray("artists");
                        List<Artist> artistList = new ArrayList<>();
                        for (int j = 0; j < artistArray.length(); j++) {
                            int artistId = artistArray.getJSONObject(j).getInt("id");
                            String artistName = artistArray.getJSONObject(j).getString("name");
                            artistList.add(new Artist(artistId, artistName));
                        }
                        MvInfo mvInfo = new MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl);
                        mVideoInfoList.add(mvInfo);
                        Log.d(TAG, "json" + i);
                    }
                    //在主线程更新
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMvPicture();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "exception is" + e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("mvList", "获取失败");
            }
        });
    }

    private void showMvPicture() {
        for (MvInfo mvInfo : mVideoInfoList) {
            showMvPicture(mvInfo);
        }
    }

    //显示第i个mv的缩略图
    private void showMvPicture(MvInfo mvInfo) {
        Log.d(TAG, "mv");
        if (mvInfo == null) {
            return;
        }
        LinearLayout mvView = mView.findViewById(R.id.mv_sv);
        View view = LayoutInflater.from(MusicApplication.getContext()).inflate(R.layout.mv_item,
                mvView, false);
        VideoView videoView = view.findViewById(R.id.mv_video);
        TextView videoNameView = view.findViewById(R.id.mv_tv_name);
        TextView videoArtistView = view.findViewById(R.id.mv_tv_artist);
        ImageView artistHeadView = view.findViewById(R.id.mv_iv_artist_head);
        mVideoViewList.add(videoView);
        MvPresenter.getMvInfo(mvInfo, new MusicCallBack() {
            @Override
            public void onMusicInfoCompleted() {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoView.setVideoPath(mvInfo.getUrl());
                            mScrollView.scrollTo(0, 0);
                        }
                    });
                }
            }
        });
        videoNameView.setText(mvInfo.getName());
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mvInfo.getArtists();
        if (mvArtistList != null) {
            for (int i = 0; i < mvArtistList.size(); i++) {
                if (i == mvArtistList.size() - 1) {
                    artistString.append(mvArtistList.get(i).getName());
                } else {
                    artistString.append(mvArtistList.get(i).getName() + "/");
                }
            }
            videoArtistView.setText(artistString);
        }
        mvView.addView(view);

        if (getActivity() != null) { // 增加判空处理，防止夜间模式切换导致空指针异常
            mediaController = new IMediaController(getActivity());
        } else {
            return;
        }
        videoView.setMediaController(mediaController);
        mMediaControllerList.add(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            // 暂停其他的video
                            for (int i = 0; i < mVideoViewList.size(); i++) {
                                if (mVideoViewList.get(i) != videoView && videoView.isPlaying()) {
                                    mVideoViewList.get(i).pause();
                                    mMediaControllerList.get(i).hide();
                                }
                            }
                            videoView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        return true;
                    }
                });
            }
        });

        videoNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo);
                }
            }
        });
        //注册MV的收藏、下载等
        dealMV(mvInfo, view);
        //设置圆角
        videoView.setOutlineProvider(new TextureVideoViewOutlineProvider(20));
        videoView.setClipToOutline(true);
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        //先用glide加载
        Glide.with(this)
                .load(mvInfo.getPictureUrl())
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        videoView.setBackground(resource);
                    }
                });
        //先用glide加载
        Glide.with(this)
                .load(mvInfo.getPictureUrl())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(artistHeadView);
    }

    //注册MV的收藏、下载等
    private void dealMV(MvInfo mvInfo, View view) {
        //mv的下载或收藏监听
        ImageButton downloadButton = view.findViewById(R.id.mv_ib_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View popView;
                if (mDownloadPopWindow != null) {
                    popView = mDownloadPopWindow.getContentView();
                    mDownloadPopWindow.setFocusable(true);
                    if (mDownloadPopWindow.isShowing()) {
                        mDownloadPopWindow.dismiss();
                    } else {
                        mDownloadPopWindow.showAtLocation(mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                        darkenBackground(0.5f);
                    }
                } else {
                    popView = getLayoutInflater().inflate(R.layout.mv_download_menu, null);
                    mDownloadPopWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                            600, false);
                    mDownloadPopWindow.setOutsideTouchable(true); // 点击外部关闭
                    mDownloadPopWindow.setFocusable(true);
                    mDownloadPopWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                    mDownloadPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mDownloadPopWindow.showAtLocation(mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                    darkenBackground(0.5f);
                    mDownloadPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            darkenBackground(1.0f);
                        }
                    });
                }
                //具体操作监听
                ImageView storeView = popView.findViewById(R.id.mv_iv_store);
                ImageView downloadView = popView.findViewById(R.id.mv_iv_download);
                TextView storeTipText = popView.findViewById(R.id.mv_tv_store_tip);
                setMvCollectButton(storeView, storeTipText, mvInfo);
                storeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDownloadPopWindow.dismiss();
                        collectMv(storeView, storeTipText, mvInfo);
                        MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE);
                    }
                });
                downloadView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDownloadPopWindow.dismiss();
                        downloadMv(mvInfo);
                    }
                });
            }
        });
    }

    private void setMvCollectButton(ImageView storeView, TextView storeTipText, MvInfo mvInfo) {
        MvPresenter.isMvCollected(String.valueOf(mvInfo.getId()), new MvCollectCallback() {
            @Override
            public void isCollected() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_discollection);
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button);
                    }
                });
            }

            @Override
            public void notCollected() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_collection);
                        storeView.setBackgroundResource(R.drawable.mv_collect_button);
                    }
                });
            }
        });
    }

    private void collectMv(ImageView storeView, TextView storeTipText, MvInfo mvInfo) {
        MvPresenter.collectMv(mvInfo, new MvCollectCallback() {
            @Override
            public void isCollected() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_discollection);
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button);
                        Toast.makeText(getContext(), "收藏成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void notCollected() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_collection);
                        storeView.setBackgroundResource(R.drawable.mv_collect_button);
                        Toast.makeText(getContext(), "取消收藏！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void downloadMv(MvInfo mvInfo) {
        if (mDownLoadBinder != null) {
            MvPresenter.getMvInfo(mvInfo, new MusicCallBack() {
                @Override
                public void onMusicInfoCompleted() {
                    mDownLoadBinder.startDownLoad(mvInfo);
                }
            });
        }
    }

    //设置屏幕透明度,bgcolor:0-1
    private void darkenBackground(float bgcolor) {
        if (getActivity() != null) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = bgcolor;
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getActivity().getWindow().setAttributes(lp);
        }
    }

    @Override
    public void onResume() {
        //恢复各video的进度
        for (int i = 0; i < mVideoProgressList.size(); i++) {
            int progress = mVideoProgressList.get(i);
            VideoView videoView = mVideoViewList.get(i);
            if (progress > 0 && !videoView.isPlaying()) {
                MvInfo mvInfo = mVideoInfoList.get(i);
                Glide.with(this)
                        .load(mvInfo.getPictureUrl())
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                videoView.setBackground(resource);
                            }
                        });
                //本次不显示播放条
                mMediaControllerList.get(i).setToHideOnce(true);
                videoView.seekTo(mVideoProgressList.get(i));
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        //保存各video的进度
        mVideoProgressList.clear();
        for (int i = 0; i < mVideoViewList.size(); i++) {
            VideoView videoView = mVideoViewList.get(i);
            int progress = videoView.getCurrentPosition();
            mVideoProgressList.add(progress);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MusicApplication.getContext().unbindService(mDownloadConnection);
        super.onDestroy();
    }
}
