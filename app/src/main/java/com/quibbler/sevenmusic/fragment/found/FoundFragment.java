package com.quibbler.sevenmusic.fragment.found;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.SearchMainActivity;
import com.quibbler.sevenmusic.activity.found.PlaylistLibActivity;
import com.quibbler.sevenmusic.activity.found.SingerLibActivity;
import com.quibbler.sevenmusic.adapter.found.FoundShowPlaylistAdapter;
import com.quibbler.sevenmusic.adapter.found.FoundTopMvAdapter;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopMvResponseBean;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopPlaylistResponseBean;
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.view.found.FoundCustomButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.quibbler.sevenmusic.bean.MusicURL.API_TOP_MV_REQUEST_URL;
import static com.quibbler.sevenmusic.bean.MusicURL.API_TOP_PLAYLIST_REQUEST_URL;


/**
 * Package:        com.quibbler.sevenmusic.fragment
 * ClassName:      FoundFragment
 * Description:    发现页面。FoundFragment类，由MainActivity托管
 * Author:         yangwuyang
 * CreateDate:     2019/9/16 17:42
 */
public class FoundFragment extends Fragment {
    private static final String TAG = "FoundFragment";

    //最新歌单获取地址
    private static final String TOP_PLAYLIST_REQUEST_URL = API_TOP_PLAYLIST_REQUEST_URL;
    //最新mv视频获取地址
    private static final String TOP_MV_REQUEST_URL = API_TOP_MV_REQUEST_URL;

    //推荐歌单RecyclerView的Adapter
    private FoundShowPlaylistAdapter mFoundTopPlaylistAdapter;
    //推荐mv视频RecyclerView的Adapter
    private FoundTopMvAdapter mFoundTopMvAdapter;

    //歌手库按钮
    private FoundCustomButton mBtnSingerLibrary;
    //歌单库按钮
    private FoundCustomButton mBtnPlaylistLibrary;
    //装饰用button
    private FoundCustomButton mBtnMy;
    private FoundCustomButton mBtnMv;
    private FoundCustomButton mBtnSearch;

    private ViewPager mOuterViewPager;
    //推荐歌单
    private RecyclerView mRvTopPlaylistRecyclerView;
    //推荐视频
    private RecyclerView mRvTopMvRecyclerView;
    //推荐歌单的源数据
    private List<PlaylistInfo> mTopPlaylistList = new ArrayList<>();
    //推荐视频的源数据
    private List<MvInfo> mMvInfoList = new ArrayList<>();

    public FoundFragment() {

    }

