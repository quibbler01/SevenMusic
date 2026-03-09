package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyDownloadMusicViewPagerAdapter
 * Description:    下载音乐ViewPager适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:54
 */
public class MyDownloadMusicViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "MyDownloadMusicViewPagerAdapter";
    private static final String titles[] = {"已下载", "下载中"};
    private List<MusicInfo> mDownloadMusicLists = new ArrayList<>();
    private List<MusicInfo> mDownloadingMusicLists = new ArrayList<>();
    private DownloadMusicAdapter mDownloadAdapter;
    private DownloadMusicAdapter mDownloadingAdapter;

    private Context mContext;

    @Deprecated
    public MyDownloadMusicViewPagerAdapter(List<MusicInfo> downloadMusicLists, List<MusicInfo> downloadingMusicLists, Context context) {
        this.mDownloadMusicLists.addAll(downloadMusicLists);
        this.mDownloadingMusicLists.addAll(downloadingMusicLists);
        this.mContext = context;
    }

    public MyDownloadMusicViewPagerAdapter() {
        this.mContext = MusicApplication.getContext();
        mDownloadingAdapter = new DownloadMusicAdapter(mContext, mDownloadingMusicLists);
        mDownloadAdapter = new DownloadMusicAdapter(mContext, mDownloadMusicLists);
    }

    public void updateData(List<MusicInfo> downloadMusicLists, List<MusicInfo> downloadingMusicLists) {
        Log.d(TAG, "数据更新" + downloadMusicLists.size() + " " + downloadingMusicLists.size());
        mDownloadingAdapter.clear();
        mDownloadingAdapter.addAll(downloadingMusicLists);

        mDownloadAdapter.clear();
        mDownloadAdapter.addAll(downloadMusicLists);

        notifyDataSetChanged();
    }

    @Deprecated
    public void onMusicDownloadDone(String id, boolean isSuccess) {
        if (isSuccess) {
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    for (MusicInfo musicInfo : mDownloadingMusicLists) {
                        if (musicInfo.getId().equals(id)) {
                            mDownloadingAdapter.remove(musicInfo);
                            mDownloadAdapter.add(musicInfo);
                        }
                    }
                }
            });

        }
    }

    public void insertData(MusicInfo musicInfo, int position) {
        switch (position) {
            case 0:
                mDownloadMusicLists.add(musicInfo);
                break;
            case 1:
                mDownloadingMusicLists.add(musicInfo);
                break;
            default:
                break;
        }
        notifyDataSetChanged();
    }

    /**
     * 0:已下载
     * 1:正在下载
     *
     * @param status
     */
    public void clearData(int status) {
        switch (status) {
            case 0:
                clearDownloadMusicData(status);
                mDownloadAdapter.clear();
                break;
            case 1:
                clearDownloadMusicData(status);
                mDownloadingAdapter.clear();
                break;
            default:
                break;
        }
        notifyDataSetChanged();
    }

    /**
     * 从数据库中删除已下载或者正在下载的歌曲记录
     * 暂不删除文件
     *
     * @param status
     */
    public void clearDownloadMusicData(int status) {
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("content://" + MusicContentProvider.MUSIC_AUTHORITY + "/download/");
                MusicApplication.getContext().getContentResolver().delete(uri, "is_download = ?", new String[]{status + ""});
            }
        });
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ListView listView = new ListView(mContext);
        listView.setDivider(null);
        switch (position) {
            case 0:
                if (mDownloadMusicLists.size() == 0) {
                    View noDownloadView = LayoutInflater.from(mContext).inflate(R.layout.my_download_download_view, container, false);
                    container.addView(noDownloadView);
                    return noDownloadView;
                }
                listView.setAdapter(mDownloadAdapter);
                break;
            case 1:
                if (mDownloadingMusicLists.size() == 0) {
                    View noDownloadingView = LayoutInflater.from(mContext).inflate(R.layout.my_download_no_downloading_view, container, false);
                    container.addView(noDownloadingView);
                    return noDownloadingView;
                }
                listView.setAdapter(mDownloadingAdapter);
                break;
        }
        container.addView(listView);
        return listView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
