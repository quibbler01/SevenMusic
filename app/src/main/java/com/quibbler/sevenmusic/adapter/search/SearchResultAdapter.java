package com.quibbler.sevenmusic.adapter.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.activity.found.PlaylistActivity;
import com.quibbler.sevenmusic.activity.found.SingerActivity;
import com.quibbler.sevenmusic.activity.mv.ActivityStart;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.MvInfo;
import com.quibbler.sevenmusic.bean.search.SearchAlbumBean;
import com.quibbler.sevenmusic.bean.search.SearchArtistsBean;
import com.quibbler.sevenmusic.bean.search.SearchBean;
import com.quibbler.sevenmusic.bean.search.SearchMvBean;
import com.quibbler.sevenmusic.bean.search.SearchPlayListBean;
import com.quibbler.sevenmusic.bean.search.SearchSongBean;
import com.quibbler.sevenmusic.service.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchResultAdapter
 * Description:     搜索，RecyclerView 多Item布局：歌曲，歌手，专辑，歌单，视频等全部显示.点击跳转到对应的界面，实现播放、展示功能
 * Author:         zhaopeng
 * CreateDate:     2019/10/8 9:35
 */
public class SearchResultAdapter extends RecyclerView.Adapter {
    public static final int TYPE_SONG = 0;
    public static final int TYPE_ALBUM = 1;
    public static final int TYPE_SINGER = 2;
    public static final int TYPE_PLAY_LIST = 3;
    public static final int TYPE_MV = 4;

    private Context mContext;
    private List<SearchBean> mResultList;
    private List<Integer> mSearchKind = new ArrayList<>(5);

    @Deprecated
    public SearchResultAdapter() {

    }

