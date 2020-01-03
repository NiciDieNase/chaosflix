package de.nicidienase.chaosflix.common

import android.content.SharedPreferences

class PreferencesManager(val sharedPref: SharedPreferences) {

    val externalPlayer: Boolean
        get() = sharedPref.getBoolean(keyAlwaysUseExternalPlayer, false)

    val analyticsDisabled: Boolean
        get() = sharedPref.getBoolean(keyAnalyticsDisabled, false)

    val downloadFolder: String?
        get() = sharedPref.getString(keyDownloadFolder, null)

    fun getMetered() = sharedPref.getBoolean(keyMetered, false)

    fun getAutoselectStream() = sharedPref.getBoolean(keyAutoselectStream, false)

    fun getAutoselectRecording() = sharedPref.getBoolean(keyAutoselectRecording, false)

    companion object {
        private val keyMetered = "allow_metered_networks"
        private val keyAutoselectStream = "auto_select_stream"
        private val keyAutoselectRecording = "auto_select_recording"
        private val keyAlwaysUseExternalPlayer = "auto_external_player"
        private val keyAnalyticsDisabled = "disable_analytics"
        private val keyDownloadFolder = "download_folder"
    }
}