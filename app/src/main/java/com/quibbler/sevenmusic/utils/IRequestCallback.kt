package com.quibbler.sevenmusic.utils

import android.graphics.Bitmap
import okhttp3.Call
import java.io.IOException

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      IRequestCallback
 * Description:    自定义callback接口，处理返回的response
 * Author:         yanwuyang
 * CreateDate:     2019/10/17 20:16
 */
interface IRequestCallback {
    fun onResponse(bitmap: Bitmap?)

    fun onFailure(call: Call?, e: IOException?)
}