    public SearchResultAdapter(Context context, List<SearchBean> results) {
        this.mContext = context;
        this.mResultList = results;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_ALBUM:
                view = LayoutInflater.from(mContext).inflate(R.layout.search_online_album_item, parent, false);
                return new AlbumViewHolder(view);
            case TYPE_SINGER:
                view = LayoutInflater.from(mContext).inflate(R.layout.search_online_singer, parent, false);
                return new SingerViewHolder(view);
            case TYPE_PLAY_LIST:
                view = LayoutInflater.from(mContext).inflate(R.layout.search_online_playlist, parent, false);
                return new PlayListViewHolder(view);
            case TYPE_MV:
                view = LayoutInflater.from(mContext).inflate(R.layout.search_online_mv, parent, false);
                return new MvViewHolder(view);
            case TYPE_SONG:
            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.search_online_song_item, parent, false);
                return new SongViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_ALBUM:
                AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
                SearchAlbumBean.Album album = (SearchAlbumBean.Album) mResultList.get(position);
                Glide.with(mContext).load(album.getBlurPicUrl()).placeholder(R.drawable.search_online_albumt).into(albumViewHolder.albumIcon);
                albumViewHolder.albumName.setText(album.getName());
                albumViewHolder.ablumDetail.setText(album.getArtist().getName());
                albumViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlaylistActivity.class);
                        intent.putExtra(mContext.getResources().getString(R.string.playlist_id), album.getId());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case TYPE_SINGER:
                SingerViewHolder singerViewHolder = (SingerViewHolder) holder;
                SearchArtistsBean.Artist artist = (SearchArtistsBean.Artist) mResultList.get(position);
                Glide.with(mContext).load(artist.getPicUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop())).placeholder(R.drawable.my_musician_list_item).into(singerViewHolder.singerHead);
                singerViewHolder.singerName.setText(artist.getName());
                singerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SingerActivity.class);
                        intent.putExtra("id", artist.getId());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case TYPE_PLAY_LIST:
                PlayListViewHolder playListViewHolder = (PlayListViewHolder) holder;
                SearchPlayListBean.PlayList playList = (SearchPlayListBean.PlayList) mResultList.get(position);
                Glide.with(mContext).load(playList.getCoverImgUrl()).placeholder(R.drawable.search_online_play_list_item).into(playListViewHolder.playListImageView);
                playListViewHolder.name.setText(playList.getName());
                playListViewHolder.detail.setText("累计播放:" + playList.getPlayCount());
                playListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlaylistActivity.class);
                        intent.putExtra(mContext.getResources().getString(R.string.playlist_id), playList.getId());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case TYPE_MV:
                MvViewHolder mvViewHolder = (MvViewHolder) holder;
                SearchMvBean.Mv mv = (SearchMvBean.Mv) mResultList.get(position);
                Glide.with(mContext).load(mv.getCover()).placeholder(R.drawable.search_online_mv_icon).into(mvViewHolder.imageView);
                mvViewHolder.name.setText(mv.getName());
                mvViewHolder.detail.setText(mv.getArtistName());
                mvViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MvInfo mvInfo = new MvInfo(Integer.valueOf(mv.getId()), mv.getName(), null, 0, null, mv.getCover());
                        ActivityStart.startMvPlayActivity(mContext, mvInfo);
                    }
                });
                break;
            case TYPE_SONG:
                SongViewHolder songViewHolder = (SongViewHolder) holder;
                SearchSongBean.Song song = (SearchSongBean.Song) mResultList.get(position);
                songViewHolder.songName.setText(song.getName());
                if (song.getArtists().size() != 0) {
                    songViewHolder.songSinger.setText(song.getArtists().get(0).getName());
                }
                songViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setId(song.getId());
                        musicInfo.setMusicSongName(song.getName());
                        if (song.getArtists().size() != 0) {
                            musicInfo.setSinger(song.getArtists().get(0).getName());
                        }
                        MusicPlayerService.playMusic(musicInfo);
                    }
                });
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mSearchKind.size() == 0) {
            return -1;
        }
        if (position >= 0 && position < mSearchKind.get(0)) {
            return TYPE_SONG;
        } else if (position < mSearchKind.get(0) + mSearchKind.get(1)) {
            return TYPE_PLAY_LIST;
        } else if (position < mSearchKind.get(0) + mSearchKind.get(1) + mSearchKind.get(2)) {
            return TYPE_SINGER;
        } else if (position < mSearchKind.get(0) + mSearchKind.get(1) + mSearchKind.get(2) + mSearchKind.get(3)) {
            return TYPE_ALBUM;
        } else if (position < mSearchKind.get(0) + mSearchKind.get(1) + mSearchKind.get(2) + mSearchKind.get(3) + mSearchKind.get(4)) {
            return TYPE_MV;
        } else {
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    public void updateDataSet(List<SearchBean> searchBeans) {
        mResultList.clear();
        mResultList.addAll(searchBeans);
        notifyDataSetChanged();
    }

    public void updateDataSet(List<SearchBean> searchBeans, List<Integer> integers) {
        mSearchKind.clear();
        mSearchKind.addAll(integers);

        mResultList.clear();
        mResultList.addAll(searchBeans);

        notifyDataSetChanged();
    }

    public void clearAll() {
        mSearchKind.clear();
        notifyDataSetChanged();
    }

    private class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView songName;
        public TextView songSinger;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.search_online_song_name);
            songSinger = itemView.findViewById(R.id.search_online_song_singer_name);
        }
    }

    private class AlbumViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumIcon;
        public TextView albumName;
        public TextView ablumDetail;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumIcon = itemView.findViewById(R.id.search_online_album_icon);
            albumName = itemView.findViewById(R.id.search_online_album_name);
            ablumDetail = itemView.findViewById(R.id.search_online_album_info);
        }
    }

    private class SingerViewHolder extends RecyclerView.ViewHolder {
        public ImageView singerHead;
        public TextView singerName;

        public SingerViewHolder(@NonNull View itemView) {
            super(itemView);
            singerHead = itemView.findViewById(R.id.search_online_singer_icon);
            singerName = itemView.findViewById(R.id.search_online_singer_name);

        }
    }


    private class PlayListViewHolder extends RecyclerView.ViewHolder {
        public ImageView playListImageView;
        public TextView name;
        public TextView detail;

        public PlayListViewHolder(@NonNull View itemView) {
            super(itemView);
            playListImageView = itemView.findViewById(R.id.search_online_play_list_icon);
            name = itemView.findViewById(R.id.search_online_play_list_name);
            detail = itemView.findViewById(R.id.search_online_play_list_detail);
        }
    }

    private class MvViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name;
        public TextView detail;

        public MvViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.search_online_mv_icon);
            name = itemView.findViewById(R.id.search_online_mv_name);
            detail = itemView.findViewById(R.id.search_online_mv_info);
        }
    }
}
