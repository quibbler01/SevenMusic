package com.quibbler.sevenmusic.fragment.mv;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.adapter.mv.TopMvClickListener;
import com.quibbler.sevenmusic.adapter.mv.TopMvListAdapter;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager;
import com.quibbler.sevenmusic.callback.MusicCallBack;
import com.quibbler.sevenmusic.callback.MvCollectCallback;
import com.quibbler.sevenmusic.listener.IScrollViewListener;
import com.quibbler.sevenmusic.presenter.MvPresenter;
import com.quibbler.sevenmusic.service.MvDownloadService;
import com.quibbler.sevenmusic.utils.HttpUtil;
import com.quibbler.sevenmusic.utils.PageEffectUtil;
import com.quibbler.sevenmusic.view.IScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.BIND_AUTO_CREATE;

/**
  *
  * Package:        com.quibbler.sevenmusic.fragment.mv
  * ClassName:      NewTopMvFragment
  * Description:    采用recyclerview的mv排行页面
  * Author:         lishijun
  * CreateDate:     2019/10/11 18:03
 */

public class NewTopMvFragment extends Fragment {

    private static final String SERVER = "http://114.116.128.229:3000";

    //一页显示mv的数量
    private static final int MV_NUMS_OF_PAGE = 16;

    private String mUrl = "/top/mv";

    private List<MvInfo> mVideoInfoList = new ArrayList<>();

    private View mView;

    private ProgressDialog mProgressBar;

    //弹出的下载收藏等popview
    private PopupWindow mDownloadPopWindow;

    private MvDownloadService.DownLoadBinder mDownLoadBinder;

    private int mPages = 0; //当前加载了第几页

    private RecyclerView mTopMvListView;

    private TopMvListAdapter mTopMvListAdapter;

