package com.quibbler.sevenmusic.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
  *
  * Package:        com.quibbler.sevenmusic.utils
  * ClassName:      PinyinUtils
  * Description:    汉字转拼音工具类
  * Author:         yanwuyang
  * CreateDate:     2019/10/30 10:41
 */
public class PinyinUtils {
    /**
     * 输入汉字，输出拼音
     * @param inputChineseStr 汉字
     * @return
     */
    public static String getPingYin(String inputChineseStr) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        char[] input = inputChineseStr.trim().toCharArray();
        String output = "";
        try {
            for (char ch : input) {
                if (java.lang.Character.toString(ch).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    output += temp[0];
                } else
                    output += java.lang.Character.toString(ch);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }
}
