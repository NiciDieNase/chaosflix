package de.nicidienase.chaosflix.touch.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.checkPermission
import de.nicidienase.chaosflix.common.viewmodel.PreferencesViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig

class SettingsFragment : PreferenceFragmentCompat() {
    private val REQUEST_DIRECTORY: Int = 0

    private lateinit var viewModel: PreferencesViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(context)).get(PreferencesViewModel::class.java)
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
        val disableAnalytics = this.findPreference("disable_analytics")

        downloadFolderPref?.setOnPreferenceClickListener {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER) {
                chooseDownloadFolder()
            }
            return@setOnPreferenceClickListener true
        }

        cleanCachePref?.setOnPreferenceClickListener {
            viewModel.cleanNonUserData()
            return@setOnPreferenceClickListener true
        }

        exportFavorites?.setOnPreferenceClickListener {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_EXPORT_FAVORITES) {
                viewModel.exportFavorites()
            }
            return@setOnPreferenceClickListener true
        }

        importFavorites?.setOnPreferenceClickListener {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_IMPORT_FAVORITES) {
                importFavorites()
            }
            return@setOnPreferenceClickListener true
        }

        disableAnalytics.setOnPreferenceChangeListener { _, state ->
            when (state) {
                true -> {
                    viewModel.stopAnalytics()
                    Snackbar.make(this.view!!, "Analytics disabled", Snackbar.LENGTH_SHORT).show()
                    true
                }
                false -> {
                    viewModel.startAnalytics()
                    Snackbar.make(this.view!!, "Analytics started", Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> true
            } }
    }

    private fun chooseDownloadFolder() {
        val chooserIntent = Intent(context, DirectoryChooserActivity::class.java)

        val config = DirectoryChooserConfig.builder()
                .newDirectoryName("Download folder")
                .allowReadOnlyDirectory(false)
                .allowNewDirectoryNameModification(true)
                .build()

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config)
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_IMPORT_FAVORITES -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    importFavorites()
                } else {
                    Snackbar.make(listView, "Cannot import without Storage Permission.", Snackbar.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_EXPORT_FAVORITES -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.exportFavorites()
                } else {
                    Snackbar.make(listView, "Cannot export without Storage Permission.", Snackbar.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseDownloadFolder()
                } else {
                    Snackbar.make(listView, "Cannot access folders without Storage Permission.", Snackbar.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun importFavorites() {
        var snackbar: Snackbar? = null
        viewModel.importFavorites().observe(viewLifecycleOwner, Observer { event ->
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

        const val PERMISSION_REQUEST_EXPORT_FAVORITES = 23
        const val PERMISSION_REQUEST_IMPORT_FAVORITES = 24
        const val PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER = 25

        fun getInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
