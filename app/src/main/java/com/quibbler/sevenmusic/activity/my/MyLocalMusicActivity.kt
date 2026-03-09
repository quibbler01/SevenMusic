package com.quibbler.sevenmusic.activity.my

import android.Manifest.permission
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.PageTransformer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MyLocalMusicViewPagerAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.utils.GetInputMethodManager
import com.quibbler.sevenmusic.utils.MusicThreadPool
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyLocalMusicActivity
 * Description:    本地音乐，搜索，分类，歌手，专辑，文件夹，播放
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:21
 */
class MyLocalMusicActivity : AppCompatActivity() {
    private var mMyLocalMusicTab: TabLayout? = null
    private var mMyLocalMusicViewPager: ViewPager? = null
    private var mSearchLocalMusic: SearchView? = null

    private var mAdapter: MyLocalMusicViewPagerAdapter? = null

    private val musicInfoLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()
    private val scanInThreadMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private val searchResultMusicLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()

    private val mInputMethodManager: InputMethodManager =
        GetInputMethodManager.getInputMethodManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_local_music)

        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        mHandler = LocalHandler(this)

        checkPermission()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler!!.removeCallbacksAndMessages(null)
    }

    fun init() {
        mAdapter = MyLocalMusicViewPagerAdapter(this, musicInfoLists)

        mMyLocalMusicViewPager = findViewById<ViewPager>(R.id.my_local_music_viewpager)
        mMyLocalMusicViewPager!!.setAdapter(mAdapter)
        mMyLocalMusicViewPager!!.setOffscreenPageLimit(3)
        mMyLocalMusicViewPager!!.setCurrentItem(0)
        mMyLocalMusicViewPager!!.setPageTransformer(true, object : PageTransformer {
            override fun transformPage(page: View, position: Float) {
                val MIN_SCALE = 0.85f
                val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                val rotate = 10 * abs(position)
                if (position <= -1) {
                    page.setScaleX(MIN_SCALE)
                    page.setScaleY(MIN_SCALE)
                    page.setRotationY(rotate)
                } else if (position < 0) {
                    page.setScaleX(scaleFactor)
                    page.setScaleY(scaleFactor)
                    page.setRotationY(rotate)
                } else if (position >= 0 && position < 1) {
                    page.setScaleX(scaleFactor)
                    page.setScaleY(scaleFactor)
                    page.setRotationY(-rotate)
                } else if (position >= 1) {
                    page.setScaleX(scaleFactor)
                    page.setScaleY(scaleFactor)
                    page.setRotationY(-rotate)
                }
            }
        })

        mMyLocalMusicTab = findViewById<TabLayout>(R.id.my_local_music_tab)
        mMyLocalMusicTab!!.setupWithViewPager(mMyLocalMusicViewPager)
        mMyLocalMusicTab!!.setTabTextColors(Color.BLACK, Color.BLUE)
    }

    /**
     * 申请storage权限
     */
    fun checkPermission() {
        if (checkSelfPermission(permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE
            )
        } else {
            scanLocalMusic()
        }
    }

    /**
     * 申请storage权限结果
     * 
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_STORAGE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scanLocalMusic()
            }
        } else {
            Toast.makeText(this, R.string.my_local_music_scan_permission, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 扫描本地音乐
     */
    @RequiresPermission(permission.READ_EXTERNAL_STORAGE)
    @WorkerThread
    fun scanLocalMusic() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                scanInThreadMusicLists.clear()
                val localMusicResolver = getContentResolver()
                val localMusicCursor = localMusicResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                )
                if (localMusicCursor == null) {
                    return
                }
                while (localMusicCursor.moveToNext()) {
                    val musicInfo = MusicInfo()
                    var name =
                        localMusicCursor.getString(localMusicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    if (name != null && name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf("."))
                    }
                    musicInfo.setMusicSongName(name)
                    musicInfo.setSinger(
                        localMusicCursor.getString(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media.ARTIST
                            )
                        )
                    )
                    musicInfo.setMusicFileSize(
                        localMusicCursor.getLong(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media.SIZE
                            )
                        )
                    )
                    musicInfo.setMusicFilePath(
                        localMusicCursor.getString(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media.DATA
                            )
                        )
                    )
                    musicInfo.setId(
                        localMusicCursor.getString(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media._ID
                            )
                        )
                    )
                    musicInfo.setAlbum(
                        localMusicCursor.getString(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media.ALBUM
                            )
                        )
                    )
                    musicInfo.setAlbumID(
                        localMusicCursor.getInt(
                            localMusicCursor.getColumnIndex(
                                MediaStore.Audio.Media.ALBUM_ID
                            )
                        )
                    )
                    scanInThreadMusicLists.add(musicInfo)
                }
                localMusicCursor.close()
                val message = Message.obtain()
                message.what = SCAN_LOCAL_MUSIC_DONE
                mHandler!!.sendMessage(message)
            }
        })
    }

    fun update() {
        musicInfoLists.clear()
        musicInfoLists.addAll(scanInThreadMusicLists)
        mAdapter!!.updateData(musicInfoLists)
        setTitle(getString(R.string.my_local_music_text) + musicInfoLists.size)
    }

    /**
     * 创建标题栏菜单
     * 
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.music_local_search_menu, menu)
        mSearchLocalMusic =
            menu.findItem(R.id.search_local_music_button).getActionView() as SearchView?
        mMyLocalMusicTab!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (mMyLocalMusicTab!!.getSelectedTabPosition()) {
                    0 -> {
                        setTitle(getString(R.string.my_local_music_text) + mAdapter!!.getDataCount(0))
                        mSearchLocalMusic!!.setVisibility(View.VISIBLE)
                    }

                    1 -> {
                        setTitle(
                            getString(R.string.my_local_singer_text) + mAdapter!!.getDataCount(
                                1
                            )
                        )
                        mSearchLocalMusic!!.setVisibility(View.GONE)
                    }

                    2 -> {
                        setTitle(getString(R.string.my_local_album_text) + mAdapter!!.getDataCount(2))
                        mSearchLocalMusic!!.setVisibility(View.GONE)
                    }

                    3 -> {
                        setTitle(getString(R.string.my_local_path_text) + mAdapter!!.getDataCount(3))
                        mSearchLocalMusic!!.setVisibility(View.GONE)
                    }

                    else -> {}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        mSearchLocalMusic!!.setQueryHint(getString(R.string.search_local_music))
        mSearchLocalMusic!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == " " || newText == "") {
                    mAdapter!!.updateData(musicInfoLists)
                    return false
                }
                searchResultMusicLists.clear()
                for (musicInfo in musicInfoLists) {
                    val tmp = MusicInfo(
                        musicInfo.getMusicSongName(),
                        musicInfo.getSinger(),
                        musicInfo.getMusicFilePath(),
                        musicInfo.getId(),
                        musicInfo.getMusicFileSize()
                    )
                    tmp.getIsMusicMatch().setKeyLength(newText.length)
                    if (musicInfo.getMusicSongName().contains(newText)) {
                        Log.d("music", "getMusicSongName")
                        tmp.getIsMusicMatch().setMusicNameMatch(true)
                        tmp.getIsMusicMatch()
                            .setMusicNameStart(musicInfo.getMusicSongName().indexOf(newText))
                    }
                    if (musicInfo.getSinger().contains(newText)) {
                        Log.d("music", "getSinger")
                        tmp.getIsMusicMatch().setSingleNameMatch(true)
                        tmp.getIsMusicMatch()
                            .setSingleNameStart(musicInfo.getSinger().indexOf(newText))
                    }
                    if (tmp.getIsMusicMatch().isMusicNameMatch() || tmp.getIsMusicMatch()
                            .isSingleNameMatch()
                    ) {
                        searchResultMusicLists.add(tmp)
                    }
                }
                mAdapter!!.updateData(searchResultMusicLists)
                return true
            }
        })
        return true
    }

    /**
     * 菜单被选中
     * 
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> if (mSearchLocalMusic!!.isIconified()) {
                super.onBackPressed()
            } else {
                mSearchLocalMusic!!.setIconified(true)
                mInputMethodManager.hideSoftInputFromWindow(
                    mSearchLocalMusic!!.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
        return true
    }

    /**
     * 返回键按下，处理收起键盘
     */
    override fun onBackPressed() {
        if (mSearchLocalMusic!!.isIconified()) {
            super.onBackPressed()
        } else {
            mSearchLocalMusic!!.setIconified(true)
            mInputMethodManager.hideSoftInputFromWindow(
                mSearchLocalMusic!!.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private class LocalHandler(activity: Activity?) : Handler() {
        var weakReference: WeakReference<Activity?>

        init {
            this.weakReference = WeakReference<Activity?>(activity)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SCAN_LOCAL_MUSIC_DONE -> (weakReference.get() as MyLocalMusicActivity).update()
                else -> {}
            }
        }
    }

    companion object {
        private const val TAG = "MyLocalMusicActivity"

        private const val READ_STORAGE = 1
        private const val SCAN_LOCAL_MUSIC_DONE = 2

        private var mHandler: LocalHandler? = null
    }
}
