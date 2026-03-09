package com.quibbler.sevenmusic.activity.my

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MusicAdapter
import com.quibbler.sevenmusic.adapter.my.PlayListMusicAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.fragment.my.MyFragment
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.ColorUtils
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils
import com.quibbler.sevenmusic.utils.MusicThreadPool
import java.text.SimpleDateFormat

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MySongListDetailActivity
 * Description:    点击歌单，进入歌单详情界面
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:49
 */
class MySongListDetailActivity : AppCompatActivity() {
    private var mListName: String? = null
    private var mCreateTime: String? = null

    private var mHeadLayout: View? = null
    private var mImageCover: ImageView? = null
    private var mListNameTextView: TextView? = null
    private var mListNameCreateTime: TextView? = null

    private var mPlayAllImageView: ImageView? = null
    private var mPlayAllTextView: TextView? = null

    private var mSongListView: ListView? = null
    private var mAdapter: MusicAdapter? = null
    private val mMusicLists: MutableList<MusicInfo?>? = ArrayList<MusicInfo?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_song_list_detail)

        initView()
        initData()
    }

    fun initView() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val intent = getIntent()
        if (intent != null) {
            mListName = intent.getStringExtra(MyFragment.Companion.TITLE_KEY)
            mCreateTime = intent.getStringExtra(MyFragment.Companion.CREATOR_KEY)
        }
        setTitle(mListName)

        mHeadLayout = findViewById<View>(R.id.playlist_ll_background)
        mImageCover = findViewById<ImageView>(R.id.playlist_iv_cover)
        mListNameTextView = findViewById<TextView>(R.id.my_song_list_detail_name)
        mListNameTextView!!.setText(mListName)
        mListNameCreateTime = findViewById<TextView>(R.id.my_song_list_detail_time_stamp)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        mListNameCreateTime!!.setText(simpleDateFormat.format(mCreateTime!!.toLong()))

        mPlayAllImageView = findViewById<ImageView>(R.id.my_song_list_detail_play_image)
        mPlayAllTextView = findViewById<TextView>(R.id.my_song_list_detail_play_text)
        mPlayAllImageView!!.setOnClickListener(mOnClickListener)
        mPlayAllTextView!!.setOnClickListener(mOnClickListener)

        mSongListView = findViewById<ListView>(R.id.my_song_list_detail_recycler_view)
        mAdapter = PlayListMusicAdapter(this, mMusicLists!!)
        mSongListView!!.setAdapter(mAdapter)
    }

    fun initData() {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val musicLists: MutableList<MusicInfo?>? =
                    MusicDatabaseUtils.getMusicList(mListName)
                if (musicLists == null || musicLists.size == 0) {
                    Log.d(TAG, "no data to be update")
                } else {
                    val bitmap = BitmapFactory.decodeFile(
                        Constant.SEVEN_MUSIC_IMAGE + "/" + musicLists.get(0)!!.getId()
                    )
                    updateListData(musicLists, bitmap)
                }
            }
        })
    }

    private fun updateListData(musicLists: MutableList<MusicInfo?>, bitmap: Bitmap?) {
        runOnUiThread(object : Runnable {
            override fun run() {
                mAdapter!!.update(musicLists)
                Glide.with(this@MySongListDetailActivity)
                    .load(Constant.SEVEN_MUSIC_IMAGE + "/" + musicLists.get(0)!!.getId())
                    .into(mImageCover!!)
                if (bitmap != null) {
                    val width = bitmap.getWidth()
                    val height = bitmap.getHeight()
                    val startColor = bitmap.getPixel(0, height - 1)
                    var midColor = bitmap.getPixel(width / 2, height / 2)
                    val endColor = bitmap.getPixel(width - 1, 0)
                    if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(midColor) && ColorUtils.isPixelShallow(
                            endColor
                        )
                    ) {
                        midColor = -1063808
                    }
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.BL_TR,
                        intArrayOf(startColor, midColor, endColor)
                    )
                    mHeadLayout!!.setBackground(gradientDrawable)
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
        }
        return true
    }

    private val mOnClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            if (v.getId() == R.id.my_song_list_detail_play_image || v.getId() == R.id.my_song_list_detail_play_text) {
                MusicPlayerService.Companion.clearPlayMusicList()
                if (mMusicLists != null && mMusicLists.size != 0) {
                    MusicPlayerService.Companion.addToPlayerList(mMusicLists)
                    mMusicLists.get(0)!!.setPlaying(true)
                    MusicPlayerService.Companion.playMusic(mMusicLists.get(0))
                }
                mAdapter!!.notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val TAG = "MySongListDetailActivity"
    }
}
