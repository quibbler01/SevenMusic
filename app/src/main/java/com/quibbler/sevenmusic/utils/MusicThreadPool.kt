package com.quibbler.sevenmusic.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Package:        com.quibbler.sevenmusic.utils
 * ClassName:      MusicThreadPool
 * Description:    本应用的线程池:更加根据CPU核心数设置核心线程及最大线程池;无界队列，不限任务数量
 * 20191025 退出应用关闭线程池closeThreadPoolImmediately()
 * Author:         zhaopeng
 * CreateDate:     2019/9/26 20:14
 */
public class MusicThreadPool {
    private static ThreadPoolExecutor mPool = null;
    private static final int CPU = Runtime.getRuntime().availableProcessors();

    public static void initThreadPool() {
        mPool = new ThreadPoolExecutor(CPU, CPU * 2, 16000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 直接把Runnable对象提交到线程池去执行
     *
     * @param runnable
     */
    public static void postRunnable(Runnable runnable) {
        mPool.execute(runnable);
    }

    /**
     * ExecutorService停止接受任何新的任务且等待已经提交的任务执行完成
     */
    public static void closeThreadPoolImmediately() {
        mPool.shutdownNow();
        mPool = null;
    }

}
