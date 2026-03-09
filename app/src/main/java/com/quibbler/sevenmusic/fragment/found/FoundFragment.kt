package com.quibbler.sevenmusic.fragment.found

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.SearchMainActivity
import com.quibbler.sevenmusic.activity.found.PlaylistLibActivity
import com.quibbler.sevenmusic.activity.found.SingerLibActivity
import com.quibbler.sevenmusic.adapter.found.FoundShowPlaylistAdapter
import com.quibbler.sevenmusic.adapter.found.FoundTopMvAdapter
import com.quibbler.sevenmusic.bean.MusicURL
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopMvResponseBean
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundTopPlaylistResponseBean
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.view.found.FoundCustomButton
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Package:        com.quibbler.sevenmusic.fragment
 * ClassName:      FoundFragment
 * Description:    发现页面。FoundFragment类，由MainActivity托管
 * Author:         yangwuyang
 * CreateDate:     2019/9/16 17:42
 */
class FoundFragment : Fragment {
    //推荐歌单RecyclerView的Adapter
    private var mFoundTopPlaylistAdapter: FoundShowPlaylistAdapter? = null

    //推荐mv视频RecyclerView的Adapter
    private var mFoundTopMvAdapter: FoundTopMvAdapter? = null

    //歌手库按钮
    private var mBtnSingerLibrary: FoundCustomButton? = null

    //歌单库按钮
    private var mBtnPlaylistLibrary: FoundCustomButton? = null

    //装饰用button
    private var mBtnMy: FoundCustomButton? = null
    private var mBtnMv: FoundCustomButton? = null
    private var mBtnSearch: FoundCustomButton? = null

    private var mOuterViewPager: ViewPager? = null

    //推荐歌单
    private var mRvTopPlaylistRecyclerView: RecyclerView? = null

    //推荐视频
    private var mRvTopMvRecyclerView: RecyclerView? = null

    //推荐歌单的源数据
    private val mTopPlaylistList: MutableList<PlaylistInfo?> = ArrayList<PlaylistInfo?>()

    //推荐视频的源数据
    private val mMvInfoList: MutableList<MvInfo?> = ArrayList<MvInfo?>()

    constructor()

