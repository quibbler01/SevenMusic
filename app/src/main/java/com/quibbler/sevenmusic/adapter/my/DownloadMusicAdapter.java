package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.graphics.Color;
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

public class DownloadMusicAdapter extends MusicAdapter {
    private static final String TAG = "DownloadMusicAdapter";

    public DownloadMusicAdapter(@NonNull Context context, @NonNull List<MusicInfo> objects) {
        super(context, R.layout.download_music_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.download_music_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_music_icon_cover);
            viewHolder.songName = convertView.findViewById(R.id.songOriginalName);
            viewHolder.singerName = convertView.findViewById(R.id.singleNames);
            viewHolder.fileSize = convertView.findViewById(R.id.fileSizeInfo);
            viewHolder.playButton = convertView.findViewById(R.id.playMusicButton);
            viewHolder.waitProgress = convertView.findViewById(R.id.my_download_progress_bar);
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

        if (("".equals(musicInfo.getMusicFilePath()) || musicInfo.getMusicFilePath() == null)) {
            viewHolder.playButton.setVisibility(View.GONE);
            if (musicInfo.isDownloadFailed()) {
                viewHolder.fileSize.setText(R.string.my_download_failed);
                viewHolder.fileSize.setTextColor(Color.RED);
                viewHolder.fileSize.setVisibility(View.VISIBLE);
                viewHolder.waitProgress.setVisibility(View.GONE);
            } else {
                Glide.with(mContext).load(R.drawable.my_downloading_icon).into(viewHolder.waitProgress);
                viewHolder.waitProgress.setVisibility(View.VISIBLE);
                viewHolder.fileSize.setVisibility(View.GONE);
            }
        } else {
            ImageView playButton = viewHolder.playButton;
            setOnClickListener(convertView, playButton, musicInfo);
        }
        return convertView;
    }

    private static class ViewHolder extends MusicAdapter.ViewHolder {
        public TextView singerName;
        public TextView fileSize;
        public ImageView waitProgress;
    }
}
