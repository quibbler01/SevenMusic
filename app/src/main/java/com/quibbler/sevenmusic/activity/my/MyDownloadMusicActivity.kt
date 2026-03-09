package com.quibbler.sevenmusic.activity.my

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MyDownloadMusicViewPagerAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyDownloadMusicActivity
 * Description:    我的下载，已下载，下载中
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:22
 */
class MyDownloadMusicActivity : AppCompatActivity(), View.OnClickListener {
    private var mToolBar: Toolbar? = null
    private var mDownloadViewPager: ViewPager? = null
    private var mDownloadTextView: TextView? = null
    private var mDownloadingTextView: TextView? = null
    private var mAdapter: MyDownloadMusicViewPagerAdapter? = null

    private val mDownloadMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private val mDownloadingMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()

    private val mDownloadReceiver = DownloadReceiver()

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SCAN_DOWN -> mAdapter!!.updateData(mDownloadMusicLists, mDownloadingMusicLists)
                else -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_download_music)
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS)
        intentFilter.addAction(MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED)
        MusicBroadcastManager.registerMusicBroadcastReceiver(mDownloadReceiver, intentFilter)

        initView()

        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        MusicBroadcastManager.unregisterMusicBroadcastReceiver(mDownloadReceiver)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    fun initView() {
        mToolBar = findViewById<Toolbar?>(R.id.my_download_toolbar)
        mDownloadTextView = findViewById<TextView>(R.id.my_download_music_done_text)
        mDownloadingTextView = findViewById<TextView>(R.id.my_downloading_music_text)
        mDownloadTextView!!.setOnClickListener(this)
        mDownloadingTextView!!.setOnClickListener(this)
        setSupportActionBar(mToolBar)
        //添加返回按钮,同时隐去标题
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
            getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        }

        mDownloadViewPager = findViewById<ViewPager>(R.id.my_download_music_view_pager)
        mAdapter = MyDownloadMusicViewPagerAdapter()
        mDownloadViewPager!!.setAdapter(mAdapter)
        mDownloadViewPager!!.setCurrentItem(0)
        mDownloadViewPager!!.setOffscreenPageLimit(0)
        mDownloadViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mDownloadTextView!!.setTextColor(Color.WHITE)
                        mDownloadingTextView!!.setTextColor(Color.BLACK)
                    }

                    1 -> {
                        mDownloadTextView!!.setTextColor(Color.BLACK)
                        mDownloadingTextView!!.setTextColor(Color.WHITE)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                mDownloadingMusicLists.clear()
                mDownloadMusicLists.clear()
                var cursor: Cursor? = null
                try {
                    cursor = MusicApplication.Companion.getContext().getContentResolver().query(
                        MusicContentProvider.Companion.DOWNLOAD_URL,
                        null,
                        null,
                        null,
                        "rowid desc"
                    )
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val musicInfo = MusicInfo()
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")))
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")))
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")))
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")))
                            musicInfo.setDownloadFailed(cursor.getInt(cursor.getColumnIndex("success")) == 1)

                            val is_download = cursor.getInt(cursor.getColumnIndex("is_download"))
                            if (is_download == 1) {
                                mDownloadingMusicLists.add(musicInfo)
                            } else {
                                mDownloadMusicLists.add(musicInfo)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor)
                }
                val message = Message()
                message.what = SCAN_DOWN
                handler.sendMessage(message)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
        } else if (item.getItemId() == R.id.my_download_clear_action_icon) {
            val builder = AlertDialog.Builder(this)
            if (mDownloadViewPager!!.getCurrentItem() == 0) {
                builder.setTitle(R.string.my_clear_download)
                    .setMessage(R.string.my_clear_download_record)
                    .setNegativeButton(R.string.confirm, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            Toast.makeText(
                                this@MyDownloadMusicActivity,
                                R.string.my_download_record_cleared,
                                Toast.LENGTH_SHORT
                            ).show()
                            mAdapter!!.clearData(0)
                        }
                    }).setPositiveButton(R.string.cancel, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                        }
                    })
                builder.show()
            } else {
                builder.setTitle(R.string.my_clear_downloading_task)
                    .setMessage(R.string.my_clear_downloading_now)
                    .setNegativeButton(R.string.confirm, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            mAdapter!!.clearData(1)
                            Toast.makeText(
                                this@MyDownloadMusicActivity,
                                R.string.my_clear_downloading,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }).setPositiveButton(R.string.cancel, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                        }
                    })
                builder.show()
            }
        }
        return true
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.my_download_music_done_text) {
            mDownloadTextView!!.setTextColor(Color.WHITE)
            mDownloadingTextView!!.setTextColor(Color.BLACK)
            mDownloadViewPager!!.setCurrentItem(0)
        } else if (v.getId() == R.id.my_downloading_music_text) {
            mDownloadTextView!!.setTextColor(Color.BLACK)
            mDownloadingTextView!!.setTextColor(Color.WHITE)
            mDownloadViewPager!!.setCurrentItem(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.my_clear_action, menu)
        return true
    }

    private inner class DownloadReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.getAction()
            if (action != null) {
                when (action) {
                    MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_SUCCESS, MusicBroadcastManager.MUSIC_GLOBAL_MUSIC_DOWNLOAD_FAILED -> initData()
                    else -> {}
                }
            }
        }
    }

    companion object {
        private const val SCAN_DOWN = 1
    }
}

