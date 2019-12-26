package de.nicidienase.chaosflix.common

import android.app.Application

object AnalyticsWrapperImpl : AnalyticsWrapper {
    override fun init(application: Application) {}

    override fun startAnalytics() {}

    override fun stopAnalytics() {}
}
