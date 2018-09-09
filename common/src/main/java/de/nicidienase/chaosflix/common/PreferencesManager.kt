package de.nicidienase.chaosflix.common

import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferencesManager {
	private val keyMetered = "allow_metered_networks"
	private val keyAutoselectStream = "auto_select_stream"
	private val keyAutoselectRecording = "auto_select_recording"

	val sharedPref: SharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(ChaosflixApplication.APPLICATION_CONTEXT)

	fun getMetered() = sharedPref.getBoolean(keyMetered, false)

	fun getAutoselectStream() = sharedPref.getBoolean(keyAutoselectStream, false)

	fun getAutoselectRecording() = sharedPref.getBoolean(keyAutoselectRecording, false)
}