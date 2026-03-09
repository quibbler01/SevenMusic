package com.quibbler.sevenmusic.activity.my;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MyChooseMusicAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      ChooseMusicActivity
 * Description:    选择歌曲作为铃声
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 19:35
 */
public class ChooseMusicActivity extends AppCompatActivity {
    private static final String TAG = "ChooseMusicActivity";

    public static final int RESULT_OK = 0;
    public static final int RESULT_NONE = -1;

    private boolean isChoose = false;
    private int mPosition = 0;

    private ListView mListView;
    private MyChooseMusicAdapter mAdapter;
    private List<MusicInfo> mMusicLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_choose_music);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mListView = findViewById(R.id.music_choose_music_list_view);
        setTitle(R.string.my_choose_ring);
        mAdapter = new MyChooseMusicAdapter(this, mMusicLists);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                Intent resultIntent = getIntent();
                resultIntent.putExtra("path", mMusicLists.get(position).getMusicFilePath());
                resultIntent.putExtra("name", mMusicLists.get(position).getMusicSongName());
                resultIntent.putExtra("id", mMusicLists.get(position).getId());
                resultIntent.putExtra("singer", mMusicLists.get(position).getSinger());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        initData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isChoose) {
                    Intent resultIntent = getIntent();
                    resultIntent.putExtra("path", mMusicLists.get(mPosition).getMusicFilePath());
                    resultIntent.putExtra("name", mMusicLists.get(mPosition).getMusicSongName());
                    resultIntent.putExtra("id", mMusicLists.get(mPosition).getId());
                    resultIntent.putExtra("singer", mMusicLists.get(mPosition).getSinger());
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Intent resultIntent = getIntent();
                    setResult(RESULT_NONE, resultIntent);
                }
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    private void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                List<MusicInfo> temp = new ArrayList<>();
                Cursor localMusicCursor = null;
                try {
                    localMusicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (localMusicCursor == null) {
                        return;
                    }
                    while (localMusicCursor.moveToNext()) {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setMusicSongName(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                        musicInfo.setSinger(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                        musicInfo.setMusicFileSize(localMusicCursor.getLong(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                        musicInfo.setMusicFilePath(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        musicInfo.setId(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        musicInfo.setAlbum(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        temp.add(musicInfo);
                    }
                    updateUI(temp);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    CloseResourceUtil.closeInputAndOutput(localMusicCursor);
                }

            }
        });
    }

    private void updateUI(List<MusicInfo> lists) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.addAll(lists);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isChoose) {
            Intent resultIntent = getIntent();
            resultIntent.putExtra("path", mMusicLists.get(mPosition).getMusicFilePath());
            resultIntent.putExtra("name", mMusicLists.get(mPosition).getMusicSongName());
            resultIntent.putExtra("id", mMusicLists.get(mPosition).getId());
            resultIntent.putExtra("singer", mMusicLists.get(mPosition).getSinger());
            setResult(RESULT_OK, resultIntent);
        } else {
            Intent resultIntent = getIntent();
            setResult(RESULT_NONE, resultIntent);
        }
    }
}
