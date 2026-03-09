package com.quibbler.sevenmusic.listener

import android.content.Intent
import android.view.View
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity
import com.quibbler.sevenmusic.activity.my.MyCollectionsActivity
import com.quibbler.sevenmusic.activity.my.MyDownloadMusicActivity
import com.quibbler.sevenmusic.activity.my.MyFavouriteMusicActivity
import com.quibbler.sevenmusic.activity.my.MyLocalMusicActivity
import com.quibbler.sevenmusic.activity.my.MyRecentlyPlayedMusicActivity

/**
 * Package:        com.quibbler.sevenmusic.listener
 * ClassName:      MyListener
 * Description:    我的页面点击事件处理,跳转到对应Activity界面中。暂时取消使用，因为需要startActivityForResult(...)
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:19
 */
class MyListener : View.OnClickListener {
    override fun onClick(v: View) {
        val intent: Intent?
        if (v.getId() == R.id.my_local_music_icon) {
            intent = Intent(v.getContext(), MyLocalMusicActivity::class.java)
            v.getContext().startActivity(intent)
        } else if (v.getId() == R.id.my_download_music_icon) {
            intent = Intent(v.getContext(), MyDownloadMusicActivity::class.java)
            v.getContext().startActivity(intent)
        } else if (v.getId() == R.id.my_recently_music_icon) {
            intent = Intent(v.getContext(), MyRecentlyPlayedMusicActivity::class.java)
            v.getContext().startActivity(intent)
        } else if (v.getId() == R.id.my_favourite_music_icon) {
            intent = Intent(v.getContext(), MyFavouriteMusicActivity::class.java)
            v.getContext().startActivity(intent)
        } else if (v.getId() == R.id.my_collection_music_icon) {
            intent = Intent(v.getContext(), MyCollectionsActivity::class.java)
            v.getContext().startActivity(intent)
        } else if (v.getId() == R.id.my_buy_music_icon) {
            intent = Intent(v.getContext(), MyCollectionMVActivity::class.java)
            v.getContext().startActivity(intent)
        }
    }
}
