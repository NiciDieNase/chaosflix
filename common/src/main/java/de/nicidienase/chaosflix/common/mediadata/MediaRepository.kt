package de.nicidienase.chaosflix.common.mediadata

import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.EventDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RecordingDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEventDao
import de.nicidienase.chaosflix.common.mediadata.network.RecordingApi
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.common.userdata.entities.progress.ProgressEventView
import de.nicidienase.chaosflix.common.userdata.entities.recommendations.Recommendation
import de.nicidienase.chaosflix.common.userdata.entities.recommendations.RecommendationDao
import de.nicidienase.chaosflix.common.userdata.entities.recommendations.RecommendationEventView
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import javax.net.ssl.SSLHandshakeException

class MediaRepository(
    private val recordingApi: RecordingApi,
    database: ChaosflixDatabase
) {

    private val supervisorJob = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val conferenceGroupDao by lazy { database.conferenceGroupDao() }
    private val conferenceDao by lazy { database.conferenceDao() }
    private val eventDao: EventDao by lazy { database.eventDao() }
    private val recordingDao: RecordingDao by lazy { database.recordingDao() }
    private val relatedEventDao: RelatedEventDao by lazy { database.relatedEventDao() }
    private val watchlistItemDao: WatchlistItemDao by lazy { database.watchlistItemDao() }
    private val playbackProgressDao: PlaybackProgressDao by lazy { database.playbackProgressDao() }
    private val recommendationDao: RecommendationDao by lazy { database.recommendationDao() }

    suspend fun getEventSync(eventId: Long) = eventDao.findEventByIdSync(eventId)

    fun getEvent(eventId: Long): LiveData<Event?> = eventDao.findEventById(eventId)
    internal val apiOperations = ApiOperations(recordingApi, coroutineScope)
    internal val databaseOperations = DatabaseOperations(database)

    internal inner class ApiOperations(private val recordingApi: RecordingApi, private val coroutineScope: CoroutineScope) {

        fun updateConferencesAndGroups(): SingleLiveEvent<LiveEvent<State, List<Conference>, String>> {
            val updateState = SingleLiveEvent<LiveEvent<State, List<Conference>, String>>()
            coroutineScope.launch(Dispatchers.IO) {
                updateConferencesAndGroupsInternal(updateState)
            }
            return updateState
        }

        internal suspend fun updateConferencesAndGroupsInternal(updateState: SingleLiveEvent<LiveEvent<State, List<Conference>, String>>) = withContext(Dispatchers.IO) {
            updateState.postValue(LiveEvent(state = State.RUNNING))
            val response = withNetworkErrorHandling { recordingApi.getConferencesWrapperSuspending() }
            if (response != null && response.isSuccessful) {
                val conferencesWrapper = response.body()
                if (conferencesWrapper != null) {
                    val saveConferences = saveConferenceGroup(conferencesWrapper)
                    updateState.postValue(LiveEvent(State.DONE, data = saveConferences))
                } else {
                    updateState.postValue(LiveEvent(State.DONE, error = "Error updating conferences."))
                }
            } else {
                updateState.postValue(LiveEvent(State.DONE, error = "Error updating conferences. ${response?.message()}"))
                Log.e(TAG, "Error: ${response?.message()} ${response?.errorBody()}")
            }
        }

        internal fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<State, List<Event>, String>> {
            val updateState = SingleLiveEvent<LiveEvent<State, List<Event>, String>>()
            updateState.postValue(LiveEvent(State.RUNNING))
            coroutineScope.launch {
                try {
                    val list =
                            updateEventsForConferencesSuspending(conference)
                    updateState.postValue(LiveEvent(State.DONE, data = list))
                } catch (e: IOException) {
                    updateState.postValue(LiveEvent(State.DONE, error = e.message))
                } catch (e: Exception) {
                    updateState.postValue(LiveEvent(State.DONE, error = "Error updating Events for ${conference.acronym} (${e.cause})"))
                    e.printStackTrace()
                }
            }
            return updateState
        }

        private suspend fun updateEventsForConferencesSuspending(conference: Conference): List<Event> {
            val response = withNetworkErrorHandling { recordingApi.getConferenceByNameSuspending(conference.acronym) }
            return if (response != null && response.isSuccessful) {
                val conferenceByName = response.body()
                val events = conferenceByName?.events
                if (events != null) {
                    databaseOperations.saveEvents(conference, events)
                } else {
                    emptyList()
                }
            } else {
                Log.e(TAG, response?.message())
                emptyList()
            }
        }

        internal suspend fun updateRecordingsForEvent(event: Event): List<Recording>? {
            return try {
                val response = withNetworkErrorHandling { recordingApi.getEventByGUIDSuspending(event.guid) }
                return if (response != null && response.isSuccessful) {
                    val eventDto = response.body()
                    eventDto?.let { databaseOperations.saveEvent(it) }
                    val recordingDtos = eventDto?.recordings
                    if (recordingDtos != null) {
                        databaseOperations.saveRecordings(event, recordingDtos)
                    } else {
                        null
                    }
                } else {
                    Log.e(TAG, "Error: ${response?.message()} ${response?.errorBody()}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        suspend fun updateSingleEvent(guid: String): Event? = withContext(Dispatchers.IO) {
            val response = withNetworkErrorHandling { recordingApi.getEventByGUIDSuspending(guid) }
            if (response == null || !response.isSuccessful) {
                Log.e(TAG, "Error: ${response?.message()} ${response?.errorBody()}")
                return@withContext null
            }
            val event = response.body()
            return@withContext if (event != null) {
                try {
                    databaseOperations.saveEvent(event)
                } catch (ex: IllegalArgumentException) {
                    Log.e(TAG, "could not save event", ex)
                    null
                }
            } else {
                null
            }
        }

        internal suspend fun updateConferencesAndGet(acronym: String): Conference? {
            val response: Response<ConferencesWrapper>? = withNetworkErrorHandling { recordingApi.getConferencesWrapper() }
            val conferences = response?.body()?.let { conferencesWrapper ->
                return@let saveConferenceGroup(conferencesWrapper)
            }
            return conferences?.find { it.acronym == acronym }
        }

        internal suspend fun searchEvent(queryString: String, updateConference: Boolean = false): Event? {
            val response = withNetworkErrorHandling { recordingApi.searchEvents(queryString) }
            if (response == null || !response.isSuccessful) {
                Log.e(TAG, "Error: ${response?.message()} ${response?.errorBody()}")
                return null
            }
            val searchEvents = response.body()
            if (searchEvents != null && searchEvents.events.isNotEmpty()) {
                val eventDto = searchEvents.events[0]
                try {
                    val conference = updateConferencesAndGet(eventDto.conferenceUrl.split("/").last())
                    if (updateConference && conference != null) {
                        updateEventsForConference(conference)
                    }
                    if (conference?.id != null) {
                        val event = Event(eventDto, conference.id)
                        databaseOperations.updateOrInsert(event)
                        return event
                    }
                } catch (ex: IllegalArgumentException) {
                    Log.e(TAG, "could not load conference", ex)
                    return null
                }
            }
            return null
        }

        private suspend fun <T> withNetworkErrorHandling(block: suspend () -> T): T? {
            return try {
                block.invoke()
            } catch (e: SSLHandshakeException) {
                Log.e(TAG, e.message, e)
                AnalyticsWrapperImpl.trackException(e)
                null
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
                AnalyticsWrapperImpl.trackException(e)
                null
            }
        }
    }

    fun updateConferencesAndGroups() = apiOperations.updateConferencesAndGroups()
    fun updateEventsForConference(conference: Conference) = apiOperations.updateEventsForConference(conference)
    suspend fun updateRecordingsForEvent(event: Event) = apiOperations.updateRecordingsForEvent(event)
    suspend fun updateSingleEvent(guid: String): Event? = apiOperations.updateSingleEvent(guid)

    internal inner class DatabaseOperations(database: ChaosflixDatabase) {

        private val conferenceGroupDao by lazy { database.conferenceGroupDao() }
        private val conferenceDao by lazy { database.conferenceDao() }
        private val eventDao: EventDao by lazy { database.eventDao() }
        private val recordingDao: RecordingDao by lazy { database.recordingDao() }
        private val relatedEventDao: RelatedEventDao by lazy { database.relatedEventDao() }
        private val watchlistItemDao: WatchlistItemDao by lazy { database.watchlistItemDao() }
        private val playbackProgressDao: PlaybackProgressDao by lazy { database.playbackProgressDao() }
        private val offlineEventDao: OfflineEventDao by lazy { database.offlineEventDao() }

        suspend fun deleteNonUserData() = withContext(Dispatchers.IO) {
            conferenceGroupDao.delete()
            conferenceDao.delete()
            eventDao.delete()
            recordingDao.delete()
            relatedEventDao.delete()
        }

        fun getBookmark(guid: String): LiveData<WatchlistItem?> = watchlistItemDao.getItemForEvent(guid)

        suspend fun addBookmark(guid: String) = withContext(Dispatchers.IO) {
            watchlistItemDao.saveItem(WatchlistItem(eventGuid = guid))
        }

        suspend fun deleteBookmark(guid: String) = withContext(Dispatchers.IO) {
            watchlistItemDao.deleteItem(guid)
        }

        suspend fun getAllOfflineEvents(): List<Long> = offlineEventDao.getAllDownloadReferences()

        suspend fun saveConferenceGroup(conferencesWrapper: ConferencesWrapper): List<Conference> {
            val conferences = conferencesWrapper.conferencesMap.map { entry ->
                saveConferenceGroup(entry.key, entry.value)
            }
            conferenceGroupDao.deleteEmptyGroups()
            return conferences.flatten()
        }

        private suspend fun saveConferenceGroup(group: String, conferenceDtos: List<ConferenceDto>): List<Conference> {
            val conferenceGroup: ConferenceGroup = databaseOperations.getOrCreateConferenceGroup(group)
            val conferences = conferenceDtos.map { Conference(it, conferenceGroup.id) }
            conferenceDao.updateOrInsert(*conferences.toTypedArray())
            return conferences
        }

        internal suspend fun saveEvent(event: EventDto): Event {
            val acronym = event.conferenceUrl.split("/").last()
            val conferenceId = conferenceDao.findConferenceByAcronym(acronym)?.id
                    ?: apiOperations.updateConferencesAndGet(acronym)?.id

            checkNotNull(conferenceId) { "Could not find Conference for event" }

            val persistentEvent = Event(event, conferenceId)
            val id = eventDao.updateOrInsert(persistentEvent)
            persistentEvent.id = id
            return persistentEvent
        }

        suspend fun findConferenceForUri(data: Uri): Conference? {
            val acronym = data.lastPathSegment
            if (acronym != null) {
                return conferenceDao.findConferenceByAcronymSuspend(acronym)
            } else {
                error("missing path")
            }
        }

        internal suspend fun saveEvents(persistentConference: Conference, events: List<EventDto>): List<Event> {
            val persistantEvents = events.map { Event(it, persistentConference.id) }
            eventDao.updateOrInsert(*persistantEvents.toTypedArray())
            persistantEvents.forEach {
                it.related = saveRelatedEvents(it)
            }
            return persistantEvents
        }

        suspend fun findEventByTitle(title: String): Event? {
            return eventDao.findEventByTitleSuspend(title) ?: apiOperations.searchEvent(title, true)
        }

        suspend fun saveOrUpdate(watchlistItem: WatchlistItem) {
            watchlistItemDao.updateOrInsert(watchlistItem)
        }

        internal suspend fun getOrCreateConferenceGroup(name: String): ConferenceGroup {
            val conferenceGroup: ConferenceGroup? =
                    conferenceGroupDao.getConferenceGroupByName(name)
            return if (conferenceGroup != null) {
                conferenceGroup
            } else {
                val group = ConferenceGroup(name)
                val index = ConferenceUtil.orderedConferencesList.indexOf(group.name)
                if (index != -1)
                    group.index = index
                else if (group.name == "other conferences")
                    group.index = 1_000_001
                group.id = conferenceGroupDao.insert(group)
                group
            }
        }

        private suspend fun saveRelatedEvents(event: Event): List<RelatedEvent> {
            val list: List<RelatedEvent> = event.related?.map { it.parentEventId = event.id; it }
                    ?: emptyList()
            relatedEventDao.updateOrInsert(*list.toTypedArray())
            return list
        }

        internal suspend fun saveRecordings(event: Event, recordings: List<RecordingDto>): List<Recording> {
            val persistentRecordings = recordings.map { Recording(it, event.id) }
            recordingDao.updateOrInsert(*persistentRecordings.toTypedArray())
            return persistentRecordings
        }

        suspend fun updateOrInsert(event: Event) = eventDao.updateOrInsert(event)
        suspend fun findEventForFrontendUrl(url: String): Event? = eventDao.findEventForFrontendUrl(url)
        fun getRelatedEvents(event: Event): LiveData<List<Event>> {
            coroutineScope.launch {
                val relatedEvents = relatedEventDao.getRelatedEventsForEventSuspend(event.id)
                relatedEvents.forEach {
                    updateSingleEvent(it.relatedEventGuid)
                }
            }
            return relatedEventDao.newGetReletedEventsForEvent(event.id)
        }
    }

    suspend fun deleteNonUserData() = databaseOperations.deleteNonUserData()
    fun getBookmark(guid: String): LiveData<WatchlistItem?> = databaseOperations.getBookmark(guid)
    suspend fun addBookmark(guid: String) = databaseOperations.addBookmark(guid)
    suspend fun deleteBookmark(guid: String) = databaseOperations.deleteBookmark(guid)
    suspend fun getAllOfflineEvents(): List<Long> = databaseOperations.getAllOfflineEvents()

    @WorkerThread
    suspend fun saveConferenceGroup(conferencesWrapper: ConferencesWrapper): List<Conference> = databaseOperations.saveConferenceGroup(conferencesWrapper)

    suspend fun findEventByTitle(title: String): Event? = databaseOperations.findEventByTitle(title)

    suspend fun saveOrUpdate(watchlistItem: WatchlistItem) = databaseOperations.saveOrUpdate(watchlistItem)

    suspend fun findEventForUri(data: Uri): Event? {
        var event: Event? = databaseOperations.findEventForFrontendUrl(data.toString())

        val pathSegment = data.lastPathSegment
        if (event == null && pathSegment != null) {
            event = apiOperations.searchEvent(pathSegment)
        }
        return event
    }

    fun getReleatedEvents(event: Event): LiveData<List<Event>> = databaseOperations.getRelatedEvents(event)
    suspend fun findConferenceForUri(data: Uri): Conference? = databaseOperations.findConferenceForUri(data)

    fun getReleatedEvents(eventId: Long): LiveData<List<Event>> {
        coroutineScope.launch {
            val relatedEvents = relatedEventDao.getRelatedEventsForEventSuspend(eventId)
            relatedEvents.forEach {
                updateSingleEvent(it.relatedEventGuid)
            }
        }
        return relatedEventDao.newGetReletedEventsForEvent(eventId)
    }

    suspend fun findEventForGuid(guid: String): Event? {
        return eventDao.findEventByGuidSync(guid) ?: updateSingleEvent(guid)
    }
    fun findRecordingsForEvent(eventId: Long): LiveData<List<Recording>> {
        return recordingDao.findRecordingByEvent(eventId)
    }

    suspend fun getEventsInProgress(): List<ProgressEventView> {
        val progress = playbackProgressDao.getAllWithEventSync()
        progress.forEach { it.event?.progress = it.progress.progress }
        return progress
    }

    suspend fun getTopEvents(count: Int): List<Event> {
        return eventDao.getTopViewedEvents(count)
    }

    suspend fun getNewestConferences(count: Int): List<Conference> {
        return conferenceDao.getLatestConferences(count)
    }

    suspend fun getBookmarkedEvents(): List<Event> = eventDao.findBookmarkedEventsSync()

    suspend fun getHomescreenRecommendations(): List<Event> {
        return getTopEvents(10)
    }

    suspend fun getActiveRecommendation(channel: String): List<RecommendationEventView> {
        return recommendationDao.getAllForChannel(channel)
    }

    suspend fun setRecommendationIdForEvent(event: Event, id: Long, channel: String) = withContext(Dispatchers.IO) {
        recommendationDao.insert(Recommendation(eventGuid = event.guid, channel = channel, programmId = id))
    }

    suspend fun resetRecommendationId(programmId: Long) = withContext(Dispatchers.IO) {
        recommendationDao.markDismissed(programmId)
    }

    suspend fun findEvents(queryString: String, page: Int = 1): SearchResponse? = withContext(Dispatchers.IO) {
        val eventsResponse = recordingApi.searchEvents(queryString, page)
        return@withContext if (eventsResponse.isSuccessful) {
            val total = eventsResponse.headers()["total"]?.toInt() ?: 0
            val links = parseLink(eventsResponse.headers()["link"])
            val events = eventsResponse.body()?.events?.map {databaseOperations.saveEvent(it) } ?: emptyList()

            SearchResponse(events, total, links)
        } else {
            null
        }
    }

    data class SearchResponse(val events: List<Event>, val total: Int, val links: Map<String, String>) {
        val hasNext: Boolean = hasLink("next")
        val hasPrev: Boolean = hasLink("prev")

        private fun hasLink(key: String) = links.keys.contains(key)
    }

    companion object {
        private val TAG = MediaRepository::class.java.simpleName

        fun parseLink(link: String?): Map<String, String> {
            if (link == null) {
                return emptyMap()
            }
            val links = link.split(",")
            return links.associate {
                val pair = it.split(";")
                pair[1].substringAfter("\"").substringBefore("\"") to pair[0].substringAfter("<").substringBefore(">")
            }
        }
    }

    enum class State {
        DONE, RUNNING
    }
}
