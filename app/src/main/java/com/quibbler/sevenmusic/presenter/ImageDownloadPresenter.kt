package com.quibbler.sevenmusic.presenter

import android.content.Context

/**
 * Package:        com.quibbler.sevenmusic.presenter
 * ClassName:      ImageDownloadPresenter
 * Description:    根据图片的url下载该图片，并传入需要展现的imageView中。模仿glide写法。三级缓存。
 * Author:         yanwuyang
 * CreateDate:     2019/10/17 11:09
 */
class ImageDownloadPresenter private constructor() {
    fun with(context: Context?): ImageDealer? {
        return ImageDealer().with(context)
    }

    interface ResourceCallback<T> {
        fun onResourceReady(t: T?)
    }

    companion object {
        private const val TAG = "ImageDownloadPresenter"

        @Volatile
        private var sInstance: ImageDownloadPresenter? = null

        const val STYLE_ORIGIN: Int = 0 //原始图片
        const val STYLE_ROUND: Int = 1 //圆角图片
        const val STYLE_CIRCLE: Int = 2 // 圆形图片

        val instance: ImageDownloadPresenter?
            get() {
                if (sInstance == null) {
                    synchronized(ImageDownloadPresenter::class.java) {
                        if (sInstance == null) {
                            sInstance = ImageDownloadPresenter()
                        }
                    }
                }
                return sInstance
            }
    }
}
