package com.quibbler.sevenmusic.fragment.mv

import android.app.Activity
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.sevenvideoview.SevenVideoPlayerManager
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.adapter.mv.RecommendMvAdapter
import com.quibbler.sevenmusic.adapter.mv.TopMvClickListener
import com.quibbler.sevenmusic.adapter.mv.TopMvListAdapter
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.callback.MvCollectCallback
import com.quibbler.sevenmusic.presenter.MvPresenter
import com.quibbler.sevenmusic.service.MvDownloadService
import com.quibbler.sevenmusic.service.MvDownloadService.DownLoadBinder
import com.quibbler.sevenmusic.utils.HttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


/**
 * 
 * Package:        com.quibbler.sevenmusic.fragment.mv
 * ClassName:      NewChildMvFragment
 * Description:    推荐Fragment
 * Author:        lishijun
 * CreateDate:     2019/11/14 14:57
 */
class NewChildMvFragment : Fragment {
    private var mUrl: String? = "/top/mv"

    private val mVideoInfoList: MutableList<MvInfo?> = ArrayList<MvInfo?>()

    private val mSmallVideoInfoList: MutableList<MvInfo?> = ArrayList<MvInfo?>()

    private var mView: View? = null

    private var mProgressBar: ProgressDialog? = null

    //弹出的下载收藏等popview
    private var mDownloadPopWindow: PopupWindow? = null

    private var mDownLoadBinder: DownLoadBinder? = null

    private var mPages = 0 //当前加载了第几页

    private var mRecommendMvListView: RecyclerView? = null

    private val mSmallRecommendMvListView: RecyclerView? = null

    private val mTopMvListAdapter: TopMvListAdapter? = null

    private var mRecommendMvAdapter: RecommendMvAdapter? = null

    private val mDownloadConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mDownLoadBinder = service as DownLoadBinder?
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    constructor()

    constructor(url: String?) {
        mUrl = url
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 加载fragment_new_child_mv布局文件
        mView = inflater.inflate(R.layout.fragment_new_child_mv, null)
        mUrl = getArguments()!!.getString("url")
        //initSmallRecommendMv();
        initRecommendMv()
        return mView!!
    }

