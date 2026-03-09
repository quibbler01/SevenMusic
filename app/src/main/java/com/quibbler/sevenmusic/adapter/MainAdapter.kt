package com.quibbler.sevenmusic.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MainAdapter
 * Description:    MainActivity的适配器类,实现主页面fragment资源的配置
 * 
 * 
 * 注意与FragmentStatePagerAdapter的区别，后面可尝试实现该适配器
 * Author:         11103876
 * CreateDate:     2019/10/9 20:45
 */
class MainAdapter(fm: FragmentManager, mFragments: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {
    /**
     * 保存my、found、mv的Fragment集合实例
     */
    private var mFragments = ArrayList<Fragment>()

    init {
        this.mFragments = mFragments
    }


    /**
     * 描述：获取指定的fragment
     * 
     * @param position fragment的索引
     * @return
     */
    override fun getItem(position: Int): Fragment {
        return mFragments.get(position)
    }

    /**
     * 描述：返回fragment的数量
     * 
     * @return
     */
    override fun getCount(): Int {
        return mFragments.size
    }
}
