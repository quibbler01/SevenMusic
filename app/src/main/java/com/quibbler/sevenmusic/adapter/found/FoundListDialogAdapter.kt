package com.quibbler.sevenmusic.adapter.found;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.CustomMusicList;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.utils.MusicDatabaseUtils;
import com.quibbler.sevenmusic.utils.MusicThreadPool;
import com.quibbler.sevenmusic.view.found.FoundCustomDialog;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.found
 * ClassName:      FoundListDialogAdapter
 * Description:
 * Author:         yanwuyang
 * CreateDate:     2019/11/13 17:24
 */
public class FoundListDialogAdapter extends RecyclerBaseAdapter<CustomMusicList, FoundListDialogAdapter.DialogViewHolder> implements View.OnClickListener {

    private List<CustomMusicList> mCustomMusicLists = mSourceList;
    private MusicInfo mMusicInfo;
    private FoundCustomDialog mFoundCustomDialog;

    static class DialogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mIvCover;
        TextView mTvName;

        public DialogViewHolder(View view) {
            super(view);
            mView = view;
            mIvCover = view.findViewById(R.id.found_list_item_iv_dialog_cover);
            mTvName = view.findViewById(R.id.found_list_item_tv_dialog_name);
        }
    }

    public FoundListDialogAdapter(List<CustomMusicList> list, MusicInfo musicInfo, FoundCustomDialog foundCustomDialog) {
        super(list);
        mMusicInfo = musicInfo;
        mFoundCustomDialog = foundCustomDialog;
    }

    @NonNull
    @Override
    public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_list_item_costum_dialog, parent, false);
        DialogViewHolder viewHolder = new DialogViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        CustomMusicList customMusicList = mCustomMusicLists.get(position);
        if (customMusicList.getCoverImgUrl() != null) {
            holder.mIvCover.setImageResource(R.drawable.search_online_play_list_item);
        } else {
            Glide.with(holder.mView.getContext())
                    .load(customMusicList.getCoverImgUrl())
                    .placeholder(R.drawable.search_online_play_list_item)
                    .into(holder.mIvCover);
        }
        holder.mTvName.setText(customMusicList.getName());

        holder.mView.setOnClickListener(this);
        holder.mView.setTag(customMusicList.getName());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.found_list_item_ll) {
            String name = (String) v.getTag();
            MusicThreadPool.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MusicDatabaseUtils.addToMusicList(name, mMusicInfo);
                }
            });
            Toast.makeText(v.getContext(), "添加成功", Toast.LENGTH_SHORT).show();

            if (mFoundCustomDialog != null) {
                mFoundCustomDialog.dismiss();
            }
        }
    }
}
