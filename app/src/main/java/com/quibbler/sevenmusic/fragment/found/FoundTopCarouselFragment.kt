package com.quibbler.sevenmusic.fragment.found;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopCarouselBean;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopCarouselResponseBean;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.quibbler.sevenmusic.bean.MusicURL.API_PRIVATE_CONTENT;

/**
 * Package:        com.quibbler.sevenmusic.fragment.found
 * ClassName:      FoundTopCarouselFragment
 * Description:    轮播图fragment
 * Author:         yanwuyang
 * CreateDate:     2019/10/10 16:14
 */
public class FoundTopCarouselFragment extends Fragment {

    private static final String TAG = "FoundTopCarouselFragment";
    private static final int MAX_TOP_CAROUSEL_NUM = 7;
    //实际轮播图数量
    private int mRealTopCarouselNum;
    //可见轮播图数量
    private int mShowTopCarouselNum;

    private ViewPager mViewPager;
    private ImageView[] mDotIvArray;
//    private View mViewPlaceholder;
//    private RelativeLayout mRlCarousel;

    private RequestImageAsyncTask mRequestImageAsyncTask;
    private List<MusicInfo> mMusicInfoList = new ArrayList<>();
    private List<String> mBannerImgUrlList = new ArrayList<>();
    private List<FoundTopCarouselItemFragment> mTopItemFragmentList = new ArrayList<>();

    private FragmentManager mChildFragmentManager;

    //定时器触发轮播
    private static final int SET_VIEWPAGER_ITEM = 0;
    //轮播间隔
    private static final int INTERVAL = 1000 * 3;
    private int mCurrentPosition = 1;
    private Thread mTimerThread;

    private static class TimerHandler extends Handler {
        WeakReference<FoundTopCarouselFragment> mWeakReference;

