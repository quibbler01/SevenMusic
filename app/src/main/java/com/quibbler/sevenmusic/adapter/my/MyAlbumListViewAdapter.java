package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity;
import com.quibbler.sevenmusic.bean.AlbumInfo;
import com.quibbler.sevenmusic.bean.MusicInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyAlbumListViewAdapter
 * Description:    我的:专辑适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:55
 */
public class MyAlbumListViewAdapter extends ArrayAdapter<AlbumInfo> {
    private Context mContext;
    private List<AlbumInfo> mAlbumInfoList = new ArrayList<>();
    private int mResource;

    public MyAlbumListViewAdapter(@NonNull Context context, int resource, @NonNull List<AlbumInfo> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mAlbumInfoList.addAll(objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.album_list_item_name);
            viewHolder.number = convertView.findViewById(R.id.album_list_song_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MyLocalMusicDetailActivity.class);
                intent.putExtra("title", "专辑:" + mAlbumInfoList.get(position).getAlbumName());
                List<MusicInfo> list = new ArrayList<>();
                list.addAll(mAlbumInfoList.get(position).getMusicInfoList());
                intent.putExtra("music", (Serializable) list);
                mContext.startActivity(intent);
            }
        });
        viewHolder.name.setText(mAlbumInfoList.get(position).getAlbumName());
        viewHolder.number.setText(mAlbumInfoList.get(position).getMusicInfoList().size() + "首");
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView number;
    }
}
