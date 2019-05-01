package de.nicidienase.chaosflix.common.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process

class ThreadHandler {

    private val uiThreadHandler: Handler
    private val backgroundThreadHandler: Handler

    init {
        val handlerTread = HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND)
        handlerTread.start()
        backgroundThreadHandler = android.os.Handler(handlerTread.looper)
        uiThreadHandler = Handler(Looper.getMainLooper())
    }

    fun runOnBackgroundThread(runnable: () -> Unit) {
        backgroundThreadHandler.post(runnable)
    }

    fun runOnMainThread(runnable: () -> Unit) {
        uiThreadHandler.post(runnable)
    }

    companion object {
        private val TAG = ThreadHandler::class.java.simpleName
    }
}