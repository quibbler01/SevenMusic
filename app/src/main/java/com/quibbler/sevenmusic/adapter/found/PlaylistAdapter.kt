package com.quibbler.sevenmusic.adapter.found

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.Artist
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider
import com.quibbler.sevenmusic.service.MusicDownloaderService
import com.quibbler.sevenmusic.service.MusicPlayerService
import com.quibbler.sevenmusic.utils.BeanConverter
import com.quibbler.sevenmusic.utils.MusicThreadPool
import com.quibbler.sevenmusic.view.found.FoundCustomDialog

class PlaylistAdapter(musicInfoList: MutableList<MusicInfo>, context: Context) :
    RecyclerBaseAdapter<MusicInfo?, PlaylistAdapter.PlaylistViewHolder?>(musicInfoList) {
    //显示歌手个数
    //    private static final Integer SINGER_NUM = 3;
    //当前正在播放的歌曲的位置，默认为-1
    private var mPlayingPosition = -1

    //是否处于初始化
    private var mIsInInit = true
    private var mQueryThread: Thread?

    //歌单的歌曲数据源list
    private val mMusicInfoList: MutableList<MusicInfo> = mSourceList
    private val mContext: Context?

    //选择将要下载的歌曲id的list
    private val mSelectedPositionList: MutableList<Int> = ArrayList<Int>()

    //是否在多选模式
    private var mIsInSelectMode = false

    //弹出的下载收藏等popView
    private var mDownloadPopWindow: PopupWindow? = null

    init {
        mContext = context

        if (mQueryThread != null) {
            mQueryThread!!.interrupt()
        }
        mQueryThread = Thread(object : Runnable {
            override fun run() {
                for (musicInfo in musicInfoList) {
                    musicInfo.setCollected(queryIdInThread(context, musicInfo.getId()))
                }
            }
        })
        mQueryThread!!.start()
    }


    internal class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mView: View
        private val mTvName: TextView
        private val mTvSinger: TextView
        private val mTvNum: TextView
        private val mBtnPlayOrPause: ImageButton
        private val mBtnPop: ImageButton
        private val mCbSelect: CheckBox

        init {
            mView = view
            mTvName = view.findViewById<TextView>(R.id.playlist_list_item_tv_name)
            mTvSinger = view.findViewById<TextView>(R.id.playlist_list_item_tv_singer_or_album)
            mTvNum = view.findViewById<TextView>(R.id.playlist_list_item_tv_num)
            mBtnPlayOrPause = view.findViewById<ImageButton>(R.id.playlist_list_item_btn_play_pause)
            mBtnPop = view.findViewById<ImageButton>(R.id.playlist_list_item_btn_pop)
            mCbSelect = view.findViewById<CheckBox>(R.id.playlist_list_item_cb_download)
        }
    }

    /**
     * 选择全部歌曲的checkbox
     */
    fun selectAll() {
        mSelectedPositionList.clear()
        for (i in mMusicInfoList.indices) {
            mSelectedPositionList.add(i)
        }
        notifyDataSetChanged()
    }

    /**
     * 清除全部歌曲的checkbox
     */
    fun unselectAll() {
        mSelectedPositionList.clear()
        notifyDataSetChanged()
    }

    /**
     * 开始下载歌曲
     */
    fun startDownload() {
//        StringBuffer logStr = new StringBuffer();
        val musicInfoList = ArrayList<MusicInfo?>()
        for (i in mSelectedPositionList) {
//            logStr.append(String.valueOf(i) + ", ");
            val musicInfo = mMusicInfoList.get(i)
            musicInfo.setSinger(musicInfo.getArName())
            musicInfoList.add(musicInfo)
            Log.d(TAG, "singer: " + musicInfo.getSinger())
        }
        if (musicInfoList.size != 0) {
            val intent = Intent(mContext, MusicDownloaderService::class.java)
            intent.putExtra("musics", musicInfoList)

            MusicApplication.Companion.getContext().startService(intent)
        }

        //        Log.d(TAG, "开始下载：" + logStr.toString());
        mSelectedPositionList.clear()
        //        changeSelectMode(false);
    }

    fun changeSelectMode(mode: Boolean) {
        mIsInSelectMode = mode
        notifyDataSetChanged()
    }

    override fun updateData(list: MutableList<MusicInfo>) {
        super.updateData(list)
        if (mQueryThread != null) {
            mQueryThread!!.interrupt()
        }
        mQueryThread = Thread(object : Runnable {
            override fun run() {
                for (musicInfo in list) {
                    musicInfo.setCollected(queryIdInThread(mContext!!, musicInfo.getId()))
                }
            }
        })
        mQueryThread!!.start()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.playlist_list_item_tracks, parent, false)
        val viewHolder = PlaylistViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val logStr = StringBuffer()
        for (i in mSelectedPositionList) {
            logStr.append(i.toString() + ", ")
        }
        Log.d(TAG, logStr.toString())

        //popView状态
        holder.mBtnPop.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val popView: View
                if (mDownloadPopWindow != null) {
                    popView = mDownloadPopWindow!!.getContentView()
                    mDownloadPopWindow!!.setFocusable(true)
                    if (mDownloadPopWindow!!.isShowing()) {
                        mDownloadPopWindow!!.dismiss()
                    } else {
                        mDownloadPopWindow!!.showAtLocation(
                            holder.mView,
                            Gravity.BOTTOM or Gravity.CENTER,
                            0,
                            0
                        )
                        darkenBackground(0.5f)
                    }
                } else {
                    popView = LayoutInflater.from(holder.mView.getContext())
                        .inflate(R.layout.playlist_pop_view, null)
                    mDownloadPopWindow =
                        PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, 600, false)
                    mDownloadPopWindow!!.setOutsideTouchable(true) // 点击外部关闭
                    mDownloadPopWindow!!.setFocusable(true)
                    mDownloadPopWindow!!.setAnimationStyle(android.R.style.Animation_Dialog)
                    mDownloadPopWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    mDownloadPopWindow!!.showAtLocation(
                        holder.mView,
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
                val musicInfo = mMusicInfoList.get(position)
                //收藏歌曲操作
                val llCollect = popView.findViewById<LinearLayout>(R.id.playlist_pop_ll_collect)
                val ivCollect = popView.findViewById<ImageView>(R.id.playlist_pop_iv_collect)
                val tvCollect = popView.findViewById<TextView>(R.id.playlist_pop_tv_collect)
                if (musicInfo.isCollected()) {
                    ivCollect.setImageResource(R.drawable.playlist_btn_music_collected)
                    tvCollect.setText(R.string.playlist_pop_collected)
                } else {
                    ivCollect.setImageResource(R.drawable.playlist_btn_music_not_collected)
                    tvCollect.setText(R.string.playlist_pop_not_collected)
                }
                llCollect.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        if (musicInfo.isCollected()) {
                            //已收藏，点击后取消收藏
                            ivCollect.setImageResource(R.drawable.playlist_btn_music_not_collected)
                            tvCollect.setText(R.string.playlist_pop_not_collected)
                            musicInfo.setCollected(false)
                            MusicThreadPool.postRunnable(object : Runnable {
                                override fun run() {
                                    updateMusicCollectInThread(
                                        holder.mView.getContext(),
                                        musicInfo,
                                        false
                                    )
                                }
                            })
                        } else {
                            //未收藏，点击后收藏
                            ivCollect.setImageResource(R.drawable.playlist_btn_music_collected)
                            tvCollect.setText(R.string.playlist_pop_collected)
                            musicInfo.setCollected(true)
                            MusicThreadPool.postRunnable(object : Runnable {
                                override fun run() {
                                    updateMusicCollectInThread(
                                        holder.mView.getContext(),
                                        musicInfo,
                                        true
                                    )
                                }
                            })
                        }
                    }
                })

                //添加到歌单
                val llAddToCustomList =
                    popView.findViewById<LinearLayout>(R.id.playlist_pop_ll_add_to_custom_list)
                llAddToCustomList.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val dialog = FoundCustomDialog(mContext, musicInfo)
                        dialog.show()
                        //                        MusicDatabaseUtils.addToMusicList(String listName,MusicInfo musicInfo);
                    }
                })
            }
        })

        //checkbox状态
        //先清空checkbox的监听器，防止ViewHolder复用时监听错乱
        holder.mCbSelect.setOnCheckedChangeListener(null)
        if (mIsInSelectMode) {
            holder.mCbSelect.setVisibility(View.VISIBLE)
            holder.mCbSelect.setChecked(mSelectedPositionList.contains(position as Int?))
        } else {
            holder.mCbSelect.setVisibility(View.GONE)
            //            holder.mCbSelect.setChecked(mSelectedPositionList.contains((Integer) position));
        }
        holder.mCbSelect.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    mSelectedPositionList.add((position as Int?)!!)
                } else {
                    mSelectedPositionList.remove(position as Int?)
                }
            }
        })

        val musicInfo = mMusicInfoList.get(position)
        holder.mTvName.setText(musicInfo.getMusicSongName())
        holder.mTvSinger.setText(musicInfo.getAllArName())
        holder.mTvNum.setText((position + 1).toString())


        if (mIsInInit) {
            if (MusicPlayerService.Companion.isPlaying && musicInfo.getId() == MusicPlayerService.Companion.sMusicID) {
                //如果该歌曲正在播放，则显示暂停按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red)
                mPlayingPosition = position
            } else {
                //如果该歌曲未播放，则显示播放按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play)
            }

            if (position == mMusicInfoList.size - 1) {
                mIsInInit = false
            }
        } else {
            if (mPlayingPosition == position) {
                //如果该歌曲正在播放，则显示暂停按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red)
            } else {
                //如果该歌曲未播放，则显示播放按钮
                holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play)
            }
        }

        //页面点击事件的反馈比service控制歌曲要快，所以无法用service的参数进行状态判断，而要在当前页面中自行控制
        holder.mView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mPlayingPosition == -1) {
                    //当前歌单没有歌曲在播放
                    //更新mPlayingPosition，开启播放服务
                    mPlayingPosition = position

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red)

                    musicInfo.setSinger(musicInfo.getFirstArName())
                    MusicPlayerService.Companion.playMusic(musicInfo)
                } else if (mPlayingPosition == position) {
                    //当前歌单有歌曲正在播放，且是当前点击的歌曲
                    //开启播放activity
                    val id = musicInfo.getId().toInt()
                    val name = musicInfo.getMusicSongName()
                    val url = musicInfo.getAlbumPicUrl()
                    val artistList: MutableList<Artist?> = ArrayList<Artist?>()
                    for (singerInfo in musicInfo.getAr()) {
                        artistList.add(BeanConverter.convertSingerInfo2Artist(singerInfo))
                    }
                    val mvMusicInfo = MvMusicInfo(id, name, url, artistList)
                    if (mContext is Activity) {
                        val activity = mContext
                        ActivityStart.startMusicPlayActivity(activity, mvMusicInfo)
                    }
                } else {
                    //当前歌单有歌曲正在播放，但不是当前点击的歌曲
                    //更新mPlayingPosition，更新播放服务
                    mPlayingPosition = position

                    notifyDataSetChanged()

                    musicInfo.setSinger(musicInfo.getFirstArName())
                    MusicPlayerService.Companion.playMusic(musicInfo)
                }
            }
        })

        holder.mBtnPlayOrPause.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (mPlayingPosition == -1) {
                    //当前歌单没有歌曲在播放
                    //更新mPlayingPosition，开启播放服务
                    mPlayingPosition = position

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_pause_red)

                    musicInfo.setSinger(musicInfo.getFirstArName())
                    MusicPlayerService.Companion.playMusic(musicInfo)
                } else if (mPlayingPosition == position) {
                    //当前歌曲正在播放，点击后暂停播放
                    mPlayingPosition = -1

                    holder.mBtnPlayOrPause.setImageResource(R.drawable.playlist_btn_play)

                    MusicPlayerService.Companion.pauseMusic()
                } else {
                    //当前歌单有歌曲正在播放，但不是当前点击的歌曲
                    //更新mPlayingPosition，更新播放服务
                    mPlayingPosition = position

                    notifyDataSetChanged()

                    musicInfo.setSinger(musicInfo.getFirstArName())
                    MusicPlayerService.Companion.playMusic(musicInfo)
                }
            }
        })
    }

    /**
     * 刷新页面信息，不包括checkbox的状态
     */
    fun refresh() {
        mIsInInit = true
        mPlayingPosition = -1
        queryAllIfCollect((mContext as Activity?)!!)
        //        notifyDataSetChanged();
    }

    /**
     * 刷新页面信息，包括checkbox
     */
    fun refreshCompletely() {
    }

    /**
     * 查询某一首歌是否被用户收藏
     * 
     * @param context 上下文
     * @param id      歌曲id
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

    private fun queryAllIfCollect(activity: Activity) {
        if (mQueryThread != null) {
            mQueryThread!!.interrupt()
        }
        mQueryThread = Thread(object : Runnable {
            override fun run() {
                for (musicInfo in mMusicInfoList) {
                    musicInfo.setCollected(queryIdInThread(mContext!!, musicInfo.getId()))
                }

                activity.runOnUiThread(object : Runnable {
                    override fun run() {
                        notifyDataSetChanged()
                    }
                })
            }
        })
        mQueryThread!!.start()
    }

    /**
     * 更新歌曲收藏数据库
     * 
     * @param context
     * @param musicInfo
     * @param collected
     */
    private fun updateMusicCollectInThread(
        context: Context,
        musicInfo: MusicInfo,
        collected: Boolean
    ) {
        val authorityUri: Uri = MusicContentProvider.Companion.COLLECTION_URL
        if (collected) {
            //增加该数据
            val values = ContentValues()
            values.put("id", musicInfo.getId())
            values.put("title", musicInfo.getMusicSongName())
            values.put("kind", MUSIC_KIND)
            //            values.put("description", "");
            context.getContentResolver().insert(authorityUri, values)
        } else {
            //删除该数据
            val collectionUrl = Uri.parse(authorityUri.toString() + "/" + musicInfo.getId())
            context.getContentResolver().delete(collectionUrl, null, null)
        }
    }

    fun setPlayingPosition(playingPosition: Int) {
        mPlayingPosition = playingPosition
    }

    //设置屏幕透明度,bgcolor:0-1
    private fun darkenBackground(bgcolor: Float) {
        if (mContext != null) {
            val activity = mContext as Activity
            val lp = activity.getWindow().getAttributes()
            lp.alpha = bgcolor
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            activity.getWindow().setAttributes(lp)
        }
    }

    companion object {
        private const val TAG = "PlaylistAdapter"

        //kind 0歌曲 1歌手 2专辑
        private const val MUSIC_KIND = 0
    }
}
