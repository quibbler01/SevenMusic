package com.quibbler.sevenmusic.bean.jsonbean.found;

import com.quibbler.sevenmusic.bean.mv.MvInfo;

import java.util.List;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.jsonbean
  * ClassName:      FoundTopMvResponseBean
  * Description:    网络请求“发现”页面中推荐mv视频，返回的responseJson解析得到的Bean
  * Author:         yanwuyang
  * CreateDate:     2019/9/18 20:41
 */
public class FoundTopMvResponseBean {
    //返回的mv视频实体
    private List<MvInfo> data;
    //返回码
    private int code;

    public List<MvInfo> getData() {
        return data;
    }

    public int getCode() {
        return code;
    }
}
