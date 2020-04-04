package de.nicidienase.chaosflix.common.mediadata

import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.nicidienase.chaosflix.common.ChaosflixDatabase
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
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class MediaRepository(
    private val recordingApi: RecordingService,
    private val database: ChaosflixDatabase
) {

    private val supervisorJob = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val conferenceGroupDao by lazy { database.conferenceGroupDao() }
    private val conferenceDao by lazy { database.conferenceDao() }
    private val eventDao: EventDao by lazy { database.eventDao() }
    private val recordingDao: RecordingDao by lazy { database.recordingDao() }
    private val relatedEventDao: RelatedEventDao by lazy { database.relatedEventDao() }
    private val watchlistItemDao: WatchlistItemDao by lazy { database.watchlistItemDao() }

    fun updateConferencesAndGroups(): SingleLiveEvent<LiveEvent<State, List<Conference>, String>> {
        val updateState = SingleLiveEvent<LiveEvent<State, List<Conference>, String>>()
            coroutineScope.launch(Dispatchers.IO) {
                updateState.postValue(LiveEvent(state = State.RUNNING))
                try {
                    val conferencesWrapper = recordingApi.getConferencesWrapperSuspending()
                    if (conferencesWrapper != null) {
                        val saveConferences = saveConferences(conferencesWrapper)
                        updateState.postValue(LiveEvent(State.DONE, data = saveConferences))
                    } else {
                        updateState.postValue(LiveEvent(State.DONE, error = "Error updating conferences."))
                    }
                } catch (e: IOException) {
                    updateState.postValue(LiveEvent(State.DONE, error = e.message))
                } catch (e: Exception) {
                    updateState.postValue(LiveEvent(State.DONE, error = "Error updating Conferences (${e.cause})"))
                    e.printStackTrace()
                }
        }
        return updateState
    }

    fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<State, List<Event>, String>> {
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
        return updateState }

    private suspend fun updateEventsForConferencesSuspending(conference: Conference): List<Event> {
        val conferenceByName = recordingApi.getConferenceByNameSuspending(conference.acronym)
        val events = conferenceByName?.events
        return if (events != null) {
            saveEvents(conference, events)
        } else {
            emptyList()
        }
    }

    fun updateRecordingsForEvent(event: Event): LiveData<LiveEvent<State, List<Recording>, String>> {
        val updateState = SingleLiveEvent<LiveEvent<State, List<Recording>, String>>()
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val eventDto = recordingApi.getEventByGUIDSuspending(event.guid)
                val recordingDtos = eventDto?.recordings
                if (recordingDtos != null) {
                    val recordings: List<Recording> = saveRecordings(event, recordingDtos)
                    updateState.postValue(LiveEvent(State.DONE, data = recordings))
                } else {
                    updateState.postValue(LiveEvent(State.DONE, error = "Error updating Recordings for ${event.title}"))
                }
            } catch (e: IOException) {
                updateState.postValue(LiveEvent(State.DONE, error = e.message))
            } catch (e: Exception) {
                updateState.postValue(LiveEvent(State.DONE, error = "Error updating Recordings for ${event.title} (${e.cause})"))
                e.printStackTrace() }
        }
        return updateState
    }

    suspend fun updateSingleEvent(guid: String): Event? = withContext(Dispatchers.IO) {
        val event = recordingApi.getEventByGUIDSuspending(guid)
        return@withContext if (event != null) {
            saveEvent(event)
        } else {
            null
        }
    }

    @WorkerThread
    fun deleteNonUserData() {
        with(database) {
            conferenceGroupDao().delete()
            conferenceDao().delete()
            eventDao().delete()
            recordingDao().delete()
            relatedEventDao().delete()
        }
    }

    @WorkerThread
    suspend fun saveConferences(conferencesWrapper: ConferencesWrapper): List<Conference> {
        return conferencesWrapper.conferencesMap.map { entry ->
            val conferenceGroup: ConferenceGroup = getOrCreateConferenceGroup(entry.key)
            val conferenceList = entry.value
                .map { Conference(it) }
                .map { it.conferenceGroupId = conferenceGroup.id; it }
            conferenceDao.updateOrInsert(*conferenceList.toTypedArray())
            conferenceGroupDao.deleteEmptyGroups()
            return@map conferenceList
        }.flatten()
    }

    private suspend fun getOrCreateConferenceGroup(name: String): ConferenceGroup {
        val conferenceGroup: ConferenceGroup? =
            conferenceGroupDao.getConferenceGroupByName(name)
        if (conferenceGroup != null) {
            return conferenceGroup
        }
        val group = ConferenceGroup(name)
        val index = ConferenceUtil.orderedConferencesList.indexOf(group.name)
        if (index != -1)
            group.index = index
        else if (group.name == "other conferences")
            group.index = 1_000_001
        group.id = conferenceGroupDao.insert(group)
        return group
    }

    private suspend fun saveEvents(persistentConference: Conference, events: List<EventDto>): List<Event> {
        val persistantEvents = events.map { Event(it, persistentConference.id) }
        eventDao.updateOrInsert(*persistantEvents.toTypedArray())
        persistantEvents.forEach {
            saveRelatedEvents(it)
        }
        return persistantEvents
    }

    private suspend fun saveEvent(event: EventDto): Event {
        val acronym = event.conferenceUrl.split("/").last()
        val conferenceId = conferenceDao.findConferenceByAcronym(acronym)?.id
            ?: updateConferencesAndGet(acronym)?.id

        checkNotNull(conferenceId) { "Could not find Conference for event" }

        val persistentEvent = Event(event, conferenceId)
        val id = eventDao.insert(persistentEvent)
        persistentEvent.id = id
        return persistentEvent
    }

    private suspend fun updateConferencesAndGet(acronym: String): Conference? {
        val response: Response<ConferencesWrapper>? = recordingApi.getConferencesWrapper().execute()
        val conferences = response?.body()?.let { conferencesWrapper ->
            return@let saveConferences(conferencesWrapper)
        }
        return conferences?.find { it.acronym == acronym }
    }

    private suspend fun saveRelatedEvents(event: Event): List<RelatedEvent> {
        val list = event.related?.map { it.parentEventId = event.id; it }
        relatedEventDao.updateOrInsert(*list?.toTypedArray() ?: emptyArray())
        return list ?: emptyList()
    }

    private suspend fun saveRecordings(event: Event, recordings: List<RecordingDto>): List<Recording> {
        val persistentRecordings = recordings.map { Recording(it, event.id) }
        recordingDao.updateOrInsert(*persistentRecordings.toTypedArray())
        return persistentRecordings
    }

    suspend fun findEventForUri(data: Uri): Event? {
        var event: Event? = eventDao.findEventForFrontendUrl(data.toString())

        val pathSegment = data.lastPathSegment
        if (event == null && pathSegment != null) {
            event = searchEvent(pathSegment)
        }

        return event
    }

    suspend fun findConferenceForUri(data: Uri): Conference? {
        val acronym = data.lastPathSegment
        if(acronym != null){
            return conferenceDao.findConferenceByAcronymSuspend(acronym)
        } else {
            error("missing path")
        }
    }

    suspend fun findEventByTitle(title: String): Event? {
        var event: Event? = eventDao.findEventByTitleSuspend(title)
        if (event == null) {
            event = searchEvent(title, true)
        }
        return event
    }

    private suspend fun searchEvent(queryString: String, updateConference: Boolean = false): Event? {
        val searchEvents = recordingApi.searchEvents(queryString)
        if (searchEvents.events.isNotEmpty()) {
            val eventDto = searchEvents.events[0]
            val conference = updateConferencesAndGet(eventDto.conferenceUrl.split("/").last())
            if (updateConference && conference != null) {
                updateEventsForConference(conference)
            }
            if (conference?.id != null) {
                var event = Event(eventDto, conference.id)
                eventDao.updateOrInsert(event)
                return event
            }
        }
        return null
    }

    suspend fun getAllOfflineEvents(): List<Long> = database.offlineEventDao().getAllDownloadReferences()

    suspend fun saveOrUpdate(watchlistItem: WatchlistItem) {
        watchlistItemDao.updateOrInsert(watchlistItem)
    }

    fun getReleatedEvents(event: Event, viewModelScope: CoroutineScope): LiveData<List<Event>> {
        val data = MutableLiveData<List<Event>>()
        viewModelScope.launch(Dispatchers.IO) {
            val guids = relatedEventDao.getRelatedEventsForEventSuspend(event.id)
            val relatedEvents: List<Event> = guids.mapNotNull { findEventForGuid(it) }
            if (guids.size != relatedEvents.size) {
                Log.e(TAG, "Could not find all related Events")
            }
            data.postValue(relatedEvents)
        }
        return data
    }

    private suspend fun findEventForGuid(guid: String): Event? {
        val eventFromDB = eventDao.findEventByGuidSync(guid)
        if (eventFromDB != null) {
            return eventFromDB
        }
        return updateSingleEvent(guid)
    }

    companion object {
        private val TAG = MediaRepository::class.java.simpleName
    }

    enum class State {
        DONE, RUNNING
    }
}