    private ServiceConnection mDownloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownLoadBinder = (MvDownloadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public NewTopMvFragment() {

    }

    public NewTopMvFragment(String url) {
        mUrl = url;
    }

    public static Fragment newInstance(String arg){
        NewTopMvFragment fragment = new NewTopMvFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载fragment_mv_all布局文件
        mView = inflater.inflate(R.layout.fragment_mv_all, null);
        mUrl = getArguments().getString("url");
        mTopMvListView = mView.findViewById(R.id.mv_rv_mvs);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                RecyclerView.VERTICAL, false);
        mTopMvListView.setLayoutManager(gridLayoutManager);
        mTopMvListView.setNestedScrollingEnabled(false);
        mTopMvListAdapter = new TopMvListAdapter(mVideoInfoList, new TopMvClickListener() {
            @Override
            public void onStartMvPlayActivity(MvInfo mvInfo) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo);
                }
            }
            @Override
            public void onClickMoreButton(MvInfo mvInfo) {
                popMoreWindown(mvInfo);
            }
        });
        mTopMvListView.setAdapter(mTopMvListAdapter);
        getMvInfoListFromChosen(0);
        mTopMvListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean mIsSlidingToLast = false;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //设置什么布局管理器,就获取什么的布局管理器
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                // 当停止滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = manager.getItemCount();
                    // 判断是否滚动到底部
                    if (lastVisibleItem == (totalItemCount - 1) && mIsSlidingToLast) {
                        showProgressBar();
                        getMvInfoListFromChosen(++mPages);
                    }
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    mIsSlidingToLast = true;
                } else {
                    mIsSlidingToLast = false;
                }
            }
        });
        Intent intent = new Intent(getActivity(), MvDownloadService.class);
        MusicApplication.getContext().startService(intent);
        MusicApplication.getContext().bindService(intent, mDownloadConnection, BIND_AUTO_CREATE);
        return mView;
    }

    //offset:偏移
    private void getMvInfoListFromChosen(int offset) {
        HttpUtil.sendOkHttpRequest(SERVER + mUrl + "?limit=" + MV_NUMS_OF_PAGE + "&offset="
                + offset * MV_NUMS_OF_PAGE, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.body() == null) {
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject mvObject = jsonArray.getJSONObject(i);
                        int mvId = mvObject.getInt("id");
                        String name = mvObject.getString("name");
                        //String copyWriter = mvObject.getString("copywriter");
                        String copyWriter = "";
                        String pictureUrl = mvObject.getString("cover");
                        int playCount = mvObject.getInt("playCount");
                        JSONArray artistArray = mvObject.getJSONArray("artists");
                        List<Artist> artistList = new ArrayList<>();
                        for (int j = 0; j < artistArray.length(); j++) {
                            int artistId = artistArray.getJSONObject(j).getInt("id");
                            String artistName = artistArray.getJSONObject(j).getString("name");
                            artistList.add(new Artist(artistId, artistName));
                        }
                        MvInfo mvInfo = new MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl);
                        mVideoInfoList.add(mvInfo);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTopMvListAdapter.updateData(mVideoInfoList);
                                closeProgressBar();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d("mvPath", "出错");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("mvList", "获取失败");
            }
        });
    }

    private void popMoreWindown(MvInfo mvInfo){
        View popView;
        if (mDownloadPopWindow != null) {
            popView = mDownloadPopWindow.getContentView();
            mDownloadPopWindow.setFocusable(true);
            if (mDownloadPopWindow.isShowing()) {
                mDownloadPopWindow.dismiss();
            } else {
                mDownloadPopWindow.showAtLocation(mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                darkenBackground(0.5f);
            }
        } else {
            popView = getLayoutInflater().inflate(R.layout.mv_download_menu, null);
            mDownloadPopWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                    600, false);
            mDownloadPopWindow.setOutsideTouchable(true); // 点击外部关闭
            mDownloadPopWindow.setFocusable(true);
            mDownloadPopWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            mDownloadPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDownloadPopWindow.showAtLocation(mView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
            darkenBackground(0.5f);
            mDownloadPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    darkenBackground(1.0f);
                }
            });
        }
        //具体操作监听
        ImageView storeView = popView.findViewById(R.id.mv_iv_store);
        ImageView downloadView = popView.findViewById(R.id.mv_iv_download);
        TextView storeTipText = popView.findViewById(R.id.mv_tv_store_tip);
        setMvCollectButton(storeView, storeTipText, mvInfo);
        storeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadPopWindow.dismiss();
                collectMv(storeView, storeTipText, mvInfo);
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE);
            }
        });
        downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadPopWindow.dismiss();
                downloadMv(mvInfo);
            }
        });
    }

    private void setMvCollectButton(ImageView storeView, TextView storeTipText, MvInfo mvInfo){
        MvPresenter.isMvCollected(String.valueOf(mvInfo.getId()), new MvCollectCallback() {
            @Override
            public void isCollected() {
                Activity activity = getActivity();
                if(activity == null){
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_discollection);
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button);
                    }
                });
            }
            @Override
            public void notCollected() {
                Activity activity = getActivity();
                if(activity == null){
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_collection);
                        storeView.setBackgroundResource(R.drawable.mv_collect_button);
                    }
                });
            }
        });
    }

    private void collectMv(ImageView storeView, TextView storeTipText, MvInfo mvInfo){
        MvPresenter.collectMv(mvInfo, new MvCollectCallback() {
            @Override
            public void isCollected() {
                Activity activity = getActivity();
                if(activity == null){
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_discollection);
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button);
                        Toast.makeText(getContext(), "收藏成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void notCollected() {
                Activity activity = getActivity();
                if(activity == null){
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        storeTipText.setText(R.string.mv_collection);
                        storeView.setBackgroundResource(R.drawable.mv_collect_button);
                        Toast.makeText(getContext(), "取消收藏！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void downloadMv(MvInfo mvInfo) {
        if(mDownLoadBinder != null){
            MvPresenter.getMvInfo(mvInfo, new MusicCallBack() {
                @Override
                public void onMusicInfoCompleted() {
                    mDownLoadBinder.startDownLoad(mvInfo);
                }
            });
        }
    }

    //设置屏幕透明度,bgcolor:0-1
    private void darkenBackground(float bgcolor) {
        if (getActivity() != null) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = bgcolor;
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getActivity().getWindow().setAttributes(lp);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
    }

    //进度对话框
    private void showProgressBar() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(getActivity());
            mProgressBar.setMessage("正在加载...");
            mProgressBar.setCanceledOnTouchOutside(false);
        }
        mProgressBar.show();
    }

    //关闭进度对话框
    private void closeProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicApplication.getContext().unbindService(mDownloadConnection);
    }

}
