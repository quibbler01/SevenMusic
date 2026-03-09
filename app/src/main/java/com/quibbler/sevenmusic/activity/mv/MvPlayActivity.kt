package com.quibbler.sevenmusic.activity.mv

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvComment
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.presenter.MusicPresnter
import com.quibbler.sevenmusic.presenter.MvPresenter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.DateUtil
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.ICallback
import com.quibbler.sevenmusic.utils.NetUtil
import com.quibbler.sevenmusic.utils.PageEffectUtil
import com.quibbler.sevenmusic.view.mv.IMediaController
import com.quibbler.sevenmusic.view.mv.MediaControlListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException

/**
 * 
 * Package:        com.quibbler.sevenmusic.activity
 * ClassName:      MvPlayActivity
 * Description:    点击mv跳转到此页播放
 * Author:         lishijun
 * CreateDate:     2019/9/20 11:36
 */
class MvPlayActivity : Activity() {
    private var mVideoLayout: LinearLayout? = null

    private var mVideoView: VideoView? = null

    private var mMediaController: IMediaController? = null

    private var mMvInfo: MvInfo? = null

    private val mSimilarVideoInfoList: MutableList<MvInfo?> = ArrayList<MvInfo?>()

    private val mSimilarSongList: MutableList<MvMusicInfo?> = ArrayList<MvMusicInfo?>()

    private val mMvCommentList: MutableList<MvComment?> = ArrayList<MvComment?>()

    //用于进入后台时videoview的状态保存
    private var mSeekPosition = 0

    private var mIsFullScreen = false