    public FoundFragment(ViewPager viewPager) {
        mOuterViewPager = viewPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载fragment_found布局文件
        View view = inflater.inflate(R.layout.fragment_found, null);

        //初始化歌单库、歌手库按钮
        initCustomButton(view);

        //初始化推荐歌单部分
        initTopPlaylist(view);
        //初始化推荐视频部分
        initTopMv(view);
        //获取推荐歌单并显示
        getTopPlaylist();
        //获取推荐mv视频并显示
        getTopMv();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 初始化歌手库、歌单库按钮
     *
     * @param view 父view
     */
    private void initCustomButton(View view) {
        mBtnSingerLibrary = view.findViewById(R.id.found_btn_singer_library);
        mBtnPlaylistLibrary = view.findViewById(R.id.found_btn_playlist_library);
        mBtnMy = view.findViewById(R.id.found_btn_to_my);
        mBtnMv = view.findViewById(R.id.found_btn_to_mv);
        mBtnSearch = view.findViewById(R.id.found_btn_to_search);

        mBtnSingerLibrary.setImgResource(R.drawable.singer_lib_icon);
        mBtnSingerLibrary.setText("歌手库");
        mBtnSingerLibrary.setImgSize(130, 130);

        mBtnPlaylistLibrary.setImgResource(R.drawable.playlist_lib_icon);
        mBtnPlaylistLibrary.setText("歌单库");
        mBtnPlaylistLibrary.setImgSize(130, 130);

        mBtnMy.setImgResource(R.drawable.found_to_my_icon);
        mBtnMy.setText("我的");
        mBtnMy.setImgSize(130, 130);
        mBtnMv.setImgResource(R.drawable.found_to_mv_icon);
        mBtnMv.setText("MV");
        mBtnMv.setImgSize(130, 130);
        mBtnSearch.setImgResource(R.drawable.found_to_search_icon);
        mBtnSearch.setText("搜索");
        mBtnSearch.setImgSize(130, 130);


        //歌手库按钮点击事件监听器
        mBtnSingerLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SingerLibActivity.class);
                startActivity(intent);
            }
        });

        //歌单库按钮点击事件监听
        mBtnPlaylistLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PlaylistLibActivity.class);
                startActivity(intent);
            }
        });

        mBtnMy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOuterViewPager != null) {
                    mOuterViewPager.setCurrentItem(0);
                }
            }
        });

        mBtnMv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOuterViewPager != null) {
                    mOuterViewPager.setCurrentItem(2);
                }
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), SearchMainActivity.class);
                startActivityForResult(searchIntent, 0);
            }
        });
    }

    /**
     * 初始化“发现”页面的推荐歌单部分
     *
     * @param view 父view
     */
    private void initTopPlaylist(View view) {
        mRvTopPlaylistRecyclerView = view.findViewById(R.id.found_rv_top_playlist);
        //绑定LayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false);

        mRvTopPlaylistRecyclerView.setLayoutManager(gridLayoutManager);
        mRvTopPlaylistRecyclerView.setNestedScrollingEnabled(false);
        //绑定Adapter
        mFoundTopPlaylistAdapter = new FoundShowPlaylistAdapter(mTopPlaylistList);
        mRvTopPlaylistRecyclerView.setAdapter(mFoundTopPlaylistAdapter);
    }

    /**
     * 获取推荐歌单并显示
     */
    private void getTopPlaylist() {
        //先检测有无网络，没网络则提示开启，并加载缓存，无缓存则提供刷新按钮。考虑后期优化
        List<PlaylistInfo> playlistInfoList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            playlistInfoList.add(new PlaylistInfo());
        }
        mFoundTopPlaylistAdapter.updateData(playlistInfoList);


        RequestTopPlaylistAsyncTask asyncTask = new RequestTopPlaylistAsyncTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 获取推荐mv视频并显示
     */
    private void getTopMv() {
        List<MvInfo> mvInfoList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mvInfoList.add(new MvInfo());
        }
        mFoundTopMvAdapter.updateData(mvInfoList);

        RequestTopMvAsyncTask asyncTask = new RequestTopMvAsyncTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 初始化“发现”页面的推荐视频部分
     *
     * @param view 父view
     */
    private void initTopMv(View view) {
        mRvTopMvRecyclerView = view.findViewById(R.id.found_rv_top_mv);

        //绑定LayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false);
        mRvTopMvRecyclerView.setLayoutManager(gridLayoutManager);
        mRvTopMvRecyclerView.setNestedScrollingEnabled(false);
        //绑定Adapter
        mFoundTopMvAdapter = new FoundTopMvAdapter(mMvInfoList);
        mRvTopMvRecyclerView.setAdapter(mFoundTopMvAdapter);
    }

    /**
     * Package:        com.quibbler.sevenmusic.fragment.found
     * ClassName:      FoundFragment
     * Description:    内部类，用来获取“发现”页面的推荐歌单response
     * Author:         yanwuyang
     * CreateDate:     2019/9/18 20:21
     */
    private class RequestTopPlaylistAsyncTask extends AsyncTask<Void, Void, List<PlaylistInfo>> {

        @Override
        protected List<PlaylistInfo> doInBackground(Void... voids) {

            String path = TOP_PLAYLIST_REQUEST_URL;

            //OkHttp获取网络资源
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = client.newCall(request).execute();
                Log.d(TAG, "response is: " + response);
                int responseCode = response.code();
                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    String responseData = response.body().string();
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!");
                        return null;
                    }
                    Log.d(TAG, "responseData is: " + responseData);
                    Gson gson = new Gson();
                    FoundTopPlaylistResponseBean responseBean = gson.fromJson(responseData, FoundTopPlaylistResponseBean.class);
                    if (responseBean.getCode() == 200) {
                        return responseBean.getPlaylists();
                    }
                } else {
                    //没有获取到网络资源，可以用缓存，没有缓存则提供刷新按钮。考虑后期优化
                }
            } catch (IOException e) {
                Log.e(TAG, "网络异常！");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PlaylistInfo> playlistInfoList) {
            super.onPostExecute(playlistInfoList);
            if (playlistInfoList == null || playlistInfoList.size() == 0) {
                return;
            }
            mFoundTopPlaylistAdapter.updateData(playlistInfoList);
        }
    }


    /**
     * Package:        com.quibbler.sevenmusic.fragment.found
     * ClassName:      FoundFragment
     * Description:    内部类，用来获取“发现”页面的推荐mv视频response
     * Author:         yanwuyang
     * CreateDate:     2019/9/18 20:21
     */
    private class RequestTopMvAsyncTask extends AsyncTask<Void, Void, List<MvInfo>> {

        @Override
        protected List<MvInfo> doInBackground(Void... voids) {

            String path = TOP_MV_REQUEST_URL;

            //OkHttp获取网络资源
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(path)
                        .build();
                Response response = client.newCall(request).execute();
                Log.d(TAG, "response is: " + response);
                int responseCode = response.code();
                Log.d(TAG, "responseCode is: " + responseCode);
                if (responseCode == 200) {
                    String responseData = response.body().string();
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!");
                        return null;
                    }
                    Log.d(TAG, "responseData is: " + responseData);
                    Gson gson = new Gson();
                    FoundTopMvResponseBean responseBean = gson.fromJson(responseData, FoundTopMvResponseBean.class);
                    if (responseBean.getCode() == 200) {
                        return responseBean.getData();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "网络异常！");
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MvInfo> MvInfoList) {
            super.onPostExecute(MvInfoList);
            //只显示前三个视频
            if (MvInfoList == null) {
                //没联网，或者服务器问题
                return;
            }
//            List<MvInfo> list = MvInfoList.subList(0, 3);
            mFoundTopMvAdapter.updateData(MvInfoList);
        }
    }
}
