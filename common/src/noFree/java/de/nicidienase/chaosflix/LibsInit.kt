package de.nicidienase.chaosflix

import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager

object LibsInit : ChaosflixInitializer {
    override fun init(chaosflixApplication: ChaosflixApplication) {
        val preferencesManager =
            ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(chaosflixApplication.applicationContext))

        if (!preferencesManager.analyticsDisabled) {
            AnalyticsWrapperImpl.init(chaosflixApplication)
        }
    }
}
