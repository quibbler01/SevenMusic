package com.quibbler.sevenmusic.comparator

import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo
import okhttp3.Response.code
import java.util.Locale

class PinyinComparator : Comparator<FoundSingerInfo?> {
    override fun compare(leftSinger: FoundSingerInfo?, rightSinger: FoundSingerInfo?): Int {
        return sort(leftSinger, rightSinger)
    }

    private fun sort(leftSinger: FoundSingerInfo?, rightSinger: FoundSingerInfo?): Int {
        if (leftSinger == null) {
            if (rightSinger == null) {
                return 0
            }
            return -1
        }
        if (rightSinger == null) {
            return 1
        }

        if (leftSinger.getFirstPinyin() == null) {
            if (rightSinger.getFirstPinyin() == null) {
                return 0
            }
            return -1
        }
        if (rightSinger.getFirstPinyin() == null) {
            return 1
        }

        // 获取ascii值
        val leftAscii: Int = leftSinger.getFirstPinyin().uppercase(Locale.getDefault()).get(0).code
        val rightAscii: Int =
            rightSinger.getFirstPinyin().uppercase(Locale.getDefault()).get(0).code
        // 判断若不是字母，则排在字母之后
        if (leftAscii < 65 || leftAscii > 90) return 1
        else if (rightAscii < 65 || rightAscii > 90) return -1
        else return leftSinger.getFullPinyin().compareTo(rightSinger.getFullPinyin())
    }
}
