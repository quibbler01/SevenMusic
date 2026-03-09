package com.quibbler.sevenmusic.adapter.search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.activity.found.PlaylistActivity
import com.quibbler.sevenmusic.activity.found.SingerActivity
import com.quibbler.sevenmusic.activity.mv.ActivityStart
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.bean.mv.MvInfo
import com.quibbler.sevenmusic.bean.search.SearchAlbumBean.Album
import com.quibbler.sevenmusic.bean.search.SearchArtistsBean
import com.quibbler.sevenmusic.bean.search.SearchBean
import com.quibbler.sevenmusic.bean.search.SearchMvBean.Mv
import com.quibbler.sevenmusic.bean.search.SearchPlayListBean.PlayList
import com.quibbler.sevenmusic.bean.search.SearchSongBean
import com.quibbler.sevenmusic.service.MusicPlayerService

/**
 * Package:        com.quibbler.sevenmusic.adapter.search
 * ClassName:      SearchResultAdapter
 * Description:     搜索，RecyclerView 多Item布局：歌曲，歌手，专辑，歌单，视频等全部显示.点击跳转到对应的界面，实现播放、展示功能
 * Author:         zhaopeng
 * CreateDate:     2019/10/8 9:35
 */
class SearchResultAdapter : RecyclerView.Adapter<Any?> {
    private var mContext: Context? = null
    private var mResultList: MutableList<SearchBean?>? = null
    private val mSearchKind: MutableList<Int?> = ArrayList<Int?>(5)

    @Deprecated("")
    constructor()

    constructor(context: Context, results: MutableList<SearchBean?>) {
        this.mContext = context
        this.mResultList = results
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            TYPE_ALBUM -> {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.search_online_album_item, parent, false)
                return AlbumViewHolder(view)
            }

