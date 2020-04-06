package de.nicidienase.chaosflix.leanback.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.leanback.preference.LeanbackSettingsFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceScreen
import de.nicidienase.chaosflix.leanback.R

class ChaosflixSettingsFragment : LeanbackSettingsFragment() {
    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.leanback_preferences, null))
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragment?, pref: PreferenceScreen?): Boolean = false

    override fun onPreferenceStartFragment(caller: PreferenceFragment?, pref: Preference?): Boolean {
        val frag = buildPreferenceFragment(R.xml.leanback_preferences, pref?.key)
        startPreferenceFragment(frag)
        return true
    }

    private fun buildPreferenceFragment(preferenceResId: Int, root: String?): PreferenceFragment {
        val fragment: PreferenceFragment = PreferencesFragment()
        val args = Bundle()
        args.putInt("preferenceResource", preferenceResId)
        args.putString("root", root)
        fragment.arguments = args
        return fragment
    }

    class PreferencesFragment : LeanbackPreferenceFragment() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val root = arguments.getString("root", null)
            val prefResId = arguments.getInt("preferenceResource")
            if (root == null) {
                addPreferencesFromResource(prefResId)
            } else {
                setPreferencesFromResource(prefResId, root)
            }
            // Disable preferences not relevant for leanback
//            listOf("allow_metered_networks",
//                    "auto_external_player",
//                    "download_folder",
//                    "delete_data",
//                    "export_favorites",
//                    "import_favorites").forEach {
//                preferenceManager.findPreference(it).apply {
//                    isVisible = false
//                    isEnabled = false
//                }
//            }
        }
    }
}
