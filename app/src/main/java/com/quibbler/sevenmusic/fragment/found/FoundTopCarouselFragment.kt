package com.quibbler.sevenmusic.fragment.found

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.gson.Gson
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopCarouselResponseBean
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 * Package:        com.quibbler.sevenmusic.fragment.found
 * ClassName:      FoundTopCarouselFragment
 * Description:    轮播图fragment
 * Author:         yanwuyang
 * CreateDate:     2019/10/10 16:14
 */
class FoundTopCarouselFragment : Fragment() {
    //实际轮播图数量
    private var mRealTopCarouselNum = 0

    //可见轮播图数量
    private var mShowTopCarouselNum = 0

    private var mViewPager: ViewPager? = null
    private var mDotIvArray: Array<ImageView?>

    //    private View mViewPlaceholder;
    //    private RelativeLayout mRlCarousel;
    private var mRequestImageAsyncTask: RequestImageAsyncTask? = null
    private val mMusicInfoList: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
    private val mBannerImgUrlList: MutableList<String?> = ArrayList<String?>()
    private val mTopItemFragmentList: MutableList<FoundTopCarouselItemFragment> =
        ArrayList<FoundTopCarouselItemFragment>()

    private var mChildFragmentManager: FragmentManager? = null

    private var mCurrentPosition = 1
    private var mTimerThread: Thread? = null

    private class TimerHandler(topCarouselFragment: FoundTopCarouselFragment?) : Handler() {
        var mWeakReference: WeakReference<FoundTopCarouselFragment?>

        init {
            mWeakReference = WeakReference<FoundTopCarouselFragment?>(topCarouselFragment)
        }

        override fun handleMessage(msg: Message) {
            if (mWeakReference.get() == null) {
                return
            }
            val fragment = mWeakReference.get()
            if (fragment == null) {
                return
            }
            when (msg.what) {
                SET_VIEWPAGER_ITEM -> if (fragment.mViewPager != null) {
                    val currentItemIndex = fragment.mViewPager!!.getCurrentItem()
                    fragment.mViewPager!!.setCurrentItem(currentItemIndex + 1, true)
                }
            }
        }
    }

    private var mHandler: Handler? = null

