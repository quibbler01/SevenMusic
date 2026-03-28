package com.quibbler.sevenmusic.activity.found

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.PlaylistAdapter
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.jsonbean.found.SingerResponseBean
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.HttpUtil
import com.quibbler.sevenmusic.utils.ICallback

class SingerActivity : BaseMusicListActivity<PlaylistAdapter?>() {
    //歌手页面标签名
    private val SINGER_TAB_NAMES: Array<String?> = arrayOf<String>("歌曲", "简介")

    private var mIvSingerCover: ImageView? = null
    private var mTvSingerName: TextView? = null
    private var mBtnSingerLoved: Button? = null
    private var mBtnSingerNotLoved: Button? = null

    private var mRecyclerView: RecyclerView? = null

    private var mSingerMusicAdapter: PlaylistAdapter? = null

    //歌手页面的全局歌手对象
    private var mArtist = Artist()
    private var mArtistId: String? = null

    private var mMusicInfoList: MutableList<MusicInfo>? = ArrayList<MusicInfo>()

    //Check if the singer is bookmarked
    private var mQueryThread: Thread? = null

    //更新歌手收藏状态
    private var mUpdateThread: Thread? = null
    private var mIsLoved = false

    //开启下载模式
    private var mTvSelectMode: TextView? = null
    private var mIsInSelectMode = false
    private var mLlBottom: LinearLayout? = null
    private var mTvSelectAll: TextView? = null
    private var mTvUnselectAll: TextView? = null
    private var mTvStartDownload: TextView? = null

