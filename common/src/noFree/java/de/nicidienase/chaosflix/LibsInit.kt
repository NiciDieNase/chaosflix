package de.nicidienase.chaosflix

import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.PreferencesManager

object LibsInit : ChaosflixInitializer {
    override fun init(chaosflixApplication: ChaosflixApplication) {
        val preferencesManager =
            PreferencesManager(PreferenceManager.getDefaultSharedPreferences(chaosflixApplication.applicationContext))

        if (!preferencesManager.analyticsDisabled) {
            AnalyticsWrapperImpl.init(chaosflixApplication)
        }
    }
}