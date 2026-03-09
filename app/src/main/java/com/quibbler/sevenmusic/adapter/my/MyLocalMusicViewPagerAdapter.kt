package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.viewpager.widget.PagerAdapter
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.AlbumInfo
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.MusicPathInfo
import com.quibbler.sevenmusic.bean.SingerInfo

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyLocalMusicViewPagerAdapter
 * Description:    本地音乐，ViewPager数据适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/21 15:58
 */
class MyLocalMusicViewPagerAdapter(context: Context, musicInfoLists: MutableList<MusicInfo?>) :
    PagerAdapter() {
    private val mContext: Context

    private val mMusicInfoLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()

    private val mSingerList: MutableList<SingerInfo?> = ArrayList<SingerInfo?>()
    private val mAlbumList: MutableList<AlbumInfo?> = ArrayList<AlbumInfo?>()
    private val mPathList: MutableList<MusicPathInfo?> = ArrayList<MusicPathInfo?>()

    private val mSingerMap: MutableMap<String?, MutableList<MusicInfo?>?> =
        HashMap<String?, MutableList<MusicInfo?>?>()
    private val mAlbumsMap: MutableMap<String?, MutableList<MusicInfo?>?> =
        HashMap<String?, MutableList<MusicInfo?>?>()
    private val mPathsMap: MutableMap<String?, MutableList<MusicInfo?>?> =
        HashMap<String?, MutableList<MusicInfo?>?>()

    init {
        this.mContext = context
        this.mMusicInfoLists.addAll(musicInfoLists)
        initData()
    }

    fun initData() {
        Thread(object : Runnable {
            override fun run() {
                classifyData()
                tranData()
            }
        }).start()
        notifyDataSetChanged()
    }

    fun classifyData() {
        mSingerMap.clear()
        mAlbumsMap.clear()
        mPathsMap.clear()
        for (musicInfo in mMusicInfoLists) {
            //歌手分类
            if (mSingerMap.containsKey(musicInfo.getSinger())) {
                mSingerMap.get(musicInfo.getSinger())!!.add(musicInfo)
            } else {
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.add(musicInfo)
                mSingerMap.put(musicInfo.getSinger(), list)
            }
            //专辑分类
            if (mAlbumsMap.containsKey(musicInfo.getAlbum())) {
                mAlbumsMap.get(musicInfo.getAlbum())!!.add(musicInfo)
            } else {
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.add(musicInfo)
                mAlbumsMap.put(musicInfo.getAlbum(), list)
            }
            //文件分类
            val path = musicInfo.getMusicFilePath()
                .substring(0, musicInfo.getMusicFilePath().lastIndexOf("/"))
            if (mPathsMap.containsKey(path)) {
                mPathsMap.get(path)!!.add(musicInfo)
            } else {
                val list: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()
                list.add(musicInfo)
                mPathsMap.put(path, list)
            }
        }
    }

    /*
        准备数据给ViewPager使用
     */
    fun tranData() {
        mSingerList.clear()
        mAlbumList.clear()
        mPathList.clear()
        for (entry in mSingerMap.entries) {
            val singerName = entry.key
            mSingerList.add(SingerInfo(singerName, mSingerMap.get(singerName)))
        }
        for (entry in mAlbumsMap.entries) {
            val albumName = entry.key
            mAlbumList.add(AlbumInfo(albumName, mAlbumsMap.get(albumName)))
        }
        for (entry in mPathsMap.entries) {
            val path: String = entry.key!!
            mPathList.add(MusicPathInfo(path, mPathsMap.get(path)))
        }
    }

    fun updateData(list: MutableList<MusicInfo?>) {
        mMusicInfoLists.clear()
        mMusicInfoLists.addAll(list)
        initData()
        notifyDataSetChanged()
    }

    fun getDataCount(position: Int): Int {
        when (position) {
            1 -> return mSingerList.size
            2 -> return mAlbumList.size
            3 -> return mPathList.size
            else -> return mMusicInfoLists.size

        }
    }

    /**
     * 重要方法，必须重写。才能使notifyDataSetChanged()生效
     * 
     * @param object
     * @return
     */
    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val listView = ListView(mContext)
        listView.setDividerHeight(0)
        when (position) {
            0 -> {
                val musicInfoAdapter = LocalMusicAdapter(mContext, mMusicInfoLists)
                listView.setAdapter(musicInfoAdapter)
            }

            1 -> {
                val singerInfoListAdapter =
                    MySingerInfoListAdapter(mContext, R.layout.my_singer_list_item, mSingerList)
                listView.setAdapter(singerInfoListAdapter)
            }

            2 -> {
                val albumListViewAdapter =
                    MyAlbumListViewAdapter(mContext, R.layout.my_album_list_item_layout, mAlbumList)
                listView.setAdapter(albumListViewAdapter)
            }

            3 -> {
                val musicPathListVIewAdapter =
                    MyFilePathListVIewAdapter(mContext, R.layout.my_music_path_item, mPathList)
                listView.setAdapter(musicPathListVIewAdapter)
            }

            else -> {}
        }
        container.addView(listView)
        return listView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return titles!!.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles!![position]
    }

    companion object {
        private val titles: Array<String?>? = arrayOf<String?>("歌曲", "歌手", "专辑", "文件夹")
    }
}
