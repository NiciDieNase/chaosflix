package de.nicidienase.chaosflix

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.DarkmodeUtil
import de.nicidienase.chaosflix.common.OfflineItemManager
import de.nicidienase.chaosflix.common.ResourcesFacade
import de.nicidienase.chaosflix.common.mediadata.EventInfoRepository
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.StreamingRepository
import de.nicidienase.chaosflix.common.mediadata.ThumbnailParser
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import de.nicidienase.chaosflix.common.viewmodel.FavoritesImportViewModel
import de.nicidienase.chaosflix.common.viewmodel.PlayerViewModel
import de.nicidienase.chaosflix.common.viewmodel.PreferencesViewModel
import de.nicidienase.chaosflix.common.viewmodel.SplashViewModel
import de.nicidienase.chaosflix.touch.browse.cast.CastService
import de.nicidienase.chaosflix.touch.browse.cast.CastServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import java.io.File

class ChaosflixApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StageInit.init(this)
        LibsInit.init(this)
        BuildTypeInit.init(this)
        DarkmodeUtil.init(this)

        startKoin {
            androidContext(this@ChaosflixApplication)
            androidLogger(Level.DEBUG)
            modules(chaosflixModul)
        }
    }

    companion object {

        private val chaosflixModul = module {
            single<StageConfiguration> { ChaosflixStageConfiguration(get()) }
            single { ApiFactory(get()).eventInfoApi }
            single { ApiFactory(get()).recordingApi }
            single { ApiFactory(get()).streamingApi }
            single { ApiFactory(get()).client }
            single { ChaosflixDatabase.getInstance(get()) }
            single { get<ChaosflixDatabase>().conferenceDao() }
            single { get<ChaosflixDatabase>().conferenceGroupDao() }
            single { get<ChaosflixDatabase>().eventDao() }
            single { get<ChaosflixDatabase>().eventInfoDao() }
            single { get<ChaosflixDatabase>().offlineEventDao() }
            single { get<ChaosflixDatabase>().playbackProgressDao() }
            single { get<ChaosflixDatabase>().recommendationDao() }
            single { get<ChaosflixDatabase>().recordingDao() }
            single { get<ChaosflixDatabase>().relatedEventDao() }
            single { get<ChaosflixDatabase>().watchlistItemDao() }
            single { StreamingRepository(get()) }
            single { EventInfoRepository(get(), get()) }
            single { ChaosflixPreferenceManager(PreferenceManager.getDefaultSharedPreferences(androidContext())) }
            single { OfflineItemManager(get(), get(), get()) }
            single { ResourcesFacade(get()) }
            single { ThumbnailParser(get()) }
            single<CastService> { CastServiceImpl(get()) }
            single { MediaRepository(get(), get()) }

            viewModel { BrowseViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
            viewModel { PlayerViewModel(get(), get()) }
            viewModel { DetailsViewModel(get(), get(), get(), get(), get()) }
            viewModel { PreferencesViewModel(get(), get(), get(), get()) }
            viewModel { FavoritesImportViewModel(get()) }
            viewModel { SplashViewModel(get()) }
        }
    }
}

interface StageConfiguration {
    val recordingUrl: String
    val eventInfoUrl: String
    val cacheDir: File?
    val externalFilesDir: File?
    val streamingApiBaseUrl: String
    val streamingApiPath: String
    val appcenterId: String?
}

class ChaosflixStageConfiguration(context: Context) : StageConfiguration {
    override val recordingUrl = context.resources.getString(R.string.recording_url)
            ?: throw error("Recording Url not definded")
    override val eventInfoUrl = context.resources.getString(R.string.event_info_url)
            ?: throw error("EventInfo Url not definded")
    override val cacheDir: File? = context.cacheDir
    override val streamingApiBaseUrl = BuildConfig.STREAMING_API_BASE_URL
    override val streamingApiPath = BuildConfig.STREAMING_API_OFFERS_PATH
    override val appcenterId: String? = BuildConfig.APPCENTER_ID
    override val externalFilesDir: File? = android.os.Environment.getExternalStorageDirectory()
}
