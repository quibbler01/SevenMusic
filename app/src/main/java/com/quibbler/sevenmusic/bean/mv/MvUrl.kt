package com.quibbler.sevenmusic.bean.mv;

import com.google.gson.annotations.SerializedName;

/**
  *
  * Package:        com.quibbler.sevenmusic.bean.mv
  * ClassName:      MvUrl
  * Description:    json解析用到
  * Author:         lishijun
  * CreateDate:     2019/10/9 15:55
 */
public class MvUrl{
    @SerializedName("480")
    private String mUrl;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
