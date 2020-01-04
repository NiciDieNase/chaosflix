package de.nicidienase.chaosflix

import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.PreferencesManager

fun libsInit(application: ChaosflixApplication) {

    val preferencesManager =
        PreferencesManager(PreferenceManager.getDefaultSharedPreferences(application.applicationContext))

    if (!preferencesManager.analyticsDisabled) {
        AnalyticsWrapperImpl.init(application)
    }
}