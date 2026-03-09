package com.quibbler.sevenmusic.adapter.found

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.PlaylistActivity
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      FoundShowPlaylistAdapter
 * Description:    “发现”页面推荐歌单RecyclerView的adapter，同时也是歌单库页面每个标签对应的RecyclerView的adapter
 * Author:         yanwuyang
 * CreateDate:     2019/9/20 10:54
 */
class FoundShowPlaylistAdapter(topPlaylistInfoList: MutableList<PlaylistInfo?>?) :
    RecyclerBaseAdapter<PlaylistInfo?, FoundShowPlaylistAdapter.PlaylistViewHolder?>(
        topPlaylistInfoList
    ) {
    //实际使用的数据源
    private val mTopPlaylistInfoList: MutableList<PlaylistInfo> = mSourceList

    internal class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mView: View
        var mImageView: ImageView
        var mTextView: TextView

        init {
            mView = view
            mImageView = view.findViewById<ImageView>(R.id.found_list_item_top_playlist_iv)
            mTextView = view.findViewById<TextView>(R.id.found_list_item_top_playlist_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.found_list_item_playlist, parent, false)
        val viewHolder = PlaylistViewHolder(view)
        viewHolder.mView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val position = viewHolder.getAdapterPosition()
                val playlistInfo = mTopPlaylistInfoList.get(position)
                val id = playlistInfo.getId()
                val intent = Intent(viewHolder.mView.getContext(), PlaylistActivity::class.java)
                intent.putExtra(
                    viewHolder.mView.getContext().getResources().getString(R.string.playlist_id), id
                )
                viewHolder.mView.getContext().startActivity(intent)
            }
        })
        return viewHolder
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val playlistInfo = mTopPlaylistInfoList.get(position)

        //显示歌单名
        holder.mTextView.setText(playlistInfo.getName())
        Log.d(TAG, "playlistInfo.getCoverImgUrl() is: " + playlistInfo.getCoverImgUrl())

        //下载歌单封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
        ImageDownloadPresenter.Companion.getInstance().with(holder.mImageView.getContext())
            .load(playlistInfo.getCoverImgUrl())
            .imageStyle(ImageDownloadPresenter.Companion.STYLE_ROUND)
            .into(holder.mImageView)
    }

    companion object {
        private const val TAG = "FoundShowPlaylistAdapter"
    }
}
