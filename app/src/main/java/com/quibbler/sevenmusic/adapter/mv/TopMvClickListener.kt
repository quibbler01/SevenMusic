package com.quibbler.sevenmusic.adapter.mv

import com.quibbler.sevenmusic.bean.mv.MvInfo

interface TopMvClickListener {
    fun onStartMvPlayActivity(mvInfo: MvInfo?)
    fun onClickMoreButton(mvInfo: MvInfo?)
}
