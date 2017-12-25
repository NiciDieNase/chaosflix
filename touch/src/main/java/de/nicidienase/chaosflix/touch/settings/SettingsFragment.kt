package de.nicidienase.chaosflix.touch.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.preference.PreferenceFragmentCompat
import de.nicidienase.chaosflix.R

class SettingsFragment : PreferenceFragmentCompat() {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences,rootKey)
	}

	companion object {
		fun getInstance(): SettingsFragment {
			val fragment = SettingsFragment()
			val args = Bundle()
			fragment.arguments = args
			return fragment
		}
	}
}