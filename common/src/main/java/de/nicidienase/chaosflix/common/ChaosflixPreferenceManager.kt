package de.nicidienase.chaosflix.common

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ChaosflixPreferenceManager(private val sharedPref: SharedPreferences) {

    var channelId: Long by LongPreferenceDelegate(CHANNEL_ID, 0)

    val externalPlayer: Boolean by BooleanPreferencesDelegate(keyAlwaysUseExternalPlayer, false)

    val analyticsDisabled: Boolean by BooleanPreferencesDelegate(keyAnalyticsDisabled, false)

    val downloadFolder: String? by StringPreferenceDelegate(keyDownloadFolder, "")

    var autoselectRecording: Boolean by BooleanPreferencesDelegate(keyAutoselectRecording, false)

    var autoselectStream: Boolean by BooleanPreferencesDelegate(keyAutoselectStream, false)

    var recommendationsEnabled: Boolean by BooleanPreferencesDelegate(RECOMMENDATIONS_ENABLED, true)

    private inner class BooleanPreferencesDelegate(key: String, default: Boolean) :
            PreferencesDelegate<Boolean>(key, default, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

    private inner class StringPreferenceDelegate(key: String, default: String) :
            PreferencesDelegate<String>(key, default, SharedPreferences::getString, SharedPreferences.Editor::putString)

    private inner class LongPreferenceDelegate(key: String, default: Long) :
            PreferencesDelegate<Long>(key, default, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

    abstract inner class PreferencesDelegate<T>(
        private val key: String,
        private val default: T,
        private val getter: SharedPreferences.(String, T) -> T?,
        private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
    ) : ReadWriteProperty<ChaosflixPreferenceManager, T> {

        override fun getValue(thisRef: ChaosflixPreferenceManager, property: KProperty<*>): T {
            return sharedPref.getter(key, default) ?: default
        }

        override fun setValue(thisRef: ChaosflixPreferenceManager, property: KProperty<*>, value: T) {
            sharedPref.edit().setter(key, value).apply()
        }
    }

    fun getMetered() = sharedPref.getBoolean(keyMetered, false)

    companion object {
        private const val keyMetered = "allow_metered_networks"
        private const val keyAutoselectStream = "auto_select_stream"
        private const val keyAutoselectRecording = "auto_select_recording"
        private const val keyAlwaysUseExternalPlayer = "auto_external_player"
        private const val keyAnalyticsDisabled = "disable_analytics"
        private const val keyDownloadFolder = "download_folder"
        private const val CHANNEL_ID = "channelId"
        private const val RECOMMENDATIONS_ENABLED = "recommendation_enabled"
    }
}
