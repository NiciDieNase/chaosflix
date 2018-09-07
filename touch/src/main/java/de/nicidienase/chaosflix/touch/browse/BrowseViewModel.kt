package de.nicidienase.chaosflix.touch.browse

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.OfflineItemManager
import de.nicidienase.chaosflix.touch.sync.Downloader
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class BrowseViewModel(
		val database: ChaosflixDatabase,
		val recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)
	lateinit var offlineItemManager: OfflineItemManager

	init {
		getOfflineEvents().observeForever(Observer {
			val downloadRefs = it?.map { it.downloadReference } ?: emptyList()
			offlineItemManager = OfflineItemManager(downloadRefs, database.offlineEventDao())
		})
	}

	fun getConferenceGroups()
			= database.conferenceGroupDao().getAll()

	fun getConference(conferenceId: Long)
			= database.conferenceDao().findConferenceById(conferenceId)

	fun getConferencesByGroup(groupId: Long)
			= database.conferenceDao().findConferenceByGroup(groupId)

	fun getEventsforConference(conferenceId: Long)
			= database.eventDao().findEventsByConference(conferenceId)

	fun updateConferences()
			= downloader.updateConferencesAndGroups()

	fun updateEventsForConference(conferenceId: Long)
			= downloader.updateEventsForConference(conferenceId)

	fun getBookmarkedEvents()
			= database.eventDao().findBookmarkedEvents()

	fun getInProgressEvents()
			= database.eventDao().findInProgressEvents()

	private val TAG = BrowseViewModel::class.simpleName

	fun getLivestreams(): LiveData<List<LiveConference>> {
		val result = MutableLiveData<List<LiveConference>>()
		streamingApi.getStreamingConferences()
				.subscribeOn(Schedulers.io())
				.subscribe({
					result.postValue(it)
				}, { t ->
					Log.d(TAG, t.message, t)
					result.postValue(emptyList())
				})
		return result
	}

	fun getOfflineEvents(): LiveData<List<OfflineEvent>> = database.offlineEventDao().getAll()

	fun getEventById(eventId: Long) = database.eventDao().findEventById(eventId)

	fun getRecordingByid(recordingId: Long) = database.recordingDao().findRecordingById(recordingId)

	fun updateDownloadStatus() {
		Completable.fromAction {
			offlineItemManager.updateDownloadStatus(database.offlineEventDao().getAllSynchronous())
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		Completable.fromAction {
			offlineItemManager.deleteOfflineItem(item)
		}.subscribeOn(Schedulers.io()).subscribe()
	}
}

