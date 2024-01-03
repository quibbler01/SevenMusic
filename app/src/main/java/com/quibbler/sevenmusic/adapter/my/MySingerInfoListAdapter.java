package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.SingerInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_SINGER;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MySingerInfoListAdapter
 * Description:    本地音乐:歌手适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:56
 */
public class MySingerInfoListAdapter extends ArrayAdapter<SingerInfo> {
    private Context mContext;
    private List<SingerInfo> mSingerMusicInfoLists;
    private int mResource;
    private List<View> mViews;

    public MySingerInfoListAdapter(@NonNull Context context, int resource, @NonNull List<SingerInfo> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mSingerMusicInfoLists = objects;
        mViews = new ArrayList<>(objects.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SingerInfo singerInfo = mSingerMusicInfoLists.get(position);
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_singer_head_icon);
            viewHolder.name = convertView.findViewById(R.id.singer_list_item_name);
            viewHolder.number = convertView.findViewById(R.id.singer_list_song_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MyLocalMusicDetailActivity.class);
                intent.putExtra("title", "歌手:" + singerInfo.getName());
                List<MusicInfo> list = new ArrayList<>();
                list.addAll(singerInfo.getSongLists());
                intent.putExtra("music", (Serializable) list);
                mContext.startActivity(intent);
            }
        });

        Glide.with(mContext).load(SEVEN_MUSIC_SINGER + "/" + singerInfo.getName()).placeholder(R.drawable.my_collection_singer_icon_item).into(viewHolder.icon);
        viewHolder.name.setText(mSingerMusicInfoLists.get(position).getName());
        viewHolder.number.setText(mSingerMusicInfoLists.get(position).getSongLists().size() + "首");
        return convertView;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView number;
    }
}
