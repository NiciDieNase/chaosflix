package de.nicidienase.chaosflix.touch.settings

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
		context?.let { context ->
			viewModel = ViewModelProviders.of(this, ViewModelFactory(context)).get(PreferencesViewModel::class.java)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == REQUEST_DIRECTORY) {
			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				val dir = data?.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
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
		setPreferencesFromResource(R.xml.preferences, rootKey)
		updateSummary()
		val downloadFolderPref = this.findPreference("download_folder")
		val cleanCachePref = this.findPreference("delete_data")
		val exportFavorites = this.findPreference("export_favorites")
		val importFavorites = this.findPreference("import_favorites")

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

		exportFavorites?.setOnPreferenceClickListener {
			if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
						PERMISSION_REQUEST_WRITE_STORAGE)
			} else {
				viewModel.exportFavorites()
			}
			return@setOnPreferenceClickListener true
		}

		importFavorites?.setOnPreferenceClickListener {
			if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
						PERMISSION_REQUEST_READ_STORAGE)
			} else {
				importFavorites()
			}
			return@setOnPreferenceClickListener true
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		when(requestCode){
			PERMISSION_REQUEST_READ_STORAGE -> {
				if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
					importFavorites()
				} else {
					Snackbar.make(listView, "Cannot import without Storage Permission.", Snackbar.LENGTH_SHORT).show()
				}
			}
			PERMISSION_REQUEST_WRITE_STORAGE -> {
				if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					viewModel.exportFavorites()
				} else {
					Snackbar.make(listView, "Cannot export without Storage Permission.", Snackbar.LENGTH_SHORT).show()
				}
			}
			else ->  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		}
	}

	private fun importFavorites() {
		var snackbar: Snackbar? = null
		viewModel.importFavorites().observe(this, Observer { event ->
			when {
				event?.state == PreferencesViewModel.State.Loading -> {
					snackbar?.dismiss()
					snackbar = Snackbar.make(listView, "Importing", Snackbar.LENGTH_INDEFINITE)
					snackbar?.show()
				}
				event?.error != null -> {
					snackbar?.dismiss()
					val message: String = event.error?.message ?: event.error.toString()
					snackbar = Snackbar.make(listView, message, Snackbar.LENGTH_SHORT)
					snackbar?.show()
				}
				event?.state == PreferencesViewModel.State.Done -> {
					snackbar?.dismiss()
					snackbar = Snackbar.make(listView, "Import Done", Snackbar.LENGTH_SHORT)
					snackbar?.show()
				}
			}
		})
	}

	companion object {

		const val PERMISSION_REQUEST_WRITE_STORAGE = 23
		const val PERMISSION_REQUEST_READ_STORAGE = 24

		fun getInstance(): SettingsFragment {
			val fragment = SettingsFragment()
			val args = Bundle()
			fragment.arguments = args
			return fragment
		}
	}
}