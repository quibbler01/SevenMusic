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

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.my.MyLocalMusicDetailActivity;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.MusicPathInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyFilePathListVIewAdapter
 * Description:    我的:文件夹适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 21:55
 */
public class MyFilePathListVIewAdapter extends ArrayAdapter<MusicPathInfo> {
    private Context mContext;
    private int mResource;
    private List<MusicPathInfo> mMusicPathInfoLists = new ArrayList<>();

    public MyFilePathListVIewAdapter(@NonNull Context context, int resource, @NonNull List<MusicPathInfo> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mMusicPathInfoLists.addAll(objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicPathInfo musicPathInfo = mMusicPathInfoLists.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_file_dir_icon);
            viewHolder.detail = convertView.findViewById(R.id.path_detail_item);
            viewHolder.path = convertView.findViewById(R.id.music_list_item_path);
            viewHolder.count = convertView.findViewById(R.id.path_list_song_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MyLocalMusicDetailActivity.class);
                intent.putExtra("title", mMusicPathInfoLists.get(position).getPathName());
                List<MusicInfo> list = new ArrayList<>();
                list.addAll(mMusicPathInfoLists.get(position).getMusicInfoLists());
                intent.putExtra("music", (Serializable) list);
                mContext.startActivity(intent);
            }
        });
        if ("QQ音乐".equals(musicPathInfo.getPathName())) {
            viewHolder.icon.setImageResource(R.drawable.path_icon_qq);
        } else if ("网易云音乐".equals(musicPathInfo.getPathName())) {
            viewHolder.icon.setImageResource(R.drawable.path_icon_easynet);
        } else if ("七音".equals(musicPathInfo.getPathName())) {
            viewHolder.icon.setImageResource(R.drawable.ic_launcher_web);
        }

        viewHolder.detail.setText(musicPathInfo.getPathDetail());
        viewHolder.path.setText(musicPathInfo.getPathName());
        viewHolder.count.setText(musicPathInfo.getMusicInfoLists().size() + "首");
        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView path;
        TextView detail;
        TextView count;
    }
}
