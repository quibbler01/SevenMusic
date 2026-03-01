package com.quibbler.sevenmusic.activity.my;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MyDownloadMusicViewPagerAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.utils.CloseResourceUtil;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.DOWNLOAD_URL;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyDownloadMusicActivity
 * Description:    我的下载，已下载，下载中
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:22
 */
public class MyDownloadMusicActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolBar;
    private ViewPager mDownloadViewPager;
    private TextView mDownloadTextView;
    private TextView mDownloadingTextView;
    private MyDownloadMusicViewPagerAdapter mAdapter;

    private static final int SCAN_DOWN = 1;
    private List<MusicInfo> mDownloadMusicLists = new ArrayList<>();
    private List<MusicInfo> mDownloadingMusicLists = new ArrayList<>();

    private DownloadReceiver mDownloadReceiver = new DownloadReceiver();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SCAN_DOWN:
                    mAdapter.updateData(mDownloadMusicLists, mDownloadingMusicLists);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_download_music);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS);
        intentFilter.addAction(MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED);
        MusicBroadcastManager.registerMusicBroadcastReceiver(mDownloadReceiver, intentFilter);

        initView();

        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mDownloadReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initView() {
        mToolBar = findViewById(R.id.my_download_toolbar);
        mDownloadTextView = findViewById(R.id.my_download_music_done_text);
        mDownloadingTextView = findViewById(R.id.my_downloading_music_text);
        mDownloadTextView.setOnClickListener(this);
        mDownloadingTextView.setOnClickListener(this);
        setSupportActionBar(mToolBar);
        //添加返回按钮,同时隐去标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mDownloadViewPager = findViewById(R.id.my_download_music_view_pager);
        mAdapter = new MyDownloadMusicViewPagerAdapter();
        mDownloadViewPager.setAdapter(mAdapter);
        mDownloadViewPager.setCurrentItem(0);
        mDownloadViewPager.setOffscreenPageLimit(0);
        mDownloadViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mDownloadTextView.setTextColor(Color.WHITE);
                        mDownloadingTextView.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        mDownloadTextView.setTextColor(Color.BLACK);
                        mDownloadingTextView.setTextColor(Color.WHITE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initData() {
        MusicThreadPool.postRunnable(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                mDownloadingMusicLists.clear();
                mDownloadMusicLists.clear();
                Cursor cursor = null;
                try {
                    cursor = MusicApplication.getContext().getContentResolver().query(DOWNLOAD_URL, null, null, null, "rowid desc");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MusicInfo musicInfo = new MusicInfo();
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")));
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")));
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")));
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")));
                            musicInfo.setDownloadFailed(cursor.getInt(cursor.getColumnIndex("success")) == 1);

                            int is_download = cursor.getInt(cursor.getColumnIndex("is_download"));
                            if (is_download == 1) {
                                mDownloadingMusicLists.add(musicInfo);
                            } else {
                                mDownloadMusicLists.add(musicInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor);
                }
                Message message = new Message();
                message.what = SCAN_DOWN;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.my_download_clear_action_icon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (mDownloadViewPager.getCurrentItem() == 0) {
                builder.setTitle(R.string.my_clear_download).setMessage(R.string.my_clear_download_record).setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MyDownloadMusicActivity.this, R.string.my_download_record_cleared, Toast.LENGTH_SHORT).show();
                        mAdapter.clearData(0);
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            } else {
                builder.setTitle(R.string.my_clear_downloading_task).setMessage(R.string.my_clear_downloading_now).setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.clearData(1);
                        Toast.makeText(MyDownloadMusicActivity.this, R.string.my_clear_downloading, Toast.LENGTH_SHORT).show();
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_download_music_done_text) {
            mDownloadTextView.setTextColor(Color.WHITE);
            mDownloadingTextView.setTextColor(Color.BLACK);
            mDownloadViewPager.setCurrentItem(0);
        } else if (v.getId() == R.id.my_downloading_music_text) {
            mDownloadTextView.setTextColor(Color.BLACK);
            mDownloadingTextView.setTextColor(Color.WHITE);
            mDownloadViewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_clear_action, menu);
        return true;
    }

    private class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS:
                    case MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED:
                        initData();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

