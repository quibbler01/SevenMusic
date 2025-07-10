package com.quibbler.sevenmusic.fragment.mv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.androidkun.xtablayout.XTabLayout;
import com.example.sevenvideoview.SevenVideoPlayerManager;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.utils.NetUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * Package:        com.quibbler.sevenmusic.fragment
 * ClassName:      MvFragment
 * Description:    MvFragment类，由MainActivity托管
 * Author:         lishijun
 * CreateDate:     2019/9/16 17:43
 */

public class MvFragment extends Fragment {

    private ViewPager mChildViewPager;

    private List<Fragment> mChildMvFragmentList;

    private View mView;

    private XTabLayout mTabLayout;

    private final static String []TITLES = {"推荐", "全部MV"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载fragment_mv布局文件
        mView = inflater.inflate(R.layout.fragment_mv, null);
        if(NetUtil.getNetWorkStart(getContext()) == NetUtil.NETWORK_NONE){
            showNoNetPage();
        }else{
            hideNoNetPage();
            mChildViewPager = mView.findViewById(R.id.mv_child_pager);
            mTabLayout =mView.findViewById(R.id.mv_xTabs);
            mChildMvFragmentList = new ArrayList<>();
            mChildMvFragmentList.add(NewChildMvFragment.newInstance("/top/mv"));
            mChildMvFragmentList.add(NewTopMvFragment.newInstance("/mv/all"));
            mChildViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
                @NonNull
                @Override
                public Fragment getItem(int position) {
                    return mChildMvFragmentList.get(position);
                }

                @Override
                public int getCount() {
                    return mChildMvFragmentList.size();
                }

                @Nullable
                @Override
                public CharSequence getPageTitle(int position) {
                    return TITLES[position];
                }
            });
            mTabLayout.setupWithViewPager(mChildViewPager);
            mChildViewPager.setCurrentItem(0);
            mChildViewPager.setOffscreenPageLimit(2);
            mChildViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    releaseVideoPlayer();
                }
                @Override
                public void onPageSelected(int position) {
                    releaseVideoPlayer();
                }
                @Override
                public void onPageScrollStateChanged(int state) {
                    releaseVideoPlayer();
                }
            });
        }
        return mView;
    }

    public void releaseVideoPlayer(){
        if(mChildMvFragmentList != null && mChildMvFragmentList.size() >= 1){
            SevenVideoPlayerManager.getInstance().releaseSevenVideoPlayer();
        }
    }

    //无网络，展示提示页
    private void showNoNetPage(){
        mView.findViewById(R.id.mv_nonet_tip).setVisibility(View.VISIBLE);
    }
    //有网络，关闭提示页
    private void hideNoNetPage(){
        mView.findViewById(R.id.mv_nonet_tip).setVisibility(View.GONE);
    }
}