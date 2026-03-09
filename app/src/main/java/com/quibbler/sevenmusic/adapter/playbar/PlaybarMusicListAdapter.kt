package com.quibbler.sevenmusic.adapter.playbar

import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.quibbler.sevenmusic.MusicApplication
import com.quibbler.sevenmusic.R
import com.quibbler.sevenmusic.bean.MusicInfo
import com.quibbler.sevenmusic.service.MusicPlayerService

/**
 * Package:        com.quibbler.sevenmusic.adapter.playbar
 * ClassName:      PlayBarMuiscListAdapter
 * Description:    音乐播放条播放列表适配器
 * Author:         11103876
 * CreateDate:     2019/10/12 16:09
 */
class PlaybarMusicListAdapter(listener: View.OnClickListener?, musicList: MutableList<MusicInfo?>) :
    BaseAdapter() {
    /**
     * 最近音乐播放列表集合
     */
    private var mPlaybarMusicList: MutableList<MusicInfo?> = ArrayList<MusicInfo?>()

    /**
     * item布局上删除图标的监听器
     */
    private val mListener: View.OnClickListener?
    /**
     * 描述：设置ListView中点击选中item的id
     * 
     * @param position
     */
    /**
     * 保存当前点击item的id，默认赋值为-1
     */
    var selectedId: Int = -1

    init {
        mListener = listener
        mPlaybarMusicList = musicList
    }

    /**
     * 描述:返回容器中的元素个数
     * 
     * @return
     */
    override fun getCount(): Int {
        return mPlaybarMusicList.size
    }

    /**
     * 描述：返回容器中指定位置的数据项
     * 
     * @param position
     * @return
     */
    override fun getItem(position: Int): Any? {
        return mPlaybarMusicList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val musicInfo = getItem(position) as MusicInfo

        if (convertView == null) {
            convertView = LayoutInflater.from(MusicApplication.Companion.getContext())
                .inflate(R.layout.play_bar_music_list_item, null)
            viewHolder = PlaybarMusicListAdapter.ViewHolder()
            viewHolder.musicName =
                convertView.findViewById<TextView>(R.id.play_bar_tv_play_list_music_name)
            //            viewHolder.musicSinger = convertView.findViewById(R.id.play_bar_tv_play_list_music_singer);
            viewHolder.musicHorn = convertView.findViewById<ImageView>(R.id.play_bar_iv_horn)
            viewHolder.musicDelete =
                convertView.findViewById<ImageView>(R.id.play_bar_iv_play_list_single_delete)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder
        }
        viewHolder.musicName!!.setText(musicInfo.getMusicSongName()) // 设置显示歌曲名字与作者

        val currentMusicinfo: MusicInfo? = MusicPlayerService.Companion.getMusicInfo()
        if (selectedId == position || (selectedId == -1 && currentMusicinfo != null && TextUtils.equals(
                musicInfo.getId(),
                currentMusicinfo.getId()
            ))
        ) { // 点击当前item，设置item字体颜色为红色，并显示播放喇叭图标
            viewHolder.musicName!!.setTextColor(Color.RED)
            //            viewHolder.musicHorn.setBackgroundResource(R.mipmap.play_bar_music_list_horn);
            viewHolder.musicHorn!!.setVisibility(View.VISIBLE)
            selectedId = position
        } else { // 点击下一个item时，恢复上一个item字体默认颜色，取消显示播放喇叭图标
            viewHolder.musicName!!.setTextColor(Color.BLACK)
            //            viewHolder.musicHorn.setBackgroundResource(0);
            viewHolder.musicHorn!!.setVisibility(View.INVISIBLE)
        }
        viewHolder.musicDelete!!.setOnClickListener(mListener) // 给Item布局上删除图标添加点击监听，具体事件处理在PlayBarMusicDialog类中
        viewHolder.musicDelete!!.setTag(position) // 通过setTag将被点击控件所在条目的位置传递出去

        return convertView
    }

    internal inner class ViewHolder {
        var musicName: TextView? = null
        var musicHorn: ImageView? = null
        var musicDelete: ImageView? = null
    }
}
