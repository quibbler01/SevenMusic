package com.example.sevenvideoview

/**
 * 视频播放器管理器.
 */
class SevenVideoPlayerManager private constructor() {
    var currentNiceVideoPlayer: SevenVideoPlayer? = null
        private set

    fun setCurrentSevenVideoPlayer(videoPlayer: SevenVideoPlayer?) {
        if (this.currentNiceVideoPlayer !== videoPlayer) {
            releaseSevenVideoPlayer()
            this.currentNiceVideoPlayer = videoPlayer
        }
    }

    fun suspendSevenVideoPlayer() {
        if (this.currentNiceVideoPlayer != null && (currentNiceVideoPlayer!!.isPlaying || currentNiceVideoPlayer!!.isBufferingPlaying)) {
            currentNiceVideoPlayer!!.pause()
        }
    }

    fun resumeSevenVideoPlayer() {
        if (this.currentNiceVideoPlayer != null && (currentNiceVideoPlayer!!.isPaused || currentNiceVideoPlayer!!.isBufferingPaused)) {
            currentNiceVideoPlayer!!.restart()
        }
    }

    fun releaseSevenVideoPlayer() {
        if (this.currentNiceVideoPlayer != null) {
            currentNiceVideoPlayer!!.release()
            this.currentNiceVideoPlayer = null
        }
    }

    fun onBackPressd(): Boolean {
        if (this.currentNiceVideoPlayer != null) {
            if (currentNiceVideoPlayer!!.isFullScreen) {
                return currentNiceVideoPlayer!!.exitFullScreen()
            } else if (currentNiceVideoPlayer!!.isTinyWindow) {
                return currentNiceVideoPlayer!!.exitTinyWindow()
            }
        }
        return false
    }

    companion object {
        private var sInstance: SevenVideoPlayerManager? = null

        @get:Synchronized
        val instance: SevenVideoPlayerManager
            get() {
                if (sInstance == null) {
                    sInstance = SevenVideoPlayerManager()
                }
                return sInstance!!
            }
    }
}
