package de.nicidienase.chaosflix.touch.settings

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.preference.PreferenceFragmentCompat
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.checkPermission
import de.nicidienase.chaosflix.common.viewmodel.PreferencesViewModel
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory

class SettingsFragment : PreferenceFragmentCompat() {
    private val REQUEST_DIRECTORY: Int = 0

    private lateinit var viewModel: PreferencesViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        context?.let { c ->
            viewModel = ViewModelProviders.of(this, ViewModelFactory(c))
                .get(PreferencesViewModel::class.java)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_DIRECTORY && resultCode == Activity.RESULT_OK) {
            data?.data
            val dir = data?.data

            val uri = data?.data

            if (uri != null && "content".equals(uri.getScheme())) {
                val cursor = requireContext()
                    .contentResolver
                    .query(uri, arrayOf(android.provider.MediaStore.Files.), null, null, null)
                cursor.moveToFirst()
                val filePath = cursor.getString(0)
                cursor.close()
}

            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
            val edit = sharedPref.edit()
            edit.putString(DOWNLOAD_FOLDER_KEY, dir.toString())
            edit.apply()
            this.updateSummary()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateSummary() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        val folder = sharedPref.getString(DOWNLOAD_FOLDER_KEY, "")
        val pref = this.findPreference(DOWNLOAD_FOLDER_KEY)
        pref.summary = folder
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        updateSummary()
        val downloadFolderPref = this.findPreference(DOWNLOAD_FOLDER_KEY)
        val cleanCachePref = this.findPreference("delete_data")
        val exportFavorites = this.findPreference("export_favorites")
        val importFavorites = this.findPreference("import_favorites")

        downloadFolderPref?.setOnPreferenceClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER
            ) {
                chooseDownloadFolder()
            }
            return@setOnPreferenceClickListener true
        }

        cleanCachePref?.setOnPreferenceClickListener {
            viewModel.cleanNonUserData()
            return@setOnPreferenceClickListener true
        }

        exportFavorites?.setOnPreferenceClickListener {
            checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_EXPORT_FAVORITES
            ) {
                viewModel.exportFavorites()
            }
            return@setOnPreferenceClickListener true
        }

        importFavorites?.setOnPreferenceClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_IMPORT_FAVORITES
            ) {
                importFavorites()
            }
            return@setOnPreferenceClickListener true
        }
    }

    private fun chooseDownloadFolder() {
        val chooserIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_IMPORT_FAVORITES -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    importFavorites()
                } else {
                    Snackbar.make(
                        listView,
                        "Cannot import without Storage Permission.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            PERMISSION_REQUEST_EXPORT_FAVORITES -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.exportFavorites()
                } else {
                    Snackbar.make(
                        listView,
                        "Cannot export without Storage Permission.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseDownloadFolder()
                } else {
                    Snackbar.make(
                        listView,
                        "Cannot access folders without Storage Permission.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

        const val PERMISSION_REQUEST_EXPORT_FAVORITES = 23
        const val PERMISSION_REQUEST_IMPORT_FAVORITES = 24
        const val PERMISSION_REQUEST_CHOOSE_DOWNLOAD_FOLDER = 25
        const val DOWNLOAD_FOLDER_KEY = "download_folder"

        fun getInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}