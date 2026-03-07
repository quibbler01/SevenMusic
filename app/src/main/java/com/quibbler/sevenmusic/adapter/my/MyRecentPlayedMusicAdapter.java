package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.service.MusicPlayerService;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;

public class MyRecentPlayedMusicAdapter extends MusicAdapter {
    private static final String TAG = "MyRecentPlayedMusicAdapter";

    public MyRecentPlayedMusicAdapter(Context context, List<MusicInfo> objects) {
        super(context, R.layout.my_recently_played_music_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = getItem(position);
        ItemViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_recently_played_music_item, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_music_icon_cover);
            viewHolder.songName = convertView.findViewById(R.id.songOriginalName);
            viewHolder.singerName = convertView.findViewById(R.id.singleNames);
            viewHolder.playTime = convertView.findViewById(R.id.fileSizeInfo);
            viewHolder.playButton = convertView.findViewById(R.id.playMusicButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ItemViewHolder) convertView.getTag();
        }

        Glide.with(mContext).load(SEVEN_MUSIC_IMAGE + "/" + musicInfo.getId()).placeholder(R.drawable.music_item_icon).into(viewHolder.icon);

        if (MusicPlayerService.isPlaying && musicInfo.getId().equals(MusicPlayerService.sMusicID)) {
            viewHolder.playButton.setImageResource(R.drawable.my_music_pause_icon);
            mButtonWeakReference = new WeakReference<>(viewHolder.playButton);
        } else {
            viewHolder.playButton.setImageResource(R.drawable.my_music_play_icon);
        }
        viewHolder.songName.setText(musicInfo.getMusicSongName());
        viewHolder.singerName.setText(musicInfo.getSinger());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        viewHolder.playTime.setText(simpleDateFormat.format(musicInfo.getLastPlayedTime()));

        ImageView playButton = viewHolder.playButton;
        setOnClickListener(convertView, playButton, musicInfo);
        return convertView;
    }

    private static class ItemViewHolder extends MusicAdapter.ViewHolder {
        public TextView singerName;
        public TextView playTime;
    }
}
