package com.quibbler.sevenmusic.activity.found

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter
import com.quibbler.sevenmusic.bean.Creator
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistDetailResponseBean
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.ColorUtils
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.ThreadDispatcher
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Package:        com.quibbler.sevenmusic.activity.found
 * ClassName:      PlaylistActivity
 * Description:    单个歌单的显示页面
 * Author:         yanwuyang
 * CreateDate:     2019/9/24 10:49
 */
class PlaylistActivity : BaseMusicListActivity<PlaylistAdapter?>() {
    //当前歌单页面所显示歌单的id
    private var mPlaylistId: String? = null

    //歌单的歌曲列表
    private var mMusicInfoList: MutableList<MusicInfo>? = ArrayList<MusicInfo>()

    //获取歌单详细信息的AsyncTask
    private var mRequestPlaylistDetailAsyncTask: RequestPlaylistDetailAsyncTask? = null

    //显示歌曲的RecyclerView的Adapter
    private var mPlaylistAdapter: PlaylistAdapter? = null

    //该歌单是否被收藏
    private var mIsCollected = false
    private var mQueryThread: Thread? = null

    //当前页面的歌单对象
    private var mPlaylistInfo: PlaylistInfo? = PlaylistInfo()

    //管控线程的标志位
    @Volatile
    private var mIsCancelled = false

    //显示歌曲列表的RecyclerView
    private var mTracksRecyclerView: RecyclerView? = null

    //歌单封面
    private var mIvCover: ImageView? = null

    //歌单名
    private var mTvPlaylistName: TextView? = null

    //歌单创建者
    private var mTvCreator: TextView? = null

    //收藏歌单按钮
    private var mBtnCollect: ImageButton? = null

    //封面背景
    private var mLinearLayout: LinearLayout? = null

    //播放全部
    private var mTvPlayAll: TextView? = null
    private var mIbtnPlayAll: ImageButton? = null

    //开启下载模式
    private var mTvSelectMode: TextView? = null
    private var mIsInSelectMode = false
    private var mLlBottom: LinearLayout? = null
    private var mTvSelectAll: TextView? = null
    private var mTvUnselectAll: TextView? = null
    private var mTvStartDownload: TextView? = null


