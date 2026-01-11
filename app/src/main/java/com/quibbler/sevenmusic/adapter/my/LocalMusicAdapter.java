package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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

public class LocalMusicAdapter extends MusicAdapter {
    private static final String TAG = "LocalMusicAdapter";

    public LocalMusicAdapter(@NonNull Context context, @NonNull List<MusicInfo> objects) {
        super(context, R.layout.local_music_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.local_music_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_local_music_icon_cover);
            viewHolder.songName = convertView.findViewById(R.id.songOriginalName);
            viewHolder.singerName = convertView.findViewById(R.id.singleNames);
            viewHolder.fileSize = convertView.findViewById(R.id.fileSizeInfo);
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

        if (musicInfo.getIsMusicMatch().isMusicNameMatch()) {
            SpannableString spannableString = new SpannableString(musicInfo.getMusicSongName());
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#008AFF"));
            spannableString.setSpan(colorSpan, musicInfo.getIsMusicMatch().getMusicNameStart(), musicInfo.getIsMusicMatch().getMusicNameStart() + musicInfo.getIsMusicMatch().getKeyLength(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.songName.setText(spannableString);
        } else {
            viewHolder.songName.setText(musicInfo.getMusicSongName());
        }

        if (musicInfo.getIsMusicMatch().isSingleNameMatch()) {
            SpannableString spannableString = new SpannableString(musicInfo.getSinger());
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#008AFF"));
            spannableString.setSpan(colorSpan, musicInfo.getIsMusicMatch().getSingleNameStart(), musicInfo.getIsMusicMatch().getSingleNameStart() + musicInfo.getIsMusicMatch().getKeyLength(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.singerName.setText(spannableString);
        } else {
            viewHolder.singerName.setText(musicInfo.getSinger());
        }

        String fileSizeM = String.format("%.2f", musicInfo.getMusicFileSize() / 1024.0 / 1024.0);
        viewHolder.fileSize.setText(fileSizeM + "Mb");

        ImageView playButton = viewHolder.playButton;
        setOnClickListener(convertView, playButton, musicInfo);
        return convertView;
    }

    private static class ViewHolder extends MusicAdapter.ViewHolder {
        public TextView singerName;
        public TextView fileSize;
    }
}
