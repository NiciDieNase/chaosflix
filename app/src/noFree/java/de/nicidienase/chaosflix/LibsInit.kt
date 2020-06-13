package de.nicidienase.chaosflix

import android.app.Application
import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager

object LibsInit : ChaosflixInitializer {
    override fun init(application: Application) {
        val preferencesManager =
            ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(application.applicationContext))

        if (!preferencesManager.analyticsDisabled) {
            AnalyticsWrapperImpl.init(application)
        }
    }
}
