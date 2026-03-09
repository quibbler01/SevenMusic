package com.quibbler.sevenmusic.adapter.splash

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.quibbler.sevenmusic.Constant
import com.quibbler.sevenmusic.MainActivity
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.utils.APPUtil
import com.quibbler.sevenmusic.utils.ResUtil
import com.quibbler.sevenmusic.utils.SharedPreferencesUtils

/**
 * Package:        com.quibbler.sevenmusic.adapter.splash
 * ClassName:      SplashPagerAdapter
 * Description:    导航页Adapter
 * Author:         11103876
 * CreateDate:     2019/10/7 15:09
 */
class SplashPagerAdapter(mViews: MutableList<View>?, mActivity: Activity) : PagerAdapter() {
    private var mEnterTv: TextView? = null

    private val mViews: MutableList<View>?
    private val mActivity: Activity

    init {
        this.mViews = mViews
        this.mActivity = mActivity
    }

    override fun getCount(): Int {
        return if (mViews == null) 0 else mViews.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(mViews!!.get(position), 0)

        if (position == mViews.size - 1) {    // 当滑动到启动页最后一页的时候，监听按钮
            mEnterTv = container.findViewById<TextView>(R.id.splash_tv_guide_enter)
            mEnterTv!!.setText(
                String.format(
                    ResUtil.getString(R.string.str_splash_enter),
                    APPUtil.Companion.getVersionName(mActivity)
                )
            )
            mEnterTv!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    SharedPreferencesUtils.Companion.getInstance()
                        .saveData(Constant.KEY_IS_FIRST_LOGIN, true) // 保存第一次启动的记录
                    ActivityStart.startActivity(mActivity, MainActivity::class.java)
                    mActivity.finish()
                }
            })
        }
        return mViews.get(position)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view === `object`)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(mViews!!.get(position))
    }
}
