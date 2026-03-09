package com.quibbler.sevenmusic.adapter.sidebar;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

/**
 * Package:        com.quibbler.sevenmusic.adapter.sidebar
 * ClassName:      MusicRecognitionAdapter
 * Description:    ViewPager的适配器类
 * Author:         11103876
 * CreateDate:     2019/10/7 9:32
 */
public class MusicRecognitionAdapter extends PagerAdapter {

    /**
     * 存储ViewPager下子视图的集合实例
     */
    public ArrayList<View> mViews = new ArrayList<>();
    /**
     * 子视图的标题
     */
    private String[] mTitles;

    public MusicRecognitionAdapter(String[] titles, ArrayList<View> views) {
        this.mViews.addAll(views);
        this.mTitles = titles;
    }

    /**
     * 描述：返回页卡的数量
     *
     * @return
     */
    @Override
    public int getCount() {
        return mViews.size();
    }

    /**
     * 描述：判断两个对象是否相等
     *
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * 描述：销毁页卡
     *
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViews.get(position)); // 将子视图移出视图存储集合

    }

    /**
     * 描述：实例化页卡
     *
     * @param container
     * @param position
     * @return
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = mViews.get(position);
        container.addView(view);
        return view;
    }

    /**
     * 描述：设置页卡标签显示的标题
     *
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
