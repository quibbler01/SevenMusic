package com.quibbler.sevenmusic.adapter.mv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.adapter.found.RecyclerBaseAdapter
import com.quibbler.sevenmusic.bean.mv.MvInfo

/**
 * 
 * Package:        com.quibbler.sevenmusic.adapter.mv
 * ClassName:      TopMvListAdapter
 * Description:    排行和更多mv列表的adapter
 * Author:         lishijun
 * CreateDate:     2019/10/11 17:00
 */
class TopMvListAdapter(infoList: MutableList<MvInfo?>?, topMvClickListener: TopMvClickListener?) :
    RecyclerBaseAdapter<MvInfo?, TopMvListAdapter.ViewHolder?>(infoList) {
    private val mMvInfoList: MutableList<MvInfo> = mSourceList

    private val mTopMvClickListener: TopMvClickListener?

    init {
        mTopMvClickListener = topMvClickListener
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.small_mv_item,
            parent, false
        )
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mvInfo = mMvInfoList.get(position)
        holder.mVideoNameView.setText(mvInfo.getName())
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
            holder.mVideoArtistView.setText(artistString)
        }
        //设置图片圆角角度
        val roundedCorners = RoundedCorners(20)
        val options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(MusicApplication.Companion.getContext())
            .load(mvInfo.getPictureUrl())
            .apply(options)
            .placeholder(R.drawable.default_mv_cover)
            .into(holder.mVideoImageView)
        holder.mVideoImageView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //跳转进入播放activity
                if (mTopMvClickListener != null) {
                    mTopMvClickListener.onStartMvPlayActivity(mvInfo)
                }
            }
        })
        holder.mMoreButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mTopMvClickListener!!.onClickMoreButton(mvInfo)
            }
        })
    }
}
