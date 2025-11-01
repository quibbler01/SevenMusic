package com.quibbler.sevenmusic.activity.my;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MusicAdapter;
import com.quibbler.sevenmusic.adapter.my.MyRecentPlayedMusicAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.MUSIC_AUTHORITY;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyRecentlyPlayedMusicActivity
 * Description:    最近播放Activity
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:20
 */
public class MyRecentlyPlayedMusicActivity extends AppCompatActivity {
    private static final String TAG = "MyRecentlyPlayedMusicActivity";

    private final int RESULT_OK = 0;
    private final int RESULT_GO_TO_FOUND = 1;

    private ListView mPlayedMusicListView;
    private ViewStub mNonePlayedMusicFoundViewStub;
    private MusicAdapter mAdapter;
    private List<MusicInfo> mRecentlyPlayedMusicLists = new ArrayList<>();
    private List<MusicInfo> mTempRecentlyPlayedMusicLists = new ArrayList<>();
    private Uri playedUri = Uri.parse("content://" + MUSIC_AUTHORITY + "/played");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_recently_played_music);
        init();
        initData();
    }

    public void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.my_played_music_text);

        mPlayedMusicListView = findViewById(R.id.my_recently_played_music_list_view);
        mPlayedMusicListView.setDivider(null);

        mNonePlayedMusicFoundViewStub = findViewById(R.id.my_recently_zero_played_music_text_view_stub);

        mAdapter = new MyRecentPlayedMusicAdapter(this, mRecentlyPlayedMusicLists);
        mPlayedMusicListView.setAdapter(mAdapter);
    }

    public void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                mTempRecentlyPlayedMusicLists.clear();
                Cursor playedMusicCursor = getContentResolver().query(playedUri, null, null, null, "last_played desc");
                if (playedMusicCursor != null) {
                    while (playedMusicCursor.moveToNext()) {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setId(playedMusicCursor.getString(playedMusicCursor.getColumnIndex("id")));
                        musicInfo.setMusicSongName(playedMusicCursor.getString(playedMusicCursor.getColumnIndex("name")));
                        musicInfo.setSinger(playedMusicCursor.getString(playedMusicCursor.getColumnIndex("singer")));
                        musicInfo.setMusicFilePath(playedMusicCursor.getString(playedMusicCursor.getColumnIndex("path")));
                        musicInfo.setLastPlayedTime(playedMusicCursor.getLong(playedMusicCursor.getColumnIndex("last_played")));
                        mTempRecentlyPlayedMusicLists.add(musicInfo);
                    }
                    playedMusicCursor.close();
                }
                updateUI();
            }
        });
    }

    public void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTempRecentlyPlayedMusicLists.size() > 0) {
                    mAdapter.clear();
                    mAdapter.addAll(mTempRecentlyPlayedMusicLists);
                    mAdapter.notifyDataSetChanged();
                    mNonePlayedMusicFoundViewStub.setVisibility(View.GONE);
                } else {
                    mNonePlayedMusicFoundViewStub.setVisibility(View.VISIBLE);
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.my_recently_zero_played_music_text));
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent intent = getIntent();
                            setResult(RESULT_GO_TO_FOUND, intent);
                            finish();
                        }
                    };
                    spannableStringBuilder.setSpan(clickableSpan, 11, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    TextView textView = findViewById(R.id.my_recently_zero_played_music_text);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setText(spannableStringBuilder);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.my_download_clear_action_icon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.my_played_record_clear)
                    .setMessage("")
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.clear();
                            mAdapter.notifyDataSetChanged();
                            mPlayedMusicListView.setVisibility(View.GONE);
                            mNonePlayedMusicFoundViewStub.setVisibility(View.VISIBLE);
                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.my_recently_zero_played_music_text));
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    Intent intent = getIntent();
                                    setResult(RESULT_GO_TO_FOUND, intent);
                                    finish();
                                }
                            };
                            spannableStringBuilder.setSpan(clickableSpan, 11, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            TextView textView = findViewById(R.id.my_recently_zero_played_music_text);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setText(spannableStringBuilder);
                            Toast.makeText(MyRecentlyPlayedMusicActivity.this, R.string.my_played_record_clear_toast, Toast.LENGTH_SHORT).show();
                            MusicThreadPool.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    getContentResolver().delete(playedUri, null, null);
                                }
                            });
                        }
                    });
            builder.show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_clear_action, menu);
        return true;
    }

}
