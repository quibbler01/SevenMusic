package com.quibbler.sevenmusic.activity.found;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.sidebar.SingerSiderBar;
import com.quibbler.sevenmusic.adapter.found.FoundSingerLibRecommendAdapter;
import com.quibbler.sevenmusic.adapter.found.FoundSingerLibSelectedAdapter;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerLibBean;
import com.quibbler.sevenmusic.comparator.PinyinComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      SingerLibActivity
 * Description:    歌手库页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 18:49
 */
public class SingerLibActivity extends AppCompatActivity {
    private static final String TAG = "SingerLibActivity";

    //歌手库上方tab指示器,歌手风格
    private TabLayout mStyleTabLayout;
    //歌手风格名称
    private static final String[] SINGER_STYLE = new String[]{"华语", "欧美", "日本", "韩国", "其他"};
    //歌手风格名对应的请求码
    private static final String[] SINGER_STYLE_CODE = new String[]{"10", "20", "60", "70", "40"};
    //歌手库上方tab指示器,歌手性别
    private TabLayout mGenderTabLayout;
    //歌手性别名称
    private static final String[] SINGER_GENDER = new String[]{"男歌手", "女歌手", "组合/乐队"};
    //歌手性别名对应的请求码
    private static final String[] SINGER_GENDER_CODE = new String[]{"01", "02", "03"};

    //当前style标签的index，默认为0
    private int mStylePosition = -1;
    //当前gender标签的index，默认为0
    private int mGenderPosition = -1;

    private static final String REQUEST_SINGER_AUTHORITY_HEAD = "http://114.116.128.229:3000/artist/list?cat=";
    private static final String REQUEST_SINGER_AUTHORITY_TAIL = "&limit=30";

    private static final String REQUEST_TOP_SINGER_URL = "http://114.116.128.229:3000/top/artists?offset=0&limit=8";

    //异步请求筛选歌手信息的AsyncTask
    private RequestShowSingerAsyncTask mRequestShowSingerAsyncTask;
    //异步请求推荐歌手信息的AsyncTask
    private RequestTopSingerAsyncTask mRequestTopSingerAsyncTask;

    //显示推荐歌手的RecyclerView的adapter
    private FoundSingerLibRecommendAdapter mSingerLibRecommendAdapter;
    //显示歌手筛选结果的RecyclerView的adapter
    private FoundSingerLibSelectedAdapter mSingerLibSelectedAdapter;
    //显示歌手筛选结果的RecyclerView
    private RecyclerView mSelectedRecyclerView;
    //显示推荐歌手的RecyclerView
    private RecyclerView mRecommendRecyclerView;
    //从网络获取的筛选歌手数据源list
    private List<FoundSingerInfo> mSelectedSingerInfoList = new ArrayList<>();
    //从网络获取的推荐歌手数据源list
    private List<FoundSingerInfo> mRecommendSingerInfoList = new ArrayList<>();

