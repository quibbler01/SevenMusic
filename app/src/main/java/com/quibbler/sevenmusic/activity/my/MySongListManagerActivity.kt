package com.quibbler.sevenmusic.activity.my

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.PlaylistActivity
import com.quibbler.sevenmusic.adapter.my.MySongListAdapter
import com.quibbler.sevenmusic.bean.MySongListInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MySongListManagerActivity
 * Description:    我的歌单管理Activity：同时管理我的歌单和收藏的歌单
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 11:12
 */
class MySongListManagerActivity : AppCompatActivity() {
    private var mSongListView: ListView? = null
    private var mNoSongInListTextView: TextView? = null
    private val mSongListInfoList: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()
    private var mAdapter: MySongListAdapter? = null
    private var type = 0
    private val mCallback: ChangeViewCallBack = object : ChangeViewCallBack {
        override fun hideList() {
            noSongListToShow(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_song_list_manager)

        init()
    }

    fun init() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val intent = getIntent()
        type = intent.getIntExtra(MyFragment.Companion.TYPE_KEY, 0)
        setTitle(intent.getStringExtra(MyFragment.Companion.TITLE_KEY))

        mAdapter = MySongListAdapter(this, R.layout.my_song_list_item, mSongListInfoList, true)
        mAdapter!!.setCallBack(mCallback)
        mSongListView = findViewById<ListView>(R.id.my_song_lists_manager_list)
        mSongListView!!.setAdapter(mAdapter)
        mSongListView!!.setDivider(null)
        mSongListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (mSongListInfoList.get(position)!!.getType() == 0) {
                    val intent =
                        Intent(this@MySongListManagerActivity, MySongListDetailActivity::class.java)
                    intent.putExtra(
                        MyFragment.Companion.TITLE_KEY,
                        mSongListInfoList.get(position)!!.getListName()
                    )
                    intent.putExtra(
                        MyFragment.Companion.CREATOR_KEY,
                        mSongListInfoList.get(position)!!.getCreator()
                    )
                    startActivity(intent)
                } else {
                    val intent =
                        Intent(this@MySongListManagerActivity, PlaylistActivity::class.java)
                    intent.putExtra(
                        getString(R.string.playlist_id),
                        mSongListInfoList.get(position)!!.getId()
                    )
                    startActivity(intent)
                }
            }
        })

        mNoSongInListTextView = findViewById<TextView>(R.id.my_song_list_no_music)

        updateCollectionData()
    }

    fun noSongListToShow(show: Boolean) {
        if (show) {
            mNoSongInListTextView!!.setVisibility(View.VISIBLE)
            mSongListView!!.setVisibility(View.GONE)
        }
    }

    override fun onStart() {
        super.onStart()
        registerForContextMenu(mSongListView)
    }

    override fun onStop() {
        super.onStop()
        unregisterForContextMenu(mSongListView)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    fun updateCollectionData() {
        MusicThreadPool.postRunnable(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                val collectionLists: MutableList<MySongListInfo?> = ArrayList<MySongListInfo?>()
                var songListCursor: Cursor? = null
                try {
                    songListCursor = getContentResolver().query(
                        MusicContentProvider.Companion.SONGLIST_URL,
                        null,
                        null,
                        null,
                        null
                    )
                    if (songListCursor != null) {
                        while (songListCursor.moveToNext()) {
                            val mySongListInfo = MySongListInfo()
                            mySongListInfo.setListName(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "name"
                                    )
                                )
                            )
                            mySongListInfo.setDescription(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "description"
                                    )
                                )
                            )
                            mySongListInfo.setType(
                                songListCursor.getInt(
                                    songListCursor.getColumnIndex(
                                        "type"
                                    )
                                )
                            )
                            mySongListInfo.setNumber(
                                songListCursor.getInt(
                                    songListCursor.getColumnIndex(
                                        "number"
                                    )
                                )
                            )
                            mySongListInfo.setId(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "id"
                                    )
                                )
                            )
                            mySongListInfo.setCreator(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "creator"
                                    )
                                )
                            )
                            mySongListInfo.setSongsJsonData(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "songs"
                                    )
                                )
                            )
                            mySongListInfo.setImageUrl(
                                songListCursor.getString(
                                    songListCursor.getColumnIndex(
                                        "coverimgurl"
                                    )
                                )
                            )
                            if (mySongListInfo.getType() == type) {
                                collectionLists.add(mySongListInfo)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (songListCursor != null) {
                        songListCursor.close()
                    }
                }
                updateUI(collectionLists)
            }
        })
    }

    private fun updateUI(collectionLists: MutableList<MySongListInfo?>) {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (collectionLists.size != 0) {
                    mNoSongInListTextView!!.setVisibility(View.GONE)
                }
                mAdapter!!.update(collectionLists)
            }
        })
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
        getMenuInflater().inflate(R.menu.my_song_list_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.getMenuInfo() as AdapterContextMenuInfo?
        val pos = mSongListView!!.getAdapter().getItemId(menuInfo!!.position).toInt()
        if (item.getItemId() == R.id.my_song_list_context_menu_delete) {
            getContentResolver().delete(
                MusicContentProvider.Companion.SONGLIST_URL,
                "name = ?",
                arrayOf<String?>(mSongListInfoList.get(pos)!!.getListName())
            )
            mSongListInfoList.removeAt(pos)
            mAdapter!!.notifyDataSetChanged()
            if (mSongListInfoList.size == 0) {
                noSongListToShow(true)
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_edit) {
            if (mSongListInfoList.get(pos)!!.getType() == 0) {
                val intent =
                    Intent(this@MySongListManagerActivity, MySongListDetailActivity::class.java)
                intent.putExtra("json", mSongListInfoList.get(pos)!!.getSongsJsonData())
                intent.putExtra("title", mSongListInfoList.get(pos)!!.getListName())
                intent.putExtra("number", mSongListInfoList.get(pos)!!.getNumber())
                startActivity(intent)
            } else {
                val intent = Intent(this@MySongListManagerActivity, PlaylistActivity::class.java)
                intent.putExtra(
                    getString(R.string.playlist_id),
                    mSongListInfoList.get(pos)!!.getId()
                )
                startActivity(intent)
            }
        } else if (item.getItemId() == R.id.my_song_list_context_menu_add_to_play) {
            //
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
        }
        return true
    }

    interface ChangeViewCallBack {
        fun hideList()
    }
}