    //    private void initSmallRecommendMv(){
    //        mSmallRecommendMvListView = mView.findViewById(R.id.mv_rv_recommend_mvs);
    //        GridLayoutManager gridLayoutManager = new GridLayoutManager(MusicApplication.getContext(),
    //                2, RecyclerView.VERTICAL, false);
    //        mSmallRecommendMvListView.setLayoutManager(gridLayoutManager);
    //        mSmallRecommendMvListView.setNestedScrollingEnabled(false);
    //        mTopMvListAdapter = new TopMvListAdapter(mSmallVideoInfoList, new TopMvClickListener() {
    //            @Override
    //            public void onStartMvPlayActivity(MvInfo mvInfo) {
    //                //跳转进入播放activity
    //                if (getActivity() != null) {
    //                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo);
    //                }
    //            }
    //            @Override
    //            public void onClickMoreButton(MvInfo mvInfo) {
    //                popMoreWindown(mvInfo);
    //            }
    //        });
    //        mSmallRecommendMvListView.setAdapter(mTopMvListAdapter);
    //        getMvInfoListFromRecommend();
    //    }
    private fun initRecommendMv() {
        mRecommendMvListView = mView!!.findViewById<RecyclerView>(R.id.mv_rv_mvs)
        val layoutManager = GridLayoutManager(
            MusicApplication.Companion.getContext(), 2,
            RecyclerView.VERTICAL, false
        )
        layoutManager.setSpanSizeLookup(object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == 0) {
                    return layoutManager.getSpanCount()
                } else if (position <= 4) {
                    return 1
                } else {
                    return layoutManager.getSpanCount()
                }
            }
        })
        mRecommendMvListView!!.setLayoutManager(layoutManager)
        mRecommendMvAdapter = RecommendMvAdapter(mVideoInfoList, object : TopMvClickListener {
            override fun onStartMvPlayActivity(mvInfo: MvInfo?) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo)
                }
            }

            override fun onClickMoreButton(mvInfo: MvInfo) {
                popMoreWindown(mvInfo)
            }
        })
        if (getActivity() != null) {
            mRecommendMvAdapter!!.setAppCompatActivity(getActivity())
        } else {
            mRecommendMvAdapter!!.setAppCompatActivity(MusicApplication.Companion.getContext())
        }
        mRecommendMvListView!!.setAdapter(mRecommendMvAdapter)
        this.mvInfoListFromRecommend
        //滑动到底部，加载更多
        mRecommendMvListView!!.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            var mIsSlidingToLast: Boolean = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //设置什么布局管理器,就获取什么的布局管理器
                val manager = recyclerView.getLayoutManager() as GridLayoutManager?
                // 当停止滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    val lastVisibleItem = manager!!.findLastCompletelyVisibleItemPosition()
                    //所有条目,数量值
                    val totalItemCount = manager.getItemCount()
                    // 判断是否滚动到底部
                    if (lastVisibleItem == (totalItemCount - 1) && mIsSlidingToLast) {
                        showProgressBar()
                        getMvInfoListFromChosen(++mPages)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    mIsSlidingToLast = true
                } else {
                    mIsSlidingToLast = false
                }
            }
        })
        val intent = Intent(getActivity(), MvDownloadService::class.java)
        MusicApplication.Companion.getContext().startService(intent)
        MusicApplication.Companion.getContext()
            .bindService(intent, mDownloadConnection, Context.BIND_AUTO_CREATE)
    }

    private val mvInfoListFromRecommend: Unit
        get() {
            HttpUtil.sendOkHttpRequest(
                SERVER + MV_RECOMMEND,
                object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        if (response.body == null) {
                            return
                        }
                        try {
                            val jsonArray =
                                JSONObject(response.body!!.string()).getJSONArray("result")
                            for (i in 0..<jsonArray.length()) {
                                val mvObject = jsonArray.getJSONObject(i)
                                val mvId = mvObject.getInt("id")
                                val name = mvObject.getString("name")
                                val copyWriter = mvObject.getString("copywriter")
                                val pictureUrl = mvObject.getString("picUrl")
                                val playCount = mvObject.getInt("playCount")
                                val artistArray = mvObject.getJSONArray("artists")
                                val artistList: MutableList<Artist?> =
                                    ArrayList<Artist?>()
                                for (j in 0..<artistArray.length()) {
                                    val artistId = artistArray.getJSONObject(j).getInt("id")
                                    val artistName =
                                        artistArray.getJSONObject(j).getString("name")
                                    artistList.add(
                                        Artist(
                                            artistId,
                                            artistName
                                        )
                                    )
                                }
                                val mvInfo = MvInfo(
                                    mvId,
                                    name,
                                    artistList,
                                    playCount,
                                    copyWriter,
                                    pictureUrl
                                )
                                mVideoInfoList.add(mvInfo)
                            }
                            //获取更多MV
                            getMvInfoListFromChosen(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.e("mvList", "get error")
                    }
                })
        }


    //offset:偏移
    private fun getMvInfoListFromChosen(offset: Int) {
        HttpUtil.sendOkHttpRequest(
            (SERVER + mUrl + "?limit=" + MV_NUMS_OF_PAGE + "&offset="
                    + offset * MV_NUMS_OF_PAGE), object : Callback {
                override fun onResponse(call: Call?, response: Response) {
                    if (response.body == null) {
                        return
                    }
                    try {
                        val jsonArray = JSONObject(response.body!!.string()).getJSONArray("data")
                        for (i in 0..<jsonArray.length()) {
                            val mvObject = jsonArray.getJSONObject(i)
                            val mvId = mvObject.getInt("id")
                            val name = mvObject.getString("name")
                            //String copyWriter = mvObject.getString("copywriter");
                            val copyWriter = ""
                            val pictureUrl = mvObject.getString("cover")
                            val playCount = mvObject.getInt("playCount")
                            val artistArray = mvObject.getJSONArray("artists")
                            val artistList: MutableList<Artist?> = ArrayList<Artist?>()
                            for (j in 0..<artistArray.length()) {
                                val artistId = artistArray.getJSONObject(j).getInt("id")
                                val artistName = artistArray.getJSONObject(j).getString("name")
                                artistList.add(Artist(artistId, artistName))
                            }
                            val mvInfo =
                                MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl)
                            mVideoInfoList.add(mvInfo)
                        }
                        if (getActivity() != null) {
                            getActivity()!!.runOnUiThread(object : Runnable {
                                override fun run() {
                                    Log.d(TAG, "mVideoInfoList.size() = " + mVideoInfoList.size)
                                    mRecommendMvAdapter!!.updateData(mVideoInfoList)
                                    closeProgressBar()
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Log.d("mvPath", "get error")
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    Log.d("mvList", "get error")
                }
            })
    }

    private fun popMoreWindown(mvInfo: MvInfo) {
        val popView: View
        if (mDownloadPopWindow != null) {
            popView = mDownloadPopWindow!!.getContentView()
            mDownloadPopWindow!!.setFocusable(true)
            if (mDownloadPopWindow!!.isShowing()) {
                mDownloadPopWindow!!.dismiss()
            } else {
                mDownloadPopWindow!!.showAtLocation(mView, Gravity.BOTTOM or Gravity.CENTER, 0, 0)
                darkenBackground(0.5f)
            }
        } else {
            popView = getLayoutInflater().inflate(R.layout.mv_download_menu, null)
            mDownloadPopWindow = PopupWindow(
                popView, ViewGroup.LayoutParams.MATCH_PARENT,
                600, false
            )
            mDownloadPopWindow!!.setOutsideTouchable(true) // 点击外部关闭
            mDownloadPopWindow!!.setFocusable(true)
            mDownloadPopWindow!!.setAnimationStyle(android.R.style.Animation_Dialog)
            mDownloadPopWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDownloadPopWindow!!.showAtLocation(mView, Gravity.BOTTOM or Gravity.CENTER, 0, 0)
            darkenBackground(0.5f)
            mDownloadPopWindow!!.setOnDismissListener(object : PopupWindow.OnDismissListener {
                override fun onDismiss() {
                    darkenBackground(1.0f)
                }
            })
        }
        //具体操作监听
        val storeView = popView.findViewById<ImageView>(R.id.mv_iv_store)
        val downloadView = popView.findViewById<ImageView>(R.id.mv_iv_download)
        val storeTipText = popView.findViewById<TextView>(R.id.mv_tv_store_tip)
        setMvCollectButton(storeView, storeTipText, mvInfo)
        storeView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mDownloadPopWindow!!.dismiss()
                collectMv(storeView, storeTipText, mvInfo)
                MusicBroadcastManager.sendBroadcast(MusicBroadcastManager.MUSIC_GLOBAL_DATABASE_UPDATE)
            }
        })
        downloadView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mDownloadPopWindow!!.dismiss()
                downloadMv(mvInfo)
            }
        })
    }

    private fun setMvCollectButton(storeView: ImageView, storeTipText: TextView, mvInfo: MvInfo) {
        MvPresenter.isMvCollected(mvInfo.getId().toString(), object : MvCollectCallback {
            override fun isCollected() {
                val activity: Activity? = getActivity()
                if (activity == null) {
                    return
                }
                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        storeTipText.setText(R.string.mv_discollection)
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button)
                    }
                })
            }

            override fun notCollected() {
                val activity: Activity? = getActivity()
                if (activity == null) {
                    return
                }
                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        storeTipText.setText(R.string.mv_collection)
                        storeView.setBackgroundResource(R.drawable.mv_collect_button)
                    }
                })
            }
        })
    }

    private fun collectMv(storeView: ImageView, storeTipText: TextView, mvInfo: MvInfo?) {
        MvPresenter.collectMv(mvInfo, object : MvCollectCallback {
            override fun isCollected() {
                val activity: Activity? = getActivity()
                if (activity == null) {
                    return
                }
                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        storeTipText.setText(R.string.mv_discollection)
                        storeView.setBackgroundResource(R.drawable.mv_discollect_button)
                        Toast.makeText(getContext(), "收藏成功！", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun notCollected() {
                val activity: Activity? = getActivity()
                if (activity == null) {
                    return
                }
                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        storeTipText.setText(R.string.mv_collection)
                        storeView.setBackgroundResource(R.drawable.mv_collect_button)
                        Toast.makeText(getContext(), "取消收藏！", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })
    }

    private fun downloadMv(mvInfo: MvInfo) {
        if (mDownLoadBinder != null) {
            MvPresenter.getMvInfo(mvInfo, object : MusicCallBack {
                override fun onMusicInfoCompleted() {
                    mDownLoadBinder!!.startDownLoad(mvInfo)
                }
            })
        }
    }

    //设置屏幕透明度,bgcolor:0-1
    private fun darkenBackground(bgcolor: Float) {
        if (getActivity() != null) {
            val lp = getActivity()!!.getWindow().getAttributes()
            lp.alpha = bgcolor
            getActivity()!!.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            getActivity()!!.getWindow().setAttributes(lp)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            else -> {}
        }
    }

    //进度对话框
    private fun showProgressBar() {
        if (mProgressBar == null) {
            mProgressBar = ProgressDialog(getActivity())
            mProgressBar!!.setMessage("正在加载...")
            mProgressBar!!.setCanceledOnTouchOutside(false)
        }
        mProgressBar!!.show()
    }

    //关闭进度对话框
    private fun closeProgressBar() {
        if (mProgressBar != null) {
            mProgressBar!!.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        SevenVideoPlayerManager.getInstance().releaseSevenVideoPlayer()
    }


    override fun onDestroy() {
        super.onDestroy()
        MusicApplication.Companion.getContext().unbindService(mDownloadConnection)
    }

    companion object {
        private const val TAG = "NewChildMvFragment"

        private const val SERVER = "http://114.116.128.229:3000"

        private const val MV_RECOMMEND = "/personalized/mv"

        //一页显示mv的数量
        private const val MV_NUMS_OF_PAGE = 10

        fun newInstance(arg: String?): Fragment {
            val fragment = NewChildMvFragment()
            val bundle = Bundle()
            bundle.putString("url", arg)
            fragment.setArguments(bundle)
            return fragment
        }
    }
}