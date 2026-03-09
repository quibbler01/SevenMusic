package com.quibbler.sevenmusic.activity.my

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MyChooseMusicAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      ChooseMusicActivity
 * Description:    选择歌曲作为铃声
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 19:35
 */
class ChooseMusicActivity : AppCompatActivity() {
    private val isChoose = false
    private var mPosition = 0

    private var mListView: ListView? = null
    private var mAdapter: MyChooseMusicAdapter? = null
    private val mMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_choose_music)

        init()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        }
        mListView = findViewById<ListView>(R.id.music_choose_music_list_view)
        setTitle(R.string.my_choose_ring)
        mAdapter = MyChooseMusicAdapter(this, mMusicLists)
        mListView!!.setAdapter(mAdapter)
        mListView!!.setDivider(null)
        mListView!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mPosition = position
                val resultIntent = getIntent()
                resultIntent.putExtra("path", mMusicLists.get(position)!!.getMusicFilePath())
                resultIntent.putExtra("name", mMusicLists.get(position)!!.getMusicSongName())
                resultIntent.putExtra("id", mMusicLists.get(position)!!.getId())
                resultIntent.putExtra("singer", mMusicLists.get(position)!!.getSinger())
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        })
        initData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                if (isChoose) {
                    val resultIntent = getIntent()
                    resultIntent.putExtra("path", mMusicLists.get(mPosition)!!.getMusicFilePath())
                    resultIntent.putExtra("name", mMusicLists.get(mPosition)!!.getMusicSongName())
                    resultIntent.putExtra("id", mMusicLists.get(mPosition)!!.getId())
                    resultIntent.putExtra("singer", mMusicLists.get(mPosition)!!.getSinger())
                    setResult(RESULT_OK, resultIntent)
                } else {
                    val resultIntent = getIntent()
                    setResult(RESULT_NONE, resultIntent)
                }
                onBackPressed()
            }

            else -> {}
        }
        return true
    }

    private fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val temp: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                var localMusicCursor: Cursor? = null
                try {
                    localMusicCursor = getContentResolver().query(
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
                        musicInfo.setMusicSongName(
                            localMusicCursor.getString(
                                localMusicCursor.getColumnIndex(
                                    MediaStore.Audio.Media.DISPLAY_NAME
                                )
                            )
                        )
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
                        temp.add(musicInfo)
                    }
                    updateUI(temp)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                } finally {
                    CloseResourceUtil.closeInputAndOutput(localMusicCursor)
                }
            }
        })
    }

    private fun updateUI(lists: MutableList<MusicInfo?>) {
        runOnUiThread(object : Runnable {
            override fun run() {
                mAdapter!!.clear()
                mAdapter!!.addAll(lists)
                mAdapter!!.notifyDataSetChanged()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isChoose) {
            val resultIntent = getIntent()
            resultIntent.putExtra("path", mMusicLists.get(mPosition)!!.getMusicFilePath())
            resultIntent.putExtra("name", mMusicLists.get(mPosition)!!.getMusicSongName())
            resultIntent.putExtra("id", mMusicLists.get(mPosition)!!.getId())
            resultIntent.putExtra("singer", mMusicLists.get(mPosition)!!.getSinger())
            setResult(RESULT_OK, resultIntent)
        } else {
            val resultIntent = getIntent()
            setResult(RESULT_NONE, resultIntent)
        }
    }

    companion object {
        private const val TAG = "ChooseMusicActivity"

        const val RESULT_OK: Int = 0
        val RESULT_NONE: Int = -1
    }
}
