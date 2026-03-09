package com.quibbler.sevenmusic.utils

/**
 * 
 * Package:        com.quibbler.sevenmusic.util
 * ClassName:      ICallback
 * Description:    自己定义callback类，处理返回的response字符串
 * Author:         lishijun
 * CreateDate:     2019/9/17 17:46
 */
interface ICallback {
    fun onResponse(responseText: String?)

    //void onFailure(Call call, IOException e);
    fun onFailure()
}
