package com.quibbler.sevenmusic.adapter.playbar;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quibbler.sevenmusic.MusicApplication;
import com.quibbler.sevenmusic.R;
import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.service.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Package:        com.quibbler.sevenmusic.adapter.playbar
 * ClassName:      PlayBarMuiscListAdapter
 * Description:    音乐播放条播放列表适配器
 * Author:         11103876
 * CreateDate:     2019/10/12 16:09
 */
public class PlaybarMusicListAdapter extends BaseAdapter {

    /**
     * 最近音乐播放列表集合
     */
    private List<MusicInfo> mPlaybarMusicList = new ArrayList<>();
    /**
     * item布局上删除图标的监听器
     */
    private View.OnClickListener mListener;
    /**
     * 保存当前点击item的id，默认赋值为-1
     */
    private int selectedId = -1;

    public PlaybarMusicListAdapter(View.OnClickListener listener, List<MusicInfo> musicList) {
        mListener = listener;
        mPlaybarMusicList = musicList;
    }

    /**
     * 描述:返回容器中的元素个数
     *
     * @return
     */
    @Override
    public int getCount() {
        return mPlaybarMusicList.size();
    }

    /**
     * 描述：返回容器中指定位置的数据项
     *
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        return mPlaybarMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        MusicInfo musicInfo = (MusicInfo) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(MusicApplication.getContext()).inflate(R.layout.play_bar_music_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.musicName = convertView.findViewById(R.id.play_bar_tv_play_list_music_name);
//            viewHolder.musicSinger = convertView.findViewById(R.id.play_bar_tv_play_list_music_singer);
            viewHolder.musicHorn = convertView.findViewById(R.id.play_bar_iv_horn);
            viewHolder.musicDelete = convertView.findViewById(R.id.play_bar_iv_play_list_single_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.musicName.setText(musicInfo.getMusicSongName());// 设置显示歌曲名字与作者

        MusicInfo currentMusicinfo = MusicPlayerService.getMusicInfo();
        if (selectedId == position || (selectedId == -1 && currentMusicinfo != null && TextUtils.equals(musicInfo.getId(),
                currentMusicinfo.getId()))) { // 点击当前item，设置item字体颜色为红色，并显示播放喇叭图标
            viewHolder.musicName.setTextColor(Color.RED);
//            viewHolder.musicHorn.setBackgroundResource(R.mipmap.play_bar_music_list_horn);
            viewHolder.musicHorn.setVisibility(View.VISIBLE);
            selectedId = position;
        } else { // 点击下一个item时，恢复上一个item字体默认颜色，取消显示播放喇叭图标
            viewHolder.musicName.setTextColor(Color.BLACK);
//            viewHolder.musicHorn.setBackgroundResource(0);
            viewHolder.musicHorn.setVisibility(View.INVISIBLE);
        }
        viewHolder.musicDelete.setOnClickListener(mListener);// 给Item布局上删除图标添加点击监听，具体事件处理在PlayBarMusicDialog类中
        viewHolder.musicDelete.setTag(position); // 通过setTag将被点击控件所在条目的位置传递出去

        return convertView;
    }

    /**
     * 描述：设置ListView中点击选中item的id
     *
     * @param position
     */
    public void setSelectedId(int position) {
        selectedId = position;
    }

    public int getSelectedId() {
        return selectedId;
    }

    class ViewHolder {
        TextView musicName;
        ImageView musicHorn;
        ImageView musicDelete;
    }
}
