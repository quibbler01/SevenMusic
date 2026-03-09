package com.quibbler.sevenmusic.bean.song;

import com.quibbler.sevenmusic.bean.song.impl.LrcRow;

import java.util.List;

/**
  * 
  * Package:        com.quibbler.sevenmusic.bean.song
  * ClassName:      ILrcBuilder     
  * Description:    解析歌词builder接口
  * Author:         lishijun
  * CreateDate:     2019/9/28 14:50  
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}
