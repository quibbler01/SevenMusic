package com.quibbler.sevenmusic.activity.my;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.found.PlaylistActivity;
import com.quibbler.sevenmusic.adapter.my.MySongListAdapter;
import com.quibbler.sevenmusic.bean.MySongListInfo;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;
import com.quibbler.sevenmusic.fragment.my.MyFragment;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.SONGLIST_URL;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MySongListManagerActivity
 * Description:    我的歌单管理Activity：同时管理我的歌单和收藏的歌单
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 11:12
 */
public class MySongListManagerActivity extends AppCompatActivity {
    private ListView mSongListView;
    private TextView mNoSongInListTextView;
    private List<MySongListInfo> mSongListInfoList = new ArrayList<>();
    private MySongListAdapter mAdapter;
    private int type;
    private ChangeViewCallBack mCallback = new ChangeViewCallBack() {
        @Override
        public void hideList() {
            noSongListToShow(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_song_list_manager);

        init();
    }

    public void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        type = intent.getIntExtra(MyFragment.TYPE_KEY, 0);
        setTitle(intent.getStringExtra(MyFragment.TITLE_KEY));

        mAdapter = new MySongListAdapter(this, R.layout.my_song_list_item, mSongListInfoList, true);
        mAdapter.setCallBack(mCallback);
        mSongListView = findViewById(R.id.my_song_lists_manager_list);
        mSongListView.setAdapter(mAdapter);
        mSongListView.setDivider(null);
        mSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSongListInfoList.get(position).getType() == 0) {
                    Intent intent = new Intent(MySongListManagerActivity.this, MySongListDetailActivity.class);
                    intent.putExtra(MyFragment.TITLE_KEY, mSongListInfoList.get(position).getListName());
                    intent.putExtra(MyFragment.CREATOR_KEY, mSongListInfoList.get(position).getCreator());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MySongListManagerActivity.this, PlaylistActivity.class);
                    intent.putExtra(getString(R.string.playlist_id), mSongListInfoList.get(position).getId());
                    startActivity(intent);
                }
            }
        });

        mNoSongInListTextView = findViewById(R.id.my_song_list_no_music);

        updateCollectionData();
    }

    public void noSongListToShow(boolean show) {
        if (show) {
            mNoSongInListTextView.setVisibility(View.VISIBLE);
            mSongListView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerForContextMenu(mSongListView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterForContextMenu(mSongListView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void updateCollectionData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                List<MySongListInfo> collectionLists = new ArrayList<>();
                Cursor songListCursor = null;
                try {
                    songListCursor = getContentResolver().query(SONGLIST_URL, null, null, null, null);
                    if (songListCursor != null) {
                        while (songListCursor.moveToNext()) {
                            MySongListInfo mySongListInfo = new MySongListInfo();
                            mySongListInfo.setListName(songListCursor.getString(songListCursor.getColumnIndex("name")));
                            mySongListInfo.setDescription(songListCursor.getString(songListCursor.getColumnIndex("description")));
                            mySongListInfo.setType(songListCursor.getInt(songListCursor.getColumnIndex("type")));
                            mySongListInfo.setNumber(songListCursor.getInt(songListCursor.getColumnIndex("number")));
                            mySongListInfo.setId(songListCursor.getString(songListCursor.getColumnIndex("id")));
                            mySongListInfo.setCreator(songListCursor.getString(songListCursor.getColumnIndex("creator")));
                            mySongListInfo.setSongsJsonData(songListCursor.getString(songListCursor.getColumnIndex("songs")));
                            mySongListInfo.setImageUrl(songListCursor.getString(songListCursor.getColumnIndex("coverimgurl")));
                            if (mySongListInfo.getType() == type) {
                                collectionLists.add(mySongListInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (songListCursor != null) {
                        songListCursor.close();
                    }
                }
                updateUI(collectionLists);
            }
        });
    }

    private void updateUI(List<MySongListInfo> collectionLists) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (collectionLists.size() != 0) {
                    mNoSongInListTextView.setVisibility(View.GONE);
                }
                mAdapter.update(collectionLists);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.my_song_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = (int) mSongListView.getAdapter().getItemId(menuInfo.position);
        if (item.getItemId() == R.id.my_song_list_context_menu_delete) {
            getContentResolver().delete(MusicContentProvider.SONGLIST_URL, "name = ?", new String[]{mSongListInfoList.get(pos).getListName()});
            mSongListInfoList.remove(pos);
            mAdapter.notifyDataSetChanged();
            if (mSongListInfoList.size() == 0) {
                noSongListToShow(true);
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_edit) {
            if (mSongListInfoList.get(pos).getType() == 0) {
                Intent intent = new Intent(MySongListManagerActivity.this, MySongListDetailActivity.class);
                intent.putExtra("json", mSongListInfoList.get(pos).getSongsJsonData());
                intent.putExtra("title", mSongListInfoList.get(pos).getListName());
                intent.putExtra("number", mSongListInfoList.get(pos).getNumber());
                startActivity(intent);
            } else {
                Intent intent = new Intent(MySongListManagerActivity.this, PlaylistActivity.class);
                intent.putExtra(getString(R.string.playlist_id), mSongListInfoList.get(pos).getId());
                startActivity(intent);
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_add_to_play) {
            //
        }
        return true;
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

    public interface ChangeViewCallBack {
        public void hideList();
    }
}
