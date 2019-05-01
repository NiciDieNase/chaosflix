package de.nicidienase.chaosflix.common

import android.content.SharedPreferences

class PreferencesManager(val sharedPref: SharedPreferences) {
    private val keyMetered = "allow_metered_networks"
    private val keyAutoselectStream = "auto_select_stream"
    private val keyAutoselectRecording = "auto_select_recording"
    private val keyAlwaysUseExternalPlayer = "auto_external_player"

    val externalPlayer: Boolean
        get() = sharedPref.getBoolean(keyAlwaysUseExternalPlayer, false)

    fun getMetered() = sharedPref.getBoolean(keyMetered, false)

    fun getAutoselectStream() = sharedPref.getBoolean(keyAutoselectStream, false)

    fun getAutoselectRecording() = sharedPref.getBoolean(keyAutoselectRecording, false)
}