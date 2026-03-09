package com.quibbler.sevenmusic.adapter.mv;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.adapter.found.RecyclerBaseAdapter;
import com.quibbler.sevenmusic.bean.mv.Artist;
import com.quibbler.sevenmusic.bean.mv.MvInfo;

import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.adapter.mv
  * ClassName:      TopMvListAdapter
  * Description:    排行和更多mv列表的adapter
  * Author:         lishijun
  * CreateDate:     2019/10/11 17:00
 */
public class TopMvListAdapter extends RecyclerBaseAdapter<MvInfo, TopMvListAdapter.ViewHolder> {

    private List<MvInfo> mMvInfoList = mSourceList;

    private TopMvClickListener mTopMvClickListener;

    public TopMvListAdapter(List<MvInfo> infoList, TopMvClickListener topMvClickListener) {
        super(infoList);
        mTopMvClickListener = topMvClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView mVideoImageView;
        TextView mVideoNameView;
        TextView mVideoArtistView;
        ImageButton mMoreButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mVideoImageView = view.findViewById(R.id.mv_video);
            mVideoNameView = view.findViewById(R.id.mv_tv_name);
            mVideoArtistView = view.findViewById(R.id.mv_tv_artist);
            mMoreButton = view.findViewById(R.id.mv_ib_download);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.small_mv_item,
                parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MvInfo mvInfo = mMvInfoList.get(position);
        holder.mVideoNameView.setText(mvInfo.getName());
        StringBuffer artistString = new StringBuffer();
        List<Artist> mvArtistList = mvInfo.getArtists();
        if (mvArtistList != null) {
            for (int i = 0; i < mvArtistList.size(); i++) {
                if (i == mvArtistList.size() - 1) {
                    artistString.append(mvArtistList.get(i).getName());
                } else {
                    artistString.append(mvArtistList.get(i).getName() + "/");
                }
            }
            holder.mVideoArtistView.setText(artistString);
        }
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(20);
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners);
        Glide.with(MusicApplication.getContext())
                .load(mvInfo.getPictureUrl())
                .apply(options)
                .placeholder(R.drawable.default_mv_cover)
                .into(holder.mVideoImageView);
        holder.mVideoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转进入播放activity
                if(mTopMvClickListener != null){
                    mTopMvClickListener.onStartMvPlayActivity(mvInfo);
                }
            }
        });
        holder.mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopMvClickListener.onClickMoreButton(mvInfo);
            }
        });
    }
}
