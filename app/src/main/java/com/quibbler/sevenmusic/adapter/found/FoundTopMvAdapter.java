package com.quibbler.sevenmusic.adapter.found;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.MvPlayActivity;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.presenter.ImageDownloadPresenter;
import com.quibbler.sevenmusic.utils.BeanConverter;
import com.quibbler.sevenmusic.utils.GlideRoundTransform;
import com.quibbler.sevenmusic.utils.HttpUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FoundTopMvAdapter extends RecyclerBaseAdapter<MvInfo, FoundTopMvAdapter.ViewHolder> {
    private static final String TAG = "FoundTopMvAdapter";
    private static final String MV_URL_AUTHORITY = "http://114.116.128.229:3000/mv/detail?mvid=";

    //实际使用的数据源
    private List<MvInfo> mMvInfoList = mSourceList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.found_list_item_top_mv_iv);
            mTextView = view.findViewById(R.id.found_list_item_top_mv_tv);
        }
    }

    public FoundTopMvAdapter(List<MvInfo> MvInfoList) {
        super(MvInfoList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_list_item_top_mv, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        MvInfo MvInfo = mMvInfoList.get(position);

        //显示歌单名
        holder.mTextView.setText(MvInfo.getName());
        Log.d(TAG, "MvInfo.getCoverImgUrl() is: " + MvInfo.getPictureUrl());

        //下载歌单封面并显示
        //最好在这里给imageView一个占位图片，否则textView可能错乱
//        ImageDownloadPresenter.getInstance().with(holder.mImageView.getContext())
//                .load(MvInfo.getPictureUrl())
//                .imageStyle(ImageDownloadPresenter.STYLE_ROUND)
//                .into(holder.mImageView);

        RequestOptions options = new RequestOptions().transform(new GlideRoundTransform(MusicApplication.getContext(), 30));

        Glide.with(holder.mImageView.getContext())
                .load(MvInfo.getPictureUrl())
                .apply(options)
                .into(holder.mImageView);

        HttpUtil.sendOkHttpRequest(MV_URL_AUTHORITY + MvInfo.getId(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("data");

                    MvInfo.setUrl(jsonObject.getJSONObject("brs").getString("480"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(MvInfo.getUrl())) {
                    Toast.makeText(holder.mView.getContext(), "视频不存在或已下架！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(MusicApplication.getContext(), MvPlayActivity.class);
                    intent.putExtra("mvInfo", BeanConverter.convertMvInfo2MvInfo(MvInfo));
                    holder.mView.getContext().startActivity(intent);
                }

            }
        });
    }

}
