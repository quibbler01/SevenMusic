package com.quibbler.sevenmusic.adapter.my

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.quibbler.sevenmusic.fragment.my.MyCollectionSongFragment

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionViewsAdapter
 * Description:    收藏
 * Author:         zhaopeng
 * CreateDate:     2019/9/19 17:28
 */
class MyCollectionViewsAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val titles = arrayOf<String?>("歌曲", "歌手", "专辑")

    override fun getItem(position: Int): Fragment {
        return MyCollectionSongFragment(position)
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
