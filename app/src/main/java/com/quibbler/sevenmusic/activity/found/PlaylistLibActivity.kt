package com.quibbler.sevenmusic.activity.found

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.fragment.found.PlaylistLibItemFragment

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      PlaylistLibActivity
 * Description:    歌单库页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 18:48
 */
class PlaylistLibActivity : AppCompatActivity() {
    //歌单库上方tab指示器
    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager? = null

    //歌单库tab标签名
    private var PLAYLIST_LIB_TAB_NAMES: Array<String?>

    //    歌单库tab标签对应cat名，用于request获取
    private var PLAYLIST_LIB_CATEGORY_NAMES: Array<String?>

    //每个tab对应的fragment
    private var mPlaylistFragments: MutableList<PlaylistLibItemFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_playlist_lib)

        PLAYLIST_LIB_TAB_NAMES = getResources().getStringArray(R.array.playlist_lib_tab_names)
        PLAYLIST_LIB_CATEGORY_NAMES =
            getResources().getStringArray(R.array.playlist_lib_category_names)
        mPlaylistFragments = ArrayList<PlaylistLibItemFragment>(PLAYLIST_LIB_TAB_NAMES.size)
        init()
    }

    /**
     * 初始化页面
     */
    private fun init() {
        initActionBar()
        initTabLayout()
        initViewPager()
    }

    /**
     * 初始化页面的actionBar
     */
    private fun initActionBar() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle("歌单库 ")
    }

    /**
     * 初始化页面的tabLayout
     */
    private fun initTabLayout() {
        mTabLayout = findViewById<TabLayout>(R.id.playlist_lib_tablayout)

        //为tabLayout手动添加标签
        for (i in PLAYLIST_LIB_TAB_NAMES.indices) {
            mTabLayout!!.addTab(mTabLayout!!.newTab().setText(PLAYLIST_LIB_TAB_NAMES[i]))
        }
        //为tabLayout添加一个监听器。重写对应的方法，以设定tab的属性
        mTabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabTextView(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabTextView(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        //设置tab指示器可以滚动
        mTabLayout!!.setTabMode(TabLayout.MODE_SCROLLABLE)
    }

    private fun updateTabTextView(tab: TabLayout.Tab, isSelect: Boolean) {
        //第一次要判空
        val view = tab.getCustomView()
        if (null == view) {
            tab.setCustomView(R.layout.tab_found_lib_custom)
        }

        //设置标签是否选中的样式
        if (isSelect) {
            //选中的样式
            val tabSelect = tab.getCustomView()!!.findViewById<TextView>(R.id.tab_found_lib_tv)
            tabSelect.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
            tabSelect.setTextColor(getResources().getColor(R.color.found_tab_selected))
            tabSelect.setText(tab.getText())
            tabSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        } else {
            //未选中的样式
            val tabUnSelect = tab.getCustomView()!!.findViewById<TextView>(R.id.tab_found_lib_tv)
            tabUnSelect.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            tabUnSelect.setTextColor(getResources().getColor(R.color.found_tab_unselected))
            tabUnSelect.setText(tab.getText())
            tabUnSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        //创建歌单的对应页面。可以考虑按需创建，后期优化
        for (i in PLAYLIST_LIB_TAB_NAMES.indices) {
            mPlaylistFragments!!.add(
                PlaylistLibItemFragment(
                    PLAYLIST_LIB_CATEGORY_NAMES[i],
                    PLAYLIST_LIB_TAB_NAMES[i]
                )
            )
        }
        mViewPager = findViewById<ViewPager>(R.id.playlist_lib_viewpager)

        mViewPager!!.setAdapter(object : FragmentPagerAdapter(getSupportFragmentManager()) {
            override fun getItem(position: Int): Fragment {
                return mPlaylistFragments!!.get(position)
            }

            override fun getCount(): Int {
                return mPlaylistFragments!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return PLAYLIST_LIB_TAB_NAMES[position]
            }
        })
        //将tabLayout和viewPager绑定
        mTabLayout!!.setupWithViewPager(mViewPager)
        //放弃：为viewPager设置缓存，否则来回切换时会重复加载，但是fragment用懒加载方式，考虑后期优化。
        //新方案：用图片缓存代替fragment缓存。
//        mViewPager.setOffscreenPageLimit(5);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> super.onBackPressed()
            else -> {}
        }
        return true
    }
}
