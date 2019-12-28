package de.nicidienase.chaosflix.common.viewmodel

import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.PreferencesManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.SingletonHolder
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader

class ViewModelFactory private constructor(context: Context) : ViewModelProvider.Factory {

    private val apiFactory = ApiFactory.getInstance(context.resources)

    private val database by lazy { ChaosflixDatabase.getInstance(context) }
    private val recordingApi = apiFactory.recordingApi
    private val streamingApi = apiFactory.streamingApi
    private val preferencesManager =
        PreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
    private val offlineItemManager =
        OfflineItemManager(
            context.applicationContext,
            database.offlineEventDao(),
            preferencesManager
        )
    private val downloader by lazy { Downloader(recordingApi, database) }
    private val externalFilesDir = Environment.getExternalStorageDirectory()
    private val resourcesFacade by lazy { ResourcesFacade(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            BrowseViewModel::class.java -> BrowseViewModel(
                offlineItemManager,
                database,
                recordingApi,
                streamingApi,
                preferencesManager,
                resourcesFacade
            ) as T
            PlayerViewModel::class.java -> PlayerViewModel(database) as T
            DetailsViewModel::class.java -> DetailsViewModel(
                database,
                offlineItemManager,
                preferencesManager,
                downloader
            ) as T
            PreferencesViewModel::class.java -> PreferencesViewModel(
                downloader,
                database.watchlistItemDao(),
                externalFilesDir
            ) as T
            FavoritesImportViewModel::class.java -> FavoritesImportViewModel(
                database.conferenceDao(),
                database.eventDao(),
                downloader,
                apiFactory.fahrplanMappingApi
            ) as T
            else -> throw UnsupportedOperationException(
                "The requested ViewModel is currently unsupported. " +
                        "Please make sure to implement are correct creation of it. " +
                        " Request: ${modelClass.canonicalName}"
            )
        }
    }

    companion object : SingletonHolder<ViewModelFactory, Context>(::ViewModelFactory)
}