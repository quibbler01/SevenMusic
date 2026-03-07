package com.quibbler.sevenmusic.fragment.mv;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.callback.MusicCallBack;
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
 * Package:        com.quibbler.sevenmusic.fragment.mv
 * ClassName:      ChildMvFragment
 * Description:    mv模块下排行和更多fragment
 * Author:         lishijun
 * CreateDate:     2019/9/24 17:22
 */
public class TopMvFragment extends Fragment {

    private static final String SERVER = "http://114.116.128.229:3000";

    private String mUrl = "/top/mv";

    private List<MvInfo> mVideoInfoList = new ArrayList<>();

    private View mView;

    private ProgressDialog mProgressBar;

    //弹出的下载收藏等popview
    private PopupWindow mDownloadPopWindow;

    private MvDownloadService.DownLoadBinder mDownLoadBinder;

    //交替添加mv的view，双排显示
    private boolean mFlag = true;

    private IScrollView mIScrollView;

    private int mPages = 0; //当前加载了第几页

    private ServiceConnection mDownloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownLoadBinder = (MvDownloadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public TopMvFragment() {

    }

    public TopMvFragment(String url) {
        mUrl = url;
    }

    public static Fragment newInstance(String arg) {
        TopMvFragment fragment = new TopMvFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载fragment_friend布局文件
        mView = inflater.inflate(R.layout.fragment_choosen_child_mv, null);
        mIScrollView = mView.findViewById(R.id.mv_sv_mvs);
        PageEffectUtil.setScrollViewSpringback(mIScrollView);
        //滑动到底部，加载更多
        mIScrollView.setIScrollViewListener(new IScrollViewListener() {
            @Override
            public void onScrollToBottom() {
                //加载更多mv
                showProgressBar();
                getMvInfoListFromChosen(++mPages);
            }
        });
        mUrl = getArguments().getString("url");
        getMvInfoListFromChosen(0);
        Intent intent = new Intent(getActivity(), MvDownloadService.class);
        MusicApplication.getContext().startService(intent);
        MusicApplication.getContext().bindService(intent, mDownloadConnection, BIND_AUTO_CREATE);
        return mView;
    }

    //offset:偏移
    private void getMvInfoListFromChosen(int offset) {
        HttpUtil.sendOkHttpRequest(SERVER + mUrl + "?limit=10" + "&offset=" + offset * 10, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
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
                        //在主线程更新
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMvPicture(mvInfo);
                                }
                            });
                        }
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("mvList", "获取失败");
            }
        });
    }


    //显示第i个mv的缩略图
    private void showMvPicture(MvInfo mvInfo) {
        if (mvInfo == null) {
            return;
        }
        //双排显示
        LinearLayout mvView = null;
        if (mFlag) {
            mvView = mView.findViewById(R.id.mv_sv1);
            mFlag = false;
        } else {
            mvView = mView.findViewById(R.id.mv_sv2);
            mFlag = true;
        }
        View view = LayoutInflater.from(MusicApplication.getContext()).inflate(R.layout.small_mv_item,
                mvView, false);
        final ImageView videoView = view.findViewById(R.id.mv_video);
        TextView videoNameView = view.findViewById(R.id.mv_tv_name);
        TextView videoArtistView = view.findViewById(R.id.mv_tv_artist);
        videoNameView.setText(mvInfo.getName());
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mvInfo.getArtists();
        if (mvArtistList != null) {
            for (int i = 0; i < mvArtistList.size(); i++) {
                if (i == mvArtistList.size() - 1) {
                    artistString.append(mvArtistList.get(i).getName());
                } else {
                    artistString.append(mvArtistList.get(i).getName() + "/");
                }
            }
            videoArtistView.setText(artistString);
        }
        mvView.addView(view);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo);
                }
            }
        });
        //注册MV的收藏、下载等
        dealMV(mvInfo, view);
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        //先用glide加载
        if (getContext() != null) {
            Glide.with(getContext())
                    .load(mvInfo.getPictureUrl())
                    .apply(options)
                    .into(videoView);
        }
    }

    //注册MV的收藏、下载等
    private void dealMV(MvInfo mvInfo, View view) {
        //mv的下载或收藏监听
        ImageButton downloadButton = view.findViewById(R.id.mv_ib_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                storeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDownloadPopWindow.dismiss();
                        Toast.makeText(getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
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
        });
    }

    private void downloadMv(MvInfo mvInfo) {
        if (mDownLoadBinder != null) {
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
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT).show();
            }
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

