package com.quibbler.sevenmusic.activity.found;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.fragment.found.PlaylistLibItemFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      PlaylistLibActivity
 * Description:    歌单库页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 18:48
 */
public class PlaylistLibActivity extends AppCompatActivity {

    //歌单库上方tab指示器
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    //歌单库tab标签名
    private String[] PLAYLIST_LIB_TAB_NAMES;
    //    歌单库tab标签对应cat名，用于request获取
    private String[] PLAYLIST_LIB_CATEGORY_NAMES;

    //每个tab对应的fragment
    private List<PlaylistLibItemFragment> mPlaylistFragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_playlist_lib);

        PLAYLIST_LIB_TAB_NAMES = getResources().getStringArray(R.array.playlist_lib_tab_names);
        PLAYLIST_LIB_CATEGORY_NAMES = getResources().getStringArray(R.array.playlist_lib_category_names);
        mPlaylistFragments = new ArrayList<>(PLAYLIST_LIB_TAB_NAMES.length);
        init();
    }

    /**
     * 初始化页面
     */
    private void init() {
        initActionBar();
        initTabLayout();
        initViewPager();
    }

    /**
     * 初始化页面的actionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("歌单库 ");
    }

    /**
     * 初始化页面的tabLayout
     */
    private void initTabLayout() {
        mTabLayout = findViewById(R.id.playlist_lib_tablayout);

        //为tabLayout手动添加标签
        for (int i = 0; i < PLAYLIST_LIB_TAB_NAMES.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(PLAYLIST_LIB_TAB_NAMES[i]));
        }
        //为tabLayout添加一个监听器。重写对应的方法，以设定tab的属性
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabTextView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabTextView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //设置tab指示器可以滚动
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private void updateTabTextView(TabLayout.Tab tab, boolean isSelect) {
        //第一次要判空
        View view = tab.getCustomView();
        if (null == view) {
            tab.setCustomView(R.layout.tab_found_lib_custom);
        }

        //设置标签是否选中的样式
        if (isSelect) {
            //选中的样式
            TextView tabSelect = tab.getCustomView().findViewById(R.id.tab_found_lib_tv);
            tabSelect.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tabSelect.setTextColor(getResources().getColor(R.color.found_tab_selected));
            tabSelect.setText(tab.getText());
            tabSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        } else {
            //未选中的样式
            TextView tabUnSelect = tab.getCustomView().findViewById(R.id.tab_found_lib_tv);
            tabUnSelect.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tabUnSelect.setTextColor(getResources().getColor(R.color.found_tab_unselected));
            tabUnSelect.setText(tab.getText());
            tabUnSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
    }

    /**
     * 初始化viewPager
     */
    private void initViewPager() {
        //创建歌单的对应页面。可以考虑按需创建，后期优化
        for (int i = 0; i < PLAYLIST_LIB_TAB_NAMES.length; i++) {
            mPlaylistFragments.add(new PlaylistLibItemFragment(PLAYLIST_LIB_CATEGORY_NAMES[i], PLAYLIST_LIB_TAB_NAMES[i]));
        }
        mViewPager = findViewById(R.id.playlist_lib_viewpager);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mPlaylistFragments.get(position);
            }

            @Override
            public int getCount() {
                return mPlaylistFragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return PLAYLIST_LIB_TAB_NAMES[position];
            }
        });
        //将tabLayout和viewPager绑定
        mTabLayout.setupWithViewPager(mViewPager);
        //放弃：为viewPager设置缓存，否则来回切换时会重复加载，但是fragment用懒加载方式，考虑后期优化。
        //新方案：用图片缓存代替fragment缓存。
//        mViewPager.setOffscreenPageLimit(5);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
