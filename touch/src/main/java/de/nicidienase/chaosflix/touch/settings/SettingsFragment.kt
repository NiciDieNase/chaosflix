package de.nicidienase.chaosflix.touch.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.PreferenceFragmentCompat
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.touch.ChaosflixApplication
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig


class SettingsFragment : PreferenceFragmentCompat() {
    private val REQUEST_DIRECTORY: Int = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                val dir = data!!.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(ChaosflixApplication.APPLICATION_CONTEXT)
                val edit = sharedPref.edit()
                edit.putString("download_folder", dir)
                edit.apply()
                this.updateSummary()
            }
        }
    }

    private fun updateSummary() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(ChaosflixApplication.APPLICATION_CONTEXT)
        val folder = sharedPref.getString("download_folder", "")
        val pref = this.findPreference("download_folder")
        pref.setSummary(folder)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences,rootKey)
        updateSummary()
		val pref = this.findPreference("download_folder")

		pref.setOnPreferenceClickListener({
            val chooserIntent = Intent(context, DirectoryChooserActivity::class.java)

            val config = DirectoryChooserConfig.builder()
                    .newDirectoryName("Download folder")
                    .allowReadOnlyDirectory(false)
                    .allowNewDirectoryNameModification(true)
                    .build()

            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config)
            startActivityForResult(chooserIntent, REQUEST_DIRECTORY)

            return@setOnPreferenceClickListener true

        })
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