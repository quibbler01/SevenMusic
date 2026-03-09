package com.quibbler.sevenmusic.adapter.splash;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.quibbler.sevenmusic.Constant;
import com.quibbler.sevenmusic.MainActivity;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.utils.APPUtil;
import com.quibbler.sevenmusic.utils.ResUtil;
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.splash
 * ClassName:      SplashPagerAdapter
 * Description:    导航页Adapter
 * Author:         11103876
 * CreateDate:     2019/10/7 15:09
 */
public class SplashPagerAdapter extends PagerAdapter {

    private TextView mEnterTv;

    private List<View> mViews;
    private Activity mActivity;

    public SplashPagerAdapter(List<View> mViews, Activity mActivity) {
        this.mViews = mViews;
        this.mActivity = mActivity;
    }

    @Override
    public int getCount() {
        return (mViews == null) ? 0 : mViews.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViews.get(position), 0);

        if (position == mViews.size() - 1) {    // 当滑动到启动页最后一页的时候，监听按钮
            mEnterTv = container.findViewById(R.id.splash_tv_guide_enter);
            mEnterTv.setText(String.format(ResUtil.getString(R.string.str_splash_enter), APPUtil.getVersionName(mActivity)));
            mEnterTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.getInstance().saveData(Constant.KEY_IS_FIRST_LOGIN, true); // 保存第一次启动的记录
                    ActivityStart.startActivity(mActivity, MainActivity.class);
                    mActivity.finish();
                }
            });
        }
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViews.get(position));
    }

}
