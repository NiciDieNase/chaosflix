package de.nicidienase.chaosflix.common

import android.app.Application

interface AnalyticsWrapper {
   fun init(application: Application)

    fun startAnalytics()

    fun stopAnalytics()
}