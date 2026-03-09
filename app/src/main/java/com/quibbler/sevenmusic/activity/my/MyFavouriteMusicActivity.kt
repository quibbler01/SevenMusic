package com.quibbler.sevenmusic.activity.my

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.FavouriteMusicAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyFavouriteMusicActivity
 * Description:    我喜欢的歌曲
 * Author:         zhaopeng
 * CreateDate:     2019/09/26 09:21
 */
class MyFavouriteMusicActivity : AppCompatActivity(), View.OnClickListener {
    private val mFavouriteMusicLists: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private var mAdapter: FavouriteMusicAdapter? = null
    private var mFavouriteMusicListView: ListView? = null
    private var mNoneMusicTextView: TextView? = null

    private var mButtonView: View? = null
    private var mSelectAll: TextView? = null
    private var mCancel: TextView? = null
    private var mDelete: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_favourite_music)

        init()
        initData()

        initManagerFunction()
    }

    fun init() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle(R.string.my_favourite_music_title)
        mNoneMusicTextView = findViewById<TextView>(R.id.my_favourite_no_music)
        val spannableStringBuilder =
            SpannableStringBuilder(getString(R.string.my_favourite_no_music))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                setResult(MyFragment.Companion.RESULT_GO_TO_FOUND)
                finish()
            }
        }
        spannableStringBuilder.setSpan(clickableSpan, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mNoneMusicTextView!!.setMovementMethod(LinkMovementMethod.getInstance())
        mNoneMusicTextView!!.setText(spannableStringBuilder)

        mFavouriteMusicListView = findViewById<ListView>(R.id.my_favourite_music_list_view)
        mFavouriteMusicListView!!.setDivider(null)
        mAdapter = FavouriteMusicAdapter(this, mFavouriteMusicLists)
        mFavouriteMusicListView!!.setAdapter(mAdapter)
        //        mFavouriteMusicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
    }

    private fun initManagerFunction() {
        mButtonView = findViewById<View>(R.id.my_music_manager_button_layout)

        mSelectAll = findViewById<TextView>(R.id.my_music_manager_menu_text_select_all)
        mCancel = findViewById<TextView>(R.id.my_music_manager_menu_text_cancel)
        mDelete = findViewById<TextView>(R.id.my_music_manager_menu_text_delete)

        mSelectAll!!.setOnClickListener(View.OnClickListener { v: View? -> this.onClick(v!!) })
        mCancel!!.setOnClickListener(View.OnClickListener { v: View? -> this.onClick(v!!) })
        mDelete!!.setOnClickListener(View.OnClickListener { v: View? -> this.onClick(v!!) })
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.my_music_manager_menu_text_select_all) {
            mAdapter!!.selectAll()
        } else if (v.getId() == R.id.my_music_manager_menu_text_cancel) {
            mAdapter!!.changeManagerState()
            mButtonView!!.setVisibility(View.GONE)
            mAdapter!!.unSelectAndReset()
        } else if (v.getId() == R.id.my_music_manager_menu_text_delete) {
            val select = mAdapter!!.getSelectCount()
            if (select == 0) {
                Toast.makeText(this, R.string.my_favourite_music_manager_toast, Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (select == mFavouriteMusicLists.size) {
                    mNoneMusicTextView!!.setVisibility(View.VISIBLE)
                }
                mAdapter!!.removeFavourite()
                mAdapter!!.changeManagerState()
                mButtonView!!.setVisibility(View.GONE)
            }
        }
    }

    fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                val tempList: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                val uri =
                    Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/favourite")
                var cursor: Cursor? = null
                try {
                    cursor = getContentResolver().query(uri, null, null, null, "rowid desc")
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val musicInfo = MusicInfo()
                            musicInfo.setId(cursor.getString(cursor.getColumnIndex("id")))
                            musicInfo.setMusicSongName(cursor.getString(cursor.getColumnIndex("name")))
                            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex("singer")))
                            musicInfo.setMusicFilePath(cursor.getString(cursor.getColumnIndex("path")))
                            tempList.add(musicInfo)
                        }
                        cursor.close()
                    }
                    updateUI(tempList)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                } finally {
                    if (cursor != null) cursor.close()
                }
            }
        })
    }

    private fun updateUI(tempList: MutableList<MusicInfo?>) {
        if (tempList.size == 0) {
            mNoneMusicTextView!!.setVisibility(View.VISIBLE)
            return
        }
        mNoneMusicTextView!!.setVisibility(View.GONE)
        mAdapter!!.update(tempList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
        } else if (item.getItemId() == R.id.my_music_manager_menu_text) {
            if (mFavouriteMusicLists.size == 0) {
                Toast.makeText(this, R.string.my_favourite_music_manager_edit, Toast.LENGTH_SHORT)
                    .show()
                return true
            }
            mAdapter!!.changeManagerState()
            if (mButtonView!!.getVisibility() == View.VISIBLE) {
                mButtonView!!.setVisibility(View.GONE)
                mAdapter!!.unSelectAndReset()
            } else {
                mButtonView!!.setVisibility(View.VISIBLE)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.my_music_favourite_manager, menu)
        return true
    }

    override fun onBackPressed() {
        if (mButtonView!!.getVisibility() == View.VISIBLE) {
            mAdapter!!.changeManagerState()
            mButtonView!!.setVisibility(View.GONE)
            mAdapter!!.unSelectAndReset()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "MyFavouriteMusicActivity"
    }
}
