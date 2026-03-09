package com.quibbler.sevenmusic.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

/**
 * 
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      PinyinUtils
 * Description:    汉字转拼音工具类
 * Author:         yanwuyang
 * CreateDate:     2019/10/30 10:41
 */
object PinyinUtils {
    /**
     * 输入汉字，输出拼音
     * @param inputChineseStr 汉字
     * @return
     */
    fun getPingYin(inputChineseStr: String): String {
        val format = HanyuPinyinOutputFormat()
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE)
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE)
        format.setVCharType(HanyuPinyinVCharType.WITH_V)
        val input = inputChineseStr.trim { it <= ' ' }.toCharArray()
        var output = ""
        try {
            for (ch in input) {
                if (ch.toString().matches("[\\u4E00-\\u9FA5]+".toRegex())) {
                    val temp = PinyinHelper.toHanyuPinyinStringArray(ch, format)
                    output += temp[0]
                } else output += ch.toString()
            }
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            e.printStackTrace()
        }
        return output
    }
}
