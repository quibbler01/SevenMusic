package com.quibbler.sevenmusic.adapter.my;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MyCollectionsInfo;
import com.quibbler.sevenmusic.listener.MyCollectionViewListener;

import java.util.List;

import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_IMAGE;
import static com.quibbler.sevenmusic.Constant.SEVEN_MUSIC_SINGER;
import static com.quibbler.sevenmusic.contentprovider.MusicContentProvider.MUSIC_AUTHORITY;

/**
 * Package:        com.quibbler.sevenmusic.adapter
 * ClassName:      MyCollectionAdapter
 * Description:    我的收藏适配器:三用，同时兼备歌曲，歌手，专辑三种类型数据的适配器
 * Author:         11103905
 * CreateDate:     2019/9/20 10:31
 */
public class MyCollectionAdapter extends ArrayAdapter<MyCollectionsInfo> {
    private int mCollectionKind;
    private int mResourceID;
    private Context mContext;
    private List<MyCollectionsInfo> myCollectionsInfoList;
    private MyCollectionViewListener myCollectionViewListener;

    public MyCollectionAdapter(@NonNull Context context, int resource, @NonNull List<MyCollectionsInfo> myCollectionsInfoList, int kind, MyCollectionViewListener listener) {
        super(context, resource, myCollectionsInfoList);
        this.mContext = context;
        this.mCollectionKind = kind;
        this.mResourceID = resource;
        this.myCollectionsInfoList = myCollectionsInfoList;
        this.myCollectionViewListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MyCollectionsInfo myCollectionsInfo = myCollectionsInfoList.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResourceID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.my_collection_head_icon);
            viewHolder.title = convertView.findViewById(R.id.my_collection_title_text);
            viewHolder.description = convertView.findViewById(R.id.my_collection_description_text);
            viewHolder.start = convertView.findViewById(R.id.my_collection_button_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (mCollectionKind) {
            case 0:
//                File songCache = new File(SEVEN_MUSIC_IMAGE + "/" + myCollectionsInfo.getTitle().hashCode());
//                if (songCache.exists()) {
                Glide.with(mContext).load(SEVEN_MUSIC_IMAGE + "/" + myCollectionsInfo.getId()).placeholder(R.drawable.music_item_icon).into(viewHolder.icon);
//                } else {
//                    GetMusicImage.with(mContext).setID(Integer.toString(myCollectionsInfo.getId())).placeHolder(R.drawable.my_collection_song_icon_item).icon(viewHolder.icon);
//                }
                break;
            case 1:
//                File singleCache = new File(SEVEN_MUSIC_SINGER + "/" + myCollectionsInfo.getTitle());
//                if (singleCache.exists()) {
                Glide.with(mContext).load(SEVEN_MUSIC_SINGER + "/" + myCollectionsInfo.getTitle()).placeholder(R.drawable.my_collection_singer_icon_item).into(viewHolder.icon);
//                } else {
//                    LoadSingerThumbnailAsyncTask asyncTask = new LoadSingerThumbnailAsyncTask(viewHolder.icon);
//                    asyncTask.execute(myCollectionsInfo.getTitle());
//                }
                break;
            case 2:
                viewHolder.icon.setImageResource(R.drawable.my_collection_album_icon_item);
                break;
        }

        viewHolder.title.setText(myCollectionsInfo.getTitle());

        viewHolder.description.setText(myCollectionsInfo.getDescription());

        ImageView star = viewHolder.start;
        viewHolder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star.setImageResource(R.drawable.my_collection_un_star);
                Uri collectionUrl = Uri.parse("content://" + MUSIC_AUTHORITY + "/collection/" + myCollectionsInfo.getId());
                MusicApplication.getContext().getContentResolver().delete(collectionUrl, null, null);
                myCollectionsInfoList.remove(myCollectionsInfo);
                myCollectionViewListener.removeData(myCollectionsInfo.getId());
                notifyDataSetChanged();
                if (myCollectionsInfoList.size() == 0) {
                    myCollectionViewListener.changeView();
                }
                Snackbar.make(v, "移除收藏", Snackbar.LENGTH_SHORT).setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues values = new ContentValues();
                        values.put("id", myCollectionsInfo.getId());
                        values.put("title", myCollectionsInfo.getTitle());
                        values.put("description", myCollectionsInfo.getDescription());
                        values.put("kind", myCollectionsInfo.getKind());
                        MusicApplication.getContext().getContentResolver().insert(collectionUrl, values);
                        if (myCollectionsInfoList.size() == 0) {
                            myCollectionViewListener.changeView();
                        }
                        myCollectionsInfoList.add(myCollectionsInfo);
                        notifyDataSetChanged();
                    }
                }).show();
            }
        });
        return convertView;
    }

    public void updateData(List<MyCollectionsInfo> list) {
        myCollectionsInfoList.clear();
        myCollectionsInfoList.addAll(list);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;
        ImageView start;
    }
}
