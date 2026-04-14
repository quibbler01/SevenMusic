package com.example.sevenvideoview

/**
 * NiceVideoPlayer抽象接口
 */
interface ISevenVideoPlayer {
    /**
     * 设置视频Url，以及headers
     * 
     * @param url     视频地址，可以是本地，也可以是网络视频
     * @param headers 请求header.
     */
    fun setUp(url: String?, headers: MutableMap<String?, String?>?)

    /**
     * 开始播放
     */
    fun start()

    /**
     * 从指定的位置开始播放
     * 
     * @param position 播放位置
     */
    fun start(position: Long)

    /**
     * 重新播放，播放器被暂停、播放错误、播放完成后，需要调用此方法重新播放
     */
    fun restart()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * seek到制定的位置继续播放
     * 
     * @param pos 播放位置
     */
    fun seekTo(pos: Long)

    /**
     * 设置播放速度，目前只有IjkPlayer有效果，原生MediaPlayer暂不支持
     * 
     * @param speed 播放速度
     */
    fun setSpeed(speed: Float)

    /**
     * 开始播放时，是否从上一次的位置继续播放
     * 
     * @param continueFromLastPosition true 接着上次的位置继续播放，false从头开始播放
     */
    fun continueFromLastPosition(continueFromLastPosition: Boolean)

    /*********************************
     * 以下9个方法是播放器在当前的播放状态
     */
    val isIdle: Boolean
    val isPreparing: Boolean
    val isPrepared: Boolean
    val isBufferingPlaying: Boolean
    val isBufferingPaused: Boolean
    val isPlaying: Boolean
    val isPaused: Boolean
    val isError: Boolean
    val isCompleted: Boolean

    /*********************************
     * 以下3个方法是播放器的模式
     */
    val isFullScreen: Boolean
    val isTinyWindow: Boolean
    val isNormal: Boolean

    /**
     * 获取最大音量
     * 
     * @return 最大音量值
     */
    val maxVolume: Int

    /**
     * 获取当前音量
     * 
     * @return 当前音量值
     */
    /**
     * 设置音量
     * 
     * @param volume 音量值
     */
    var volume: Int

    /**
     * 获取办法给总时长，毫秒
     * 
     * @return 视频总时长ms
     */
    val duration: Long

    /**
     * 获取当前播放的位置，毫秒
     * 
     * @return 当前播放位置，ms
     */
    val currentPosition: Long

    /**
     * 获取视频缓冲百分比
     * 
     * @return 缓冲白百分比
     */
    val bufferPercentage: Int

    /**
     * 获取播放速度
     * 
     * @param speed 播放速度
     * @return 播放速度
     */
    fun getSpeed(speed: Float): Float

    /**
     * 获取网络加载速度
     * 
     * @return 网络加载速度
     */
    val tcpSpeed: Long

    /**
     * 进入全屏模式
     */
    fun enterFullScreen()

    /**
     * 退出全屏模式
     * 
     * @return true 退出
     */
    fun exitFullScreen(): Boolean

    /**
     * 进入小窗口模式
     */
    fun enterTinyWindow()

    /**
     * 退出小窗口模式
     * 
     * @return true 退出小窗口
     */
    fun exitTinyWindow(): Boolean

    /**
     * 此处只释放播放器（如果要释放播放器并恢复控制器状态需要调用[.release]方法）
     * 不管是全屏、小窗口还是Normal状态下控制器的UI都不恢复初始状态
     * 这样以便在当前播放器状态下可以方便的切换不同的清晰度的视频地址
     */
    fun releasePlayer()

    /**
     * 释放INiceVideoPlayer，释放后，内部的播放器被释放掉，同时如果在全屏、小窗口模式下都会退出
     * 并且控制器的UI也应该恢复到最初始的状态.
     */
    fun release()
}
