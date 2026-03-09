package com.quibbler.sevenmusic.fragment.mv

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.broadcast.MusicBroadcastManager
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.callback.MvCollectCallback
import com.quibbler.sevenmusic.presenter.MvPresenter
import com.quibbler.sevenmusic.service.MvDownloadService
import com.quibbler.sevenmusic.service.MvDownloadService.DownLoadBinder
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.PageEffectUtil
import com.quibbler.sevenmusic.utils.TextureVideoViewOutlineProvider
import com.quibbler.sevenmusic.view.mv.IMediaController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Package:        com.quibbler.sevenmusic.fragment.mv
 * ClassName:      ChildMvFragment
 * Description:    mv模块下推荐fragment
 * Author:         lishijun
 * CreateDate:     2019/9/24 17:22
 */
class ChildMvFragment : Fragment() {
    private val mVideoInfoList: MutableList<MvInfo> = ArrayList<MvInfo>()

    private var mView: View? = null

    private val mVideoViewList: MutableList<VideoView> = ArrayList<VideoView>()

    private val mMediaControllerList: MutableList<IMediaController> = ArrayList<IMediaController>()

    private val mVideoProgressList: MutableList<Int?> = ArrayList<Int?>()

    //弹出的下载收藏等popview
    private var mDownloadPopWindow: PopupWindow? = null

    private var mDownLoadBinder: DownLoadBinder? = null

    var mScrollView: ScrollView? = null
    private var mediaController: IMediaController? = null


    private val mDownloadConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mDownLoadBinder = service as DownLoadBinder?
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 加载fragment_friend布局文件
        //LinearLayout layout = R.layout.fragment_child_mv;
        mView = inflater.inflate(R.layout.fragment_child_mv, null)
        mScrollView = mView!!.findViewById<ScrollView>(R.id.mv_sv_mvs)
        mScrollView!!.setOnScrollChangeListener(object : View.OnScrollChangeListener {
            override fun onScrollChange(
                v: View?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                //滑动时隐藏播放条
                hideAllMediaController()
            }
        })
        PageEffectUtil.setScrollViewSpringback(mScrollView)
        this.mvInfoListFromRecommend
        val intent = Intent(MusicApplication.Companion.getContext(), MvDownloadService::class.java)
        MusicApplication.Companion.getContext().startService(intent)
        MusicApplication.Companion.getContext()
            .bindService(intent, mDownloadConnection, Context.BIND_AUTO_CREATE)
        return mView!!
    }

    fun hideAllMediaController() {
        for (mediaController in mMediaControllerList) {
            mediaController.hide()
        }
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
                            Log.d(
                                TAG,
                                "json length is : " + jsonArray.length()
                            )
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
                                Log.d(TAG, "json" + i)
                            }
                            //在主线程更新
                            if (getActivity() != null) {
                                getActivity()!!.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        showMvPicture()
                                    }
                                })
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "exception is" + e)
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.d("mvList", "获取失败")
                    }
                })
        }

    private fun showMvPicture() {
        for (mvInfo in mVideoInfoList) {
            showMvPicture(mvInfo)
        }
    }

    //显示第i个mv的缩略图
    private fun showMvPicture(mvInfo: MvInfo?) {
        Log.d(TAG, "mv")
        if (mvInfo == null) {
            return
        }
        val mvView = mView!!.findViewById<LinearLayout>(R.id.mv_sv)
        val view = LayoutInflater.from(MusicApplication.Companion.getContext()).inflate(
            R.layout.mv_item,
            mvView, false
        )
        val videoView = view.findViewById<VideoView>(R.id.mv_video)
        val videoNameView = view.findViewById<TextView>(R.id.mv_tv_name)
        val videoArtistView = view.findViewById<TextView>(R.id.mv_tv_artist)
        val artistHeadView = view.findViewById<ImageView>(R.id.mv_iv_artist_head)
        mVideoViewList.add(videoView)
        MvPresenter.getMvInfo(mvInfo, object : MusicCallBack {
            override fun onMusicInfoCompleted() {
                val activity: Activity? = getActivity()
                if (activity != null) {
                    activity.runOnUiThread(object : Runnable {
                        override fun run() {
                            videoView.setVideoPath(mvInfo.getUrl())
                            mScrollView!!.scrollTo(0, 0)
                        }
                    })
                }
            }
        })
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

        if (getActivity() != null) { // 增加判空处理，防止夜间模式切换导致空指针异常
            mediaController = IMediaController(getActivity())
        } else {
            return
        }
        videoView.setMediaController(mediaController)
        mMediaControllerList.add(mediaController!!)
        videoView.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.setOnInfoListener(object : MediaPlayer.OnInfoListener {
                    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            // 暂停其他的video
                            for (i in mVideoViewList.indices) {
                                if (mVideoViewList.get(i) !== videoView && videoView.isPlaying()) {
                                    mVideoViewList.get(i).pause()
                                    mMediaControllerList.get(i).hide()
                                }
                            }
                            videoView.setBackgroundColor(Color.TRANSPARENT)
                        }
                        return true
                    }
                })
            }
        })

        videoNameView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //跳转进入播放activity
                if (getActivity() != null) {
                    ActivityStart.startMvPlayActivity(getActivity(), mvInfo)
                }
            }
        })
        //注册MV的收藏、下载等
        dealMV(mvInfo, view)
        //设置圆角
        videoView.setOutlineProvider(TextureVideoViewOutlineProvider(20f))
        videoView.setClipToOutline(true)
        //设置图片圆角角度
        val roundedCorners = RoundedCorners(20)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        val options = RequestOptions.bitmapTransform(roundedCorners)
        //先用glide加载
        Glide.with(this)
            .load(mvInfo.getPictureUrl())
            .apply(options)
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable?,
                    transition: Transition<in Drawable?>?
                ) {
                    videoView.setBackground(resource)
                }
            })
        //先用glide加载
        Glide.with(this)
            .load(mvInfo.getPictureUrl())
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(artistHeadView)
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

    override fun onResume() {
        //恢复各video的进度
        for (i in mVideoProgressList.indices) {
            val progress = mVideoProgressList.get(i)!!
            val videoView = mVideoViewList.get(i)
            if (progress > 0 && !videoView.isPlaying()) {
                val mvInfo = mVideoInfoList.get(i)
                Glide.with(this)
                    .load(mvInfo.getPictureUrl())
                    .into(object : SimpleTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable?,
                            transition: Transition<in Drawable?>?
                        ) {
                            videoView.setBackground(resource)
                        }
                    })
                //本次不显示播放条
                mMediaControllerList.get(i).setToHideOnce(true)
                videoView.seekTo(mVideoProgressList.get(i)!!)
            }
        }
        super.onResume()
    }

    override fun onPause() {
        //保存各video的进度
        mVideoProgressList.clear()
        for (i in mVideoViewList.indices) {
            val videoView = mVideoViewList.get(i)
            val progress = videoView.getCurrentPosition()
            mVideoProgressList.add(progress)
        }
        super.onPause()
    }

    override fun onDestroy() {
        MusicApplication.Companion.getContext().unbindService(mDownloadConnection)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ChildMvFragment"

        private const val SERVER = "http://114.116.128.229:3000"

        private const val MV_ARTIST = "/artist/mv?id=6452"

        private const val MV_RECOMMEND = "/personalized/mv"

        private const val MV_DETAIL_URL = "/mv/detail?mvid="
    }
}
