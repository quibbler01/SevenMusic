package com.quibbler.sevenmusic.activity.my

import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.adapter.my.MyCollectionMvAdapter
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.utils.CloseResourceUtil
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyCollectionMVActivity
 * Description:    收藏MV 长按编辑、播放 。mv收藏展示
 * Author:         zhaopeng
 * CreateDate:     2019/9/28 15:22
 */
class MyCollectionMVActivity : AppCompatActivity() {
    private var mNoMvCollectionHint: TextView? = null
    private var mCollectionMV: ListView? = null
    private var mAdapter: MyCollectionMvAdapter? = null
    private val mLists: MutableList<MVShowInfo?> = ArrayList<MVShowInfo?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_bought_music)
        init()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        registerForContextMenu(mCollectionMV)
    }

    override fun onPause() {
        super.onPause()
        unregisterForContextMenu(mCollectionMV)
    }

    @MainThread
    fun init() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle(getString(R.string.my_collection_mv))

        mNoMvCollectionHint = findViewById<TextView>(R.id.my_bought_no_music_found)
        val spannableStringBuilder =
            SpannableStringBuilder(getString(R.string.my_bought_none_music_text))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                setResult(MyFragment.Companion.RESULT_GO_TO_FOUND)
                finish()
            }
        }
        spannableStringBuilder.setSpan(clickableSpan, 11, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mNoMvCollectionHint!!.setMovementMethod(LinkMovementMethod.getInstance())
        mNoMvCollectionHint!!.setText(spannableStringBuilder)

        mCollectionMV = findViewById<ListView>(R.id.my_collection_mv)
        mCollectionMV!!.setDivider(null)

        mAdapter = MyCollectionMvAdapter(this, R.layout.my_collection_mv_item, mLists)
        mCollectionMV!!.setAdapter(mAdapter)
    }

    private fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val temp: MutableList<MVShowInfo?> = ArrayList<MVShowInfo?>()
                var cursor: Cursor? = null
                try {
                    cursor = getContentResolver().query(
                        MusicContentProvider.Companion.MV_URL,
                        null,
                        null,
                        null,
                        "rowid desc"
                    )
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val mvInfo = MVShowInfo()
                            mvInfo.id = cursor.getString(cursor.getColumnIndex("id"))
                            mvInfo.name = cursor.getString(cursor.getColumnIndex("name"))
                            mvInfo.pictureurl =
                                cursor.getString(cursor.getColumnIndex("pictureurl"))
                            temp.add(mvInfo)
                        }
                        mHandler.post(object : Runnable {
                            override fun run() {
                                if (temp.size == 0) {
                                    mNoMvCollectionHint!!.setVisibility(View.VISIBLE)
                                } else {
                                    mNoMvCollectionHint!!.setVisibility(View.GONE)
                                }
                                mAdapter!!.updateData(temp)
                                popTips()
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    CloseResourceUtil.closeInputAndOutput(cursor)
                }
            }
        })
    }

    private fun popTips() {
        if (mLists.size != 0) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    try {
                        Thread.sleep(500)
                        mHandler.post(object : Runnable {
                            override fun run() {
                                Snackbar.make(
                                    mNoMvCollectionHint!!,
                                    getString(R.string.my_collection_mv_onlong_click),
                                    Snackbar.LENGTH_LONG
                                ).setAction(
                                    getString(R.string.my_collection_mv_onlong_know),
                                    object : View.OnClickListener {
                                        override fun onClick(v: View?) {
                                            //
                                        }
                                    }).show()
                            }
                        })
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(getString(R.string.my_collection_mv_manager))
        menu.add(0, 1, 1, getString(R.string.my_collection_mv_manager_play))
        menu.add(0, 2, 2, getString(R.string.my_collection_mv_manager_delete))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.getMenuInfo() as AdapterContextMenuInfo?
        val pos = mCollectionMV!!.getAdapter().getItemId(menuInfo!!.position).toInt()
        when (item.getItemId()) {
            1 -> {
                val mvInfo = MvInfo(
                    mLists.get(pos)!!.id!!.toInt(),
                    mLists.get(pos)!!.name, null, 0, null,
                    mLists.get(pos)!!.pictureurl
                )
                ActivityStart.startMvPlayActivity(this@MyCollectionMVActivity, mvInfo)
            }

            2 -> {
                getContentResolver().delete(
                    MusicContentProvider.Companion.MV_URL, "id = ?", arrayOf<String?>(
                        mLists.get(pos)!!.id
                    )
                )
                mLists.removeAt(pos)
                mAdapter!!.notifyDataSetChanged()
                if (mLists.size == 0) {
                    mNoMvCollectionHint!!.setVisibility(View.VISIBLE)
                }
            }

            else -> {}
        }
        return true
    }

    inner class MVShowInfo {
        var id: String? = ""
        var name: String? = ""
        var pictureurl: String? = ""
    }

    companion object {
        private const val TAG = "MyCollectionMVActivity"

        private val mHandler = Handler(Looper.getMainLooper())
    }
}
