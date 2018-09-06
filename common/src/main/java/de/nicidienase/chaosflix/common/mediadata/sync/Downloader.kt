package de.nicidienase.chaosflix.common.mediadata.sync

import android.arch.lifecycle.LiveData
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import de.nicidienase.chaosflix.common.Util
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler

class Downloader(val recordingApi: RecordingService,
                 val database: ChaosflixDatabase) {

	private val threadHandler = ThreadHandler()

	enum class DownloaderState{
		RUNNING, DONE
	}

	fun updateConferencesAndGroups(): LiveData<LiveEvent<DownloaderState, ConferencesWrapper, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, ConferencesWrapper, String>>()
		threadHandler.runOnBackgroundThread {
			updateState.postValue(LiveEvent(DownloaderState.RUNNING,null, null))
			val response = recordingApi.getConferencesWrapper().execute()

			if(!response.isSuccessful){
				updateState.postValue(LiveEvent(state = DownloaderState.DONE, error = response.message()))
				return@runOnBackgroundThread
			}
			try {
				response.body()?.let { conferencesWrapper ->
					saveConferences(conferencesWrapper)
					updateState.postValue(LiveEvent(DownloaderState.DONE,conferencesWrapper))
				}
			} catch (e: Exception){
				updateState.postValue(LiveEvent(DownloaderState.DONE, error = "Error updating conferences."))
				e.printStackTrace()
			}
		}
		return updateState
	}

	private val TAG: String? = Downloader::class.simpleName

	fun updateEventsForConference(conference: PersistentConference) : LiveData<LiveEvent<DownloaderState, List<PersistentEvent>, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, List<PersistentEvent>, String>>()
		updateState.postValue(LiveEvent(DownloaderState.RUNNING))
		threadHandler.runOnBackgroundThread {
			val response = recordingApi.getConferenceByName(conference.acronym).execute()
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

	fun updateRecordingsForEvent(event: PersistentEvent) :
			LiveData<LiveEvent<DownloaderState, List<PersistentRecording>, String>> {
		val updateState = SingleLiveEvent<LiveEvent<DownloaderState, List<PersistentRecording>, String>>()
		updateState.postValue(LiveEvent(DownloaderState.RUNNING))
		threadHandler.runOnBackgroundThread {
			val response = recordingApi.getEventByGUID(event.guid).execute()
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

	fun saveConferences(conferencesWrapper: ConferencesWrapper): List<PersistentConference> {
		return conferencesWrapper.conferencesMap.map { entry ->
			val conferenceGroup: ConferenceGroup = getOrCreateConferenceGroup(entry.key)
			val conferenceList = entry.value
					.map { PersistentConference(it) }
					.map { it.conferenceGroupId = conferenceGroup.id; it }.toTypedArray()
			val conferenceIds = database.conferenceDao().insert(*conferenceList).toList()
			return@map conferenceList.zip(conferenceIds) { conf: PersistentConference, id: Long ->
				conf.id = id
				return@zip conf
			}
		}.flatten()
	}

	private fun getOrCreateConferenceGroup(name: String): ConferenceGroup{
		val conferenceGroup: ConferenceGroup?
				= database.conferenceGroupDao().getConferenceGroupByName(name)
		if (conferenceGroup != null) {
			return conferenceGroup
		}
		val group = ConferenceGroup(name)
		val index = Util.orderedConferencesList.indexOf(group.name)
		if (index != -1)
			group.index = index
		else if (group.name == "other conferences")
			group.index = 1_000_001
		group.id = database.conferenceGroupDao().insert(group)
		return group
	}

	private fun saveEvents(persistentConference: PersistentConference, events: List<Event>): List<PersistentEvent> {
		val persistantEvents = events.map { PersistentEvent(it,persistentConference.id) }
		val insertEventIds = database.eventDao().insert(*(persistantEvents.toTypedArray())).asList()
		val oldEvents = database.eventDao()
				.findEventsByConferenceSync(persistentConference.id)
				.filter { !insertEventIds.contains(it.id) }
				.toTypedArray()
		try {
			database.eventDao().delete(*oldEvents)
		} catch (ex: SQLiteConstraintException){
			Log.d(TAG,"SQLiteException",ex)
		}
		persistantEvents.zip(insertEventIds) {event: PersistentEvent, id: Long ->
			event.id = id
		}
		return persistantEvents
	}

	private fun saveRecordings(event: PersistentEvent,recordings: List<Recording>): List<PersistentRecording> {
		val persistentRecordings = recordings.map { PersistentRecording(it, event.id) }
		database.recordingDao().insert(*persistentRecordings.toTypedArray())
		return persistentRecordings
	}

}