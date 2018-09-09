package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.mediadata.network.StreamingService
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import de.nicidienase.chaosflix.touch.OfflineItemManager
import retrofit2.Response
import java.io.IOException

class BrowseViewModel(
		val database: ChaosflixDatabase,
		recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)
	lateinit var offlineItemManager: OfflineItemManager
	private val handler = ThreadHandler()

	init {
		handler.runOnBackgroundThread {
			val downloadRefs =
					database
							.offlineEventDao()
							.getAllSync()
							.map { it.downloadReference }
			offlineItemManager = OfflineItemManager(downloadRefs, database.offlineEventDao())
		}
	}

	fun getConferenceGroups(): LiveData<List<ConferenceGroup>> {
		downloader.updateConferencesAndGroups()
		return database.conferenceGroupDao().getAll()
	}

	fun getConference(conferenceId: Long)
			= database.conferenceDao().findConferenceById(conferenceId)

	fun getConferencesByGroup(groupId: Long)
			= database.conferenceDao().findConferenceByGroup(groupId)

	fun getEventsforConference(conference: PersistentConference)
			= database.eventDao().findEventsByConference(conference.id)

	fun updateConferences()
			= downloader.updateConferencesAndGroups()

	fun updateEventsForConference(conference: PersistentConference)
			= downloader.updateEventsForConference(conference)

	fun getBookmarkedEvents(): LiveData<List<PersistentEvent>> = updateAndGetEventsForGuids {
		database
				.watchlistItemDao()
				.getAllSync().map { it.eventGuid } }

	fun getInProgressEvents(): LiveData<List<PersistentEvent>> = updateAndGetEventsForGuids {
		database
				.playbackProgressDao()
				.getAllSync()
				.map { it.eventGuid } }

	private fun updateAndGetEventsForGuids(guidProvider: ()->List<String>):LiveData<List<PersistentEvent>>{
		val result = MutableLiveData<List<PersistentEvent>>()
		handler.runOnBackgroundThread {
			val guids = guidProvider.invoke()
			val events = guids.map { downloader.updateSingleEvent(it) }.filterNotNull()
			result.postValue(events)
		}
		return result
	}

	private val TAG = BrowseViewModel::class.simpleName

	fun getLivestreams(): LiveData<List<LiveConference>> {
		// TODO use LiveEvent for Result
		val result = MutableLiveData<List<LiveConference>>()
		handler.runOnBackgroundThread {
			val request: Response<List<LiveConference>>
			try {
				request = streamingApi.getStreamingConferences().execute()
			} catch (e: IOException){
				result.postValue(emptyList())
				return@runOnBackgroundThread
			}
			if(!request.isSuccessful){
				result.postValue(emptyList())
				return@runOnBackgroundThread
			}
			result.postValue(request.body())
		}
		return result
	}

	fun getOfflineEvents(): LiveData<List<Pair<OfflineEvent,PersistentEvent>>> {
		val result = MutableLiveData<List<Pair<OfflineEvent, PersistentEvent>>>()
		handler.runOnBackgroundThread {
			val offlineEventMap = database.offlineEventDao().getAllSync()
					.map { it.eventGuid to it }.toMap()
			val persistentEventMap = database.eventDao().findEventsByGUIDsSync(offlineEventMap.keys.toList())
					.map { it.guid to it }.toMap()

			val resultList = ArrayList<Pair<OfflineEvent, PersistentEvent>>()
			for (key in offlineEventMap.keys){
				val offlineEvent = offlineEventMap[key]
				var persistentEvent: PersistentEvent? = persistentEventMap[key]
				if(persistentEvent == null){
					persistentEvent = downloader.updateSingleEvent(key)
				}
				if(persistentEvent != null && offlineEvent != null){
					resultList.add(Pair(offlineEvent, persistentEvent))
				}
			}
			result.postValue(resultList)
		}
		return result
	}

	fun getEventById(eventId: Long) = database.eventDao().findEventById(eventId)

	fun getRecordingByid(recordingId: Long) = database.recordingDao().findRecordingById(recordingId)

	fun updateDownloadStatus() {
		handler.runOnBackgroundThread {
			offlineItemManager.updateDownloadStatus(database.offlineEventDao().getAllSync())
		}
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		handler.runOnBackgroundThread {
			offlineItemManager.deleteOfflineItem(item)
		}
	}
}


