package de.nicidienase.chaosflix.common

import android.app.Application

interface AnalyticsWrapper {
    fun init(application: Application)

    fun startAnalytics()

    fun stopAnalytics()
    fun addAnalyticsEvent(event: String, params: Map<String, String>)

    companion object {
        const val thumbnailsStatEvent: String = "ThumbnailStats"
    }
}
