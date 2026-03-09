package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyChooseMusicAdapter
 * Description:    简单的音乐选择适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/27 19:27
 */
public class MyChooseMusicAdapter extends MusicAdapter {
    private static final String TAG = "MyChooseMusicAdapter";

    public MyChooseMusicAdapter(@NonNull Context context, @NonNull List<MusicInfo> objects) {
        super(context, R.layout.local_music_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = mList.get(position);
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.local_music_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_music_icon_cover);
            viewHolder.alarm = convertView.findViewById(R.id.playMusicButton);
            viewHolder.name = convertView.findViewById(R.id.songOriginalName);
            viewHolder.singer = convertView.findViewById(R.id.singleNames);
            viewHolder.size = convertView.findViewById(R.id.fileSizeInfo);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.icon.setImageResource(R.drawable.music_item_icon);

        viewHolder.alarm.setImageResource(R.drawable.music_choose_alarm);

        viewHolder.name.setText(musicInfo.getMusicSongName().substring(0, musicInfo.getMusicSongName().lastIndexOf(".")));

        viewHolder.singer.setText(musicInfo.getSinger());

        String fileSizeM = String.format("%.2f", musicInfo.getMusicFileSize() / 1024.0 / 1024.0);
        viewHolder.size.setText(fileSizeM + "Mb");

        return convertView;
    }

    private static class ViewHolder extends MusicAdapter.ViewHolder {
        public ImageView icon;
        public ImageView alarm;
        public TextView name;
        public TextView singer;
        public TextView size;
    }
}
