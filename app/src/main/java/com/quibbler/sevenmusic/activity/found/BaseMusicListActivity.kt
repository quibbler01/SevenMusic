package com.quibbler.sevenmusic.activity.found

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.broadcast.MusicBroadcastReceiver
import com.quibbler.sevenmusic.listener.BroadcastMusicStateChangeListener

/**
 * 
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      BaseMusicListActivity
 * Description:    显示歌曲列表页面的基本activity，用于对adapter的通用操作、接收广播等
 * Author:         yanwuyang
 * CreateDate:     2019/10/9 19:53
 */
abstract class BaseMusicListActivity<T : PlaylistAdapter?> : AppCompatActivity() {
    var mAdapter: T? = null

    private val mMusicBroadcastReceiver =
        MusicBroadcastReceiver(object : BroadcastMusicStateChangeListener {
            override fun onNoCopyright() {
                mAdapter!!.refresh()
            }

            override fun onSomethingWrong() {
                mAdapter!!.refresh()
            }

            override fun handBroadcast() {
            }

            override fun onMusicPlay() {
                mAdapter!!.refresh()
            }

            override fun onMusicPause() {
                mAdapter!!.refresh()
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //注册本地音乐广播接收器
        MusicBroadcastManager.registerMusicBroadcastReceiverForMusicIntent(mMusicBroadcastReceiver)
    }

    /**
     * 页面可见时，刷新列表以统一播放状态的显示
     */
    override fun onStart() {
        super.onStart()
        mAdapter!!.refresh()
        onSelectModeChange(false)
    }

    /**
     * selectMode改变时的回调
     * @param mode
     */
    protected abstract fun onSelectModeChange(mode: Boolean)


    /**
     * 一旦失焦就退出selectMode
     */
    override fun onPause() {
        super.onPause()
        onSelectModeChange(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mMusicBroadcastReceiver)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> super.onBackPressed()
            else -> {}
        }
        return true
    }
}
