package de.nicidienase.chaosflix.touch.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.DarkmodeUtil
import de.nicidienase.chaosflix.common.checkPermission
import de.nicidienase.chaosflix.common.viewmodel.PreferencesViewModel
import de.nicidienase.chaosflix.leanback.conferences.ConferencesActivity
import de.nicidienase.chaosflix.touch.BuildConfig
import de.nicidienase.chaosflix.touch.R
import net.rdrei.android.dirchooser.DirectoryChooserActivity
import net.rdrei.android.dirchooser.DirectoryChooserConfig
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: PreferencesViewModel by viewModel()

    private val chaosflixPreferenceManager: ChaosflixPreferenceManager by lazy { ChaosflixPreferenceManager(preferenceManager.sharedPreferences) }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.preference_key_darkmode_setting)) {
            DarkmodeUtil.init(requireContext())
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
        val folder = sharedPref.getString("download_folder", chaosflixPreferenceManager.downloadFolder)
        val pref = this.findPreference("download_folder")
        pref.summary = folder
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        if (BuildConfig.DEBUG || chaosflixPreferenceManager.debugEnabled) {
            addPreferencesFromResource(R.xml.debug_settings)
        }

        updateSummary()
        val disableAnalytics = this.findPreference("disable_analytics")
        val downloadFolderPref = this.findPreference("download_folder")

        // debug preferences
        val cleanCachePref: Preference? = this.findPreference("delete_data")
        val exportFavorites: Preference? = this.findPreference("export_favorites")
        val importFavorites: Preference? = this.findPreference("import_favorites")
        val switchUi: Preference? = this.findPreference("launch_other")

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

        switchUi?.setOnPreferenceClickListener {
            requireContext().startActivity(Intent(requireContext(), ConferencesActivity::class.java))
            return@setOnPreferenceClickListener true
        }

        disableAnalytics.setOnPreferenceChangeListener { _, state ->
            when (state) {
                true -> {
                    viewModel.stopAnalytics()
                    Snackbar.make(this.requireView(), "Analytics disabled", Snackbar.LENGTH_SHORT).show()
                    true
                }
                false -> {
                    viewModel.startAnalytics()
                    Snackbar.make(this.requireView(), "Analytics started", Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(listView, "Cannot importFavorites without Storage Permission.", Snackbar.LENGTH_SHORT).show()
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
        const val REQUEST_DIRECTORY: Int = 0
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
