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
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;

public class PlayListMusicAdapter extends MusicAdapter {
    private static final String TAG = "PlayListMusicAdapter";

    public PlayListMusicAdapter(@NonNull Context context, @NonNull List<MusicInfo> objects) {
        super(context, R.layout.local_music_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.play_list_music_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_music_icon_cover);
            viewHolder.songName = convertView.findViewById(R.id.songOriginalName);
            viewHolder.singerName = convertView.findViewById(R.id.singleNames);
            viewHolder.playButton = convertView.findViewById(R.id.playMusicButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
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

        ImageView playButton = viewHolder.playButton;
        setOnClickListener(convertView, playButton, musicInfo);
        return convertView;
    }

    private static class ViewHolder extends MusicAdapter.ViewHolder {
        public TextView singerName;
    }
}
