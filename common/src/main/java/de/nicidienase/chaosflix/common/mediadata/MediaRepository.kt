package de.nicidienase.chaosflix.common.mediadata

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
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
import de.nicidienase.chaosflix.common.mediadata.sync.IDownloader
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.util.ConferenceUtil
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class MediaRepository(
    private val recordingApi: RecordingService,
    private val database: ChaosflixDatabase
): IDownloader {

    private val supervisorJob = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private val conferenceGroupDao by lazy { database.conferenceGroupDao() }
    private val conferenceDao by lazy { database.conferenceDao() }
    private val eventDao: EventDao by lazy { database.eventDao() }
    private val recordingDao: RecordingDao by lazy { database.recordingDao() }
    private val relatedEventDao: RelatedEventDao by lazy { database.relatedEventDao() }

    override fun updateConferencesAndGroups(): SingleLiveEvent<LiveEvent<IDownloader.State, List<Conference>, String>> {
        val updateState = SingleLiveEvent<LiveEvent<IDownloader.State, List<Conference>, String>>()
            coroutineScope.launch (Dispatchers.IO){
                updateState.postValue(LiveEvent(state = IDownloader.State.RUNNING))
                try {
                    val conferencesWrapper = recordingApi.getConferencesWrapperSuspending()
                    if(conferencesWrapper != null){
                        val saveConferences = saveConferences(conferencesWrapper)
                        updateState.postValue(LiveEvent(IDownloader.State.DONE, data = saveConferences))
                    } else {
                        updateState.postValue(LiveEvent(IDownloader.State.DONE, error = "Error updating conferences."))
                    }
                } catch (e: IOException) {
                    updateState.postValue(LiveEvent(IDownloader.State.DONE, error = e.message))
                } catch (e: Exception) {
                    updateState.postValue(LiveEvent(IDownloader.State.DONE, error = "Error updating Conferences (${e.cause})"))
                    e.printStackTrace()
                }
        }
        return updateState
    }

    override fun updateEventsForConference(conference: Conference): LiveData<LiveEvent<IDownloader.State, List<Event>, String>> {
        val updateState = SingleLiveEvent<LiveEvent<IDownloader.State, List<Event>, String>>()
        updateState.postValue(LiveEvent(IDownloader.State.RUNNING))
        coroutineScope.launch {
            try {
                val list =
                    updateEventsForConferencesSuspending(conference)
                updateState.postValue(LiveEvent(IDownloader.State.DONE, data = list))
            } catch (e: IOException) {
                updateState.postValue(LiveEvent(IDownloader.State.DONE, error = e.message))
            } catch (e: Exception) {
                updateState.postValue(LiveEvent(IDownloader.State.DONE, error = "Error updating Events for ${conference.acronym} (${e.cause})"))
                e.printStackTrace()
            }
        }
        return updateState    }

    private suspend fun updateEventsForConferencesSuspending(conference: Conference): List<Event> {
        val conferenceByName = recordingApi.getConferenceByNameSuspending(conference.acronym)
        val events = conferenceByName?.events
        return if (events != null) {
            saveEvents(conference, events)
        } else {
            emptyList()
        }
    }

    override fun updateRecordingsForEvent(event: Event): LiveData<LiveEvent<IDownloader.State, List<Recording>, String>> {
        val updateState = SingleLiveEvent<LiveEvent<IDownloader.State, List<Recording>, String>>()
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val eventDto = recordingApi.getEventByGUIDSuspending(event.guid)
                val recordingDtos = eventDto?.recordings
                if(recordingDtos != null){
                    val recordings: List<Recording> = saveRecordings(event, recordingDtos)
                    updateState.postValue(LiveEvent(IDownloader.State.DONE, data = recordings))
                } else {
                    updateState.postValue(LiveEvent(IDownloader.State.DONE, error = "Error updating Recordings for ${event.title}"))
                }
            } catch (e: IOException) {
                updateState.postValue(LiveEvent(IDownloader.State.DONE, error = e.message))
            } catch (e: Exception) {
                updateState.postValue(LiveEvent(IDownloader.State.DONE, error = "Error updating Recordings for ${event.title} (${e.cause})"))
                e.printStackTrace() }
        }
        return updateState
    }

    override suspend fun updateSingleEvent(guid: String): Event? {
        val event = recordingApi.getEventByGUIDSuspending(guid)
        return if(event != null) {
            saveEvent(event)
        } else {
            null
        }
    }

    @WorkerThread
    override fun deleteNonUserData() {
        with(database) {
            conferenceGroupDao().delete()
            conferenceDao().delete()
            eventDao().delete()
            recordingDao().delete()
            relatedEventDao().delete()
        }
    }

    @WorkerThread
    override fun saveConferences(conferencesWrapper: ConferencesWrapper): List<Conference> {
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

    override fun getOrCreateConferenceGroup(name: String): ConferenceGroup {
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


    override fun saveEvents(persistentConference: Conference, events: List<EventDto>): List<Event> {
        val persistantEvents = events.map { Event(it, persistentConference.id) }
        eventDao.updateOrInsert(*persistantEvents.toTypedArray())
        persistantEvents.forEach {
            saveRelatedEvents(it)
        }
        return persistantEvents
    }

    override fun saveEvent(event: EventDto): Event {
        val acronym = event.conferenceUrl.split("/").last()
        val conferenceId = conferenceDao.findConferenceByAcronymSync(acronym)?.id
            ?: updateConferencesAndGet(acronym)

        check(conferenceId != -1L) { "Could not find Conference for event" }

        val persistentEvent = Event(event, conferenceId)
        val id = eventDao.insert(persistentEvent)
        persistentEvent.id = id
        return persistentEvent
    }

    override fun updateConferencesAndGet(acronym: String): Long {
        val response: Response<ConferencesWrapper>? = recordingApi.getConferencesWrapper().execute()
        val conferences = response?.body()?.let { conferencesWrapper ->
            return@let saveConferences(conferencesWrapper)
        }
        return conferences?.find { it.acronym == acronym }?.id ?: -1
    }

    override fun saveRelatedEvents(event: Event): List<RelatedEvent> {
        val list = event.related?.map { it.parentEventId = event.id; it }
        relatedEventDao.updateOrInsert(*list?.toTypedArray() ?: emptyArray())
        return list ?: emptyList()
    }


    override fun saveRecordings(event: Event, recordings: List<RecordingDto>): List<Recording> {
        val persistentRecordings = recordings.map { Recording(it, event.id) }
        recordingDao.updateOrInsert(*persistentRecordings.toTypedArray())
        return persistentRecordings
    }

    suspend fun getAllOfflineEvents(): List<Long> = database.offlineEventDao().getAllDownloadReferences()
}