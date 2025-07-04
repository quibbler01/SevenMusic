package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.service.MusicPlayerService;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.my
 * ClassName:      MusicAdapter
 * Description:    抽象Adapter类
 * Author:         zhaopeng
 * CreateDate:     2019/11/15 21:37
 */
public abstract class MusicAdapter extends ArrayAdapter<MusicInfo> {
    private static final String TAG = "MusicAdapter";

    List<MusicInfo> mList;
    Context mContext;
    WeakReference<ImageView> mButtonWeakReference = null;

    MusicAdapter(Context context, int resource, List<MusicInfo> objects) {
        super(context, resource, objects);
        mList = objects;
        this.mContext = context;
    }

    public void update(List<MusicInfo> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnClickListener(View view, ImageView playButton, MusicInfo musicInfo) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayerService.isPlaying) {
                    if (musicInfo.getId().equals(MusicPlayerService.sMusicID)) {
                        MusicPlayerService.pauseMusic();
                        playButton.setImageResource(R.drawable.my_music_play_icon);
                    } else {
                        MusicPlayerService.playMusic(musicInfo);
                        if (mButtonWeakReference != null) {
                            mButtonWeakReference.get().setImageResource(R.drawable.my_music_play_icon);
                        }
                        playButton.setImageResource(R.drawable.my_music_pause_icon);
                        mButtonWeakReference = new WeakReference<>(playButton);
                    }
                } else {
                    if (musicInfo.getId().equals(MusicPlayerService.sMusicID)) {
                        MusicPlayerService.playMusic(musicInfo);
                        playButton.setImageResource(R.drawable.my_music_pause_icon);
                    } else if (MusicPlayerService.sMusicID == null) {
                        MusicPlayerService.playMusic(musicInfo);
                        playButton.setImageResource(R.drawable.my_music_pause_icon);
                        mButtonWeakReference = new WeakReference<>(playButton);
                    } else {
                        MusicPlayerService.playMusic(musicInfo);
                        if (mButtonWeakReference != null) {
                            mButtonWeakReference.get().setImageResource(R.drawable.my_music_play_icon);
                        }
                        playButton.setImageResource(R.drawable.my_music_pause_icon);
                        mButtonWeakReference = new WeakReference<>(playButton);
                    }
                }
            }
        });
    }

    protected static class ViewHolder {
        ImageView icon;
        ImageView playButton;
        TextView songName;
    }
}