    //播放全部
    private var mTvPlayAll: TextView? = null
    private var mIbtnPlayAll: ImageButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singer)

        mArtistId = getIntent().getStringExtra("id")
        mArtist.id = mArtistId!!.toInt())

        init()
    }

    override fun onSelectModeChange(mode: Boolean) {
        if (mode) {
            //显示取消按钮，显示下方全选、确定按钮，通知adapter
            mTvSelectMode!!.setText("取消")
            mLlBottom!!.setVisibility(View.VISIBLE)
            mSingerMusicAdapter!!.changeSelectMode(true)
        } else {
            //显示下载按钮，隐藏下方全选、确定按钮，通知adapter
            mTvSelectMode!!.setText("下载")
            mLlBottom!!.setVisibility(View.GONE)
            mSingerMusicAdapter!!.changeSelectMode(false)
        }
    }

    private fun init() {
        initView()
        initActionBar()
        initBtnDownloadAll()
        initRecyclerView()
        requestSingerData()
    }


    private fun initView() {
        mIvSingerCover = findViewById<ImageView?>(R.id.singer_activity_iv_cover)
        mTvSingerName = findViewById<TextView>(R.id.singer_activity_tv_name)
        mBtnSingerLoved = findViewById<Button>(R.id.singer_activity_btn_loved)
        mBtnSingerNotLoved = findViewById<Button>(R.id.singer_activity_btn_not_loved)

        mRecyclerView = findViewById<RecyclerView>(R.id.singer_activity_recyclerview)

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
                mSingerMusicAdapter!!.selectAll()
            }
        })

        mTvUnselectAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mSingerMusicAdapter!!.unselectAll()
            }
        })

        mTvStartDownload!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mSingerMusicAdapter!!.startDownload()
                onSelectModeChange(false)
            }
        })

        mTvPlayAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mMusicInfoList != null && mMusicInfoList!!.size > 0) {
                    MusicPlayerService.Companion.addToPlayerList(mMusicInfoList)
                    MusicPlayerService.Companion.playMusic(mMusicInfoList!!.get(0))
                    mSingerMusicAdapter!!.setPlayingPosition(0)
                    mSingerMusicAdapter!!.notifyDataSetChanged()
                }
            }
        })
        mIbtnPlayAll!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mMusicInfoList != null && mMusicInfoList!!.size > 0) {
                    MusicPlayerService.Companion.addToPlayerList(mMusicInfoList)
                    MusicPlayerService.Companion.playMusic(mMusicInfoList!!.get(0))
                    mSingerMusicAdapter!!.setPlayingPosition(0)
                    mSingerMusicAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    /**
     * 初始化页面的actionBar
     */
    private fun initActionBar() {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setTitle("歌手 ")
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(linearLayoutManager)
        mSingerMusicAdapter = PlaylistAdapter(mMusicInfoList, this)

        super.mAdapter = mSingerMusicAdapter
        mRecyclerView!!.setAdapter(mSingerMusicAdapter)
        mRecyclerView!!.setNestedScrollingEnabled(false)
    }

    private fun requestSingerData() {
        val url: String = SINGER_URL_AUTHORITY + mArtistId
        HttpUtil.sendOkHttpRequest(url, this, object : ICallback {
            override fun onResponse(responseText: String?) {
                if (responseText == null) {
                    return
                }
                val gson = Gson()
                val singerResponseBean =
                    gson.fromJson<SingerResponseBean?>(responseText, SingerResponseBean::class.java)
                if (singerResponseBean == null) {
                    return
                }
                mArtist = singerResponseBean.artist
                mMusicInfoList = singerResponseBean.hotSongs
                for (musicInfo in mMusicInfoList!!) {
                    musicInfo.setSinger(musicInfo.firstArName)
                }
                mSingerMusicAdapter!!.updateData(mMusicInfoList)

                showViewContent()
            }

            override fun onFailure() {
            }
        })
    }

    /**
     * 显示页面中各个view的具体内容
     */
    private fun showViewContent() {
        //显示歌手图片
        ImageDownloadPresenter.Companion.instance.with(MusicApplication.Companion.context)
            .load(mArtist.picUrl)
            .imageStyle(ImageDownloadPresenter.Companion.STYLE_ORIGIN)
            .into(mIvSingerCover, object : ImageDownloadPresenter.ResourceCallback<Bitmap?> {
                override fun onResourceReady(resource: Bitmap) {
                    val rgb = resource.getPixel(
                        resource.getWidth() / 6,
                        resource.getHeight() * 9 / 10 - 1
                    )
                    val r = (rgb and 16711680) shr 16
                    val g = (rgb and 65280) shr 8
                    val b = (rgb and 255)
                    Log.d(TAG, r.toString() + "," + g + "," + b + ",")
                    if (r > 190 && g > 190 && b > 190) {
                        //封面左下角为浅色，歌手名调整为黑色
                        mTvSingerName!!.setTextColor(
                            getResources().getColor(
                                R.color.my_download_bar_text_black,
                                null
                            )
                        )
                        mTvSingerName!!.setVisibility(View.VISIBLE)
                    } else {
                        //封面左下角为深色，歌手名调整为浅色
                        mTvSingerName!!.setTextColor(
                            getResources().getColor(
                                R.color.colorWhite,
                                null
                            )
                        )
                        mTvSingerName!!.setVisibility(View.VISIBLE)
                    }
                }
            })

        //显示歌手名称
        mTvSingerName!!.setText(mArtist.name)
        //显示关注按钮
        mQueryThread = Thread(object : Runnable {
            override fun run() {
                mIsLoved = queryIdInThread(this@SingerActivity, mArtistId)
                if (mIsLoved) {
                    mBtnSingerLoved!!.setVisibility(View.VISIBLE)
                } else {
                    mBtnSingerNotLoved!!.setVisibility(View.VISIBLE)
                }
            }
        })
        mQueryThread!!.start()

        mBtnSingerLoved!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //已关注，点击后取消关注
                mBtnSingerLoved!!.setVisibility(View.GONE)
                mBtnSingerNotLoved!!.setVisibility(View.VISIBLE)

                if (mUpdateThread != null) {
                    mUpdateThread!!.interrupt()
                }
                mUpdateThread = Thread(object : Runnable {
                    override fun run() {
                        updateSingerLoveInThread(this@SingerActivity, mArtist, false)
                    }
                })
                mUpdateThread!!.start()
            }
        })
        mBtnSingerNotLoved!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //未关注，点击后关注
                mBtnSingerLoved!!.setVisibility(View.VISIBLE)
                mBtnSingerNotLoved!!.setVisibility(View.GONE)
                if (mUpdateThread != null) {
                    mUpdateThread!!.interrupt()
                }
                mUpdateThread = Thread(object : Runnable {
                    override fun run() {
                        updateSingerLoveInThread(this@SingerActivity, mArtist, true)
                    }
                })
                mUpdateThread!!.start()
            }
        })
    }

    private fun updateSingerLoveInThread(context: Context, artist: Artist, loved: Boolean) {
        val authorityUri: Uri = MusicContentProvider.Companion.COLLECTION_URL
        if (loved) {
            //增加该数据
            val values = ContentValues()
            values.put("id", artist.id)
            values.put("title", artist.name)
            values.put("kind", SINGER_KIND)
            values.put("description", artist.briefDesc)
            context.getContentResolver().insert(authorityUri, values)
        } else {
            //删除该数据
            val collectionUrl = Uri.parse(authorityUri.toString() + "/" + artist.id)
            context.getContentResolver().delete(collectionUrl, null, null)
        }
    }

    /**
     * 查询该歌手是否被收藏
     * 
     * @param context
     * @param id
     * @return
     */
    private fun queryIdInThread(context: Context, id: String?): Boolean {
        var result = false
        val uri: Uri = MusicContentProvider.Companion.COLLECTION_URL
        val cursor =
            context.getContentResolver().query(uri, null, "id = ?", arrayOf<String?>(id), null)
        if (cursor != null && cursor.moveToFirst()) {
            //本地收藏数据库有记录
            result = true
        }
        if (cursor != null) {
            cursor.close()
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mUpdateThread != null) {
            mUpdateThread!!.interrupt()
        }
        if (mQueryThread != null) {
            mQueryThread!!.interrupt()
        }
    }

    companion object {
        private const val SINGER_URL_AUTHORITY = "http://114.116.128.229:3000/artists?id="
        private const val TAG = "SingerActivityTest"

        //kind 0歌曲 1歌手 2专辑
        private const val SINGER_KIND = 1
    }
}
