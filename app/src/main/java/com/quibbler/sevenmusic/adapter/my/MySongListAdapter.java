package com.quibbler.sevenmusic.adapter.my;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.my.MySongListManagerActivity;
import com.quibbler.sevenmusic.bean.MySongListInfo;
import com.quibbler.sevenmusic.contentprovider.MusicContentProvider;
import com.quibbler.sevenmusic.utils.MusicThreadPool;

import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MySongListAdapter
 * Description:    歌单数据适配器
 * Author:         zhaopeng
 * CreateDate:     2019/9/20 16:49
 */
public class MySongListAdapter extends ArrayAdapter<MySongListInfo> {
    private static final String TAG = "MySongListAdapter";

    private Context mContext;
    private int mResourceID;
    private List<MySongListInfo> mSongsList;
    private boolean mMode = false;
    private MySongListManagerActivity.ChangeViewCallBack mCallBack;

    public MySongListAdapter(@NonNull Context context, int resource, @NonNull List<MySongListInfo> objects) {
        super(context, resource, objects);
        this.mResourceID = resource;
        this.mContext = context;
        this.mSongsList = objects;
    }

    public MySongListAdapter(@NonNull Context context, int resource, @NonNull List<MySongListInfo> objects, boolean deleteMode) {
        super(context, resource, objects);
        this.mResourceID = resource;
        this.mContext = context;
        this.mSongsList = objects;
        this.mMode = deleteMode;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MySongListInfo mySongListInfo = mSongsList.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResourceID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_music_lists_item_icon);
            viewHolder.name = convertView.findViewById(R.id.my_song_list_item_name_text);
            viewHolder.number = convertView.findViewById(R.id.my_song_list_song_number_hint);
            viewHolder.detail = convertView.findViewById(R.id.my_song_list_item_detail);
            viewHolder.delete = convertView.findViewById(R.id.my_song_list_item_delete_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Glide.with(mContext).load(mySongListInfo.getImageUrl()).placeholder(R.drawable.search_online_play_list_item)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(viewHolder.icon);

        if (mMode) {
            Log.d(TAG, "deleteMode");
            viewHolder.delete.setVisibility(View.VISIBLE);
        }

        viewHolder.name.setText(mySongListInfo.getListName());
        viewHolder.number.setText(mySongListInfo.getNumber() + "首");
        if (mySongListInfo.getType() == 0) {
            viewHolder.detail.setVisibility(View.GONE);
        } else {
            viewHolder.detail.setText(mySongListInfo.getDescription());
            viewHolder.detail.setSelected(true);
        }

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongsList.remove(mySongListInfo);
                notifyDataSetChanged();
                MusicThreadPool.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        MusicApplication.getContext().getContentResolver().delete(MusicContentProvider.SONGLIST_URL, "name = ?", new String[]{mySongListInfo.getListName()});
                    }
                });
                if (mSongsList.size() == 0) {
                    if (mCallBack != null) {
                        mCallBack.hideList();
                    }
                }
            }
        });
        return convertView;
    }

    public void update(List<MySongListInfo> songListInfos) {
        this.mSongsList.clear();
        this.mSongsList.addAll(songListInfos);
        notifyDataSetChanged();
    }

    public void setCallBack(MySongListManagerActivity.ChangeViewCallBack callBack) {
        this.mCallBack = callBack;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView number;
        public TextView detail;
        public ImageView delete;
    }
}
