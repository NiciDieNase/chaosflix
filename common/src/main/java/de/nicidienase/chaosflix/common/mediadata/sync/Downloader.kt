package de.nicidienase.chaosflix.common.mediadata.sync

import android.arch.lifecycle.LiveData
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.ChaosflixUtil
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEvent
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import retrofit2.Response
import java.io.IOException

class Downloader(private val recordingApi: RecordingService,
                 private val database: ChaosflixDatabase) {

	private val threadHandler = ThreadHandler()

	enum class DownloaderState{
		RUNNING, DONE
	}

	fun updateConferencesAndGroups(): LiveData<LiveEvent<DownloaderState, List<Conference>, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, List<Conference>, String>>()
		threadHandler.runOnBackgroundThread {
			updateState.postValue(LiveEvent(DownloaderState.RUNNING,null, null))
			val response: Response<ConferencesWrapper>?
			try {
				response = recordingApi.getConferencesWrapper().execute()
			} catch (e: IOException){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = e.message))
				return@runOnBackgroundThread
			}

			if(!response.isSuccessful){
				updateState.postValue(LiveEvent(state = DownloaderState.DONE, error = response.message()))
				return@runOnBackgroundThread
			}
			try {
				response.body()?.let { conferencesWrapper ->
					val saveConferences = saveConferences(conferencesWrapper)
					updateState.postValue(LiveEvent(DownloaderState.DONE,data = saveConferences))
				}
			} catch (e: Exception){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = "Error updating conferences."))
				e.printStackTrace()
			}
		}
		return updateState
	}

	private val TAG: String? = Downloader::class.simpleName

	fun updateEventsForConference(conference: Conference) : LiveData<LiveEvent<DownloaderState, List<Event>, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, List<Event>, String>>()
		updateState.postValue(LiveEvent(DownloaderState.RUNNING))
		threadHandler.runOnBackgroundThread {
			val response: Response<ConferenceDto>?
			try {
				response = recordingApi.getConferenceByName(conference.acronym).execute()
			} catch (e: IOException){
				updateState.postValue(LiveEvent(DownloaderState.DONE,error = e.message))
				return@runOnBackgroundThread
			}
			if(!response.isSuccessful){
				updateState.postValue(LiveEvent(DownloaderState.DONE,error = response.message()))
				return@runOnBackgroundThread
			}
			try {
				val persistentEvents = response.body()?.events?.let { events ->
					return@let saveEvents(conference, events)
				}
				updateState.postValue(LiveEvent(DownloaderState.DONE, data = persistentEvents))
			} catch (e: Exception){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = "Error updating Events for ${conference.acronym}"))
				e.printStackTrace()
			}
		}
		return updateState
	}

	fun updateRecordingsForEvent(event: Event) :
			LiveData<LiveEvent<DownloaderState, List<Recording>, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, List<Recording>, String>>()
		updateState.postValue(LiveEvent(DownloaderState.RUNNING))
		threadHandler.runOnBackgroundThread {
			val response: Response<EventDto>?
			try {
				response = recordingApi.getEventByGUID(event.guid).execute()
			} catch (e: IOException){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = e.message))
				return@runOnBackgroundThread
			}
			if(!response.isSuccessful){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = response.message()))
				return@runOnBackgroundThread
			}
			try {
				val recordings = response.body()?.recordings?.let { recordings ->
					return@let saveRecordings(event, recordings)
				}
				updateState.postValue(LiveEvent(DownloaderState.DONE, data = recordings))
			} catch (e: Exception){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = "Error updating Recordings for ${event.title}"))
				e.printStackTrace()}
		}
		return updateState
	}

	fun updateSingleEvent(guid: String): Event? {
		val request: Response<EventDto>?
		try {
			request = recordingApi.getEventByGUID(guid).execute()
		} catch (e: IOException){
			return null
		}
		if(!request.isSuccessful){
			return null
		}
		val body = request.body()
		if(body != null){
			return saveEvent(body)
		} else {
			return null
		}
	}

	fun deleteNonUserData(){
		with(database){
			conferenceGroupDao().delete()
			conferenceDao().delete()
			eventDao().delete()
			recordingDao().delete()
			relatedEventDao().delete()
		}

	}

	private fun saveConferences(conferencesWrapper: ConferencesWrapper): List<Conference> {
		return conferencesWrapper.conferencesMap.map { entry ->
			val conferenceGroup: ConferenceGroup = getOrCreateConferenceGroup(entry.key)
			val conferenceList = entry.value
					.map { Conference(it) }
					.map { it.conferenceGroupId = conferenceGroup.id; it }
			database.conferenceDao().updateOrInsert(*conferenceList.toTypedArray())
			database.conferenceGroupDao().deleteEmptyGroups()
			return@map conferenceList
		}.flatten()
	}

	private fun getOrCreateConferenceGroup(name: String): ConferenceGroup{
		val conferenceGroup: ConferenceGroup?
				= database.conferenceGroupDao().getConferenceGroupByName(name)
		if (conferenceGroup != null) {
			return conferenceGroup
		}
		val group = ConferenceGroup(name)
		val index = ChaosflixUtil.orderedConferencesList.indexOf(group.name)
		if (index != -1)
			group.index = index
		else if (group.name == "other conferences")
			group.index = 1_000_001
		group.id = database.conferenceGroupDao().insert(group)
		return group
	}

	private fun saveEvents(persistentConference: Conference, events: List<EventDto>): List<Event> {
		val persistantEvents = events.map { Event(it,persistentConference.id) }
		database.eventDao().updateOrInsert(*persistantEvents.toTypedArray())
		persistantEvents.forEach{
			saveRelatedEvents(it)
		}
		return persistantEvents
	}

	private fun saveEvent(event: EventDto): Event {
		val split = event.conferenceUrl.split("/")
		val acronym = split[split.size - 1]
		val conferenceId = database.conferenceDao().findConferenceByAcronymSync(acronym)?.id
				?: updateConferencesAndGet(acronym)

		if(conferenceId == -1L){
			throw IllegalStateException("Could not find Conference for event")
		}

		val persistentEvent = Event(event, conferenceId)
		val id = database.eventDao().insert(persistentEvent)
		persistentEvent.id = id
		return persistentEvent
	}

	private fun updateConferencesAndGet(acronym: String): Long{
		val response: Response<ConferencesWrapper>? = recordingApi.getConferencesWrapper().execute()
		val conferences = response?.body()?.let { conferencesWrapper ->
			return@let saveConferences(conferencesWrapper)
		}
		return conferences?.find { it.acronym == acronym }?.id ?: -1
	}

	private fun saveRelatedEvents(event: Event): List<RelatedEvent> {
		val list = event.related?.map { it.parentEventId = event.id; it }
		database.relatedEventDao().updateOrInsert(*list?.toTypedArray()?: emptyArray())
		return list ?: emptyList()
	}

	private fun saveRecordings(event: Event, recordings: List<RecordingDto>): List<Recording> {
		val persistentRecordings = recordings.map { Recording(it, event.id) }
		database.recordingDao().updateOrInsert(*persistentRecordings.toTypedArray())
		return persistentRecordings
	}
}