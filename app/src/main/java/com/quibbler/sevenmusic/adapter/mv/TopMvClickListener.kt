package com.quibbler.sevenmusic.adapter.mv

import com.quibbler.sevenmusic.bean.mv.MvInfo

interface TopMvClickListener {
        /**
     * Brief description for onStartMvPlayActivity.
     *
     * @param context the operating context
     * @return the result of the operation
     */
fun onStartMvPlayActivity(mvInfo: MvInfo?)
        /**
     * Brief description for onClickMoreButton.
     *
     * @param context the operating context
     * @return the result of the operation
     */
fun onClickMoreButton(mvInfo: MvInfo?)
}
