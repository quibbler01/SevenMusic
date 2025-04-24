package com.quibbler.sevenmusic.activity.my;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MusicAdapter;
import com.quibbler.sevenmusic.adapter.my.PlayListMusicAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.fragment.my.MyFragment;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.ColorUtils;
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MySongListDetailActivity
 * Description:    点击歌单，进入歌单详情界面
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:49
 */
public class MySongListDetailActivity extends AppCompatActivity {
    private static final String TAG = "MySongListDetailActivity";

    private String mListName;
    private String mCreateTime;

    private View mHeadLayout;
    private ImageView mImageCover;
    private TextView mListNameTextView;
    private TextView mListNameCreateTime;

    private ImageView mPlayAllImageView;
    private TextView mPlayAllTextView;

    private ListView mSongListView;
    private MusicAdapter mAdapter;
    private List<MusicInfo> mMusicLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_song_list_detail);

        initView();
        initData();
    }

    public void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            mListName = intent.getStringExtra(MyFragment.TITLE_KEY);
            mCreateTime = intent.getStringExtra(MyFragment.CREATOR_KEY);
        }
        setTitle(mListName);

        mHeadLayout = findViewById(R.id.playlist_ll_background);
        mImageCover = findViewById(R.id.playlist_iv_cover);
        mListNameTextView = findViewById(R.id.my_song_list_detail_name);
        mListNameTextView.setText(mListName);
        mListNameCreateTime = findViewById(R.id.my_song_list_detail_time_stamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mListNameCreateTime.setText(simpleDateFormat.format(Long.valueOf(mCreateTime)));

        mPlayAllImageView = findViewById(R.id.my_song_list_detail_play_image);
        mPlayAllTextView = findViewById(R.id.my_song_list_detail_play_text);
        mPlayAllImageView.setOnClickListener(mOnClickListener);
        mPlayAllTextView.setOnClickListener(mOnClickListener);

        mSongListView = findViewById(R.id.my_song_list_detail_recycler_view);
        mAdapter = new PlayListMusicAdapter(this, mMusicLists);
        mSongListView.setAdapter(mAdapter);
    }

    public void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                List<MusicInfo> musicLists = MusicDatabaseUtils.getMusicList(mListName);
                if (musicLists == null || musicLists.size() == 0) {
                    Log.d(TAG, "no data to be update");
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(SEVEN_MUSIC_IMAGE + "/" + musicLists.get(0).getId());
                    updateListData(musicLists, bitmap);
                }
            }
        });
    }

    private void updateListData(List<MusicInfo> musicLists, Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.update(musicLists);
                Glide.with(MySongListDetailActivity.this).load(SEVEN_MUSIC_IMAGE + "/" + musicLists.get(0).getId()).into(mImageCover);
                if (bitmap != null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int startColor = bitmap.getPixel(0, height - 1);
                    int midColor = bitmap.getPixel(width / 2, height / 2);
                    int endColor = bitmap.getPixel(width - 1, 0);
                    if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(midColor) && ColorUtils.isPixelShallow(endColor)) {
                        midColor = -1063808;
                    }
                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[]{startColor, midColor, endColor});
                    mHeadLayout.setBackground(gradientDrawable);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.my_song_list_detail_play_image || v.getId() == R.id.my_song_list_detail_play_text) {
                MusicPlayerService.clearPlayMusicList();
                if (mMusicLists != null && mMusicLists.size() != 0) {
                    MusicPlayerService.addToPlayerList(mMusicLists);
                    mMusicLists.get(0).setPlaying(true);
                    MusicPlayerService.playMusic(mMusicLists.get(0));
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };
}
