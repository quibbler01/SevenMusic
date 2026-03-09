package com.quibbler.sevenmusic.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.quibbler.sevenmusic.MusicApplication

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      GetInputMethodManager
 * Description:    输入法管理工具类,关闭输入法
 * Author:         zhaopeng
 * CreateDate:     2019/9/17 15:54
 */
object GetInputMethodManager {
    val inputMethodManager: InputMethodManager?
        get() = MusicApplication.Companion.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
}
