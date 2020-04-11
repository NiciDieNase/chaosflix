package de.nicidienase.chaosflix.common

import android.content.SharedPreferences

class PreferencesManager(private val sharedPref: SharedPreferences) {


    var channelId: Long
        get() = sharedPref.getLong(CHANNEL_ID, 0)
        set(value) { sharedPref.edit().putLong(CHANNEL_ID, value).apply() }

    val externalPlayer: Boolean
        get() = sharedPref.getBoolean(keyAlwaysUseExternalPlayer, false)

    val analyticsDisabled: Boolean
        get() = sharedPref.getBoolean(keyAnalyticsDisabled, false)

    val downloadFolder: String?
        get() = sharedPref.getString(keyDownloadFolder, null)

    var autoselectRecording: Boolean
        get() = sharedPref.getBoolean(keyAutoselectRecording, false)
        set(value) = sharedPref.edit().putBoolean(keyAutoselectRecording, value).apply()

    var autoselectStream: Boolean
        get() = sharedPref.getBoolean(keyAutoselectStream, false)
        set(value) = sharedPref.edit().putBoolean(keyAutoselectStream, value).apply()

    fun getMetered() = sharedPref.getBoolean(keyMetered, false)

    companion object {
        private const val keyMetered = "allow_metered_networks"
        private const val keyAutoselectStream = "auto_select_stream"
        private const val keyAutoselectRecording = "auto_select_recording"
        private const val keyAlwaysUseExternalPlayer = "auto_external_player"
        private const val keyAnalyticsDisabled = "disable_analytics"
        private const val keyDownloadFolder = "download_folder"
        private const val CHANNEL_ID = "channelId"
    }
}
