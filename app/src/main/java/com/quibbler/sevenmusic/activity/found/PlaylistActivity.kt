package com.quibbler.sevenmusic.activity.found;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter;
import com.quibbler.sevenmusic.bean.Creator;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistDetailResponseBean;
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.ColorUtils;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.ThreadDispatcher;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.SONGLIST_URL;

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      PlaylistActivity
 * Description:    单个歌单的显示页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/24 10:49
 */
public class PlaylistActivity extends BaseMusicListActivity<PlaylistAdapter> {
    private static final String TAG = "PlaylistActivityTest";

    private static final int PLAYLIST_KIND = 1; //非自建歌单，不可修改
    //获取歌单详细信息的url
    private static final String REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY = "http://114.116.128.229:3000/playlist/detail?id=";
    //当前歌单页面所显示歌单的id
    private String mPlaylistId;
    //歌单的歌曲列表
    private List<MusicInfo> mMusicInfoList = new ArrayList<>();
    //获取歌单详细信息的AsyncTask
    private RequestPlaylistDetailAsyncTask mRequestPlaylistDetailAsyncTask;
    //显示歌曲的RecyclerView的Adapter
    private PlaylistAdapter mPlaylistAdapter;
    //该歌单是否被收藏
    private boolean mIsCollected;
    private Thread mQueryThread;

    //当前页面的歌单对象
    private PlaylistInfo mPlaylistInfo = new PlaylistInfo();
    //管控线程的标志位
    private volatile boolean mIsCancelled = false;

    //显示歌曲列表的RecyclerView
    private RecyclerView mTracksRecyclerView;
    //歌单封面
    private ImageView mIvCover;
    //歌单名
    private TextView mTvPlaylistName;
    //歌单创建者
    private TextView mTvCreator;
    //收藏歌单按钮
    private ImageButton mBtnCollect;
    //封面背景
    private LinearLayout mLinearLayout;
    //播放全部
    private TextView mTvPlayAll;
    private ImageButton mIbtnPlayAll;

    //开启下载模式
    private TextView mTvSelectMode;
    private boolean mIsInSelectMode;
    private LinearLayout mLlBottom;
    private TextView mTvSelectAll;
    private TextView mTvUnselectAll;
    private TextView mTvStartDownload;


