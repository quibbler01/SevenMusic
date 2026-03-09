package com.quibbler.sevenmusic.utils;

import java.io.IOException;

import okhttp3.Call;

/**
  *
  * Package:        com.quibbler.sevenmusic.util
  * ClassName:      ICallback
  * Description:    自己定义callback类，处理返回的response字符串
  * Author:         lishijun
  * CreateDate:     2019/9/17 17:46
 */
public interface ICallback {

    void onResponse(String responseText);

    //void onFailure(Call call, IOException e);

    void onFailure();
}
