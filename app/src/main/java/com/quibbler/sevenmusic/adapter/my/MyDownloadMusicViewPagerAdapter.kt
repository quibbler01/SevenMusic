package com.quibbler.sevenmusic.adapter.my

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.viewpager.widget.PagerAdapter
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.utils.MusicThreadPool

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyDownloadMusicViewPagerAdapter
 * Description:    下载音乐ViewPager适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:54
 */
class MyDownloadMusicViewPagerAdapter : PagerAdapter {
    private val mDownloadMusicLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()
    private val mDownloadingMusicLists: MutableList<MusicInfo> = ArrayList<MusicInfo>()
    private var mDownloadAdapter: DownloadMusicAdapter? = null
    private var mDownloadingAdapter: DownloadMusicAdapter? = null

    private val mContext: Context

    @Deprecated("")
    constructor(
        downloadMusicLists: MutableList<MusicInfo?>,
        downloadingMusicLists: MutableList<MusicInfo?>,
        context: Context
    ) {
        this.mDownloadMusicLists.addAll(downloadMusicLists)
        this.mDownloadingMusicLists.addAll(downloadingMusicLists)
        this.mContext = context
    }

    constructor() {
        this.mContext = MusicApplication.Companion.getContext()
        mDownloadingAdapter = DownloadMusicAdapter(mContext, mDownloadingMusicLists)
        mDownloadAdapter = DownloadMusicAdapter(mContext, mDownloadMusicLists)
    }

    fun updateData(
        downloadMusicLists: MutableList<MusicInfo?>,
        downloadingMusicLists: MutableList<MusicInfo?>
    ) {
        Log.d(TAG, "数据更新" + downloadMusicLists.size + " " + downloadingMusicLists.size)
        mDownloadingAdapter!!.clear()
        mDownloadingAdapter!!.addAll(downloadingMusicLists)

        mDownloadAdapter!!.clear()
        mDownloadAdapter!!.addAll(downloadMusicLists)

        notifyDataSetChanged()
    }

    @Deprecated("")
    fun onMusicDownloadDone(id: String?, isSuccess: Boolean) {
        if (isSuccess) {
            MusicThreadPool.postRunnable(object : Runnable {
                override fun run() {
                    for (musicInfo in mDownloadingMusicLists) {
                        if (musicInfo.getId() == id) {
                            mDownloadingAdapter!!.remove(musicInfo)
                            mDownloadAdapter!!.add(musicInfo)
                        }
                    }
                }
            })
        }
    }

    fun insertData(musicInfo: MusicInfo?, position: Int) {
        when (position) {
            0 -> mDownloadMusicLists.add(musicInfo!!)
            1 -> mDownloadingMusicLists.add(musicInfo!!)
            else -> {}
        }
        notifyDataSetChanged()
    }

    /**
     * 0:已下载
     * 1:正在下载
     * 
     * @param status
     */
    fun clearData(status: Int) {
        when (status) {
            0 -> {
                clearDownloadMusicData(status)
                mDownloadAdapter!!.clear()
            }

            1 -> {
                clearDownloadMusicData(status)
                mDownloadingAdapter!!.clear()
            }

            else -> {}
        }
        notifyDataSetChanged()
    }

    /**
     * 从数据库中删除已下载或者正在下载的歌曲记录
     * 暂不删除文件
     * 
     * @param status
     */
    fun clearDownloadMusicData(status: Int) {
        MusicThreadPool.postRunnable(object : Runnable {
            override fun run() {
                val uri =
                    Uri.parse("content://" + MusicContentProvider.Companion.MUSIC_AUTHORITY + "/download/")
                MusicApplication.Companion.getContext().getContentResolver()
                    .delete(uri, "is_download = ?", arrayOf<String>(status.toString() + ""))
            }
        })
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val listView = ListView(mContext)
        listView.setDivider(null)
        when (position) {
            0 -> {
                if (mDownloadMusicLists.size == 0) {
                    val noDownloadView = LayoutInflater.from(mContext)
                        .inflate(R.layout.my_download_download_view, container, false)
                    container.addView(noDownloadView)
                    return noDownloadView
                }
                listView.setAdapter(mDownloadAdapter)
            }

            1 -> {
                if (mDownloadingMusicLists.size == 0) {
                    val noDownloadingView = LayoutInflater.from(mContext)
                        .inflate(R.layout.my_download_no_downloading_view, container, false)
                    container.addView(noDownloadingView)
                    return noDownloadingView
                }
                listView.setAdapter(mDownloadingAdapter)
            }
        }
        container.addView(listView)
        return listView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles!![position]
    }

    override fun getCount(): Int {
        return titles!!.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    companion object {
        private const val TAG = "MyDownloadMusicViewPagerAdapter"
        private val titles: Array<String?>? = arrayOf<String?>("已下载", "下载中")
    }
}
