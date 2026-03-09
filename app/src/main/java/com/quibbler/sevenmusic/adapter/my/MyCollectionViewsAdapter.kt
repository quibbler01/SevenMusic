package com.quibbler.sevenmusic.adapter.my;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.quibbler.sevenmusic.fragment.my.MyCollectionSongFragment;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionViewsAdapter
 * Description:    收藏
 * Author:         zhaopeng
 * CreateDate:     2019/9/19 17:28
 */
public class MyCollectionViewsAdapter extends FragmentStatePagerAdapter {
    private String[] titles = {"歌曲", "歌手", "专辑"};

    public MyCollectionViewsAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new MyCollectionSongFragment(position);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