    /**
     * 只有通过intent传入id，才能打开歌单页面
     * 
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        mPlaylistId = getIntent().getStringExtra(getString(R.string.playlist_id))
        mPlaylistInfo!!.setId(mPlaylistId)

        init()
    }

    private fun init() {
        mIsCancelled = false
        initView()
        initActionBar()
        initRecyclerView()

        initBtnDownloadAll()

        //开启线程，在线程中：查询数据库是否有记录，有则显示页面，没有则进行网络请求。最后初始化button按钮。
        mQueryThread = Thread(object : Runnable {
            override fun run() {
                this.playlistDataInThread
                if (!mIsCollected || mMusicInfoList == null || mMusicInfoList!!.size == 0) {
                    requestPlaylistDetailData()
                } else {
                    //上面流程走完，更新页面显示，再初始化button（包括button的图片、button的listener）
                    //用标志位管控
                    if (!mIsCancelled) {
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                initBtnCollect()
                                showPlaylist()
                            }
                        })
                    }
                }
            }
        })
        mQueryThread!!.start()
    }

    /**
     * 初始化页面的actionBar
     */
    private fun initActionBar() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle("歌单")
    }

    private fun initView() {
        mIvCover = findViewById<ImageView?>(R.id.playlist_iv_cover)
        mTvPlaylistName = findViewById<TextView>(R.id.playlist_tv_name)
        mTvCreator = findViewById<TextView>(R.id.playlist_tv_creator)
        mBtnCollect = findViewById<ImageButton>(R.id.playlist_btn_add_to_collection)
        mTracksRecyclerView = findViewById<RecyclerView>(R.id.playlist_recyclerview_tracks)
        mLinearLayout = findViewById<LinearLayout>(R.id.playlist_ll_background)

        mTvSelectMode = findViewById<TextView>(R.id.playlist_tv_download_mode)
        mLlBottom = findViewById<LinearLayout>(R.id.playlist_ll_bottom)
        mTvSelectAll = findViewById<TextView>(R.id.playlist_tv_select_all)
        mTvUnselectAll = findViewById<TextView>(R.id.playlist_tv_unselect_all)
        mTvStartDownload = findViewById<TextView>(R.id.playlist_tv_start_download)

        mTvPlayAll = findViewById<TextView>(R.id.playlist_tv_play_all)
        mIbtnPlayAll = findViewById<ImageButton>(R.id.playlist_ib_play_all)
    }

    private fun initBtnDownloadAll() {
        mTvSelectMode!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onSelectModeChange(!mIsInSelectMode)
                mIsInSelectMode = !mIsInSelectMode
            }
        })

        mTvSelectAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mPlaylistAdapter!!.selectAll()
            }
        })

        mTvUnselectAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mPlaylistAdapter!!.unselectAll()
            }
        })

        mTvStartDownload!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mPlaylistAdapter!!.startDownload()
                onSelectModeChange(false)
            }
        })

        mTvPlayAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mMusicInfoList != null && mMusicInfoList!!.size > 0) {
                    MusicPlayerService.Companion.addToPlayerList(mMusicInfoList)
                    MusicPlayerService.Companion.playMusic(mMusicInfoList!!.get(0))
                    mPlaylistAdapter!!.setPlayingPosition(0)
                    mPlaylistAdapter!!.notifyDataSetChanged()
                }
            }
        })
        mIbtnPlayAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mMusicInfoList != null && mMusicInfoList!!.size > 0) {
                    MusicPlayerService.Companion.addToPlayerList(mMusicInfoList)
                    MusicPlayerService.Companion.playMusic(mMusicInfoList!!.get(0))
                    mPlaylistAdapter!!.setPlayingPosition(0)
                    mPlaylistAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onSelectModeChange(mode: Boolean) {
        if (mode) {
            //显示取消按钮，显示下方全选、确定按钮，通知adapter
            mTvSelectMode!!.setText("取消")
            mLlBottom!!.setVisibility(View.VISIBLE)
            mPlaylistAdapter!!.changeSelectMode(true)
        } else {
            //显示下载按钮，隐藏下方全选、确定按钮，通知adapter
            mTvSelectMode!!.setText("下载")
            mLlBottom!!.setVisibility(View.GONE)
            mPlaylistAdapter!!.changeSelectMode(false)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        mTracksRecyclerView!!.setLayoutManager(linearLayoutManager)
        mPlaylistAdapter = PlaylistAdapter(mMusicInfoList, this)
        super.mAdapter = mPlaylistAdapter
        mTracksRecyclerView!!.setAdapter(mPlaylistAdapter)
        mTracksRecyclerView!!.setNestedScrollingEnabled(false)
    }

    private val playlistDataInThread: Boolean
        /**
         * 获取歌单信息。先查本地数据库，若没有再去网络请求。
         * 方法名中加XXXInThread，提醒使用者要开启线程来调用
         */
        get() {
            val uri: Uri = MusicContentProvider.Companion.SONGLIST_URL
            if (!TextUtils.isEmpty(mPlaylistInfo!!.getId())) {
                val cursor = getContentResolver().query(
                    uri,
                    null,
                    "id = ?",
                    arrayOf<String?>(mPlaylistInfo!!.getId()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    //本地收藏数据库有记录
                    mIsCollected = true
                    mPlaylistInfo!!.setName(cursor.getString(cursor.getColumnIndex("name")))
                    mPlaylistInfo!!.setDescription(cursor.getString(cursor.getColumnIndex("description")))
                    mPlaylistInfo!!.setTrackCount(cursor.getInt(cursor.getColumnIndex("number")))

                    //            mPlaylistInfo.setCoverImgUrl(cursor.getString(cursor.getColumnIndex("coverimgurl")));
                    val creatorJson =
                        cursor.getString(cursor.getColumnIndex("creator"))
                    val creator =
                        Gson().fromJson<Creator?>(
                            creatorJson,
                            Creator::class.java
                        )
                    mPlaylistInfo!!.setCreator(creator)

                    val tracksJson =
                        cursor.getString(cursor.getColumnIndex("songs"))
                    mMusicInfoList = Gson().fromJson<MutableList<MusicInfo>?>(
                        tracksJson,
                        object : TypeToken<MutableList<MusicInfo?>?>() {
                        }.getType()
                    )
                    mPlaylistInfo!!.setTracks(mMusicInfoList)
                } else {
                    mIsCollected = false
                }
                cursor!!.close()
                return mIsCollected
            } else {
                Log.d(
                    TAG,
                    "getPlaylistDataInThread,mPlaylistInfo.getId() is null"
                )
                return false
            }
        }

    /**
     * 将歌单数据在页面上显示
     */
    private fun showPlaylist() {
        mTvPlaylistName!!.setText(mPlaylistInfo!!.getName())
        val creator = mPlaylistInfo!!.getCreator()
        if (creator != null) {
            mTvCreator!!.setText(creator.getNickname())
        }
        mPlaylistAdapter!!.updateData(mMusicInfoList)

        if (mPlaylistInfo!!.getCoverImgUrl() == null) {
            HttpUtil.sendOkHttpRequest(
                REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY + mPlaylistInfo!!.getId(),
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body!!.string()
                        if (!TextUtils.isEmpty(responseBody)) {
                            val gson = Gson()
                            val responseBean = gson.fromJson<PlaylistDetailResponseBean>(
                                responseBody,
                                PlaylistDetailResponseBean::class.java
                            )
                            if (responseBean.getCode() == "200") {
                                mPlaylistInfo!!.setCoverImgUrl(
                                    responseBean.getPlaylist().getCoverImgUrl()
                                )
                            }
                        }
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                ImageDownloadPresenter.Companion.getInstance()
                                    .with(MusicApplication.Companion.getContext())
                                    .load(mPlaylistInfo!!.getCoverImgUrl())
                                    .imageStyle(ImageDownloadPresenter.Companion.STYLE_ORIGIN)
                                    .into(
                                        mIvCover,
                                        object : ImageDownloadPresenter.ResourceCallback<Bitmap?> {
                                            override fun onResourceReady(resource: Bitmap) {
                                                val width = resource.getWidth()
                                                val height = resource.getHeight()

                                                val startColor = resource.getPixel(0, height - 1)
                                                var midColor =
                                                    resource.getPixel(width / 2, height / 2)
                                                val endColor = resource.getPixel(width - 1, 0)

                                                if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(
                                                        midColor
                                                    ) && ColorUtils.isPixelShallow(endColor)
                                                ) {
                                                    midColor = -1063808
                                                }
                                                val gradientDrawable = GradientDrawable(
                                                    GradientDrawable.Orientation.BL_TR,
                                                    intArrayOf(startColor, midColor, endColor)
                                                )
                                                mLinearLayout!!.setBackground(gradientDrawable)
                                            }
                                        })
                            }
                        })
                    }
                })
        } else {
            ImageDownloadPresenter.Companion.getInstance()
                .with(MusicApplication.Companion.getContext())
                .load(mPlaylistInfo!!.getCoverImgUrl())
                .imageStyle(ImageDownloadPresenter.Companion.STYLE_ORIGIN)
                .into(mIvCover, object : ImageDownloadPresenter.ResourceCallback<Bitmap?> {
                    override fun onResourceReady(resource: Bitmap) {
                        val width = resource.getWidth()
                        val height = resource.getHeight()

                        val startColor = resource.getPixel(0, height - 1)
                        var midColor = resource.getPixel(width / 2, height / 2)
                        val endColor = resource.getPixel(width - 1, 0)

                        if (ColorUtils.isPixelShallow(startColor) && ColorUtils.isPixelShallow(
                                midColor
                            ) && ColorUtils.isPixelShallow(endColor)
                        ) {
                            midColor = -1063808
                        }
                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.BL_TR,
                            intArrayOf(startColor, midColor, endColor)
                        )
                        mLinearLayout!!.setBackground(gradientDrawable)
                    }
                })
        }
    }

    /**
     * 根据歌单id获取歌单的详细信息
     */
    private fun requestPlaylistDetailData() {
        val url: String = REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY + mPlaylistId
        mRequestPlaylistDetailAsyncTask = RequestPlaylistDetailAsyncTask()
        mRequestPlaylistDetailAsyncTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url)
    }

    /**
     * 初始化button，收藏、下载，与数据库交互
     */
    private fun initBtnCollectListener() {
        mBtnCollect!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mIsCollected) {
                    //歌单已收藏 点击后取消收藏
                    mIsCollected = false
                    mBtnCollect!!.setImageResource(R.drawable.playlist_btn_not_collected)

                    //写数据库
                    ThreadDispatcher.Companion.getInstance().runOnWorkerThread(object : Runnable {
                        override fun run() {
                            val uri: Uri = MusicContentProvider.Companion.SONGLIST_URL
                            getContentResolver().delete(
                                uri,
                                "id = ?",
                                arrayOf<String?>(mPlaylistInfo!!.getId())
                            )
                        }
                    })
                } else {
                    //歌单未收藏，点击后收藏
                    mIsCollected = true
                    mBtnCollect!!.setImageResource(R.drawable.playlist_btn_collected)

                    //写数据库
                    ThreadDispatcher.Companion.getInstance().runOnWorkerThread(object : Runnable {
                        override fun run() {
                            val uri: Uri = MusicContentProvider.Companion.SONGLIST_URL
                            val values = ContentValues()
                            val gson = Gson()
                            values.put("id", mPlaylistInfo!!.getId())
                            values.put("name", mPlaylistInfo!!.getName())
                            values.put("type", PLAYLIST_KIND)
                            values.put("description", mPlaylistInfo!!.getDescription())
                            values.put("songs", gson.toJson(mPlaylistInfo!!.getTracks()))
                            values.put("number", mPlaylistInfo!!.getTrackCount())
                            values.put("creator", gson.toJson(mPlaylistInfo!!.getCreator()))
                            values.put("coverimgurl", mPlaylistInfo!!.getCoverImgUrl())

                            getContentResolver().insert(uri, values)
                        }
                    })
                }
            }
        })
    }

    /**
     * 初始化收藏按钮。先查询该歌单是否被收藏,是则显示红色爱心图片，否则显示空心爱心图片。然后为按钮设置监听器。
     */
    private fun initBtnCollect() {
        if (mPlaylistInfo == null) {
            return
        }
        ThreadDispatcher.Companion.getInstance().runOnWorkerThread(object : Runnable {
            override fun run() {
                val uri: Uri = MusicContentProvider.Companion.SONGLIST_URL

                val cursor = getContentResolver().query(
                    uri,
                    null,
                    "id = ?",
                    arrayOf<String?>(mPlaylistInfo!!.getId()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    mIsCollected = true
                    runOnUiThread(object : Runnable {
                        override fun run() {
                            mBtnCollect!!.setImageResource(R.drawable.playlist_btn_collected)
                        }
                    })
                } else {
                    mIsCollected = false
                    runOnUiThread(object : Runnable {
                        override fun run() {
                            mBtnCollect!!.setImageResource(R.drawable.playlist_btn_not_collected)
                        }
                    })
                }
                if (cursor != null) {
                    cursor.close()
                }
            }
        })

        initBtnCollectListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRequestPlaylistDetailAsyncTask != null) {
            mRequestPlaylistDetailAsyncTask!!.cancel(true)
        }
        mIsCancelled = true
        if (mQueryThread != null) {
            mQueryThread!!.interrupt()
        }
    }

    private inner class RequestPlaylistDetailAsyncTask :
        AsyncTask<String?, Void?, PlaylistInfo?>() {
        override fun doInBackground(vararg strings: String): PlaylistInfo? {
            val url: String = strings[0]
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()
                val response = client.newCall(request).execute()
                if (response.code == 200) {
                    val responseBody = response.body!!.string()
                    if (!TextUtils.isEmpty(responseBody)) {
                        val gson = Gson()
                        val responseBean = gson.fromJson<PlaylistDetailResponseBean>(
                            responseBody,
                            PlaylistDetailResponseBean::class.java
                        )
                        if (responseBean.getCode() == "200") {
                            mPlaylistInfo = responseBean.getPlaylist()
                            return mPlaylistInfo
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络异常！")
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(playlistInfo: PlaylistInfo?) {
            super.onPostExecute(playlistInfo)
            if (playlistInfo == null) {
                return
            }

            mMusicInfoList = playlistInfo.getTracks()
            for (musicInfo in mMusicInfoList!!) {
                musicInfo.setSinger(musicInfo.getFirstArName())
            }
            showPlaylist()
            initBtnCollect()
        }
    }

    companion object {
        private const val TAG = "PlaylistActivityTest"

        private const val PLAYLIST_KIND = 1 //非自建歌单，不可修改

        //获取歌单详细信息的url
        private const val REQUEST_PLAYLIST_DETAIL_URL_AUTHORITY =
            "http://114.116.128.229:3000/playlist/detail?id="
    }
}
