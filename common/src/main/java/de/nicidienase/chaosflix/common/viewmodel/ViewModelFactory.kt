package de.nicidienase.chaosflix.common.viewmodel

import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.SingletonHolder
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.StreamingRepository
import de.nicidienase.chaosflix.common.mediadata.ThumbnailParser
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.browse.cast.CastServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ViewModelFactory private constructor(context: Context) : ViewModelProvider.Factory {

    val apiFactory = ApiFactory.getInstance(
            context.resources.getString(R.string.recording_url),
            context.resources.getString(R.string.event_info_url),
            context.cacheDir)

    private val supervisorJob = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val database by lazy { ChaosflixDatabase.getInstance(context) }
    private val streamingRepository by lazy { StreamingRepository(apiFactory.streamingApi) }
    private val preferencesManager =
            ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
    private val offlineItemManager =
            OfflineItemManager(
                    context.applicationContext,
                    database.offlineEventDao(),
                    preferencesManager
            )
    private val externalFilesDir = Environment.getExternalStorageDirectory()
    private val resourcesFacade by lazy { ResourcesFacade(context) }
    private val thumbnailParser by lazy { ThumbnailParser(apiFactory.client) }
    private val castService: CastService by lazy { CastServiceImpl(database.playbackProgressDao(), coroutineScope) }
    val mediaRepository by lazy { MediaRepository(apiFactory.recordingApi, database) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            BrowseViewModel::class.java -> BrowseViewModel(
                    offlineItemManager,
                    mediaRepository,
                    database,
                    streamingRepository,
                    preferencesManager,
                    resourcesFacade,
                    castService) as T
            PlayerViewModel::class.java -> PlayerViewModel(database, thumbnailParser) as T
            DetailsViewModel::class.java -> DetailsViewModel(
                    database,
                    offlineItemManager,
                    preferencesManager,
                    mediaRepository,
                    castService) as T
            PreferencesViewModel::class.java -> PreferencesViewModel(
                    mediaRepository,
                    database.watchlistItemDao(),
                    database.playbackProgressDao(),
                    externalFilesDir) as T
            FavoritesImportViewModel::class.java -> FavoritesImportViewModel(
                    mediaRepository
            ) as T
            SplashViewModel::class.java -> SplashViewModel(mediaRepository) as T
            else -> throw UnsupportedOperationException(
                    "The requested ViewModel is currently unsupported. " +
                            "Please make sure to implement are correct creation of it. " +
                            " Request: ${modelClass.canonicalName}"
            )
        }
    }

    companion object : SingletonHolder<ViewModelFactory, Context>(::ViewModelFactory)
}
