package de.nicidienase.chaosflix.touch.settings

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.PreferenceFragmentCompat
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.viewmodel.PreferencesViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig


class SettingsFragment : PreferenceFragmentCompat() {
    private val REQUEST_DIRECTORY: Int = 0

	private lateinit var viewModel: PreferencesViewModel

	override fun onAttach(context: Context?) {
		super.onAttach(context)
		context?.let {context ->
			viewModel = ViewModelProviders.of(this, ViewModelFactory(context)).get(PreferencesViewModel::class.java)
		}
	}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                val dir = data!!.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                val edit = sharedPref.edit()
                edit.putString("download_folder", dir)
                edit.apply()
                this.updateSummary()
            }
        }
    }

    private fun updateSummary() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        val folder = sharedPref.getString("download_folder", "")
        val pref = this.findPreference("download_folder")
        pref.setSummary(folder)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences,rootKey)
        updateSummary()
		val downloadFolderPref = this.findPreference("download_folder")
	    val cleanCachePref = this.findPreference("delete_data")

		downloadFolderPref?.setOnPreferenceClickListener {
			val chooserIntent = Intent(context, DirectoryChooserActivity::class.java)

			val config = DirectoryChooserConfig.builder()
					.newDirectoryName("Download folder")
					.allowReadOnlyDirectory(false)
					.allowNewDirectoryNameModification(true)
					.build()

			chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config)
			startActivityForResult(chooserIntent, REQUEST_DIRECTORY)

			return@setOnPreferenceClickListener true
		}

		cleanCachePref?.setOnPreferenceClickListener {
			viewModel.cleanNonUserData()
			return@setOnPreferenceClickListener true
			}
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