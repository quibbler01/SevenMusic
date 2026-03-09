package com.quibbler.sevenmusic.activity.my

import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager.widget.ViewPager.PageTransformer
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.my.MyCollectionViewsAdapter
import kotlin.math.abs

/**
 * Package:        com.quibbler.sevenmusic.activity.my
 * ClassName:      MyCollectionsActivity
 * Description:    我的收藏:歌曲，歌手，专辑收藏,解决收藏不同步的问题
 * 20191010 歌曲、歌手、专辑收藏功能完善 取消收藏，撤销恢复；歌曲，歌手图片加载,断网显示默认图标
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:22
 */
class MyCollectionsActivity : AppCompatActivity(), View.OnClickListener {
    private var mCollectionViewPager: ViewPager? = null
    private var mAdapter: MyCollectionViewsAdapter? = null

    private var mSongTitle: TextView? = null
    private var mSingerTitle: TextView? = null
    private var mAlbumTitle: TextView? = null

    private var mSongLine: View? = null
    private var mSingerLine: View? = null
    private var mAlbumLine: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setBackgroundDrawable(null)
        setContentView(R.layout.activity_my_clooections)

        init()
    }

    fun init() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle(getString(R.string.my_collection_music_text))

        mSongTitle = findViewById<TextView>(R.id.my_collection_title_song)
        mSingerTitle = findViewById<TextView>(R.id.my_collection_title_singer)
        mAlbumTitle = findViewById<TextView>(R.id.my_collection_title_album)

        mSongTitle!!.setOnClickListener(this)
        mSingerTitle!!.setOnClickListener(this)
        mAlbumTitle!!.setOnClickListener(this)

        mSongLine = findViewById<View>(R.id.my_collection_line_song)
        mSingerLine = findViewById<View>(R.id.my_collection_line_singer)
        mAlbumLine = findViewById<View>(R.id.my_collection_line_album)

        mAdapter = MyCollectionViewsAdapter(getSupportFragmentManager())
        mCollectionViewPager = findViewById<ViewPager>(R.id.my_collection_viewpager)
        mCollectionViewPager!!.setAdapter(mAdapter)
        mCollectionViewPager!!.setCurrentItem(0)
        mCollectionViewPager!!.setOffscreenPageLimit(1)

        mCollectionViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                changeSelectStats(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        mCollectionViewPager!!.setPageTransformer(true, object : PageTransformer {
            override fun transformPage(view: View, position: Float) {
                val MIN_SCALE = 0.75f
                val pageWidth = view.getWidth()

                if (position < -1) {
                    view.setAlpha(0f)
                } else if (position <= 0) {
                    view.setAlpha(1f)
                    view.setTranslationX(0f)
                    view.setScaleX(1f)
                    view.setScaleY(1f)
                } else if (position <= 1) {
                    view.setAlpha(1 - position)
                    view.setTranslationX(pageWidth * -position)
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    view.setScaleX(scaleFactor)
                    view.setScaleY(scaleFactor)
                } else {
                    view.setAlpha(0f)
                }
            }
        })
    }

    private fun changeSelectStats(position: Int) {
        when (position) {
            0 -> {
                mSongTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                mSingerTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mAlbumTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mSongLine!!.setBackgroundColor(getColor(R.color.my_collection_line_select_color))
                mSingerLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mAlbumLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mCollectionViewPager!!.setCurrentItem(0)
            }

            1 -> {
                mSongTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mSingerTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                mAlbumTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mSongLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mSingerLine!!.setBackgroundColor(getColor(R.color.my_collection_line_select_color))
                mAlbumLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mCollectionViewPager!!.setCurrentItem(1)
            }

            2 -> {
                mSongTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mSingerTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                mAlbumTitle!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                mSongLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mSingerLine!!.setBackgroundColor(getColor(R.color.my_collection_line_color))
                mAlbumLine!!.setBackgroundColor(getColor(R.color.my_collection_line_select_color))
                mCollectionViewPager!!.setCurrentItem(2)
            }

            else -> {}
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> finish()
            else -> {}
        }
        return true
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.my_collection_title_song) {
            changeSelectStats(0)
        } else if (v.getId() == R.id.my_collection_title_singer) {
            changeSelectStats(1)
        } else if (v.getId() == R.id.my_collection_title_album) {
            changeSelectStats(2)
        }
    }

    companion object {
        private const val TAG = "MyCollectionsActivity"
    }
}
