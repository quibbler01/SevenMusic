package com.quibbler.sevenmusic.adapter.found;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;

import com.quibbler.sevenmusic.activity.found.SingerActivity;
import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;

import java.util.List;

public class FoundSingerLibSelectedAdapter extends RecyclerBaseAdapter<FoundSingerInfo, FoundSingerLibSelectedAdapter.SingerViewHolder> {

    //实际使用的数据源list
    private List<FoundSingerInfo> mFoundSingerInfoList = mSourceList;

    static class SingerViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        TextView mTvName;

        public SingerViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.found_list_item_singer_iv);
            mTvName = view.findViewById(R.id.found_list_item_singer_tv_name);
        }
    }

    public FoundSingerLibSelectedAdapter(List<FoundSingerInfo> list) {
        super(list);
    }

    @NonNull
    @Override
    public SingerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_list_item_selected_singer, parent, false);
        SingerViewHolder viewHolder = new SingerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SingerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        FoundSingerInfo foundSingerInfo = mFoundSingerInfoList.get(position);
        //歌手名
        holder.mTvName.setText(foundSingerInfo.getName());

        //下载歌手封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
        ImageDownloadPresenter.getInstance().with(MusicApplication.getContext())
                .load(foundSingerInfo.getPicUrl())
                .imageStyle(ImageDownloadPresenter.STYLE_CIRCLE)
                .into(holder.mImageView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.mView.getContext(), SingerActivity.class);
                intent.putExtra("id", foundSingerInfo.getId());
                holder.mView.getContext().startActivity(intent);
            }
        });
    }

    public int getPositionForSelection(char selection) {
        for (int i = 0; i < mFoundSingerInfoList.size(); i++) {
            String firstPinyin = mFoundSingerInfoList.get(i).getFirstPinyin();
            char first = firstPinyin.toUpperCase().charAt(0);
            if (first == selection) {
                return i;
            }
        }
        return -1;
    }
}