            TYPE_SINGER -> {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.search_online_singer, parent, false)
                return SearchResultAdapter.SingerViewHolder(view)
            }

            TYPE_PLAY_LIST -> {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.search_online_playlist, parent, false)
                return PlayListViewHolder(view)
            }

            TYPE_MV -> {
                view =
                    LayoutInflater.from(mContext).inflate(R.layout.search_online_mv, parent, false)
                return MvViewHolder(view)
            }

            TYPE_SONG -> {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.search_online_song_item, parent, false)
                return SongViewHolder(view)
            }

            else -> {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.search_online_song_item, parent, false)
                return SongViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.getItemViewType()) {
            TYPE_ALBUM -> {
                val albumViewHolder: AlbumViewHolder = holder as AlbumViewHolder
                val album = mResultList!!.get(position) as Album
                Glide.with(mContext!!).load(album.getBlurPicUrl())
                    .placeholder(R.drawable.search_online_albumt).into(albumViewHolder.albumIcon)
                albumViewHolder.albumName.setText(album.getName())
                albumViewHolder.ablumDetail.setText(album.getArtist().getName())
                albumViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val intent = Intent(mContext, PlaylistActivity::class.java)
                        intent.putExtra(
                            mContext!!.getResources().getString(R.string.playlist_id),
                            album.getId()
                        )
                        mContext!!.startActivity(intent)
                    }
                })
            }

            TYPE_SINGER -> {
                val singerViewHolder = holder as SingerViewHolder
                val artist = mResultList!!.get(position) as SearchArtistsBean.Artist
                Glide.with(mContext!!).load(artist.getPicUrl())
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.my_musician_list_item).into(singerViewHolder.singerHead)
                singerViewHolder.singerName.setText(artist.getName())
                singerViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val intent = Intent(mContext, SingerActivity::class.java)
                        intent.putExtra("id", artist.getId())
                        mContext!!.startActivity(intent)
                    }
                })
            }

            TYPE_PLAY_LIST -> {
                val playListViewHolder: PlayListViewHolder = holder as PlayListViewHolder
                val playList = mResultList!!.get(position) as PlayList
                Glide.with(mContext!!).load(playList.getCoverImgUrl())
                    .placeholder(R.drawable.search_online_play_list_item)
                    .into(playListViewHolder.playListImageView)
                playListViewHolder.name.setText(playList.getName())
                playListViewHolder.detail.setText("累计播放:" + playList.getPlayCount())
                playListViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val intent = Intent(mContext, PlaylistActivity::class.java)
                        intent.putExtra(
                            mContext!!.getResources().getString(R.string.playlist_id),
                            playList.getId()
                        )
                        mContext!!.startActivity(intent)
                    }
                })
            }

            TYPE_MV -> {
                val mvViewHolder: MvViewHolder = holder as MvViewHolder
                val mv = mResultList!!.get(position) as Mv
                Glide.with(mContext!!).load(mv.getCover())
                    .placeholder(R.drawable.search_online_mv_icon).into(mvViewHolder.imageView)
                mvViewHolder.name.setText(mv.getName())
                mvViewHolder.detail.setText(mv.getArtistName())
                mvViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val mvInfo =
                            MvInfo(mv.getId().toInt(), mv.getName(), null, 0, null, mv.getCover())
                        ActivityStart.startMvPlayActivity(mContext, mvInfo)
                    }
                })
            }

            TYPE_SONG -> {
                val songViewHolder: SongViewHolder = holder as SongViewHolder
                val song = mResultList!!.get(position) as SearchSongBean.Song
                songViewHolder.songName.setText(song.getName())
                if (song.getArtists().size != 0) {
                    songViewHolder.songSinger.setText(song.getArtists().get(0).getName())
                }
                songViewHolder.itemView.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val musicInfo = MusicInfo()
                        musicInfo.setId(song.getId())
                        musicInfo.setMusicSongName(song.getName())
                        if (song.getArtists().size != 0) {
                            musicInfo.setSinger(song.getArtists().get(0).getName())
                        }
                        MusicPlayerService.Companion.playMusic(musicInfo)
                    }
                })
            }

            else -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mSearchKind.size == 0) {
            return -1
        }
        if (position >= 0 && position < mSearchKind.get(0)!!) {
            return TYPE_SONG
        } else if (position < mSearchKind.get(0)!! + mSearchKind.get(1)!!) {
            return TYPE_PLAY_LIST
        } else if (position < mSearchKind.get(0)!! + mSearchKind.get(1)!! + mSearchKind.get(2)!!) {
            return TYPE_SINGER
        } else if (position < mSearchKind.get(0)!! + mSearchKind.get(1)!! + mSearchKind.get(2)!! + mSearchKind.get(
                3
            )!!
        ) {
            return TYPE_ALBUM
        } else if (position < mSearchKind.get(0)!! + mSearchKind.get(1)!! + mSearchKind.get(2)!! + mSearchKind.get(
                3
            )!! + mSearchKind.get(4)!!
        ) {
            return TYPE_MV
        } else {
            return -1
        }
    }

    override fun getItemCount(): Int {
        return mResultList!!.size
    }

    fun updateDataSet(searchBeans: MutableList<SearchBean?>) {
        mResultList!!.clear()
        mResultList!!.addAll(searchBeans)
        notifyDataSetChanged()
    }

    fun updateDataSet(searchBeans: MutableList<SearchBean?>, integers: MutableList<Int?>) {
        mSearchKind.clear()
        mSearchKind.addAll(integers)

        mResultList!!.clear()
        mResultList!!.addAll(searchBeans)

        notifyDataSetChanged()
    }

    fun clearAll() {
        mSearchKind.clear()
        notifyDataSetChanged()
    }

    private inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songName: TextView
        var songSinger: TextView

        init {
            songName = itemView.findViewById<TextView>(R.id.search_online_song_name)
            songSinger = itemView.findViewById<TextView>(R.id.search_online_song_singer_name)
        }
    }

    private inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumIcon: ImageView
        var albumName: TextView
        var ablumDetail: TextView

        init {
            albumIcon = itemView.findViewById<ImageView>(R.id.search_online_album_icon)
            albumName = itemView.findViewById<TextView>(R.id.search_online_album_name)
            ablumDetail = itemView.findViewById<TextView>(R.id.search_online_album_info)
        }
    }

    private inner class SingerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var singerHead: ImageView
        var singerName: TextView

        init {
            singerHead = itemView.findViewById<ImageView>(R.id.search_online_singer_icon)
            singerName = itemView.findViewById<TextView>(R.id.search_online_singer_name)
        }
    }


    private inner class PlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var playListImageView: ImageView
        var name: TextView
        var detail: TextView

        init {
            playListImageView = itemView.findViewById<ImageView>(R.id.search_online_play_list_icon)
            name = itemView.findViewById<TextView>(R.id.search_online_play_list_name)
            detail = itemView.findViewById<TextView>(R.id.search_online_play_list_detail)
        }
    }

    private inner class MvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var name: TextView
        var detail: TextView

        init {
            imageView = itemView.findViewById<ImageView>(R.id.search_online_mv_icon)
            name = itemView.findViewById<TextView>(R.id.search_online_mv_name)
            detail = itemView.findViewById<TextView>(R.id.search_online_mv_info)
        }
    }

    companion object {
        const val TYPE_SONG: Int = 0
        const val TYPE_ALBUM: Int = 1
        const val TYPE_SINGER: Int = 2
        const val TYPE_PLAY_LIST: Int = 3
        const val TYPE_MV: Int = 4
    }
}
