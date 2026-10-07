package com.quibbler.sevenmusic.bean.jsonbean.found

import android.text.TextUtils
import com.quibbler.sevenmusic.utils.PinyinUtils
import java.util.Locale
import android.util.Log

class FoundSingerInfo {
    private val name: String? = null
    val picUrl: String? = null
    val id: String? = null

    private var mFirstPinyin: String? = null
    private var mFullPinyin: String? = null

    fun getName(): String {
        Log.d("FoundSingerInfo", "getName() called")
        return name!!
    }

    val firstPinyin: String?
        get() {
            if (TextUtils.isEmpty(name)) {
                return name
            }
            if (TextUtils.isEmpty(mFullPinyin)) {
                mFirstPinyin = this.fullPinyin.get(0).toString()
            } else if (TextUtils.isEmpty(mFirstPinyin)) {
                mFirstPinyin = mFullPinyin!!.get(0).toString()
            }
            return mFirstPinyin
        }

    val fullPinyin: String
        get() {
            if (TextUtils.isEmpty(name)) {
                return name!!
            }
            if (TextUtils.isEmpty(mFullPinyin)) {
                mFullPinyin = PinyinUtils.getPingYin(name).lowercase(Locale.getDefault())
            }
            return mFullPinyin!!
        }
}
