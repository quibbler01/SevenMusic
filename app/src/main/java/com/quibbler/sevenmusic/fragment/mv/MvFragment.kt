package com.quibbler.sevenmusic.fragment.mv

import android.view.View
import androidx.fragment.app.Fragment
import com.androidkun.xtablayout.XTabLayout

/**
 * 
 * Package:        com.quibbler.sevenmusic.fragment
 * ClassName:      MvFragment
 * Description:    MvFragment类，由MainActivity托管
 * Author:         lishijun
 * CreateDate:     2019/9/16 17:43
 */
class MvFragment : Fragment() {
    private var mChildViewPager: ViewPager? = null

    private var mChildMvFragmentList: MutableList<Fragment>? = null

    private var mView: View? = null

    private var mTabLayout: XTabLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 加载fragment_mv布局文件
        mView = inflater.inflate(R.layout.fragment_mv, null)
        if (NetUtil.getNetWorkStart(getContext()) == NetUtil.NETWORK_NONE) {
            showNoNetPage()
        } else {
            hideNoNetPage()
            mChildViewPager = mView!!.findViewById<ViewPager>(R.id.mv_child_pager)
            mTabLayout = mView!!.findViewById(R.id.mv_xTabs)
            mChildMvFragmentList = ArrayList<Fragment>()
            mChildMvFragmentList!!.add(NewChildMvFragment.Companion.newInstance("/top/mv"))
            mChildMvFragmentList!!.add(NewTopMvFragment.Companion.newInstance("/mv/all"))
            mChildViewPager.setAdapter(object : FragmentPagerAdapter(getFragmentManager()) {
                override fun getItem(position: Int): Fragment {
                    return mChildMvFragmentList!!.get(position)
                }

                val count: Int
                    get() = mChildMvFragmentList!!.size

                override fun getPageTitle(position: Int): CharSequence? {
                    return TITLES[position]
                }
            })
            mTabLayout.setupWithViewPager(mChildViewPager)
            mChildViewPager.setCurrentItem(0)
            mChildViewPager.setOffscreenPageLimit(2)
            mChildViewPager.setOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    releaseVideoPlayer()
                }

                override fun onPageSelected(position: Int) {
                    releaseVideoPlayer()
                }

                override fun onPageScrollStateChanged(state: Int) {
                    releaseVideoPlayer()
                }
            })
        }
        return mView!!
    }

    fun releaseVideoPlayer() {
        if (mChildMvFragmentList != null && mChildMvFragmentList!!.size >= 1) {
            SevenVideoPlayerManager.getInstance().releaseSevenVideoPlayer()
        }
    }

    //无网络，展示提示页
    private fun showNoNetPage() {
        mView!!.findViewById<View?>(R.id.mv_nonet_tip).setVisibility(View.VISIBLE)
    }

    //有网络，关闭提示页
    private fun hideNoNetPage() {
        mView!!.findViewById<View?>(R.id.mv_nonet_tip).setVisibility(View.GONE)
    }

    companion object {
        private val TITLES = arrayOf<String?>("推荐", "全部MV")
    }
}