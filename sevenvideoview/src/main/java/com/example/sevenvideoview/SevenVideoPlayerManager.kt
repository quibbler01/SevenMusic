package com.example.sevenvideoview;

/**
 * 视频播放器管理器.
 */
public class SevenVideoPlayerManager {

    private SevenVideoPlayer mVideoPlayer;

    private SevenVideoPlayerManager() {
    }

    private static SevenVideoPlayerManager sInstance;

    public static synchronized SevenVideoPlayerManager getInstance() {
        if (sInstance == null) {
            sInstance = new SevenVideoPlayerManager();
        }
        return sInstance;
    }

    public SevenVideoPlayer getCurrentNiceVideoPlayer() {
        return mVideoPlayer;
    }

    public void setCurrentSevenVideoPlayer(SevenVideoPlayer videoPlayer) {
        if (mVideoPlayer != videoPlayer) {
            releaseSevenVideoPlayer();
            mVideoPlayer = videoPlayer;
        }
    }

    public void suspendSevenVideoPlayer() {
        if (mVideoPlayer != null && (mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying())) {
            mVideoPlayer.pause();
        }
    }

    public void resumeSevenVideoPlayer() {
        if (mVideoPlayer != null && (mVideoPlayer.isPaused() || mVideoPlayer.isBufferingPaused())) {
            mVideoPlayer.restart();
        }
    }

    public void releaseSevenVideoPlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }

    public boolean onBackPressd() {
        if (mVideoPlayer != null) {
            if (mVideoPlayer.isFullScreen()) {
                return mVideoPlayer.exitFullScreen();
            } else if (mVideoPlayer.isTinyWindow()) {
                return mVideoPlayer.exitTinyWindow();
            }
        }
        return false;
    }
}
