package de.nicidienase.chaosflix.common

import android.content.SharedPreferences
import android.preference.PreferenceManager

class PreferencesManager(val sharedPref: SharedPreferences) {
	private val keyMetered = "allow_metered_networks"
	private val keyAutoselectStream = "auto_select_stream"
	private val keyAutoselectRecording = "auto_select_recording"

	fun getMetered() = sharedPref.getBoolean(keyMetered, false)

	fun getAutoselectStream() = sharedPref.getBoolean(keyAutoselectStream, false)

	fun getAutoselectRecording() = sharedPref.getBoolean(keyAutoselectRecording, false)
}