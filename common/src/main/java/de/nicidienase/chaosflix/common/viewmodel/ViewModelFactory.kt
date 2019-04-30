package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.DatabaseFactory
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

	private val apiFactory = ApiFactory(context.resources)

	private val database by lazy { DatabaseFactory.getInstance(context) }
	private val recordingApi = apiFactory.recordingApi
	private val streamingApi = apiFactory.streamingApi
	private val preferencesManager =
			PreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
	private val offlineItemManager =
			OfflineItemManager(context.applicationContext, database.offlineEventDao(),preferencesManager)
	private val downloader by lazy { Downloader(recordingApi, database) }
	private val externalFilesDir = Environment.getExternalStorageDirectory()
	private val resourcesFacade by lazy { ResourcesFacade(context) }

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
			return BrowseViewModel(offlineItemManager, database, recordingApi, streamingApi, preferencesManager,resourcesFacade) as T
		} else if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
			return PlayerViewModel(database) as T
		} else if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
			return DetailsViewModel(database, offlineItemManager, preferencesManager, downloader) as T
		} else if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)){
			return PreferencesViewModel(downloader, database.watchlistItemDao(), externalFilesDir) as T
		} else {
			throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
					"Please make sure to implement are correct creation of it. " +
					" Request: ${modelClass.canonicalName}");
		}

	}

}