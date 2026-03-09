package com.quibbler.sevenmusic.activity.found;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver;
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener;

import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_NO_COPYRIGHT;
import static com.quibbler.sevenmusic.broadcast.MusicBroadcastManager.MUSIC_GLOBAL_SOMETHING_WRONG;

/**
  *
  * Package:        com.quibbler.sevenmusic.activity.found
  * ClassName:      BaseMusicListActivity
  * Description:    显示歌曲列表页面的基本activity，用于对adapter的通用操作、接收广播等
  * Author:         yanwuyang
  * CreateDate:     2019/10/9 19:53
 */
public abstract class BaseMusicListActivity<T extends PlaylistAdapter> extends AppCompatActivity {

    T mAdapter;

    private MusicBroadcastReceiver mMusicBroadcastReceiver = new MusicBroadcastReceiver(new BroadcastMusicStateChangeListener() {
        @Override
        public void onNoCopyright() {
            mAdapter.refresh();
        }

        @Override
        public void onSomethingWrong() {
            mAdapter.refresh();
        }

        @Override
        public void handBroadcast() {

        }

        @Override
        public void onMusicPlay() {
            mAdapter.refresh();
        }

        @Override
        public void onMusicPause() {
            mAdapter.refresh();
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册本地音乐广播接收器
        MusicBroadcastManager.registerMusicBroadcastReceiverForMusicIntent(mMusicBroadcastReceiver);
    }

    /**
     * 页面可见时，刷新列表以统一播放状态的显示
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.refresh();
        onSelectModeChange(false);
    }

    /**
     * selectMode改变时的回调
     * @param mode
     */
    abstract protected void onSelectModeChange(boolean mode) ;


    /**
     * 一旦失焦就退出selectMode
     */
    @Override
    protected void onPause() {
        super.onPause();
        onSelectModeChange(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mMusicBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
