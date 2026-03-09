package com.quibbler.sevenmusic.activity.my;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.LocalMusicAdapter;
import com.quibbler.sevenmusic.adapter.my.MusicAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyLocalMusicDetailActivity
 * Description:    音乐详细列表
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:56
 */
public class MyLocalMusicDetailActivity extends AppCompatActivity {
    private List<MusicInfo> mMusicLists = new ArrayList<>();
    private ListView mMusicListView;
    private MusicAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_local_music_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        //设置标题
        String title = intent.getStringExtra("title");
        setTitle(title);
        //获取数据
        mMusicLists = (List<MusicInfo>) intent.getSerializableExtra("music");

        mMusicListView = findViewById(R.id.my_music_list_detail);
        mAdapter = new LocalMusicAdapter(this, mMusicLists);
        mMusicListView.setAdapter(mAdapter);
        mMusicListView.setDivider(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
