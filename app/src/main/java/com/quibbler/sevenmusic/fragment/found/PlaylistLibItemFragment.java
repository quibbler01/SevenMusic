package com.quibbler.sevenmusic.fragment.found;

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

import com.google.gson.Gson;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.adapter.found.FoundShowPlaylistAdapter;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundPlaylistLibBean;
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Package:        com.quibbler.sevenmusic.fragment.found
 * ClassName:      PlaylistLibItemFragment
 * Description:    歌单库页面，每个标签对应的歌单展示页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 21:15
 */
public class PlaylistLibItemFragment extends Fragment {
    private static final String TAG = "PlaylistLibItemFragment";

    //api地址形如：http://114.116.128.229:3000/top/playlist/highquality?cat=%E5%8D%8E%E8%AF%AD
    private static final String URL_AUTHORITY = "http://114.116.128.229:3000/top/playlist/highquality?cat=";

    //向服务器请求时的cat名
    private String mCatName;
    //fragment对应tab标签上显示的名字
    private String mTabName;

    //显示歌单的RecyclerView
    private RecyclerView mRecyclerView;
    //歌单库页面的数据list
    private List<PlaylistInfo> mPlaylistInfoList = new ArrayList<>();
    private FoundShowPlaylistAdapter mPlaylistLibRecyclerAdapter = new FoundShowPlaylistAdapter(mPlaylistInfoList);

    private RequestPlaylistAsyncTask mRequestPlaylistAsyncTask;


//    public PlaylistLibItemFragment() {
//
//    }
//
//    public PlaylistLibItemFragment setCatName(String catName) {
//        mCatName = catName;
//        return this;
//    }
//
//    public PlaylistLibItemFragment setTabName(String tabName) {
//        mTabName = tabName;
//        return this;
//    }

    /**
     * 构造函数
     *
     * @param catName 向服务器请求时的cat名
     * @param tabName tab标签上显示的名字
     */
    public PlaylistLibItemFragment(String catName, String tabName) {
        mCatName = catName;
        mTabName = tabName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_found_playlist_lib, container, false);
        mRecyclerView = (RecyclerView) view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //设置RecyclerView的
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        //设置RecyclerView的adapter
        mRecyclerView.setAdapter(mPlaylistLibRecyclerAdapter);

        //网易api的url
        String url = URL_AUTHORITY + mCatName;
        requestPlaylist(url);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRequestPlaylistAsyncTask.cancel(true);
    }

    /**
     * 从网易api获取歌单数据
     *
     * @param url
     */
    private void requestPlaylist(String url) {
        mRequestPlaylistAsyncTask = new RequestPlaylistAsyncTask();
        mRequestPlaylistAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    /**
     * 根据url获取歌单库的数据，并更新显示
     */
    private class RequestPlaylistAsyncTask extends AsyncTask<String, Void, List<PlaylistInfo>> {

        @Override
        protected List<PlaylistInfo> doInBackground(String... strings) {
            String path = strings[0];

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
                    FoundPlaylistLibBean responseBean = gson.fromJson(responseData, FoundPlaylistLibBean.class);

                    return responseBean.getPlaylists();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PlaylistInfo> playlistInfoList) {
            super.onPostExecute(playlistInfoList);
            mPlaylistLibRecyclerAdapter.updateData(playlistInfoList);
        }
    }
}
