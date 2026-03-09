package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.AlbumInfo;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.MusicPathInfo;
import com.quibbler.sevenmusic.bean.SingerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyLocalMusicViewPagerAdapter
 * Description:    本地音乐，ViewPager数据适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 15:58
 */
public class MyLocalMusicViewPagerAdapter extends PagerAdapter {
    private static final String titles[] = {"歌曲", "歌手", "专辑", "文件夹"};
    private Context mContext;

    private List<MusicInfo> mMusicInfoLists = new ArrayList<>();

    private List<SingerInfo> mSingerList = new ArrayList<>();
    private List<AlbumInfo> mAlbumList = new ArrayList<>();
    private List<MusicPathInfo> mPathList = new ArrayList<>();

    private Map<String, List<MusicInfo>> mSingerMap = new HashMap<>();
    private Map<String, List<MusicInfo>> mAlbumsMap = new HashMap<>();
    private Map<String, List<MusicInfo>> mPathsMap = new HashMap<>();

    public MyLocalMusicViewPagerAdapter(Context context, List<MusicInfo> musicInfoLists) {
        this.mContext = context;
        this.mMusicInfoLists.addAll(musicInfoLists);
        initData();
    }

    public void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                classifyData();
                tranData();
            }
        }).start();
        notifyDataSetChanged();
    }

    public void classifyData() {
        mSingerMap.clear();
        mAlbumsMap.clear();
        mPathsMap.clear();
        for (MusicInfo musicInfo : mMusicInfoLists) {
            //歌手分类
            if (mSingerMap.containsKey(musicInfo.getSinger())) {
                mSingerMap.get(musicInfo.getSinger()).add(musicInfo);
            } else {
                List<MusicInfo> list = new ArrayList<>();
                list.add(musicInfo);
                mSingerMap.put(musicInfo.getSinger(), list);
            }
            //专辑分类
            if (mAlbumsMap.containsKey(musicInfo.getAlbum())) {
                mAlbumsMap.get(musicInfo.getAlbum()).add(musicInfo);
            } else {
                List<MusicInfo> list = new ArrayList<>();
                list.add(musicInfo);
                mAlbumsMap.put(musicInfo.getAlbum(), list);
            }
            //文件分类
            String path = musicInfo.getMusicFilePath().substring(0, musicInfo.getMusicFilePath().lastIndexOf("/"));
            if (mPathsMap.containsKey(path)) {
                mPathsMap.get(path).add(musicInfo);
            } else {
                List<MusicInfo> list = new ArrayList<>();
                list.add(musicInfo);
                mPathsMap.put(path, list);
            }
        }
    }

    /*
        准备数据给ViewPager使用
     */
    public void tranData() {
        mSingerList.clear();
        mAlbumList.clear();
        mPathList.clear();
        for (Map.Entry<String, List<MusicInfo>> entry : mSingerMap.entrySet()) {
            String singerName = entry.getKey();
            mSingerList.add(new SingerInfo(singerName, mSingerMap.get(singerName)));
        }
        for (Map.Entry<String, List<MusicInfo>> entry : mAlbumsMap.entrySet()) {
            String albumName = entry.getKey();
            mAlbumList.add(new AlbumInfo(albumName, mAlbumsMap.get(albumName)));
        }
        for (Map.Entry<String, List<MusicInfo>> entry : mPathsMap.entrySet()) {
            String path = entry.getKey();
            mPathList.add(new MusicPathInfo(path, mPathsMap.get(path)));
        }
    }

    public void updateData(List<MusicInfo> list) {
        mMusicInfoLists.clear();
        mMusicInfoLists.addAll(list);
        initData();
        notifyDataSetChanged();
    }

    public int getDataCount(int position) {
        switch (position) {
            case 1:
                return mSingerList.size();
            case 2:
                return mAlbumList.size();
            case 3:
                return mPathList.size();
            default:
                return mMusicInfoLists.size();

        }
    }

    /**
     * 重要方法，必须重写。才能使notifyDataSetChanged()生效
     *
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ListView listView = new ListView(mContext);
        listView.setDividerHeight(0);
        switch (position) {
            case 0:
                LocalMusicAdapter musicInfoAdapter = new LocalMusicAdapter(mContext, mMusicInfoLists);
                listView.setAdapter(musicInfoAdapter);
                break;
            case 1:
                MySingerInfoListAdapter singerInfoListAdapter = new MySingerInfoListAdapter(mContext, R.layout.my_singer_list_item, mSingerList);
                listView.setAdapter(singerInfoListAdapter);
                break;
            case 2:
                MyAlbumListViewAdapter albumListViewAdapter = new MyAlbumListViewAdapter(mContext, R.layout.my_album_list_item_layout, mAlbumList);
                listView.setAdapter(albumListViewAdapter);
                break;
            case 3:
                MyFilePathListVIewAdapter musicPathListVIewAdapter = new MyFilePathListVIewAdapter(mContext, R.layout.my_music_path_item, mPathList);
                listView.setAdapter(musicPathListVIewAdapter);
                break;
            default:
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
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
