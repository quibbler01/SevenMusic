package com.quibbler.sevenmusic.bean.jsonbean.found;

import com.quibbler.sevenmusic.bean.MusicInfo;
import com.quibbler.sevenmusic.bean.mv.MvMusicInfo;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.jsonbean.found
  * ClassName:      FoundTopCarouselBean
  * Description:    轮播内容解析的bean
  * Author:         yanwuyang
  * CreateDate:     2019/10/10 16:48
 */
public class FoundTopCarouselBean {
    //banner图的url地址
    private String pic;
    //bean的type为1即歌曲时，targetId为歌曲id
    private String targetId;
    //type为1表示是歌曲
    private String targetType;
    private String typeTitle;
    private MusicInfo song;

    public String getPic() {
        return pic;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public MusicInfo getSong() {
        return song;
    }
}
