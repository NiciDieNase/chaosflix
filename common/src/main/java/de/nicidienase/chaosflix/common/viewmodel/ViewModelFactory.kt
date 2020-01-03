package de.nicidienase.chaosflix.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.SingletonHolder
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader

class ViewModelFactory private constructor(context: Context) : ViewModelProvider.Factory {

    private val apiFactory = ApiFactory.getInstance(context.resources, context.cacheDir)

    private val database by lazy { ChaosflixDatabase.getInstance(context) }
    private val recordingApi = apiFactory.recordingApi
    private val streamingApi = apiFactory.streamingApi
    private val preferencesManager =
            PreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
    private val offlineItemManager =
            OfflineItemManager(context.applicationContext, database.offlineEventDao(), preferencesManager)
    private val downloader by lazy { Downloader(recordingApi, database) }
    private val externalFilesDir = Environment.getExternalStorageDirectory()
    private val resourcesFacade by lazy { ResourcesFacade(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
            return BrowseViewModel(offlineItemManager, database, recordingApi, streamingApi, preferencesManager, resourcesFacade) as T
        } else if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(database) as T
        } else if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(database, offlineItemManager, preferencesManager, downloader) as T
        } else if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            return PreferencesViewModel(downloader, database.watchlistItemDao(), externalFilesDir) as T
        } else {
            throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
                    "Please make sure to implement are correct creation of it. " +
                    " Request: ${modelClass.canonicalName}")
        }
    }
    companion object : SingletonHolder<ViewModelFactory, Context>(::ViewModelFactory)
    }