package com.quibbler.sevenmusic.activity.found;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.jsonbean.found.SingerResponseBean;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.ICallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.COLLECTION_URL;

public class SingerActivity extends BaseMusicListActivity<PlaylistAdapter> {
    private static final String SINGER_URL_AUTHORITY = "http://114.116.128.229:3000/artists?id=";
    private static final String TAG = "SingerActivityTest";
    //kind 0歌曲 1歌手 2专辑
    private static final Integer SINGER_KIND = 1;
    //歌手页面标签名
    private final String[] SINGER_TAB_NAMES = new String[]{"歌曲", "简介"};

    private ImageView mIvSingerCover;
    private TextView mTvSingerName;
    private Button mBtnSingerLoved;
    private Button mBtnSingerNotLoved;

    private RecyclerView mRecyclerView;

    private PlaylistAdapter mSingerMusicAdapter;

    //歌手页面的全局歌手对象
    private Artist mArtist = new Artist();
    private String mArtistId;

    private List<MusicInfo> mMusicInfoList = new ArrayList<>();

    //查询歌手是否被收藏
    private Thread mQueryThread;
    //更新歌手收藏状态
    private Thread mUpdateThread;
    private boolean mIsLoved;

    //开启下载模式
    private TextView mTvSelectMode;
    private boolean mIsInSelectMode;
    private LinearLayout mLlBottom;
    private TextView mTvSelectAll;
    private TextView mTvUnselectAll;
    private TextView mTvStartDownload;

