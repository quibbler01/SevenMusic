package com.quibbler.sevenmusic.fragment.found

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
import com.google.gson.Gson
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.FoundShowPlaylistAdapter
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundPlaylistLibBean
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Package:        com.quibbler.sevenmusic.fragment.found
 * ClassName:      PlaylistLibItemFragment
 * Description:    歌单库页面，每个标签对应的歌单展示页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/19 21:15
 */
class PlaylistLibItemFragment(catName: String, tabName: String?) : Fragment() {
    //向服务器请求时的cat名
    private val mCatName: String

    //fragment对应tab标签上显示的名字
    private val mTabName: String?

    //显示歌单的RecyclerView
    private var mRecyclerView: RecyclerView? = null

    //歌单库页面的数据list
    private val mPlaylistInfoList: MutableList<PlaylistInfo?> = ArrayList<PlaylistInfo?>()
    private val mPlaylistLibRecyclerAdapter = FoundShowPlaylistAdapter(mPlaylistInfoList)

    private var mRequestPlaylistAsyncTask: RequestPlaylistAsyncTask? = null


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
    init {
        mCatName = catName
        mTabName = tabName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_found_playlist_lib, container, false)
        mRecyclerView = view as RecyclerView
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //设置RecyclerView的
        val gridLayoutManager = GridLayoutManager(getContext(), 3)
        mRecyclerView!!.setLayoutManager(gridLayoutManager)
        //设置RecyclerView的adapter
        mRecyclerView!!.setAdapter(mPlaylistLibRecyclerAdapter)

        //网易api的url
        val url: String = URL_AUTHORITY + mCatName
        requestPlaylist(url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRequestPlaylistAsyncTask!!.cancel(true)
    }

    /**
     * 从网易api获取歌单数据
     * 
     * @param url
     */
    private fun requestPlaylist(url: String?) {
        mRequestPlaylistAsyncTask = RequestPlaylistAsyncTask()
        mRequestPlaylistAsyncTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url)
    }

    /**
     * 根据url获取歌单库的数据，并更新显示
     */
    private inner class RequestPlaylistAsyncTask :
        AsyncTask<String?, Void?, MutableList<PlaylistInfo?>?>() {
        override fun doInBackground(vararg strings: String): MutableList<PlaylistInfo?>? {
            val path: String = strings[0]

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
                    val responseBean = gson.fromJson<FoundPlaylistLibBean>(
                        responseData,
                        FoundPlaylistLibBean::class.java
                    )

                    return responseBean.getPlaylists()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(playlistInfoList: MutableList<PlaylistInfo?>?) {
            super.onPostExecute(playlistInfoList)
            mPlaylistLibRecyclerAdapter.updateData(playlistInfoList)
        }
    }

    companion object {
        private const val TAG = "PlaylistLibItemFragment"

        //api地址形如：http://114.116.128.229:3000/top/playlist/highquality?cat=%E5%8D%8E%E8%AF%AD
        private const val URL_AUTHORITY =
            "http://114.116.128.229:3000/top/playlist/highquality?cat="
    }
}
