package de.nicidienase.chaosflix.common

import android.app.Application
import android.util.Log

object AnalyticsWrapperImpl : AnalyticsWrapper {
    override fun init(application: Application) {}

    override fun startAnalytics() {}

    override fun stopAnalytics() {}

    override fun addAnalyticsEvent(event: String, params: Map<String, String>) {
        Log.i(TAG, "Not Tracking analytics event $event with parameters $params")
    }

    private val TAG = AnalyticsWrapperImpl::class.java.simpleName
}
