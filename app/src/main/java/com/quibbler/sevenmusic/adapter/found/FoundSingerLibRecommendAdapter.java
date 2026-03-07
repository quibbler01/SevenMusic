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

public class FoundSingerLibRecommendAdapter extends RecyclerBaseAdapter<FoundSingerInfo, FoundSingerLibRecommendAdapter.SingerTopViewHolder> {
    //实际使用的数据源list
    private List<FoundSingerInfo> mFoundSingerInfoList = mSourceList;

    static class SingerTopViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        TextView mTextView;

        public SingerTopViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.found_list_item_top_singer_iv);
            mTextView = view.findViewById(R.id.found_list_item_singer_top_tv_name);
        }
    }

    public FoundSingerLibRecommendAdapter(List<FoundSingerInfo> foundSingerInfoList) {
        super(foundSingerInfoList);
    }

    @NonNull
    @Override
    public SingerTopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_list_item_top_singer, parent, false);
        SingerTopViewHolder viewHolder = new SingerTopViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SingerTopViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        FoundSingerInfo foundSingerInfo = mFoundSingerInfoList.get(position);
        holder.mTextView.setText(foundSingerInfo.getName());

        ImageDownloadPresenter.getInstance().with(holder.mImageView.getContext())
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

}