    //播放全部
    private TextView mTvPlayAll;
    private ImageButton mIbtnPlayAll;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer);

        mArtistId = getIntent().getStringExtra("id");
        mArtist.setId(Integer.valueOf(mArtistId));

        init();
    }

    @Override
    protected void onSelectModeChange(boolean mode) {
        if (mode) {
            //显示取消按钮，显示下方全选、确定按钮，通知adapter
            mTvSelectMode.setText("取消");
            mLlBottom.setVisibility(View.VISIBLE);
            mSingerMusicAdapter.changeSelectMode(true);
        } else {
            //显示下载按钮，隐藏下方全选、确定按钮，通知adapter
            mTvSelectMode.setText("下载");
            mLlBottom.setVisibility(View.GONE);
            mSingerMusicAdapter.changeSelectMode(false);
        }
    }

    private void init() {
        initView();
        initActionBar();
        initBtnDownloadAll();
        initRecyclerView();
        requestSingerData();
    }


    private void initView() {
        mIvSingerCover = findViewById(R.id.singer_activity_iv_cover);
        mTvSingerName = findViewById(R.id.singer_activity_tv_name);
        mBtnSingerLoved = findViewById(R.id.singer_activity_btn_loved);
        mBtnSingerNotLoved = findViewById(R.id.singer_activity_btn_not_loved);

        mRecyclerView = findViewById(R.id.singer_activity_recyclerview);

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
                mSingerMusicAdapter.selectAll();
            }
        });

        mTvUnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSingerMusicAdapter.unselectAll();
            }
        });

        mTvStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSingerMusicAdapter.startDownload();
                onSelectModeChange(false);
            }
        });

        mTvPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicInfoList != null && mMusicInfoList.size() > 0) {
                    MusicPlayerService.addToPlayerList(mMusicInfoList);
                    MusicPlayerService.playMusic(mMusicInfoList.get(0));
                    mSingerMusicAdapter.setPlayingPosition(0);
                    mSingerMusicAdapter.notifyDataSetChanged();
                }
            }
        });
        mIbtnPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicInfoList != null && mMusicInfoList.size() > 0) {
                    MusicPlayerService.addToPlayerList(mMusicInfoList);
                    MusicPlayerService.playMusic(mMusicInfoList.get(0));
                    mSingerMusicAdapter.setPlayingPosition(0);
                    mSingerMusicAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 初始化页面的actionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("歌手 ");
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mSingerMusicAdapter = new PlaylistAdapter(mMusicInfoList, this);

        super.mAdapter = mSingerMusicAdapter;
        mRecyclerView.setAdapter(mSingerMusicAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    private void requestSingerData() {
        String url = SINGER_URL_AUTHORITY + mArtistId;
        HttpUtil.sendOkHttpRequest(url, this, new ICallback() {
            @Override
            public void onResponse(String responseText) {
                if (responseText == null) {
                    return;
                }
                Gson gson = new Gson();
                SingerResponseBean singerResponseBean = gson.fromJson(responseText, SingerResponseBean.class);
                if (singerResponseBean == null) {
                    return;
                }
                mArtist = singerResponseBean.getArtist();
                mMusicInfoList = singerResponseBean.getHotSongs();
                for (MusicInfo musicInfo : mMusicInfoList) {
                    musicInfo.setSinger(musicInfo.getFirstArName());
                }
                mSingerMusicAdapter.updateData(mMusicInfoList);

                showViewContent();

            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * 显示页面中各个view的具体内容
     */
    private void showViewContent() {
        //显示歌手图片
        ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                .load(mArtist.getPicUrl())
                .imageStyle(ImageDownloadPresenter.STYLE_ORIGIN)
                .into(mIvSingerCover, new ImageDownloadPresenter.ResourceCallback<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource) {
                        int rgb = resource.getPixel(resource.getWidth() / 6, resource.getHeight() * 9 / 10 - 1);
                        int r = (rgb & 16711680) >> 16;
                        int g = (rgb & 65280) >> 8;
                        int b = (rgb & 255);
                        Log.d(TAG, r + "," + g + "," + b + ",");
                        if (r > 190 && g > 190 && b > 190) {
                            //封面左下角为浅色，歌手名调整为黑色
                            mTvSingerName.setTextColor(getResources().getColor(R.color.my_download_bar_text_black, null));
                            mTvSingerName.setVisibility(View.VISIBLE);
                        } else {
                            //封面左下角为深色，歌手名调整为浅色
                            mTvSingerName.setTextColor(getResources().getColor(R.color.colorWhite, null));
                            mTvSingerName.setVisibility(View.VISIBLE);
                        }
                    }
                });

        //显示歌手名称
        mTvSingerName.setText(mArtist.getName());
        //显示关注按钮
        mQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mIsLoved = queryIdInThread(SingerActivity.this, mArtistId);
                if (mIsLoved) {
                    mBtnSingerLoved.setVisibility(View.VISIBLE);
                } else {
                    mBtnSingerNotLoved.setVisibility(View.VISIBLE);
                }
            }
        });
        mQueryThread.start();

        mBtnSingerLoved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已关注，点击后取消关注
                mBtnSingerLoved.setVisibility(View.GONE);
                mBtnSingerNotLoved.setVisibility(View.VISIBLE);

                if (mUpdateThread != null) {
                    mUpdateThread.interrupt();
                }
                mUpdateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateSingerLoveInThread(SingerActivity.this, mArtist, false);
                    }
                });
                mUpdateThread.start();
            }
        });
        mBtnSingerNotLoved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //未关注，点击后关注
                mBtnSingerLoved.setVisibility(View.VISIBLE);
                mBtnSingerNotLoved.setVisibility(View.GONE);
                if (mUpdateThread != null) {
                    mUpdateThread.interrupt();
                }
                mUpdateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateSingerLoveInThread(SingerActivity.this, mArtist, true);
                    }
                });
                mUpdateThread.start();
            }
        });
    }

    private void updateSingerLoveInThread(Context context, Artist artist, boolean loved) {
        Uri authorityUri = COLLECTION_URL;
        if (loved) {
            //增加该数据
            ContentValues values = new ContentValues();
            values.put("id", artist.getId());
            values.put("title", artist.getName());
            values.put("kind", SINGER_KIND);
            values.put("description", artist.getBriefDesc());
            context.getContentResolver().insert(authorityUri, values);
        } else {
            //删除该数据
            Uri collectionUrl = Uri.parse(authorityUri + "/" + artist.getId());
            context.getContentResolver().delete(collectionUrl, null, null);
        }
    }

    /**
     * 查询该歌手是否被收藏
     *
     * @param context
     * @param id
     * @return
     */
    private boolean queryIdInThread(Context context, String id) {
        boolean result = false;
        Uri uri = COLLECTION_URL;
        Cursor cursor = context.getContentResolver().query(uri, null, "id = ?", new String[]{id}, null);
        if (cursor != null && cursor.moveToFirst()) {
            //本地收藏数据库有记录
            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateThread != null) {
            mUpdateThread.interrupt();
        }
        if (mQueryThread != null) {
            mQueryThread.interrupt();
        }

    }

}
