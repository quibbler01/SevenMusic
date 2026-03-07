package com.quibbler.sevenmusic.comparator;

import com.quibbler.sevenmusic.bean.jsonbean.found.FoundSingerInfo;

import java.util.Comparator;

public class PinyinComparator implements Comparator<FoundSingerInfo> {
    @Override
    public int compare(FoundSingerInfo leftSinger, FoundSingerInfo rightSinger) {
        return sort(leftSinger, rightSinger);
    }

    private int sort(FoundSingerInfo leftSinger, FoundSingerInfo rightSinger) {
        if (leftSinger == null) {
            if (rightSinger == null) {
                return 0;
            }
            return -1;
        }
        if (rightSinger == null) {
            return 1;
        }

        if (leftSinger.getFirstPinyin() == null) {
            if (rightSinger.getFirstPinyin() == null) {
                return 0;
            }
            return -1;
        }
        if (rightSinger.getFirstPinyin() == null) {
            return 1;
        }

        // 获取ascii值
        int leftAscii = leftSinger.getFirstPinyin().toUpperCase().charAt(0);
        int rightAscii = rightSinger.getFirstPinyin().toUpperCase().charAt(0);
        // 判断若不是字母，则排在字母之后
        if (leftAscii < 65 || leftAscii > 90)
            return 1;
        else if (rightAscii < 65 || rightAscii > 90)
            return -1;
        else
            return leftSinger.getFullPinyin().compareTo(rightSinger.getFullPinyin());
    }
}
