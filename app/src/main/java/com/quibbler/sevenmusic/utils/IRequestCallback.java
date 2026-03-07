package com.quibbler.sevenmusic.utils;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Response;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      IRequestCallback
  * Description:    自定义callback接口，处理返回的response
  * Author:         yanwuyang
  * CreateDate:     2019/10/17 20:16
 */
public interface IRequestCallback {

    void onResponse(Bitmap bitmap);

    void onFailure(Call call, IOException e);
}
