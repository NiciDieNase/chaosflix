package de.nicidienase.chaosflix.touch.eventdetails

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.os.Environment
import android.util.Log
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.ChaosflixApplication
import de.nicidienase.chaosflix.touch.OfflineItemManager
import de.nicidienase.chaosflix.touch.sync.Downloader
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File

class DetailsViewModel(
		val database: ChaosflixDatabase,
		val recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)
	var writeExternalStorageAllowed: Boolean = false
	val offlineItemManager: OfflineItemManager = OfflineItemManager()

	fun getEventById(eventId: Long): LiveData<PersistentEvent> {
		downloader.updateRecordingsForEvent(eventId)
		return database.eventDao().findEventById(eventId)
	}

	fun getEventsByIds(ids: LongArray) = database.eventDao().findEventsByIds(ids)

	fun getRecordingForEvent(id: Long): LiveData<List<PersistentRecording>> {
		downloader.updateRecordingsForEvent(id)
		return database.recordingDao().findRecordingByEvent(id)
	}

	fun getBookmarkForEvent(id: Long): LiveData<WatchlistItem> = database.watchlistItemDao().getItemForEvent(id)

	fun createBookmark(apiId: Long) {
		Completable.fromAction {
			database.watchlistItemDao().saveItem(WatchlistItem(eventId = apiId))
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	fun removeBookmark(apiID: Long) {
		Completable.fromAction {
			database.watchlistItemDao().deleteItem(apiID)
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	fun download(event: PersistentEvent, recording: PersistentRecording)
			= offlineItemManager.download(event, recording)

	fun getOfflineItem(eventId: Long) = database.offlineEventDao().getByEventId(eventId)

	fun offlineItemExists(eventId: Long): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		getOfflineItem(eventId).observeForever({ event: OfflineEvent? ->
			result.postValue(event != null)
		})
		return result
	}

	fun fileExists(eventId: Long): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		getOfflineItem(eventId).observeForever({ event: OfflineEvent? ->
			result.postValue(if (event != null) File(event.localPath).exists() else false)
		})
		return result
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		Completable.fromAction {
			offlineItemManager.deleteOfflineItem(item)
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	fun deleteOfflineItem(itemId: Long): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		Completable.fromAction {
			val offlineEvent = database.offlineEventDao().getByEventIdSynchronous(itemId)
			deleteOfflineItem(offlineEvent);
			result.postValue(true);
		}.subscribeOn(Schedulers.io()).subscribe()
		return result
	}



	companion object {
		val TAG = DetailsViewModel::class.simpleName
	}
}