    constructor(viewPager: ViewPager?) {
        mOuterViewPager = viewPager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 加载fragment_found布局文件
        val view = inflater.inflate(R.layout.fragment_found, null)

        //初始化歌单库、歌手库按钮
        initCustomButton(view)

        //初始化推荐歌单部分
        initTopPlaylist(view)
        //初始化推荐视频部分
        initTopMv(view)
        //获取推荐歌单并显示
        this.topPlaylist
        //获取推荐mv视频并显示
        this.topMv

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 初始化歌手库、歌单库按钮
     * 
     * @param view 父view
     */
    private fun initCustomButton(view: View) {
        mBtnSingerLibrary = view.findViewById<FoundCustomButton>(R.id.found_btn_singer_library)
        mBtnPlaylistLibrary = view.findViewById<FoundCustomButton>(R.id.found_btn_playlist_library)
        mBtnMy = view.findViewById<FoundCustomButton>(R.id.found_btn_to_my)
        mBtnMv = view.findViewById<FoundCustomButton>(R.id.found_btn_to_mv)
        mBtnSearch = view.findViewById<FoundCustomButton>(R.id.found_btn_to_search)

        mBtnSingerLibrary!!.setImgResource(R.drawable.singer_lib_icon)
        mBtnSingerLibrary!!.setText("歌手库")
        mBtnSingerLibrary!!.setImgSize(130, 130)

        mBtnPlaylistLibrary!!.setImgResource(R.drawable.playlist_lib_icon)
        mBtnPlaylistLibrary!!.setText("歌单库")
        mBtnPlaylistLibrary!!.setImgSize(130, 130)

        mBtnMy!!.setImgResource(R.drawable.found_to_my_icon)
        mBtnMy!!.setText("我的")
        mBtnMy!!.setImgSize(130, 130)
        mBtnMv!!.setImgResource(R.drawable.found_to_mv_icon)
        mBtnMv!!.setText("MV")
        mBtnMv!!.setImgSize(130, 130)
        mBtnSearch!!.setImgResource(R.drawable.found_to_search_icon)
        mBtnSearch!!.setText("搜索")
        mBtnSearch!!.setImgSize(130, 130)


        //歌手库按钮点击事件监听器
        mBtnSingerLibrary!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(getContext(), SingerLibActivity::class.java)
                startActivity(intent)
            }
        })

        //歌单库按钮点击事件监听
        mBtnPlaylistLibrary!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(getContext(), PlaylistLibActivity::class.java)
                startActivity(intent)
            }
        })

        mBtnMy!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mOuterViewPager != null) {
                    mOuterViewPager!!.setCurrentItem(0)
                }
            }
        })

        mBtnMv!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mOuterViewPager != null) {
                    mOuterViewPager!!.setCurrentItem(2)
                }
            }
        })

        mBtnSearch!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val searchIntent = Intent(getContext(), SearchMainActivity::class.java)
                startActivityForResult(searchIntent, 0)
            }
        })
    }

    /**
     * 初始化“发现”页面的推荐歌单部分
     * 
     * @param view 父view
     */
    private fun initTopPlaylist(view: View) {
        mRvTopPlaylistRecyclerView = view.findViewById<RecyclerView>(R.id.found_rv_top_playlist)
        //绑定LayoutManager
        val gridLayoutManager = GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false)

        mRvTopPlaylistRecyclerView!!.setLayoutManager(gridLayoutManager)
        mRvTopPlaylistRecyclerView!!.setNestedScrollingEnabled(false)
        //绑定Adapter
        mFoundTopPlaylistAdapter = FoundShowPlaylistAdapter(mTopPlaylistList)
        mRvTopPlaylistRecyclerView!!.setAdapter(mFoundTopPlaylistAdapter)
    }

    private val topPlaylist: Unit
        /**
         * 获取推荐歌单并显示
         */
        get() {
            //先检测有无网络，没网络则提示开启，并加载缓存，无缓存则提供刷新按钮。考虑后期优化
            val playlistInfoList: MutableList<PlaylistInfo?> =
                ArrayList<PlaylistInfo?>()
            for (i in 0..8) {
                playlistInfoList.add(PlaylistInfo())
            }
            mFoundTopPlaylistAdapter!!.updateData(playlistInfoList)


            val asyncTask = RequestTopPlaylistAsyncTask()
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

    private val topMv: Unit
        /**
         * 获取推荐mv视频并显示
         */
        get() {
            val mvInfoList: MutableList<MvInfo?> =
                ArrayList<MvInfo?>()
            for (i in 0..3) {
                mvInfoList.add(MvInfo())
            }
            mFoundTopMvAdapter!!.updateData(mvInfoList)

            val asyncTask = RequestTopMvAsyncTask()
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

    /**
     * 初始化“发现”页面的推荐视频部分
     * 
     * @param view 父view
     */
    private fun initTopMv(view: View) {
        mRvTopMvRecyclerView = view.findViewById<RecyclerView>(R.id.found_rv_top_mv)

        //绑定LayoutManager
        val gridLayoutManager = GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false)
        mRvTopMvRecyclerView!!.setLayoutManager(gridLayoutManager)
        mRvTopMvRecyclerView!!.setNestedScrollingEnabled(false)
        //绑定Adapter
        mFoundTopMvAdapter = FoundTopMvAdapter(mMvInfoList)
        mRvTopMvRecyclerView!!.setAdapter(mFoundTopMvAdapter)
    }

    /**
     * Package:        com.quibbler.sevenmusic.fragment.found
     * ClassName:      FoundFragment
     * Description:    内部类，用来获取“发现”页面的推荐歌单response
     * Author:         yanwuyang
     * CreateDate:     2019/9/18 20:21
     */
    private inner class RequestTopPlaylistAsyncTask :
        AsyncTask<Void?, Void?, MutableList<PlaylistInfo?>?>() {
        override fun doInBackground(vararg voids: Void?): MutableList<PlaylistInfo?>? {
            val path: String = TOP_PLAYLIST_REQUEST_URL

            //OkHttp获取网络资源
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(path)
                    .build()
                val response = client.newCall(request).execute()
                Log.d(TAG, "response is: " + response)
                val responseCode = response.code
                Log.d(TAG, "responseCode is: " + responseCode)
                if (responseCode == 200) {
                    val responseData = response.body!!.string()
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!")
                        return null
                    }
                    Log.d(TAG, "responseData is: " + responseData)
                    val gson = Gson()
                    val responseBean = gson.fromJson<FoundTopPlaylistResponseBean>(
                        responseData,
                        FoundTopPlaylistResponseBean::class.java
                    )
                    if (responseBean.getCode() == 200) {
                        return responseBean.getPlaylists()
                    }
                } else {
                    //没有获取到网络资源，可以用缓存，没有缓存则提供刷新按钮。考虑后期优化
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络异常！")
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(playlistInfoList: MutableList<PlaylistInfo?>?) {
            super.onPostExecute(playlistInfoList)
            if (playlistInfoList == null || playlistInfoList.size == 0) {
                return
            }
            mFoundTopPlaylistAdapter!!.updateData(playlistInfoList)
        }
    }


    /**
     * Package:        com.quibbler.sevenmusic.fragment.found
     * ClassName:      FoundFragment
     * Description:    内部类，用来获取“发现”页面的推荐mv视频response
     * Author:         yanwuyang
     * CreateDate:     2019/9/18 20:21
     */
    private inner class RequestTopMvAsyncTask : AsyncTask<Void?, Void?, MutableList<MvInfo?>?>() {
        override fun doInBackground(vararg voids: Void?): MutableList<MvInfo?>? {
            val path: String = TOP_MV_REQUEST_URL

            //OkHttp获取网络资源
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(path)
                    .build()
                val response = client.newCall(request).execute()
                Log.d(TAG, "response is: " + response)
                val responseCode = response.code
                Log.d(TAG, "responseCode is: " + responseCode)
                if (responseCode == 200) {
                    val responseData = response.body!!.string()
                    if (TextUtils.isEmpty(responseData)) {
                        Log.d(TAG, "responseData is empty!")
                        return null
                    }
                    Log.d(TAG, "responseData is: " + responseData)
                    val gson = Gson()
                    val responseBean = gson.fromJson<FoundTopMvResponseBean>(
                        responseData,
                        FoundTopMvResponseBean::class.java
                    )
                    if (responseBean.getCode() == 200) {
                        return responseBean.getData()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "网络异常！")
            }
            return null
        }

        override fun onPostExecute(MvInfoList: MutableList<MvInfo?>?) {
            super.onPostExecute(MvInfoList)
            //只显示前三个视频
            if (MvInfoList == null) {
                //没联网，或者服务器问题
                return
            }
            //            List<MvInfo> list = MvInfoList.subList(0, 3);
            mFoundTopMvAdapter!!.updateData(MvInfoList)
        }
    }

    companion object {
        private const val TAG = "FoundFragment"

        //最新歌单获取地址
        private val TOP_PLAYLIST_REQUEST_URL = MusicURL.API_TOP_PLAYLIST_REQUEST_URL

        //最新mv视频获取地址
        private val TOP_MV_REQUEST_URL = MusicURL.API_TOP_MV_REQUEST_URL
    }
}
