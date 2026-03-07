package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.activity.my.MyCollectionMVActivity;
import com.quibbler.sevenmusic.bean.mv.MvInfo;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionMvAdapter
 * Description:    MV适配器 点击播放mv，预设一部《我和我的祖国》MV
 * Author:         zhaopeng
 * CreateDate:     2019/10/9 17:07
 */
public class MyCollectionMvAdapter extends ArrayAdapter<MyCollectionMVActivity.MVShowInfo> {
    private List<MyCollectionMVActivity.MVShowInfo> mInfos;
    private Context mContext;
    private int mResource;

    public MyCollectionMvAdapter(@NonNull Context context, int resource, @NonNull List<MyCollectionMVActivity.MVShowInfo> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mInfos = objects;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cover = convertView.findViewById(R.id.my_collection_mv_cover);
            viewHolder.name = convertView.findViewById(R.id.my_collection_mv_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if ("我和我的祖国".equals(mInfos.get(position).getName())) {
            Glide.with(mContext).load(R.drawable.my_mv_default).apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(viewHolder.cover);
        } else {
            Glide.with(mContext).load(mInfos.get(position).getPictureurl()).apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(viewHolder.cover);
        }
        viewHolder.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MvInfo mvInfo = new MvInfo(Integer.valueOf(mInfos.get(position).getId()), mInfos.get(position).getName(), null, 0, null, mInfos.get(position).getPictureurl());
                ActivityStart.startMvPlayActivity(mContext, mvInfo);
            }
        });
        viewHolder.name.setText(mInfos.get(position).getName());
        return convertView;
    }

    public void updateData(List<MyCollectionMVActivity.MVShowInfo> infoList) {
        this.mInfos.clear();
        this.mInfos.addAll(infoList);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView cover;
        TextView name;
        TextView detail;
    }
}
