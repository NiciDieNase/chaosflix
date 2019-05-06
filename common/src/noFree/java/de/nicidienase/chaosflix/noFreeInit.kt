import android.preference.PreferenceManager
import de.nicidienase.chaosflix.ChaosflixApplication
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.PreferencesManager

fun libsInit(application: ChaosflixApplication) {

    val preferencesManager =
        PreferencesManager(PreferenceManager.getDefaultSharedPreferences(application.applicationContext))

    if (!preferencesManager.analyticsDisabled) {
        AnalyticsWrapper.init(application)
    }
}