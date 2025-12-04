package com.quibbler.sevenmusic.bean.jsonbean.found;

import android.text.TextUtils;

import com.quibbler.sevenmusic.utils.PinyinUtils;

public class FoundSingerInfo {
    private String name;
    private String picUrl;
    private String id;

    private String mFirstPinyin;
    private String mFullPinyin;

    public String getName() {
        return name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getId() {
        return id;
    }

    public String getFirstPinyin() {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        if (TextUtils.isEmpty(mFullPinyin)) {
            mFirstPinyin = String.valueOf(getFullPinyin().charAt(0));
        } else if (TextUtils.isEmpty(mFirstPinyin)) {
            mFirstPinyin = String.valueOf(mFullPinyin.charAt(0));
        }
        return mFirstPinyin;
    }

    public String getFullPinyin() {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        if (TextUtils.isEmpty(mFullPinyin)) {
            mFullPinyin = PinyinUtils.getPingYin(name).toLowerCase();
        }
        return mFullPinyin;
    }
}
