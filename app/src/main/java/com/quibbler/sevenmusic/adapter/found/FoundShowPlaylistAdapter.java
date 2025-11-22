package com.quibbler.sevenmusic.adapter.found;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.found.PlaylistActivity;
import com.quibbler.sevenmusic.bean.jsonbean.found.PlaylistInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      FoundShowPlaylistAdapter
 * Description:    “发现”页面推荐歌单RecyclerView的adapter，同时也是歌单库页面每个标签对应的RecyclerView的adapter
 * Author:         yanwuyang
 * CreateDate:     2019/9/20 10:54
 */
public class FoundShowPlaylistAdapter extends RecyclerBaseAdapter<PlaylistInfo, FoundShowPlaylistAdapter.PlaylistViewHolder> {
    private static final String TAG = "FoundShowPlaylistAdapter";

    //实际使用的数据源
    private List<PlaylistInfo> mTopPlaylistInfoList = mSourceList;

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        TextView mTextView;

        public PlaylistViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.found_list_item_top_playlist_iv);
            mTextView = view.findViewById(R.id.found_list_item_top_playlist_tv);
        }
    }

    public FoundShowPlaylistAdapter(List<PlaylistInfo> topPlaylistInfoList) {
        super(topPlaylistInfoList);
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_list_item_playlist, parent, false);
        PlaylistViewHolder viewHolder = new PlaylistViewHolder(view);
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                PlaylistInfo playlistInfo = mTopPlaylistInfoList.get(position);
                String id = playlistInfo.getId();
                Intent intent = new Intent(viewHolder.mView.getContext(), PlaylistActivity.class);
                intent.putExtra(viewHolder.mView.getContext().getResources().getString(R.string.playlist_id), id);
                viewHolder.mView.getContext().startActivity(intent);

            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        PlaylistInfo playlistInfo = mTopPlaylistInfoList.get(position);

        //显示歌单名
        holder.mTextView.setText(playlistInfo.getName());
        Log.d(TAG, "playlistInfo.getCoverImgUrl() is: " + playlistInfo.getCoverImgUrl());

        //下载歌单封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
        ImageDownloadPresenter.getInstance().with(holder.mImageView.getContext())
                .load(playlistInfo.getCoverImgUrl())
                .imageStyle(ImageDownloadPresenter.STYLE_ROUND)
                .into(holder.mImageView);
    }
}
