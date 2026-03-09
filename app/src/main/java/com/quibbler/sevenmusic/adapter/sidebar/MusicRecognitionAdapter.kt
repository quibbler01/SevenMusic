package com.quibbler.sevenmusic.adapter.sidebar

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * Package:        com.quibbler.sevenmusic.adapter.sidebar
 * ClassName:      MusicRecognitionAdapter
 * Description:    ViewPager的适配器类
 * Author:         11103876
 * CreateDate:     2019/10/7 9:32
 */
class MusicRecognitionAdapter(titles: Array<String?>, views: ArrayList<View?>) : PagerAdapter() {
    /**
     * 存储ViewPager下子视图的集合实例
     */
    var mViews: ArrayList<View> = ArrayList<View>()

    /**
     * 子视图的标题
     */
    private val mTitles: Array<String?>

    init {
        this.mViews.addAll(views)
        this.mTitles = titles
    }

    /**
     * 描述：返回页卡的数量
     * 
     * @return
     */
    override fun getCount(): Int {
        return mViews.size
    }

    /**
     * 描述：判断两个对象是否相等
     * 
     * @param view
     * @param object
     * @return
     */
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    /**
     * 描述：销毁页卡
     * 
     * @param container
     * @param position
     * @param object
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mViews.get(position)) // 将子视图移出视图存储集合
    }

    /**
     * 描述：实例化页卡
     * 
     * @param container
     * @param position
     * @return
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = mViews.get(position)
        container.addView(view)
        return view
    }

    /**
     * 描述：设置页卡标签显示的标题
     * 
     * @param position
     * @return
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}
