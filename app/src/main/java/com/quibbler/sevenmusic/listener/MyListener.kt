package com.quibbler.sevenmusic.listener;

import android.content.Intent;
import android.view.View;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity;
import com.quibbler.sevenmusic.activity.my.MyCollectionsActivity;
import com.quibbler.sevenmusic.activity.my.MyDownloadMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyFavouriteMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyLocalMusicActivity;
import com.quibbler.sevenmusic.activity.my.MyRecentlyPlayedMusicActivity;

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MyListener
 * Description:    我的页面点击事件处理,跳转到对应Activity界面中。暂时取消使用，因为需要startActivityForResult(...)
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:19
 */
public class MyListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        Intent intent;
        if (v.getId() == R.id.my_local_music_icon) {
            intent = new Intent(v.getContext(), MyLocalMusicActivity.class);
            v.getContext().startActivity(intent);
        } else if (v.getId() == R.id.my_download_music_icon) {
            intent = new Intent(v.getContext(), MyDownloadMusicActivity.class);
            v.getContext().startActivity(intent);
        } else if (v.getId() == R.id.my_recently_music_icon) {
            intent = new Intent(v.getContext(), MyRecentlyPlayedMusicActivity.class);
            v.getContext().startActivity(intent);
        } else if (v.getId() == R.id.my_favourite_music_icon) {
            intent = new Intent(v.getContext(), MyFavouriteMusicActivity.class);
            v.getContext().startActivity(intent);
        } else if (v.getId() == R.id.my_collection_music_icon) {
            intent = new Intent(v.getContext(), MyCollectionsActivity.class);
            v.getContext().startActivity(intent);
        } else if (v.getId() == R.id.my_buy_music_icon) {
            intent = new Intent(v.getContext(), MyCollectionMVActivity.class);
            v.getContext().startActivity(intent);
        }
    }
}
