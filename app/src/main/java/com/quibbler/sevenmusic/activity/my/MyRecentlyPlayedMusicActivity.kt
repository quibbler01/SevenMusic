package com.quibbler.sevenmusic.activity.my

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MusicAdapter
import com.quibbler.sevenmusic.adapter.my.MyRecentPlayedMusicAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyRecentlyPlayedMusicActivity
 * Description:    最近播放Activity
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:20
 */
class MyRecentlyPlayedMusicActivity : AppCompatActivity() {
    private val RESULT_OK = 0
    private val RESULT_GO_TO_FOUND = 1

    private var mPlayedMusicListView: ListView? = null
    private var mNonePlayedMusicFoundViewStub: ViewStub? = null
    private var mAdapter: MusicAdapter? = null
    private val mRecentlyPlayedMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private val mTempRecentlyPlayedMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private val playedUri: Uri =
        Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/played")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_recently_played_music)
        init()
        initData()
    }

    fun init() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle(R.string.my_played_music_text)

        mPlayedMusicListView = findViewById<ListView>(R.id.my_recently_played_music_list_view)
        mPlayedMusicListView!!.setDivider(null)

        mNonePlayedMusicFoundViewStub =
            findViewById<ViewStub>(R.id.my_recently_zero_played_music_text_view_stub)

        mAdapter = MyRecentPlayedMusicAdapter(this, mRecentlyPlayedMusicLists)
        mPlayedMusicListView!!.setAdapter(mAdapter)
    }

    fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                mTempRecentlyPlayedMusicLists.clear()
                val playedMusicCursor =
                    getContentResolver().query(playedUri, null, null, null, "last_played desc")
                if (playedMusicCursor != null) {
                    while (playedMusicCursor.moveToNext()) {
                        val musicInfo = MusicInfo()
                        musicInfo.setId(
                            playedMusicCursor.getString(
                                playedMusicCursor.getColumnIndex(
                                    "id"
                                )
                            )
                        )
                        musicInfo.setMusicSongName(
                            playedMusicCursor.getString(
                                playedMusicCursor.getColumnIndex(
                                    "name"
                                )
                            )
                        )
                        musicInfo.setSinger(
                            playedMusicCursor.getString(
                                playedMusicCursor.getColumnIndex(
                                    "singer"
                                )
                            )
                        )
                        musicInfo.setMusicFilePath(
                            playedMusicCursor.getString(
                                playedMusicCursor.getColumnIndex(
                                    "path"
                                )
                            )
                        )
                        musicInfo.setLastPlayedTime(
                            playedMusicCursor.getLong(
                                playedMusicCursor.getColumnIndex(
                                    "last_played"
                                )
                            )
                        )
                        mTempRecentlyPlayedMusicLists.add(musicInfo)
                    }
                    playedMusicCursor.close()
                }
                updateUI()
            }
        })
    }

    fun updateUI() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mTempRecentlyPlayedMusicLists.size > 0) {
                    mAdapter!!.clear()
                    mAdapter!!.addAll(mTempRecentlyPlayedMusicLists)
                    mAdapter!!.notifyDataSetChanged()
                    mNonePlayedMusicFoundViewStub!!.setVisibility(View.GONE)
                } else {
                    mNonePlayedMusicFoundViewStub!!.setVisibility(View.VISIBLE)
                    val spannableStringBuilder =
                        SpannableStringBuilder(getString(R.string.my_recently_zero_played_music_text))
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = getIntent()
                            setResult(RESULT_GO_TO_FOUND, intent)
                            finish()
                        }
                    }
                    spannableStringBuilder.setSpan(
                        clickableSpan,
                        11,
                        15,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val textView = findViewById<TextView>(R.id.my_recently_zero_played_music_text)
                    textView.setMovementMethod(LinkMovementMethod.getInstance())
                    textView.setText(spannableStringBuilder)
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
        } else if (item.getItemId() == R.id.my_download_clear_action_icon) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.my_played_record_clear)
                .setMessage("")
                .setPositiveButton(R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
                .setNegativeButton(R.string.confirm, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        mAdapter!!.clear()
                        mAdapter!!.notifyDataSetChanged()
                        mPlayedMusicListView!!.setVisibility(View.GONE)
                        mNonePlayedMusicFoundViewStub!!.setVisibility(View.VISIBLE)
                        val spannableStringBuilder =
                            SpannableStringBuilder(getString(R.string.my_recently_zero_played_music_text))
                        val clickableSpan: ClickableSpan = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                val intent = getIntent()
                                setResult(RESULT_GO_TO_FOUND, intent)
                                finish()
                            }
                        }
                        spannableStringBuilder.setSpan(
                            clickableSpan,
                            11,
                            15,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val textView =
                            findViewById<TextView>(R.id.my_recently_zero_played_music_text)
                        textView.setMovementMethod(LinkMovementMethod.getInstance())
                        textView.setText(spannableStringBuilder)
                        Toast.makeText(
                            this@MyRecentlyPlayedMusicActivity,
                            R.string.my_played_record_clear_toast,
                            Toast.LENGTH_SHORT
                        ).show()
                        MusicThreadPool.postRunnable(object : Runnable {
                            override fun run() {
                                getContentResolver().delete(playedUri, null, null)
                            }
                        })
                    }
                })
            builder.show()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.my_clear_action, menu)
        return true
    }

    companion object {
        private const val TAG = "MyRecentlyPlayedMusicActivity"
    }
}
