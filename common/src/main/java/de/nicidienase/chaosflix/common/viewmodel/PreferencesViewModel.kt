package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.util.ThreadHandler

class PreferencesViewModel(val downloader: Downloader) : ViewModel() {
	fun cleanNonUserData() {
		ThreadHandler().runOnBackgroundThread {
			downloader.deleteNonUserData()
		}
	}
}