/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.decoding

import android.app.Activity
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * Finishes an activity after a period of inactivity.
 */
class InactivityTimer(private val activity: Activity?) {
    private val inactivityTimer: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(
            DaemonThreadFactory()
        )
    private var inactivityFuture: ScheduledFuture<*>? = null

    init {
        onActivity()
    }

    fun onActivity() {
        cancel()
        inactivityFuture = inactivityTimer.schedule(
            FinishListener(activity),
            INACTIVITY_DELAY_SECONDS.toLong(),
            TimeUnit.SECONDS
        )
    }

    private fun cancel() {
        if (inactivityFuture != null) {
            inactivityFuture!!.cancel(true)
            inactivityFuture = null
        }
    }

    fun shutdown() {
        cancel()
        inactivityTimer.shutdown()
    }

    private class DaemonThreadFactory : ThreadFactory {
        override fun newThread(runnable: Runnable?): Thread {
            val thread = Thread(runnable)
            thread.setDaemon(true)
            return thread
        }
    }

    companion object {
        private val INACTIVITY_DELAY_SECONDS = 5 * 60
    }
}
