package com.quibbler.sevenmusic.adapter.mv

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.sevenvideoview.IVideoPlayerController
import com.example.sevenvideoview.SevenVideoPlayer
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.RecyclerBaseAdapter
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.callback.MusicCallBack
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter
import com.quibbler.sevenmusic.presenter.MvPresenter
import com.quibbler.sevenmusic.utils.TextureVideoViewOutlineProvider

class RecommendMvAdapter(infoList: MutableList<MvInfo?>?, topMvClickListener: TopMvClickListener?) :
    RecyclerBaseAdapter<MvInfo?, RecyclerView.ViewHolder?>(infoList) {
    private val mMvInfoList: MutableList<MvInfo> = mSourceList

    private val mTopMvClickListener: TopMvClickListener?

    private var mAppCompatActivity: Context? = null

    //    TxVideoPlayerController controller;
    init {
        mTopMvClickListener = topMvClickListener
    }

    fun setAppCompatActivity(appCompatActivity: Context) {
        mAppCompatActivity = appCompatActivity
    }

    internal class RecommendTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTextView: TextView

        init {
            mTextView = view.findViewById<TextView>(R.id.mv_tv_text)
        }
    }

    internal class MoreTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTextView: TextView

        init {
            mTextView = view.findViewById<TextView>(R.id.mv_tv_text)
        }
    }

    internal class SmallMvViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View?
        var mVideoImageView: ImageView
        var mVideoNameView: TextView
        var mVideoArtistView: TextView
        var mMoreButton: ImageButton

        init {
            mView = view
            mVideoImageView = view.findViewById<ImageView>(R.id.mv_video)
            mVideoNameView = view.findViewById<TextView>(R.id.mv_tv_name)
            mVideoArtistView = view.findViewById<TextView>(R.id.mv_tv_artist)
            mMoreButton = view.findViewById<ImageButton>(R.id.mv_ib_download)
        }
    }

    internal class BigMvViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View?
        var mVideoView: SevenVideoPlayer
        var mVideoNameView: TextView
        var mVideoArtistView: TextView
        var mMoreButton: ImageButton
        var mArtistHeadView: ImageView

        init {
            mView = view
            mVideoView = view.findViewById<SevenVideoPlayer>(R.id.mv_video)
            mVideoNameView = view.findViewById<TextView>(R.id.mv_tv_name)
            mVideoArtistView = view.findViewById<TextView>(R.id.mv_tv_artist)
            mMoreButton = view.findViewById<ImageButton>(R.id.mv_ib_download)
            mArtistHeadView = view.findViewById<ImageView>(R.id.mv_iv_artist_head)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == RECOMMEND_TEXT_VIWE) {
            Log.d(TAG, "RECOMMEND_TEXT_VIWE")
            val view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mv_textview_item,
                parent, false
            )
            return RecommendTextViewHolder(view)
        } else if (viewType == RECOMMEND_MV_VIWE) {
            Log.d(TAG, "RECOMMEND_MV_VIWE")
            val view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.small_mv_item,
                parent, false
            )
            return SmallMvViewHolder(view)
        } else if (viewType == MORE_TEXT_VIWE) {
            Log.d(TAG, "MORE_TEXT_VIWE")
            val view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mv_textview_item,
                parent, false
            )
            return MoreTextViewHolder(view)
        } else if (viewType == MORE_MV_VIWE) {
            Log.d(TAG, "MORE_MV_VIWE")
            val view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mv_item,
                parent, false
            )
            return BigMvViewHolder(view)
        } else {
            //        controller = new TxVideoPlayerController(MusicApplication.getContext());
            return null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        Log.d(TAG, "onBindViewHolder")
        if (holder is RecommendTextViewHolder) {
            holder.mTextView.setText(
                MusicApplication.Companion.getContext()
                    .getString(R.string.mv_choosen)
            )
        } else if (holder is SmallMvViewHolder) {
            val viewHolder = holder
            val mvInfo = mMvInfoList.get(position - 1)
            Log.d(TAG, "recommend mvs: " + mvInfo.getName())
            viewHolder.mVideoNameView.setText(mvInfo.getName())
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
                viewHolder.mVideoArtistView.setText(artistString)
            }
            ImageDownloadPresenter.Companion.getInstance()
                .with(MusicApplication.Companion.getContext())
                .load(mvInfo.getPictureUrl())
                .imageStyle(ImageDownloadPresenter.Companion.STYLE_ROUND)
                .into(viewHolder.mVideoImageView)
            //设置图片圆角角度
//            RoundedCorners roundedCorners = new RoundedCorners(20);
//            RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
//            Glide.with(MusicApplication.getContext())
//                    .load(mvInfo.getPictureUrl())
//                    .apply(options)
//                    .placeholder(R.drawable.default_mv_cover)
//                    .into(viewHolder.mVideoImageView);
            viewHolder.mVideoImageView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    //跳转进入播放activity
                    if (mTopMvClickListener != null) {
                        mTopMvClickListener.onStartMvPlayActivity(mvInfo)
                    }
                }
            })
            viewHolder.mMoreButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    mTopMvClickListener!!.onClickMoreButton(mvInfo)
                }
            })
        } else if (holder is MoreTextViewHolder) {
            holder.mTextView.setText(
                MusicApplication.Companion.getContext()
                    .getString(R.string.mv_more_choosen)
            )
        } else if (holder is BigMvViewHolder) {
            val mvInfo = mMvInfoList.get(position - 2)
            Log.d(TAG, "more mvs: " + mvInfo.getName())
            val viewHolder = holder
            viewHolder.mVideoNameView.setText(mvInfo.getName())
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
                viewHolder.mVideoArtistView.setText(artistString)
            }
            viewHolder.mVideoView.setContext(mAppCompatActivity)
            //设置圆角
            viewHolder.mVideoView.setOutlineProvider(TextureVideoViewOutlineProvider(20f))
            viewHolder.mVideoView.setClipToOutline(true)
            MvPresenter.getMvInfo(mvInfo, object : MusicCallBack {
                override fun onMusicInfoCompleted() {
                    viewHolder.mVideoView.setUp(mvInfo.getUrl(), null)
                }
            })
            val controller = IVideoPlayerController(mAppCompatActivity)
            controller.setTitle(mvInfo.getPlayCount().toString())
            viewHolder.mVideoView.setController(controller)
            Glide.with(mAppCompatActivity!!)
                .load(mvInfo.getPictureUrl())
                .placeholder(R.drawable.default_mv_cover)
                .into(controller.imageView())
            //先用glide加载
            Glide.with(mAppCompatActivity!!)
                .load(mvInfo.getPictureUrl())
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(viewHolder.mArtistHeadView)
            viewHolder.mVideoNameView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    //跳转进入播放activity
                    if (mTopMvClickListener != null) {
                        mTopMvClickListener.onStartMvPlayActivity(mvInfo)
                    }
                }
            })
            viewHolder.mMoreButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    mTopMvClickListener!!.onClickMoreButton(mvInfo)
                }
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return RECOMMEND_TEXT_VIWE
        } else if (position >= 1 && position <= 4) {
            return RECOMMEND_MV_VIWE
        } else if (position == 5) {
            return MORE_TEXT_VIWE
        } else if (position > 5) {
            return MORE_MV_VIWE
        } else {
            return super.getItemViewType(position)
        }
    }

    override fun getItemCount(): Int {
        if (mMvInfoList.size <= 0) {
            return 0
        } else if (mMvInfoList.size <= 4) {
            return mMvInfoList.size + 1
        } else {
            return mMvInfoList.size + 2
        }
    }

    companion object {
        private const val RECOMMEND_TEXT_VIWE = 0

        private const val RECOMMEND_MV_VIWE = 1

        private const val MORE_TEXT_VIWE = 2

        private const val MORE_MV_VIWE = 3

        private const val TAG = "RecommendMvAdapter"
    }
}

