package com.quibbler.sevenmusic.fragment.mv

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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.listener.IScrollViewListener
import com.quibbler.sevenmusic.presenter.MvPresenter
import com.quibbler.sevenmusic.service.MvDownloadService
import com.quibbler.sevenmusic.service.MvDownloadService.DownLoadBinder
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.PageEffectUtil
import com.quibbler.sevenmusic.view.IScrollView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Package:        com.quibbler.sevenmusic.fragment.mv
 * ClassName:      ChildMvFragment
 * Description:    mv模块下排行和更多fragment
 * Author:         lishijun
 * CreateDate:     2019/9/24 17:22
 */
class TopMvFragment : Fragment {
    private var mUrl: String? = "/top/mv"

    private val mVideoInfoList: MutableList<MvInfo?> = ArrayList<MvInfo?>()

    private var mView: View? = null

    private var mProgressBar: ProgressDialog? = null

    //弹出的下载收藏等popview
    private var mDownloadPopWindow: PopupWindow? = null

    private var mDownLoadBinder: DownLoadBinder? = null

    //交替添加mv的view，双排显示
    private var mFlag = true

    private var mIScrollView: IScrollView? = null

    private var mPages = 0 //当前加载了第几页

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
        // 加载fragment_friend布局文件
        mView = inflater.inflate(R.layout.fragment_choosen_child_mv, null)
        mIScrollView = mView!!.findViewById<IScrollView>(R.id.mv_sv_mvs)
        PageEffectUtil.setScrollViewSpringback(mIScrollView)
        //滑动到底部，加载更多
        mIScrollView!!.setIScrollViewListener(object : IScrollViewListener {
            override fun onScrollToBottom() {
                //加载更多mv
                showProgressBar()
                getMvInfoListFromChosen(++mPages)
            }
        })
        mUrl = getArguments()!!.getString("url")
        getMvInfoListFromChosen(0)
        val intent = Intent(getActivity(), MvDownloadService::class.java)
        MusicApplication.Companion.getContext().startService(intent)
        MusicApplication.Companion.getContext()
            .bindService(intent, mDownloadConnection, Context.BIND_AUTO_CREATE)
        return mView!!
    }

    //offset:偏移
    private fun getMvInfoListFromChosen(offset: Int) {
        HttpUtil.sendOkHttpRequest(
            SERVER + mUrl + "?limit=10" + "&offset=" + offset * 10,
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
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
                            //在主线程更新
                            if (getActivity() != null) {
                                getActivity()!!.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        showMvPicture(mvInfo)
                                    }
                                })
                            }
                        }
                        if (getActivity() != null) {
                            getActivity()!!.runOnUiThread(object : Runnable {
                                override fun run() {
                                    closeProgressBar()
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Log.d("mvPath", "出错")
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("mvList", "获取失败")
                }
            })
    }


    //显示第i个mv的缩略图
    private fun showMvPicture(mvInfo: MvInfo?) {
        if (mvInfo == null) {
            return
        }
        //双排显示
        var mvView: LinearLayout? = null
        if (mFlag) {
            mvView = mView!!.findViewById<LinearLayout?>(R.id.mv_sv1)
            mFlag = false
        } else {
            mvView = mView!!.findViewById<LinearLayout?>(R.id.mv_sv2)
            mFlag = true
        }
        val view = LayoutInflater.from(MusicApplication.Companion.getContext()).inflate(
            R.layout.small_mv_item,
            mvView, false
        )
        val videoView = view.findViewById<ImageView>(R.id.mv_video)
        val videoNameView = view.findViewById<TextView>(R.id.mv_tv_name)
        val videoArtistView = view.findViewById<TextView>(R.id.mv_tv_artist)
        videoNameView.setText(mvInfo.getName())
        val artistString = StringBuffer()
        val mvArtistList = mvInfo.getArtists()
        if (mvArtistList != null) {
            for (i in mvArtistList.indices) {
                if (i == mvArtistList.size - 1) {
                    artistString.append(mvArtistList.get(i)!!.getName())
                } else {
                    artistString.append(mvArtistList.get(i)!!.getName() + "/")
                }
            }
            videoArtistView.setText(artistString)
        }
        mvView.addView(view)
        videoView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo)
                }
            }
        })
        //注册MV的收藏、下载等
        dealMV(mvInfo, view)
        //设置图片圆角角度
        val roundedCorners = RoundedCorners(20)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        val options = RequestOptions.bitmapTransform(roundedCorners)
        //先用glide加载
        if (getContext() != null) {
            Glide.with(getContext()!!)
                .load(mvInfo.getPictureUrl())
                .apply(options)
                .into(videoView)
        }
    }

    //注册MV的收藏、下载等
    private fun dealMV(mvInfo: MvInfo, view: View) {
        //mv的下载或收藏监听
        val downloadButton = view.findViewById<ImageButton>(R.id.mv_ib_download)
        downloadButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val popView: View
                if (mDownloadPopWindow != null) {
                    popView = mDownloadPopWindow!!.getContentView()
                    mDownloadPopWindow!!.setFocusable(true)
                    if (mDownloadPopWindow!!.isShowing()) {
                        mDownloadPopWindow!!.dismiss()
                    } else {
                        mDownloadPopWindow!!.showAtLocation(
                            mView,
                            Gravity.BOTTOM or Gravity.CENTER,
                            0,
                            0
                        )
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
                    mDownloadPopWindow!!.showAtLocation(
                        mView,
                        Gravity.BOTTOM or Gravity.CENTER,
                        0,
                        0
                    )
                    darkenBackground(0.5f)
                    mDownloadPopWindow!!.setOnDismissListener(object :
                        PopupWindow.OnDismissListener {
                        override fun onDismiss() {
                            darkenBackground(1.0f)
                        }
                    })
                }
                //具体操作监听
                val storeView = popView.findViewById<ImageView>(R.id.mv_iv_store)
                val downloadView = popView.findViewById<ImageView>(R.id.mv_iv_download)
                storeView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        mDownloadPopWindow!!.dismiss()
                        Toast.makeText(getContext(), "收藏成功", Toast.LENGTH_SHORT).show()
                    }
                })
                downloadView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        mDownloadPopWindow!!.dismiss()
                        downloadMv(mvInfo)
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
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT)
                    .show()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        MusicApplication.Companion.getContext().unbindService(mDownloadConnection)
    }

    companion object {
        private const val SERVER = "http://114.116.128.229:3000"

        fun newInstance(arg: String?): Fragment {
            val fragment = TopMvFragment()
            val bundle = Bundle()
            bundle.putString("url", arg)
            fragment.setArguments(bundle)
            return fragment
        }
    }
}