    //右侧字母栏
    private SingerSiderBar mSiderBar;
    //点击SiderBar后显示点击的字母
    private TextView mTvDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_singer_lib);

        initActionBar();
        initStyleTab();
        initGenderTab();
        initRecommendRecyclerView();
        initSelectedRecyclerView();
        initSiderBar();
        getTopSingerInfo();
    }

    /**
     * 初始化页面的actionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("歌手库");
    }

    /**
     * 初始化歌手风格的tab指示器
     */
    private void initStyleTab() {
        mStyleTabLayout = findViewById(R.id.singer_lib_style_tablayout);

        mStyleTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getText() + "被选中");
                updateTabTextView(tab, true);
                mStylePosition = tab.getPosition();
                requestSingerData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getText() + "未被选中");
                updateTabTextView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //为tabLayout手动添加标签
        for (int i = 0; i < SINGER_STYLE.length; i++) {
            if (i == 0) {
                mStyleTabLayout.addTab(mStyleTabLayout.newTab().setText(SINGER_STYLE[i]), true);
            } else {
                mStyleTabLayout.addTab(mStyleTabLayout.newTab().setText(SINGER_STYLE[i]), false);
            }
        }
        //设置指示器可以滚动
        mStyleTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    /**
     * 设置tab的textView
     *
     * @param tab
     * @param isSelect 是否被选中
     */
    private void updateTabTextView(TabLayout.Tab tab, boolean isSelect) {
        //第一次要判空
        View view = tab.getCustomView();
        if (null == view) {
            tab.setCustomView(R.layout.tab_found_lib_custom);
        }

        //设置标签是否选中的样式
        if (isSelect) {
            //选中的样式
            TextView tabSelect = tab.getCustomView().findViewById(R.id.tab_found_lib_tv);
            tabSelect.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tabSelect.setTextColor(getResources().getColor(R.color.found_tab_selected));
            tabSelect.setText(tab.getText());
            tabSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        } else {
            //未选中的样式
            TextView tabUnSelect = tab.getCustomView().findViewById(R.id.tab_found_lib_tv);
            tabUnSelect.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tabUnSelect.setTextColor(getResources().getColor(R.color.found_tab_unselected));
            tabUnSelect.setText(tab.getText());
            tabUnSelect.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
    }

    /**
     * 初始化歌手性别的tab指示器
     */
    private void initGenderTab() {
        mGenderTabLayout = findViewById(R.id.singer_lib_gender_tablayout);

        mGenderTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getText() + "被选中");
                updateTabTextView(tab, true);
                mGenderPosition = tab.getPosition();
                requestSingerData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, tab.getText() + "未被选中");
                updateTabTextView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //为tabLayout手动添加标签
        for (int i = 0; i < SINGER_GENDER.length; i++) {
            if (i == 0) {
                mGenderTabLayout.addTab(mGenderTabLayout.newTab().setText(SINGER_GENDER[i]), true);
            } else {
                mGenderTabLayout.addTab(mGenderTabLayout.newTab().setText(SINGER_GENDER[i]), false);
            }
        }
        //设置指示器可以滚动
        mGenderTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    /**
     * 初始化推荐歌手的RecyclerView
     */
    private void initRecommendRecyclerView() {
        mRecommendRecyclerView = findViewById(R.id.singer_lib_top_recyclerview);
        mSingerLibRecommendAdapter = new FoundSingerLibRecommendAdapter(mRecommendSingerInfoList);
        mRecommendRecyclerView.setAdapter(mSingerLibRecommendAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mRecommendRecyclerView.setLayoutManager(linearLayoutManager);

    }

    /**
     * 初始化歌手筛选结果的RecyclerView
     */
    private void initSelectedRecyclerView() {
        mSelectedRecyclerView = findViewById(R.id.singer_lib_show_recyclerview);
        mSingerLibSelectedAdapter = new FoundSingerLibSelectedAdapter(mSelectedSingerInfoList);
        mSelectedRecyclerView.setAdapter(mSingerLibSelectedAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSelectedRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initSiderBar() {
        mSiderBar = findViewById(R.id.singer_lib_sidebar);
        mTvDialog = findViewById(R.id.singer_lib_tv_dialog);
        mSiderBar.setTextViewDialog(mTvDialog);
        mSiderBar.setOnTouchingLetterChangedListener(new SingerSiderBar.OnTouchingLetterChangedListener() {
            @Override
            public boolean onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mSingerLibSelectedAdapter.getPositionForSelection(s.charAt(0));
                if (position != -1) {
                    mSelectedRecyclerView.getLayoutManager().scrollToPosition(position);
                    return true;
                }
                return false;
            }
        });
//        mSiderBar.refresh();

    }

    /**
     * 根据tab指示器选择的歌手信息，组合成requestCode，向网易api进行请求
     */
    private void requestSingerData() {
        //先取消上一次的请求，包括后续图片的下载请求
        if (mRequestShowSingerAsyncTask != null) {
            mRequestShowSingerAsyncTask.cancel(true);
        }
        if (mSingerLibRecommendAdapter != null) {
            mSingerLibRecommendAdapter.stopUpdateData();
        }
        if (mStylePosition == -1 || mGenderPosition == -1) {
            return;
        }

        String requestCode = SINGER_STYLE_CODE[mStylePosition] + SINGER_GENDER_CODE[mGenderPosition];
        String requestUrl = REQUEST_SINGER_AUTHORITY_HEAD + requestCode + REQUEST_SINGER_AUTHORITY_TAIL;

        mRequestShowSingerAsyncTask = new RequestShowSingerAsyncTask();
        mRequestShowSingerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestUrl);

    }

    private void getTopSingerInfo() {
        mRequestTopSingerAsyncTask = new RequestTopSingerAsyncTask();
        mRequestTopSingerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQUEST_TOP_SINGER_URL);
    }

    /**
     * 根据url获取筛选歌手的数据，并更新显示
     */
    private class RequestShowSingerAsyncTask extends AsyncTask<String, Void, List<FoundSingerInfo>> {

        @Override
        protected List<FoundSingerInfo> doInBackground(String... strings) {
            String path = strings[0];

            //OkHttp获取网络资源
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = client.newCall(request).execute();
//                Log.d(TAG, "response is: " + response);
                int responseCode = response.code();
//                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    String responseData = response.body().string();
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!");
                        return null;
                    }
//                    Log.d(TAG, "responseData is: " + responseData);
                    Gson gson = new Gson();
                    FoundSingerLibBean responseBean = gson.fromJson(responseData, FoundSingerLibBean.class);

                    return responseBean.getArtists();
                }
            } catch (IOException e) {
                Log.e(TAG, "网络异常！");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<FoundSingerInfo> foundSingerInfoList) {
            super.onPostExecute(foundSingerInfoList);
            if (foundSingerInfoList == null) {
                return;
            }
            Iterator<FoundSingerInfo> iterator = foundSingerInfoList.iterator();
            while (iterator.hasNext()) {
                FoundSingerInfo singer  = iterator.next();
                if (singer == null || singer.getFirstPinyin() == null){
                    iterator.remove();
                }
            }
            //按拼音排序
            Collections.sort(foundSingerInfoList, new PinyinComparator());
            //RecyclerView的数据源变化了，更新siderBar
            mSiderBar.refresh(foundSingerInfoList);

            mSingerLibSelectedAdapter.updateData(foundSingerInfoList);
            //切换标签之后，滚动到最上面
            mSelectedRecyclerView.scrollToPosition(0);
        }
    }

    /**
     * 根据url获取歌手库的数据，并更新显示
     */
    private class RequestTopSingerAsyncTask extends AsyncTask<String, Void, List<FoundSingerInfo>> {

        @Override
        protected List<FoundSingerInfo> doInBackground(String... strings) {
            String path = strings[0];

            //OkHttp获取网络资源
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = client.newCall(request).execute();
//                Log.d(TAG, "response is: " + response);
                int responseCode = response.code();
//                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    String responseData = response.body().string();
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!");
                        return null;
                    }
//                    Log.d(TAG, "responseData is: " + responseData);
                    Gson gson = new Gson();
                    FoundSingerLibBean responseBean = gson.fromJson(responseData, FoundSingerLibBean.class);

                    return responseBean.getArtists();
                }
            } catch (IOException e) {
                Log.e(TAG, "网络异常！");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<FoundSingerInfo> foundSingerInfoList) {
            super.onPostExecute(foundSingerInfoList);
            mSingerLibRecommendAdapter.updateData(foundSingerInfoList);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRequestShowSingerAsyncTask.cancel(true);
    }
}