    /**
     * 只有通过intent传入id，才能打开歌单页面
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        mPlaylistId = getIntent().getStringExtra(getString(R.string.playlist_id));
        mPlaylistInfo.setId(mPlaylistId);

        init();

    }

    private void init() {
        mIsCancelled = false;
        initView();
        initActionBar();
        initRecyclerView();

        initBtnDownloadAll();

        //开启线程，在线程中：查询数据库是否有记录，有则显示页面，没有则进行网络请求。最后初始化button按钮。
        mQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getPlaylistDataInThread();
                if (!mIsCollected || mMusicInfoList == null || mMusicInfoList.size() == 0) {
                    requestPlaylistDetailData();
                } else {
                    //上面流程走完，更新页面显示，再初始化button（包括button的图片、button的listener）
                    //用标志位管控
                    if (!mIsCancelled) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initBtnCollect();
                                showPlaylist();
                            }
                        });
                    }
                }
            }
        });
        mQueryThread.start();
    }

    /**
     * 初始化页面的actionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("歌单");
    }

    private void initView() {
        mIvCover = findViewById(R.id.playlist_iv_cover);
        mTvPlaylistName = findViewById(R.id.playlist_tv_name);
        mTvCreator = findViewById(R.id.playlist_tv_creator);
        mBtnCollect = findViewById(R.id.playlist_btn_add_to_collection);
        mTracksRecyclerView = findViewById(R.id.playlist_recyclerview_tracks);
        mLinearLayout = findViewById(R.id.playlist_ll_background);

        mTvSelectMode = findViewById(R.id.playlist_tv_download_mode);
        mLlBottom = findViewById(R.id.playlist_ll_bottom);
        mTvSelectAll = findViewById(R.id.playlist_tv_select_all);
        mTvUnselectAll = findViewById(R.id.playlist_tv_unselect_all);
        mTvStartDownload = findViewById(R.id.playlist_tv_start_download);

        mTvPlayAll = findViewById(R.id.playlist_tv_play_all);
        mIbtnPlayAll = findViewById(R.id.playlist_ib_play_all);
    }

    private void initBtnDownloadAll() {
        mTvSelectMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectModeChange(!mIsInSelectMode);
                mIsInSelectMode = !mIsInSelectMode;
            }
        });

        mTvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaylistAdapter.selectAll();
            }
        });

        mTvUnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaylistAdapter.unselectAll();
            }
        });

        mTvStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaylistAdapter.startDownload();
                onSelectModeChange(false);
            }
        });

        mTvPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicInfoList != null && mMusicInfoList.size() > 0) {
                    MusicPlayerService.addToPlayerList(mMusicInfoList);
                    MusicPlayerService.playMusic(mMusicInfoList.get(0));
                    mPlaylistAdapter.setPlayingPosition(0);
                    mPlaylistAdapter.notifyDataSetChanged();
                }
            }
        });
        mIbtnPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicInfoList != null && mMusicInfoList.size() > 0) {
                    MusicPlayerService.addToPlayerList(mMusicInfoList);
                    MusicPlayerService.playMusic(mMusicInfoList.get(0));
                    mPlaylistAdapter.setPlayingPosition(0);
                    mPlaylistAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onSelectModeChange(boolean mode) {
        if (mode) {
            //显示取消按钮，显示下方全选、确定按钮，通知adapter
            mTvSelectMode.setText("取消");
            mLlBottom.setVisibility(View.VISIBLE);
            mPlaylistAdapter.changeSelectMode(true);
        } else {
            //显示下载按钮，隐藏下方全选、确定按钮，通知adapter
            mTvSelectMode.setText("下载");
            mLlBottom.setVisibility(View.GONE);
            mPlaylistAdapter.changeSelectMode(false);
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mTracksRecyclerView.setLayoutManager(linearLayoutManager);
        mPlaylistAdapter = new PlaylistAdapter(mMusicInfoList, this);
        super.mAdapter = mPlaylistAdapter;
        mTracksRecyclerView.setAdapter(mPlaylistAdapter);
        mTracksRecyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * 获取歌单信息。先查本地数据库，若没有再去网络请求。
     * 方法名中加XXXInThread，提醒使用者要开启线程来调用
     */
    private boolean getPlaylistDataInThread() {
        Uri uri = SONGLIST_URL;
        if (!TextUtils.isEmpty(mPlaylistInfo.getId())){
            Cursor cursor = getContentResolver().query(uri, null, "id = ?", new String[]{mPlaylistInfo.getId()}, null);
            if (cursor != null && cursor.moveToFirst()) {
                //本地收藏数据库有记录
                mIsCollected = true;
                mPlaylistInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
                mPlaylistInfo.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                mPlaylistInfo.setTrackCount(cursor.getInt(cursor.getColumnIndex("number")));
//            mPlaylistInfo.setCoverImgUrl(cursor.getString(cursor.getColumnIndex("coverimgurl")));

                String creatorJson = cursor.getString(cursor.getColumnIndex("creator"));
                Creator creator = new Gson().fromJson(creatorJson, Creator.class);
                mPlaylistInfo.setCreator(creator);

                String tracksJson = cursor.getString(cursor.getColumnIndex("songs"));
                mMusicInfoList = new Gson().fromJson(tracksJson, new TypeToken<List<MusicInfo>>() {
                }.getType());
                mPlaylistInfo.setTracks(mMusicInfoList);
            } else {
                mIsCollected = false;
            }
            cursor.close();
            return mIsCollected;
        } else {
            Log.d(TAG,"getPlaylistDataInThread,mPlaylistInfo.getId() is null");
            return false;
        }

    }

