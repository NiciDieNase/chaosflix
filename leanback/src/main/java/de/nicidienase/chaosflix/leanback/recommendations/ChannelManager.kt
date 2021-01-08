package de.nicidienase.chaosflix.leanback.recommendations

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.leanback.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelManager(
    private val context: Context,
    private val mediaRepository: MediaRepository,
    private val prefs: ChaosflixPreferenceManager
) {

    enum class Channels(val channelId: String, @StringRes val title: Int) {
        MAIN("main", R.string.popular),
        IN_PROGRESS("in_progress", R.string.continue_watching),
        WATCHLIST("watchlist", R.string.bookmarks),
//        RECENT_CONFERENCES("recent_conferences", R.string.latest_releases),
        RECENT_EVENTS("recent_events", R.string.latest_events)
    }

    suspend fun updateRecommendations() {
        setupChannels(context, prefs)

        listOf(
                Channels.MAIN to mediaRepository.getTopEvents(5),
                Channels.WATCHLIST to mediaRepository.getBookmarkedEvents(),
                Channels.IN_PROGRESS to mediaRepository.getEventsInProgress().mapNotNull { it.event },
//                Channels.RECENT_CONFERENCES to mediaRepository.getNewestConferences(10),
                Channels.RECENT_EVENTS to mediaRepository.getLatestEventsSync(10)
        ).forEach {
            publishEvents(
                    context.contentResolver,
                    mediaRepository,
                    it.second,
                    it.first
            )
            cleanupChannel(it.second, it.first)
        }
    }

    private suspend fun setupChannels(context: Context, prefs: ChaosflixPreferenceManager) {
        withContext(Dispatchers.IO) {
            for (channel: Channels in Channels.values()) {
                if (prefs.getIdForChannelId(channel.channelId) == 0L) {
                    setupChannel(context, channel.title)?.let { prefs.setIdForChannelId(channel.channelId, it) }
                    Log.d(TAG, "Created Channel: $channel")
                }
            }
        }
    }

    private suspend fun publishEvents(contentResolver: ContentResolver, mediaRepository: MediaRepository, events: List<Event>, channel: Channels): List<Long> {
        val channelId = prefs.getIdForChannelId(channel.channelId)
        val activeRecommendation = mediaRepository.getActiveRecommendation(channel.name)
        val activeOrDismissedRecommendations = activeRecommendation.map { it.recommendation.eventGuid }

        return events.filterNot { activeOrDismissedRecommendations.contains(it.guid) }.map {
            publishEvent(channelId, it, contentResolver).apply {
                mediaRepository.setRecommendationIdForEvent(this.first, this.second, channel.name)
            }
        }.map { it.second }
    }

    private suspend fun cleanupChannel(activeEvents: List<Event>, channel: Channels) {
        val activeRecommendation = mediaRepository.getActiveRecommendation(channel.name)
        val activeGuids = activeEvents.map { it.guid }
        activeRecommendation.filter { !activeGuids.contains(it.recommendation.eventGuid) }.forEach {
            context.contentResolver.delete(TvContractCompat.buildPreviewProgramUri(it.recommendation.programmId), null, null)
        }
    }

    private fun publishEvent(channelId: Long, event: Event, contentResolver: ContentResolver): Pair<Event, Long> {
        val toContentValues = eventToBuilder(channelId, event)
                .build().toContentValues()
        val programUri: Uri? = contentResolver.insert(TvContractCompat.PreviewPrograms.CONTENT_URI, toContentValues)
        return event to ContentUris.parseId(programUri)
    }

    private fun eventToBuilder(channelId: Long, event: Event): PreviewProgram.Builder {
        return PreviewProgram.Builder()
                .setChannelId(channelId)
                .setContentId(event.guid)
                .setType(TvContractCompat.PreviewPrograms.TYPE_EVENT)
                .setTitle(event.title)
                .setPosterArtUri(Uri.parse(event.thumbUrl))
                .setDescription(event.getExtendedDescription().toString())
                .setInternalProviderId(event.guid)
                .setDurationMillis(event.length.toInt() * 1000)
                .setInteractionCount(event.viewCount.toLong())
                .setInteractionType(TvContractCompat.PreviewProgramColumns.INTERACTION_TYPE_VIEWS)
                .setLive(false)
                .setLastPlaybackPositionMillis(event.progress.toInt())
                .setReleaseDate(event.releaseDate)
                .setIntentUri(Uri.parse("$deeplinkUriSchema://event/${event.guid}"))
    }

    private fun setupChannel(context: Context, @StringRes titleRes: Int): Long? {
        val builder = Channel.Builder()
        builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(context.resources.getString(titleRes))
                .setAppLinkIntentUri(Uri.parse("$deeplinkUriSchema://main"))

        val channelUri: Uri? = context.contentResolver.insert(
                TvContractCompat.Channels.CONTENT_URI,
                builder.build().toContentValues()
        )
        return if (channelUri != null) {
            val channelId = ContentUris.parseId(channelUri)
            val icon = context.resources.getDrawable(R.mipmap.ic_launcher).toBitmap(80, 80)

            ChannelLogoUtils.storeChannelLogo(context, channelId, icon)
            TvContractCompat.requestChannelBrowsable(context, channelId)
            channelId
        } else {
            null
        }
    }

    companion object {
        private val TAG = ChannelManager::class.java.simpleName
        private const val deeplinkUriSchema = "de.nicidienase.chaosflix.leanback"
    }
}
