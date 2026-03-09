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
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.service.MusicPlayerService;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.FAVOURITE_URL;

public class FavouriteMusicAdapter extends MusicAdapter {
    private static final String TAG = "FavouriteMusicAdapter";

    public FavouriteMusicAdapter(@NonNull Context context, @NonNull List<MusicInfo> objects) {
        super(context, R.layout.favoruite_music_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MusicInfo musicInfo = getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.favoruite_music_list_item, parent, false);
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

    public void changeManagerState() {
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (MusicInfo musicInfo : mList) {
            musicInfo.setSelect(true);
        }
        notifyDataSetChanged();
    }

    public void unSelectAndReset() {
        for (MusicInfo musicInfo : mList) {
            musicInfo.setSelect(false);
        }
        notifyDataSetChanged();
    }

    public void removeFavourite() {
        List<MusicInfo> remain = new ArrayList<>();
        List<MusicInfo> delete = new ArrayList<>();
        for (MusicInfo musicInfo : mList) {
            if (musicInfo.isSelect()) {
                delete.add(musicInfo);
            } else {
                remain.add(musicInfo);
            }
        }
        update(remain);
        MusicThreadPool.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (MusicInfo musicInfo : delete) {
                    MusicApplication.getContext().getContentResolver().delete(FAVOURITE_URL, "id = ?", new String[]{musicInfo.getId()});
                }
            }
        });
    }

    @Deprecated
    public boolean hasSelect() {
        for (MusicInfo musicInfo : mList) {
            if (musicInfo.isSelect()) {
                return true;
            }
        }
        return false;
    }

    public int getSelectCount() {
        int select = 0;
        for (MusicInfo musicInfo : mList) {
            if (musicInfo.isSelect()) {
                ++select;
            }
        }
        return select;
    }

    private static class ViewHolder extends MusicAdapter.ViewHolder {
        public TextView singerName;
    }

}