    /**
     * 将歌单数据在页面上显示
     */
    private void showPlaylist() {

        mTvPlaylistName.setText(mPlaylistInfo.getName());
        Creator creator = mPlaylistInfo.getCreator();
        if (creator != null) {
            mTvCreator.setText(creator.getNickname());
        }
        mPlaylistAdapter.updateData(mMusicInfoList);

        if (mPlaylistInfo.getCoverImgUrl() == null) {
            HttpUtil.sendOkHttpRequest(REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY + mPlaylistInfo.getId(), new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (!TextUtils.isEmpty(responseBody)) {
                        Gson gson = new Gson();
                        PlaylistDetailResponseBean responseBean = gson.fromJson(responseBody, PlaylistDetailResponseBean.class);
                        if (responseBean.getCode().equals("200")) {
                            mPlaylistInfo.setCoverImgUrl(responseBean.getPlaylist().getCoverImgUrl());
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                                    .load(mPlaylistInfo.getCoverImgUrl())
                                    .imageStyle(ImageDownloadPresenter.STYLE_ORIGIN)
                                    .into(mIvCover, new ImageDownloadPresenter.ResourceCallback<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource) {
                                            int width = resource.getWidth();
                                            int height = resource.getHeight();

                                            int startColor = resource.getPixel(0, height - 1);
                                            int midColor = resource.getPixel(width / 2, height / 2);
                                            int endColor = resource.getPixel(width - 1, 0);

                                            if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(midColor) && ColorUtils.isPixelShallow(endColor)) {
                                                midColor = -1063808;
                                            }
                                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[]{startColor, midColor, endColor});
                                            mLinearLayout.setBackground(gradientDrawable);
                                        }
                                    });
                        }
                    });
                }
            });
        } else {
            ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                    .load(mPlaylistInfo.getCoverImgUrl())
                    .imageStyle(ImageDownloadPresenter.STYLE_ORIGIN)
                    .into(mIvCover, new ImageDownloadPresenter.ResourceCallback<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource) {
                            int width = resource.getWidth();
                            int height = resource.getHeight();

                            int startColor = resource.getPixel(0, height - 1);
                            int midColor = resource.getPixel(width / 2, height / 2);
                            int endColor = resource.getPixel(width - 1, 0);

                            if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(midColor) && ColorUtils.isPixelShallow(endColor)) {
                                midColor = -1063808;
                            }
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[]{startColor, midColor, endColor});
                            mLinearLayout.setBackground(gradientDrawable);
                        }
                    });
        }
    }

    /**
     * 根据歌单id获取歌单的详细信息
     */
    private void requestPlaylistDetailData() {
        String url = REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY + mPlaylistId;
        mRequestPlaylistDetailAsyncTask = new RequestPlaylistDetailAsyncTask();
        mRequestPlaylistDetailAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    /**
     * 初始化button，收藏、下载，与数据库交互
     */
    private void initBtnCollectListener() {

        mBtnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCollected) {
                    //歌单已收藏 点击后取消收藏
                    mIsCollected = false;
                    mBtnCollect.setImageResource(R.drawable.playlist_btn_not_collected);

                    //写数据库
                    ThreadDispatcher.getInstance().runOnWorkerThread(new Runnable() {
                        @Override
                        public void run() {
                            Uri uri = SONGLIST_URL;
                            getContentResolver().delete(uri, "id = ?", new String[]{mPlaylistInfo.getId()});
                        }
                    });

                } else {
                    //歌单未收藏，点击后收藏
                    mIsCollected = true;
                    mBtnCollect.setImageResource(R.drawable.playlist_btn_collected);

                    //写数据库
                    ThreadDispatcher.getInstance().runOnWorkerThread(new Runnable() {
                        @Override
                        public void run() {
                            Uri uri = SONGLIST_URL;
                            ContentValues values = new ContentValues();
                            Gson gson = new Gson();
                            values.put("id", mPlaylistInfo.getId());
                            values.put("name", mPlaylistInfo.getName());
                            values.put("type", PLAYLIST_KIND);
                            values.put("description", mPlaylistInfo.getDescription());
                            values.put("songs", gson.toJson(mPlaylistInfo.getTracks()));
                            values.put("number", mPlaylistInfo.getTrackCount());
                            values.put("creator", gson.toJson(mPlaylistInfo.getCreator()));
                            values.put("coverimgurl", mPlaylistInfo.getCoverImgUrl());

                            getContentResolver().insert(uri, values);
                        }
                    });
                }
            }
        });
    }

    /**
     * 初始化收藏按钮。先查询该歌单是否被收藏,是则显示红色爱心图片，否则显示空心爱心图片。然后为按钮设置监听器。
     */
    private void initBtnCollect() {
        if (mPlaylistInfo == null) {
            return;
        }
        ThreadDispatcher.getInstance().runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                Uri uri = SONGLIST_URL;

                Cursor cursor = getContentResolver().query(uri, null, "id = ?", new String[]{mPlaylistInfo.getId()}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mIsCollected = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBtnCollect.setImageResource(R.drawable.playlist_btn_collected);
                        }
                    });
                } else {
                    mIsCollected = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBtnCollect.setImageResource(R.drawable.playlist_btn_not_collected);
                        }
                    });
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        });

        initBtnCollectListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestPlaylistDetailAsyncTask != null) {
            mRequestPlaylistDetailAsyncTask.cancel(true);
        }
        mIsCancelled = true;
        if (mQueryThread != null) {
            mQueryThread.interrupt();
        }
    }

    private class RequestPlaylistDetailAsyncTask extends AsyncTask<String, Void, PlaylistInfo> {
        @Override
        protected PlaylistInfo doInBackground(String... strings) {
            String url = strings[0];
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    String responseBody = response.body().string();
                    if (!TextUtils.isEmpty(responseBody)) {
                        Gson gson = new Gson();
                        PlaylistDetailResponseBean responseBean = gson.fromJson(responseBody, PlaylistDetailResponseBean.class);
                        if (responseBean.getCode().equals("200")) {
                            mPlaylistInfo = responseBean.getPlaylist();
                            return mPlaylistInfo;
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "网络异常！");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(PlaylistInfo playlistInfo) {
            super.onPostExecute(playlistInfo);
            if (playlistInfo == null) {
                return;
            }

            mMusicInfoList = playlistInfo.getTracks();
            for (MusicInfo musicInfo : mMusicInfoList) {
                musicInfo.setSinger(musicInfo.getFirstArName());
            }
            showPlaylist();
            initBtnCollect();
        }
    }
}
