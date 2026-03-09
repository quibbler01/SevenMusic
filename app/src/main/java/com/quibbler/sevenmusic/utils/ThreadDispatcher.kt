package com.quibbler.sevenmusic.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      ThreadDispatcher
 * Description:    快速在UI线程和工作线程切换的工具类
 * Author:         yanwuyang
 * CreateDate:     2019/11/27 15:05
 */
public class ThreadDispatcher {
    private static final String THREAD_WORK = "magazine_work_thread";

    private Handler mWorkerHandler;
    private Handler mMainHandler;

    private HandlerThread mReportThread;
    private HandlerThread mWorkerThread;

    private static ThreadDispatcher sInstance;

    public static ThreadDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (ThreadDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new ThreadDispatcher();
                }
            }
        }
        return sInstance;
    }

    private ThreadDispatcher() {
        init();
    }

    private void init() {
        if (mWorkerThread == null) {
            mWorkerThread = new HandlerThread(THREAD_WORK);
            mWorkerThread.start();
            mWorkerHandler = new Handler(mWorkerThread.getLooper());
        }

        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    public void stop() {
        mWorkerHandler.removeCallbacksAndMessages(null);
        mMainHandler.removeCallbacksAndMessages(null);
        if (mWorkerThread != null) {
            mWorkerThread.quit();
            mWorkerThread = null;
        }

        if (mReportThread != null) {
            mReportThread.quit();
            mReportThread = null;
        }
        sInstance = null;
    }

    public Looper getWorkerLooper() {
        return mWorkerThread.getLooper();
    }

    public void runOnWorkerThread(Runnable runnable) {
        mWorkerHandler.post(runnable);
    }

    public void runOnUiThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }
}