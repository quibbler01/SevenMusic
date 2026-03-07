package com.quibbler.sevenmusic.adapter.mv;

import com.quibbler.sevenmusic.bean.mv.MvInfo;

public interface TopMvClickListener {
    void onStartMvPlayActivity(MvInfo mvInfo);
    void onClickMoreButton(MvInfo mvInfo);
}