        private TimerHandler(FoundTopCarouselFragment topCarouselFragment) {
            mWeakReference = new WeakReference<>(topCarouselFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference.get() == null) {
                return;
            }
            FoundTopCarouselFragment fragment = mWeakReference.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case SET_VIEWPAGER_ITEM:
                    if (fragment.mViewPager != null) {
                        int currentItemIndex = fragment.mViewPager.getCurrentItem();
                        fragment.mViewPager.setCurrentItem(currentItemIndex + 1, true);
                    }
                    break;
            }
        }
    }

    private Handler mHandler;

    //定时器
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Message message = mHandler.obtainMessage();
            message.what = SET_VIEWPAGER_ITEM;

            mHandler.sendMessage(message);
            //在定时器中把自己移出MQ，再自己把自己加入MQ并延时执行
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(this, INTERVAL);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_found_top_carousel, container, false);
        mViewPager = view.findViewById(R.id.found_vp_top_carousel);

        mChildFragmentManager = getChildFragmentManager();

        mHandler = new TimerHandler(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTimerThread = new Thread(mRunnable);
        mTimerThread.start();

        initTopPlaceholder();

        mRequestImageAsyncTask = new RequestImageAsyncTask();
        mRequestImageAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        if (mTimerThread != null) {
            mTimerThread.interrupt();
        }
        if (mRequestImageAsyncTask != null) {
            mRequestImageAsyncTask.cancel(true);
        }
    }

    private void initTopPlaceholder() {
        //fragment嵌套fragment时，要用getChildFragmentManager而不是getActivity.getSupportFragmentManager。
        mViewPager.setAdapter(new FragmentPagerAdapter(mChildFragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return new FoundTopPlaceholderFragment();
            }

            @Override
            public int getCount() {
                //预加载时占位用的，所以直接返回1即可
                return 1;
            }
        });
    }
    private void initDots(View view) {

        mDotIvArray = new ImageView[mShowTopCarouselNum];
        LinearLayout linearLayout = view.findViewById(R.id.found_ll_carousel_dots);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(20, 20);
        layoutParams.setMargins(4, 0, 4, 0);
        for (int i = 0; i < mDotIvArray.length; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setBackgroundResource(R.drawable.dot_unchosen);
//            imageView.setEnabled(true);
            mDotIvArray[i] = imageView;
            linearLayout.addView(mDotIvArray[i], layoutParams);
        }
    }

    private void initViewPager() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                if (position == 0) {
                    position = mRealTopCarouselNum - 2;
                } else if (position == mRealTopCarouselNum - 1) {
                    position = 1;
                }
                setDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, INTERVAL);
                    if (mCurrentPosition == 0) {
                        mViewPager.setCurrentItem(mRealTopCarouselNum - 2, false);
                    } else if (mCurrentPosition == mRealTopCarouselNum - 1) {
                        mViewPager.setCurrentItem(1, false);
                    }
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mHandler.removeCallbacks(mRunnable);
                } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        });
        mViewPager.setCurrentItem(mCurrentPosition);
        //设置缓存，否则在循环处会屏闪。
        mViewPager.setOffscreenPageLimit(mRealTopCarouselNum);
    }

    private void setDot(int position) {
        mDotIvArray[position - 1].setBackgroundResource(R.drawable.dot_chosen);
        for (int i = 0; i < mRealTopCarouselNum - 2; i++) {
            if (i != position - 1) {
                mDotIvArray[i].setBackgroundResource(R.drawable.dot_unchosen);
            }
        }
    }


    private class RequestImageAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String path = API_PRIVATE_CONTENT;
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = okHttpClient.newCall(request).execute();

                int responseCode = response.code();
                String responseData = response.body().string();
                if (responseCode == 200 && responseData != null) {
                    Gson gson = new Gson();
                    FoundTopCarouselResponseBean responseBean = gson.fromJson(responseData, FoundTopCarouselResponseBean.class);
                    List<FoundTopCarouselBean> carouselBeanList = responseBean.getBanners();

                    mMusicInfoList.clear();
                    mBannerImgUrlList.clear();
                    for (FoundTopCarouselBean bean : carouselBeanList) {
                        if ("1".equals(bean.getTargetType())) {
                            mMusicInfoList.add(bean.getSong());
                            mBannerImgUrlList.add(bean.getPic());
                        }
                    }
                } else {
                    Log.d(TAG, "Cannot get resource!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //mShowTopCarouselNum确定
            mShowTopCarouselNum = Math.min(mMusicInfoList.size(), MAX_TOP_CAROUSEL_NUM);
            mRealTopCarouselNum = mShowTopCarouselNum + 2;
            initDots(getView());

            if (mShowTopCarouselNum <= 0) {
                //没有请求到内容
                Log.d(TAG, "no carousel image!  mShowTopCarouselNum = " + mShowTopCarouselNum);
                return;
            }
            List<MusicInfo> musicInfoListExternal = new ArrayList<>();
            List<String> bannerImgUrlListExternal = new ArrayList<>();
            musicInfoListExternal.add(mMusicInfoList.get(mShowTopCarouselNum - 1));
            bannerImgUrlListExternal.add(mBannerImgUrlList.get(mShowTopCarouselNum - 1));
            for (int i = 0; i < mShowTopCarouselNum; i++) {
                musicInfoListExternal.add(mMusicInfoList.get(i));
                bannerImgUrlListExternal.add(mBannerImgUrlList.get(i));
            }
            musicInfoListExternal.add(mMusicInfoList.get(0));
            bannerImgUrlListExternal.add(mBannerImgUrlList.get(0));

            for (int i = 0; i < musicInfoListExternal.size(); i++) {
                mTopItemFragmentList.add(new FoundTopCarouselItemFragment(bannerImgUrlListExternal.get(i), musicInfoListExternal.get(i)));
            }

            //fragment嵌套fragment时，要用getChildFragmentManager而不是getActivity.getSupportFragmentManager。
            mViewPager.setAdapter(new FragmentPagerAdapter(mChildFragmentManager) {
                @Override
                public Fragment getItem(int position) {
                    return mTopItemFragmentList.get(position);
                }

                @Override
                public int getCount() {
//                        return num;
                    return mShowTopCarouselNum + 2;
                }
            });

            initViewPager();

        }
    }
}