    //定时器
    private val mRunnable: Runnable = object : Runnable {
        override fun run() {
            val message = mHandler!!.obtainMessage()
            message.what = SET_VIEWPAGER_ITEM

            mHandler!!.sendMessage(message)
            //在定时器中把自己移出MQ，再自己把自己加入MQ并延时执行
            mHandler!!.removeCallbacks(mRunnable)
            mHandler!!.postDelayed(this, INTERVAL.toLong())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_found_top_carousel, container, false)
        mViewPager = view.findViewById<ViewPager?>(R.id.found_vp_top_carousel)

        mChildFragmentManager = getChildFragmentManager()

        mHandler = TimerHandler(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mTimerThread = Thread(mRunnable)
        mTimerThread!!.start()

        initTopPlaceholder()

        mRequestImageAsyncTask = RequestImageAsyncTask()
        mRequestImageAsyncTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler!!.removeCallbacks(mRunnable)
        if (mTimerThread != null) {
            mTimerThread!!.interrupt()
        }
        if (mRequestImageAsyncTask != null) {
            mRequestImageAsyncTask!!.cancel(true)
        }
    }

    private fun initTopPlaceholder() {
        //fragment嵌套fragment时，要用getChildFragmentManager而不是getActivity.getSupportFragmentManager。
        mViewPager!!.setAdapter(object : FragmentPagerAdapter(mChildFragmentManager!!) {
            override fun getItem(position: Int): Fragment {
                return FoundTopPlaceholderFragment()
            }

            override fun getCount(): Int {
                //预加载时占位用的，所以直接返回1即可
                return 1
            }
        })
    }

    private fun initDots(view: View) {
        mDotIvArray = arrayOfNulls<ImageView>(mShowTopCarouselNum)
        val linearLayout = view.findViewById<LinearLayout>(R.id.found_ll_carousel_dots)
        val layoutParams = LinearLayout.LayoutParams(20, 20)
        layoutParams.setMargins(4, 0, 4, 0)
        for (i in mDotIvArray.indices) {
            val imageView = ImageView(getContext())
            imageView.setBackgroundResource(R.drawable.dot_unchosen)
            //            imageView.setEnabled(true);
            mDotIvArray[i] = imageView
            linearLayout.addView(mDotIvArray[i], layoutParams)
        }
    }

    private fun initViewPager() {
        mViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                var position = position
                mCurrentPosition = position
                if (position == 0) {
                    position = mRealTopCarouselNum - 2
                } else if (position == mRealTopCarouselNum - 1) {
                    position = 1
                }
                setDot(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mHandler!!.removeCallbacks(mRunnable)
                    mHandler!!.postDelayed(mRunnable, INTERVAL.toLong())
                    if (mCurrentPosition == 0) {
                        mViewPager!!.setCurrentItem(mRealTopCarouselNum - 2, false)
                    } else if (mCurrentPosition == mRealTopCarouselNum - 1) {
                        mViewPager!!.setCurrentItem(1, false)
                    }
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mHandler!!.removeCallbacks(mRunnable)
                } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    mHandler!!.removeCallbacks(mRunnable)
                }
            }
        })
        mViewPager!!.setCurrentItem(mCurrentPosition)
        //设置缓存，否则在循环处会屏闪。
        mViewPager!!.setOffscreenPageLimit(mRealTopCarouselNum)
    }

    private fun setDot(position: Int) {
        mDotIvArray[position - 1]!!.setBackgroundResource(R.drawable.dot_chosen)
        for (i in 0..<mRealTopCarouselNum - 2) {
            if (i != position - 1) {
                mDotIvArray[i]!!.setBackgroundResource(R.drawable.dot_unchosen)
            }
        }
    }


    private inner class RequestImageAsyncTask : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            val path = MusicURL.API_PRIVATE_CONTENT
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url(path)
                    .build()
                val response = okHttpClient.newCall(request).execute()

                val responseCode = response.code
                val responseData = response.body!!.string()
                if (responseCode == 200 && responseData != null) {
                    val gson = Gson()
                    val responseBean = gson.fromJson<FoundTopCarouselResponseBean>(
                        responseData,
                        FoundTopCarouselResponseBean::class.java
                    )
                    val carouselBeanList = responseBean.getBanners()

                    mMusicInfoList.clear()
                    mBannerImgUrlList.clear()
                    for (bean in carouselBeanList) {
                        if ("1" == bean.getTargetType()) {
                            mMusicInfoList.add(bean.getSong())
                            mBannerImgUrlList.add(bean.getPic())
                        }
                    }
                } else {
                    Log.d(TAG, "Cannot get resource!")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            //mShowTopCarouselNum确定
            mShowTopCarouselNum = min(mMusicInfoList.size, MAX_TOP_CAROUSEL_NUM)
            mRealTopCarouselNum = mShowTopCarouselNum + 2
            initDots(getView()!!)

            if (mShowTopCarouselNum <= 0) {
                //没有请求到内容
                Log.d(TAG, "no carousel image!  mShowTopCarouselNum = " + mShowTopCarouselNum)
                return
            }
            val musicInfoListExternal: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
            val bannerImgUrlListExternal: MutableList<String?> = ArrayList<String?>()
            musicInfoListExternal.add(mMusicInfoList.get(mShowTopCarouselNum - 1))
            bannerImgUrlListExternal.add(mBannerImgUrlList.get(mShowTopCarouselNum - 1))
            for (i in 0..<mShowTopCarouselNum) {
                musicInfoListExternal.add(mMusicInfoList.get(i))
                bannerImgUrlListExternal.add(mBannerImgUrlList.get(i))
            }
            musicInfoListExternal.add(mMusicInfoList.get(0))
            bannerImgUrlListExternal.add(mBannerImgUrlList.get(0))

            for (i in musicInfoListExternal.indices) {
                mTopItemFragmentList.add(
                    FoundTopCarouselItemFragment(
                        bannerImgUrlListExternal.get(i),
                        musicInfoListExternal.get(i)
                    )
                )
            }

            //fragment嵌套fragment时，要用getChildFragmentManager而不是getActivity.getSupportFragmentManager。
            mViewPager!!.setAdapter(object : FragmentPagerAdapter(mChildFragmentManager!!) {
                override fun getItem(position: Int): Fragment {
                    return mTopItemFragmentList.get(position)
                }

                override fun getCount(): Int {
//                        return num;
                    return mShowTopCarouselNum + 2
                }
            })

            initViewPager()
        }
    }

    companion object {
        private const val TAG = "FoundTopCarouselFragment"
        private const val MAX_TOP_CAROUSEL_NUM = 7

        //定时器触发轮播
        private const val SET_VIEWPAGER_ITEM = 0

        //轮播间隔
        private val INTERVAL = 1000 * 3
    }
}
