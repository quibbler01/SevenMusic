package com.quibbler.sevenmusic.view.song;

import com.quibbler.sevenmusic.bean.song.impl.LrcRow;

import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.view.song
  * ClassName:      ILrcView
  * Description:    歌词view接口
  * Author:         lishijun
  * CreateDate:     2019/9/28 15:11
 */
public interface ILrcView {

    boolean isScrolling();
    /**
     * 设置要展示的歌词行集合
     */
    void setLrc(List<LrcRow> lrcRows);

    /**
     * 音乐播放的时候调用该方法滚动歌词，高亮正在播放的那句歌词，cb表示是不是手动调用
     */
    void seekLrcToTime(long time, boolean cb);

    void seekLrc(int position, boolean cb);
}
