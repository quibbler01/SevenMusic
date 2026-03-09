package com.quibbler.sevenmusic.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ThreadDispatcher
 * Description:    快速在UI线程和工作线程切换的工具类
 * Author:         yanwuyang
 * CreateDate:     2019/11/27 15:05
 */
class ThreadDispatcher private constructor() {
    private var mWorkerHandler: Handler? = null
    private var mMainHandler: Handler? = null

    private var mReportThread: HandlerThread? = null
    private var mWorkerThread: HandlerThread? = null

    init {
        init()
    }

    private fun init() {
        if (mWorkerThread == null) {
            mWorkerThread = HandlerThread(THREAD_WORK)
            mWorkerThread!!.start()
            mWorkerHandler = Handler(mWorkerThread!!.getLooper())
        }

        if (mMainHandler == null) {
            mMainHandler = Handler(Looper.getMainLooper())
        }
    }

    fun stop() {
        mWorkerHandler!!.removeCallbacksAndMessages(null)
        mMainHandler!!.removeCallbacksAndMessages(null)
        if (mWorkerThread != null) {
            mWorkerThread!!.quit()
            mWorkerThread = null
        }

        if (mReportThread != null) {
            mReportThread!!.quit()
            mReportThread = null
        }
        sInstance = null
    }

    val workerLooper: Looper?
        get() = mWorkerThread!!.getLooper()

    fun runOnWorkerThread(runnable: Runnable) {
        mWorkerHandler!!.post(runnable)
    }

    fun runOnUiThread(runnable: Runnable) {
        mMainHandler!!.post(runnable)
    }

    companion object {
        private const val THREAD_WORK = "magazine_work_thread"

        private var sInstance: ThreadDispatcher? = null

        val instance: ThreadDispatcher
            get() {
                if (sInstance == null) {
                    synchronized(ThreadDispatcher::class.java) {
                        if (sInstance == null) {
                            sInstance = ThreadDispatcher()
                        }
                    }
                }
                return sInstance!!
            }
    }
}