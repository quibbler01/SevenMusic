package com.quibbler.sevenmusic.activity.my

import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.LocalMusicAdapter
import com.quibbler.sevenmusic.adapter.my.MusicAdapter
import com.quibbler.sevenmusic.bean.MusicInfo

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyLocalMusicDetailActivity
 * Description:    音乐详细列表
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:56
 */
class MyLocalMusicDetailActivity : AppCompatActivity() {
    private var mMusicLists: MutableList<MusicInfo?>? = ArrayList<MusicInfo?>()
    private var mMusicListView: ListView? = null
    private var mAdapter: MusicAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_local_music_detail)
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val intent = getIntent()
        //设置标题
        val title = intent.getStringExtra("title")
        setTitle(title)
        //获取数据
        mMusicLists = intent.getSerializableExtra("music") as MutableList<MusicInfo?>?

        mMusicListView = findViewById<ListView>(R.id.my_music_list_detail)
        mAdapter = LocalMusicAdapter(this, mMusicLists!!)
        mMusicListView!!.setAdapter(mAdapter)
        mMusicListView!!.setDivider(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }
}