    private var mVideoHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentToolbar()
        setContentView(R.layout.activity_mv_play)
        if (NetUtil.getNetWorkStart(this) == NetUtil.NETWORK_NONE) {
            showNoNetPage()
        } else {
            hideNoNetPage()
            initView()
        }
    }

    private fun initView() {
        val intent = getIntent()
        mMvInfo = intent.getSerializableExtra("mvInfo") as MvInfo?
        mVideoLayout = findViewById<LinearLayout>(R.id.mv_video_layout)
        mVideoView = findViewById<VideoView?>(R.id.mv_vv_video)
        mMediaController = IMediaController(this@MvPlayActivity)
        mVideoView!!.setMediaController(mMediaController)
        mMediaController!!.setControlListener(object : MediaControlListener {
            override fun actionForFullScreen() {
                if (!mIsFullScreen) {
                    changeToFullScreen()
                } else {
                    exitFullScreen()
                }
            }
        })
        mVideoView!!.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                mp.setOnInfoListener(object : MediaPlayer.OnInfoListener {
                    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            mVideoView!!.setBackgroundColor(Color.TRANSPARENT)
                        }
                        return true
                    }
                })
            }
        })
        val vto = mVideoView!!.getViewTreeObserver()
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mVideoView!!.getViewTreeObserver().removeGlobalOnLayoutListener(this)
                mVideoHeight = mVideoView!!.getHeight()
                mVideoView!!.setLayoutParams(
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        mVideoHeight
                    )
                )
            }
        })
        val scrollView = findViewById<ScrollView?>(R.id.mv_sv_relative)
        PageEffectUtil.setScrollViewSpringback(scrollView)
        showMvPicture(mMvInfo!!)
        MvPresenter.getMvInfo(mMvInfo, object : MusicCallBack {
            override fun onMusicInfoCompleted() {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (!TextUtils.isEmpty(mMvInfo!!.getUrl())) {
                            mVideoView!!.setVideoPath(mMvInfo!!.getUrl())
                        }
                        val nameView = findViewById<TextView>(R.id.mv_tv_palyname)
                        val palyCountView = findViewById<TextView>(R.id.mv_tv_count)
                        val descriptionView = findViewById<TextView>(R.id.mv_tv_description)
                        nameView.setText(mMvInfo!!.getName())
                        palyCountView.setText(mMvInfo!!.getPlayCount().toString() + "次播放")
                        descriptionView.setText(mMvInfo!!.getCopyWriter())
                        //折叠按钮的监听事件
                        val collapseButton = findViewById<ImageButton>(R.id.mv_btn_collapse)
                        collapseButton.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(view: View?) {
                                if (descriptionView.getVisibility() == View.GONE) {
                                    descriptionView.setVisibility(View.VISIBLE)
                                    collapseButton.setBackgroundResource(R.drawable.mv_btn_collapse_ing)
                                } else {
                                    descriptionView.setVisibility(View.GONE)
                                    collapseButton.setBackgroundResource(R.drawable.mv_btn_collapse)
                                }
                            }
                        })
                        //获取相似歌曲
                        if (mMvInfo!!.getArtists().size > 0) {
                            getSimilarSong(mMvInfo!!.getArtists().get(0).getId())
                        }
                    }
                })
            }
        })
        //获取相似mv
        getSimilarMv(mMvInfo!!.getId())
        getMvComment(mMvInfo!!.getId())
    }

    private fun setTransparentToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = getWindow().getDecorView()
            //设置让应用主题内容占据状态栏和导航栏
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.setSystemUiVisibility(option)
            //设置状态栏和导航栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT)
            getWindow().setNavigationBarColor(Color.TRANSPARENT)
        }
    }

    private fun changeToFullScreen() {
        mIsFullScreen = true
        mVideoLayout!!.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        mVideoView!!.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) //设置activity横屏
        getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE) //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun exitFullScreen() {
        mIsFullScreen = false
        mVideoLayout!!.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mVideoHeight
            )
        )
        mVideoView!!.setLayoutParams(
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mVideoHeight
            )
        )
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) //设置activity竖屏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE) //显示状态栏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setTransparentToolbar()
    }

    //无网络，展示提示页
    private fun showNoNetPage() {
        findViewById<View?>(R.id.mv_nonet_tip).setVisibility(View.VISIBLE)
    }

    //有网络，关闭提示页
    private fun hideNoNetPage() {
        findViewById<View?>(R.id.mv_nonet_tip).setVisibility(View.GONE)
    }

    //显示mv的缩略图
    private fun showMvPicture(mvInfo: MvInfo) {
        Glide.with(this)
            .load(mvInfo.getPictureUrl())
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable?,
                    transition: Transition<in Drawable?>?
                ) {
                    mVideoView!!.setBackground(resource)
                }
            })
    }

    private fun getSimilarMv(mvId: Int) {
        HttpUtil.sendHttpRequest(SERVER + SIMILAR_URL + mvId, object : ICallback {
            override fun onResponse(response: String) {
                try {
                    val jsonArray = JSONObject(response).getJSONArray("mvs")
                    for (i in 0..<jsonArray.length()) {
                        val mvObject = jsonArray.getJSONObject(i)
                        val mvId = mvObject.getInt("id")
                        val name = mvObject.getString("name")
                        val copyWriter = mvObject.getString("briefDesc")
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
                        mSimilarVideoInfoList.add(mvInfo)
                        //在主线程更新
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                showSimilarMv(mvInfo)
                            }
                        })
                    }
                    //按顺序获取video的url
                    if (mSimilarVideoInfoList.size > 0) {
                        MvPresenter.getMvUrlList(mSimilarVideoInfoList)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "mvPath:get error")
                    e.printStackTrace()
                }
            }

            override fun onFailure() {
                Log.d(TAG, "mvList:get error")
            }
        })
    }

    //获取相似的MV
    //    private void getSimilarMv(int mvId){
    //        HttpUtil.sendOkHttpRequest(SERVER + SIMILAR_URL + mvId, new Callback() {
    //            @Override
    //            public void onResponse(Call call, Response response) {
    //                if(response.body() == null){
    //                    return;
    //                }
    //                try {
    //                    JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("mvs");
    //                    for(int i = 0; i < jsonArray.length(); i++){
    //                        JSONObject mvObject = jsonArray.getJSONObject(i);
    //                        int mvId = mvObject.getInt("id");
    //                        String name = mvObject.getString("name");
    //                        String copyWriter = mvObject.getString("briefDesc");
    //                        String pictureUrl = mvObject.getString("cover");
    //                        int playCount = mvObject.getInt("playCount");
    //                        JSONArray artistArray = mvObject.getJSONArray("artists");
    //                        List<Artist> artistList = new ArrayList<>();
    //                        for(int j = 0; j < artistArray.length(); j++){
    //                            int artistId = artistArray.getJSONObject(j).getInt("id");
    //                            String artistName = artistArray.getJSONObject(j).getString("name");
    //                            artistList.add(new Artist(artistId, artistName));
    //                        }
    //                        MvInfo mvInfo = new MvInfo(mvId, name, artistList, playCount, copyWriter, pictureUrl);
    //                        mSimilarVideoInfoList.add(mvInfo);
    //                        //在主线程更新
    //                        runOnUiThread(new Runnable() {
    //                            @Override
    //                            public void run() {
    //                                showSimilarMv(mvInfo);
    //                            }
    //                        });
    //                    }
    //                    //按顺序获取video的url
    //                    if(mSimilarVideoInfoList.size() > 0){
    //                        MvPresenter.getMvUrlList(mSimilarVideoInfoList);
    //                    }
    //                } catch (Exception e) {
    //                    Log.d("mvPath", "出错");
    //                    e.printStackTrace();
    //                }
    //            }
    //
    //            @Override
    //            public void onFailure(Call call, IOException e) {
    //                Log.d("mvList", "获取失败");
    //            }
    //        });
    //    }
    //显示相似的mv
    private fun showSimilarMv(mvInfo: MvInfo) {
        val fatherView = findViewById<LinearLayout>(R.id.mv_similar_video)
        val view = LayoutInflater.from(this).inflate(
            R.layout.similar_mv_item,
            null, false
        )
        val videoView = view.findViewById<ImageView>(R.id.mv_iv_picture)
        val videoDescView = view.findViewById<TextView>(R.id.mv_tv_description)
        if (!TextUtils.equals("", mvInfo.getCopyWriter()) && !TextUtils.equals(
                "null",
                mvInfo.getCopyWriter()
            )
        ) {
            videoDescView.setText(mvInfo.getName() + "，" + mvInfo.getCopyWriter())
        } else {
            videoDescView.setText(mvInfo.getName())
        }
        fatherView.addView(view)
        videoView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //跳转进入播放activity
                if (mvInfo.getUrl() != null) {
                    ActivityStart.startMvPlayActivity(this@MvPlayActivity, mvInfo)
                }
            }
        })
        //设置图片圆角角度
        val roundedCorners = RoundedCorners(20)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        val options = RequestOptions.bitmapTransform(roundedCorners)
        //先用glide加载
        if (!this.isFinishing()) {
            Glide.with(this)
                .load(mvInfo.getPictureUrl())
                .apply(options)
                .into(videoView)
        }
    }

    //根据歌手id获取相似歌曲
    private fun getSimilarSong(id: Int) {
        HttpUtil.sendOkHttpRequest(SERVER + ARTIST_SONG_URL + id, object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                if (response.body == null) {
                    return
                }
                try {
                    val jsonArray = JSONObject(response.body!!.string()).getJSONArray("hotSongs")
                    //如果没有，直接返回
                    if (jsonArray.length() == 0) {
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                removeSimilarText()
                            }
                        })
                        return
                    }
                    for (i in 0..<jsonArray.length()) {
                        val songObject = jsonArray.getJSONObject(i)
                        val songId = songObject.getInt("id")
                        val name = songObject.getString("name")
                        val artistArray = songObject.getJSONArray("ar")
                        val artistList: MutableList<Artist?> = ArrayList<Artist?>()
                        for (j in 0..<artistArray.length()) {
                            val artistId = artistArray.getJSONObject(j).getInt("id")
                            val artistName = artistArray.getJSONObject(j).getString("name")
                            artistList.add(Artist(artistId, artistName))
                        }
                        val pictureUrl = songObject.getJSONObject("al").getString("picUrl")
                        val mvMusicInfo = MvMusicInfo(songId, name, pictureUrl, artistList)
                        mSimilarSongList.add(mvMusicInfo)
                        //显示
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                showSimilarSong(mvMusicInfo)
                            }
                        })
                        if (mSimilarSongList.size >= SIMILAR_SONG_NUMBER) {
                            break
                        }
                    }
                    //获取歌曲的可用性
                    MusicPresnter.getMusicCanUse(mSimilarSongList)
                } catch (e: Exception) {
                    Log.d("mvPath", "出错")
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("songList", "获取失败")
            }
        })
    }

    //移除相关歌曲这个view
    private fun removeSimilarText() {
        val fatherView = findViewById<LinearLayout>(R.id.mv_similar_song)
        fatherView.removeAllViews()
    }

    //显示某个相关歌曲
    private fun showSimilarSong(mvMusicInfo: MvMusicInfo) {
        val fatherView = findViewById<LinearLayout>(R.id.mv_similar_song)
        val view = LayoutInflater.from(this).inflate(
            R.layout.similar_song_item,
            null, false
        )
        val albumView = view.findViewById<ImageView>(R.id.mv_iv_albumpic)
        val songNameView = view.findViewById<TextView>(R.id.mv_tv_song_name)
        val artistNameView = view.findViewById<TextView>(R.id.mv_tv_artist_name)
        //设置图片圆角角度
        val roundedCorners = RoundedCorners(20)
        val options = RequestOptions.bitmapTransform(roundedCorners)
        //先用glide加载
        if (!this.isFinishing()) {
            Glide.with(this)
                .load(mvMusicInfo.getPictureUrl())
                .apply(options)
                .into(albumView)
        }
        songNameView.setText(mvMusicInfo.getName())
        val artistString = StringBuffer()
        val mvArtistList = mvMusicInfo.getArtistList()
        if (mvArtistList != null) {
            for (j in mvArtistList.indices) {
                if (j == mvArtistList.size - 1) {
                    artistString.append(mvArtistList.get(j)!!.getName())
                } else {
                    artistString.append(mvArtistList.get(j)!!.getName() + "/")
                }
            }
            artistNameView.setText(artistString.toString() + "-" + mvMusicInfo.getName())
        }
        fatherView.addView(view)
        //点击播放响应
        val playButton = view.findViewById<ImageButton>(R.id.mv_iv_music_play)
        val clickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mvMusicInfo.isCanUse()) {
                    if (MusicPlayerService.Companion.isPlaying && MusicPlayerService.Companion.getMusicInfo() != null && TextUtils.equals(
                            mvMusicInfo.getId().toString() + "",
                            MusicPlayerService.Companion.getMusicInfo().getId()
                        )
                    ) {
                        ActivityStart.startMusicPlayActivity(this@MvPlayActivity, mvMusicInfo)
                    } else {
                        val musicInfo = MusicInfo()
                        musicInfo.setId(mvMusicInfo.getId().toString())
                        musicInfo.setMusicSongName(mvMusicInfo.getName())
                        if (mvMusicInfo.getArtistList().size > 0) {
                            musicInfo.setSinger(mvMusicInfo.getArtistList().get(0).getName())
                        }
                        MusicPlayerService.Companion.playMusic(musicInfo)
                    }
                } else {
                    Toast.makeText(this@MvPlayActivity, "暂无版权！", Toast.LENGTH_SHORT).show()
                }
            }
        }
        playButton.setOnClickListener(clickListener)
        songNameView.setOnClickListener(clickListener)
        artistNameView.setOnClickListener(clickListener)
    }

    private fun getMvComment(mvId: Int) {
        HttpUtil.sendOkHttpRequest(SERVER + MV_COMMENT_URL + mvId, object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                if (response.body == null) {
                    return
                }
                try {
                    val jsonArray = JSONObject(response.body!!.string()).getJSONArray("hotComments")
                    for (i in 0..<jsonArray.length()) {
                        val commentObject = jsonArray.getJSONObject(i)
                        val mvId = commentObject.getInt("commentId")
                        val username = commentObject.getJSONObject("user").getString("nickname")
                        val userheadUrl = commentObject.getJSONObject("user").getString("avatarUrl")
                        val content = commentObject.getString("content")
                        val date = commentObject.getLong("time")
                        val likeCount = commentObject.getInt("likedCount")
                        val mvComment =
                            MvComment(mvId, content, username, userheadUrl, date, likeCount)
                        mMvCommentList.add(mvComment)
                        //在主线程更新
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                showMvComment(mvComment)
                            }
                        })
                    }
                } catch (e: Exception) {
                    Log.d("mvPath", "出错")
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("mvList", "获取失败")
            }
        })
    }

    //显示某个评论
    private fun showMvComment(mvComment: MvComment) {
        val fatherView = findViewById<LinearLayout>(R.id.mv_comment)
        val view = LayoutInflater.from(this).inflate(
            R.layout.mv_comment_item,
            null, false
        )
        val userheadView = view.findViewById<ImageView>(R.id.mv_comment_user_picture)
        val userameView = view.findViewById<TextView>(R.id.mv_comment_username)
        val timeView = view.findViewById<TextView>(R.id.mv_comment_time)
        val contentView = view.findViewById<TextView>(R.id.mv_comment_body)
        val likeCountView = view.findViewById<TextView>(R.id.mv_comment_likecount)
        //先用glide加载
        if (this != null) {
            Glide.with(this)
                .load(mvComment.getUerHeadUrl())
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(userheadView)
        }
        userameView.setText(mvComment.getUserName())
        try {
            val time = DateUtil.longToString(mvComment.getDate(), "yyyy-MM-dd HH:mm:ss")
            timeView.setText(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        contentView.setText(mvComment.getContent())
        likeCountView.setText(mvComment.getLikeCount().toString() + "")
        fatherView.addView(view)
    }

    override fun onPause() {
        super.onPause()
        if (mVideoView != null) {
            mSeekPosition = mVideoView!!.getCurrentPosition()
        }
    }

    override fun onResume() {
        super.onResume()
        recoveryMvState()
    }

    private fun recoveryMvState() {
        var metadataRetriever: MediaMetadataRetriever? = null
        try {
            if (mVideoView != null && mSeekPosition > 0 && !mVideoView!!.isPlaying()) {
                if (mMvInfo!!.getUrl() != null) {
                    metadataRetriever = MediaMetadataRetriever()
                    //mPath视频地址
                    metadataRetriever.setDataSource(mMvInfo!!.getUrl(), HashMap<String?, String?>())
                    //获取当前视频某一时刻(毫秒*1000)的一帧
                    val bitmap = metadataRetriever.getFrameAtTime(
                        (mSeekPosition * 1000).toLong(),
                        MediaMetadataRetriever.OPTION_CLOSEST
                    )
                    mVideoView!!.setBackground(BitmapDrawable(bitmap))
                }
                mVideoView!!.seekTo(mSeekPosition)
            }
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "recoveryMvState:" + e)
        } finally {
            if (metadataRetriever != null) {
                try {
                    metadataRetriever.release()
                } catch (ignore: Exception) {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MvPlayActivity"

        private const val SERVER = "http://114.116.128.229:3000"

        private const val SIMILAR_URL = "/simi/mv?mvid="

        private const val ARTIST_SONG_URL = "/artists?id="

        private const val MV_COMMENT_URL = "/comment/mv?id="

        private const val MUSIC_CANUSE_URL = "/check/music?id="

        private const val MV_URL = "/mv/url?id="

        private const val SIMILAR_SONG_NUMBER = 3

        private const val MV_DETAIL_URL = "/mv/detail?mvid="
    }
}
