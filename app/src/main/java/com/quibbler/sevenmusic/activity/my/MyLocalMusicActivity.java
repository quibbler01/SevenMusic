package com.quibbler.sevenmusic.activity.my;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.my.MyLocalMusicViewPagerAdapter;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.utils.GetInputMethodManager;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyLocalMusicActivity
 * Description:    本地音乐，搜索，分类，歌手，专辑，文件夹，播放
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:21
 */
public class MyLocalMusicActivity extends AppCompatActivity {
    private static final String TAG = "MyLocalMusicActivity";

    private static final int READ_STORAGE = 1;
    private static final int SCAN_LOCAL_MUSIC_DONE = 2;

    private TabLayout mMyLocalMusicTab;
    private ViewPager mMyLocalMusicViewPager;
    private SearchView mSearchLocalMusic;

    private MyLocalMusicViewPagerAdapter mAdapter;

    private List<MusicInfo> musicInfoLists = new ArrayList<>();
    private List<MusicInfo> scanInThreadMusicLists = new ArrayList<>();
    private List<MusicInfo> searchResultMusicLists = new ArrayList<>();

    private InputMethodManager mInputMethodManager = GetInputMethodManager.getInputMethodManager();

    private static LocalHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_my_local_music);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mHandler = new LocalHandler(this);

        checkPermission();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void init() {
        mAdapter = new MyLocalMusicViewPagerAdapter(this, musicInfoLists);

        mMyLocalMusicViewPager = findViewById(R.id.my_local_music_viewpager);
        mMyLocalMusicViewPager.setAdapter(mAdapter);
        mMyLocalMusicViewPager.setOffscreenPageLimit(3);
        mMyLocalMusicViewPager.setCurrentItem(0);
        mMyLocalMusicViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float MIN_SCALE = 0.85f;
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float rotate = 10 * Math.abs(position);
                if (position <= -1) {
                    page.setScaleX(MIN_SCALE);
                    page.setScaleY(MIN_SCALE);
                    page.setRotationY(rotate);
                } else if (position < 0) {
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setRotationY(rotate);
                } else if (position >= 0 && position < 1) {
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setRotationY(-rotate);
                } else if (position >= 1) {
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setRotationY(-rotate);
                }
            }
        });

        mMyLocalMusicTab = findViewById(R.id.my_local_music_tab);
        mMyLocalMusicTab.setupWithViewPager(mMyLocalMusicViewPager);
        mMyLocalMusicTab.setTabTextColors(Color.BLACK, Color.BLUE);
    }

    /**
     * 申请storage权限
     */
    public void checkPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
        } else {
            scanLocalMusic();
        }
    }

    /**
     * 申请storage权限结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                scanLocalMusic();
            }
        } else {
            Toast.makeText(this, R.string.my_local_music_scan_permission, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 扫描本地音乐
     */
    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    @WorkerThread
    public void scanLocalMusic() {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                scanInThreadMusicLists.clear();
                ContentResolver localMusicResolver = getContentResolver();
                Cursor localMusicCursor = localMusicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if (localMusicCursor == null) {
                    return;
                }
                while (localMusicCursor.moveToNext()) {
                    MusicInfo musicInfo = new MusicInfo();
                    String name = localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    if (name != null && name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                    musicInfo.setMusicSongName(name);
                    musicInfo.setSinger(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    musicInfo.setMusicFileSize(localMusicCursor.getLong(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                    musicInfo.setMusicFilePath(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    musicInfo.setId(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    musicInfo.setAlbum(localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    musicInfo.setAlbumID(localMusicCursor.getInt(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    scanInThreadMusicLists.add(musicInfo);
                }
                localMusicCursor.close();
                Message message = Message.obtain();
                message.what = SCAN_LOCAL_MUSIC_DONE;
                mHandler.sendMessage(message);
            }
        });
    }

    public void update() {
        musicInfoLists.clear();
        musicInfoLists.addAll(scanInThreadMusicLists);
        mAdapter.updateData(musicInfoLists);
        setTitle(getString(R.string.my_local_music_text) + musicInfoLists.size());
    }

    /**
     * 创建标题栏菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_local_search_menu, menu);
        mSearchLocalMusic = (SearchView) menu.findItem(R.id.search_local_music_button).getActionView();
        mMyLocalMusicTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (mMyLocalMusicTab.getSelectedTabPosition()) {
                    case 0:
                        setTitle(getString(R.string.my_local_music_text) + mAdapter.getDataCount(0));
                        mSearchLocalMusic.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        setTitle(getString(R.string.my_local_singer_text) + mAdapter.getDataCount(1));
                        mSearchLocalMusic.setVisibility(View.GONE);
                        break;
                    case 2:
                        setTitle(getString(R.string.my_local_album_text) + mAdapter.getDataCount(2));
                        mSearchLocalMusic.setVisibility(View.GONE);
                        break;
                    case 3:
                        setTitle(getString(R.string.my_local_path_text) + mAdapter.getDataCount(3));
                        mSearchLocalMusic.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mSearchLocalMusic.setQueryHint(getString(R.string.search_local_music));
        mSearchLocalMusic.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(" ") || newText.equals("")) {
                    mAdapter.updateData(musicInfoLists);
                    return false;
                }
                searchResultMusicLists.clear();
                for (MusicInfo musicInfo : musicInfoLists) {
                    MusicInfo tmp = new MusicInfo(musicInfo.getMusicSongName(), musicInfo.getSinger(), musicInfo.getMusicFilePath(), musicInfo.getId(), musicInfo.getMusicFileSize());
                    tmp.getIsMusicMatch().setKeyLength(newText.length());
                    if (musicInfo.getMusicSongName().contains(newText)) {
                        Log.d("music", "getMusicSongName");
                        tmp.getIsMusicMatch().setMusicNameMatch(true);
                        tmp.getIsMusicMatch().setMusicNameStart(musicInfo.getMusicSongName().indexOf(newText));
                    }
                    if (musicInfo.getSinger().contains(newText)) {
                        Log.d("music", "getSinger");
                        tmp.getIsMusicMatch().setSingleNameMatch(true);
                        tmp.getIsMusicMatch().setSingleNameStart(musicInfo.getSinger().indexOf(newText));
                    }
                    if (tmp.getIsMusicMatch().isMusicNameMatch() || tmp.getIsMusicMatch().isSingleNameMatch()) {
                        searchResultMusicLists.add(tmp);
                    }
                }
                mAdapter.updateData(searchResultMusicLists);
                return true;
            }
        });
        return true;
    }

    /**
     * 菜单被选中
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSearchLocalMusic.isIconified()) {
                    super.onBackPressed();
                } else {
                    mSearchLocalMusic.setIconified(true);
                    mInputMethodManager.hideSoftInputFromWindow(mSearchLocalMusic.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                break;
        }
        return true;
    }

    /**
     * 返回键按下，处理收起键盘
     */
    @Override
    public void onBackPressed() {
        if (mSearchLocalMusic.isIconified()) {
            super.onBackPressed();
        } else {
            mSearchLocalMusic.setIconified(true);
            mInputMethodManager.hideSoftInputFromWindow(mSearchLocalMusic.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private static class LocalHandler extends Handler {
        WeakReference<Activity> weakReference;

        public LocalHandler(Activity activity) {
            this.weakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_LOCAL_MUSIC_DONE:
                    ((MyLocalMusicActivity) weakReference.get()).update();
                    break;
                default:
                    break;
            }
        }
    }
}
