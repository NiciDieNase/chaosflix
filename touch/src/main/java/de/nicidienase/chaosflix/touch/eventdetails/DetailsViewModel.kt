package de.nicidienase.chaosflix.touch.eventdetails

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRelatedEvent
import de.nicidienase.chaosflix.common.mediadata.network.RecordingService
import de.nicidienase.chaosflix.common.mediadata.sync.Downloader
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.util.ThreadHandler
import de.nicidienase.chaosflix.touch.OfflineItemManager
import java.io.File
class DetailsViewModel(
		val database: ChaosflixDatabase,
		recordingApi: RecordingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)
	var writeExternalStorageAllowed: Boolean = false
	val offlineItemManager: OfflineItemManager = OfflineItemManager(offlineEventDao = database.offlineEventDao())

	private val handler = ThreadHandler()

	fun setEvent(persistentEvent: PersistentEvent): LiveData<PersistentEvent> {
		downloader.updateRecordingsForEvent(persistentEvent)
		return database.eventDao().findEventByGuid(persistentEvent.guid)
	}

	fun getRecordingForEvent(persistentEvent: PersistentEvent): LiveData<List<PersistentRecording>> {
		downloader.updateRecordingsForEvent(persistentEvent)
		return database.recordingDao().findRecordingByEvent(persistentEvent.id)
	}

	fun getBookmarkForEvent(guid: String): LiveData<WatchlistItem> =
			database.watchlistItemDao().getItemForEvent(guid)

	fun createBookmark(guid: String) {
		handler.runOnBackgroundThread {
			database.watchlistItemDao().saveItem(WatchlistItem(eventGuid= guid))
		}
	}

	fun removeBookmark(guid: String) {
		handler.runOnBackgroundThread {
			database.watchlistItemDao().deleteItem(guid)
		}
	}

	fun download(event: PersistentEvent, recording: PersistentRecording)
			= offlineItemManager.download(event, recording)

	fun getOfflineItem(guid: String): OfflineEvent?
			= database.offlineEventDao().getByEventGuidSynchronous(guid)

	fun offlineItemExists(guid: String): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		handler.runOnBackgroundThread {
			result.postValue(getOfflineItem(guid) != null )
		}
		return result
	}

	fun fileExists(guid: String): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		handler.runOnBackgroundThread {
			val offlineItem = getOfflineItem(guid)
			result.postValue( offlineItem != null && File(offlineItem.localPath).exists())
		}
		return result
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		handler.runOnBackgroundThread {
			offlineItemManager.deleteOfflineItem(item)
		}
	}

	fun deleteOfflineItem(itemId: Long): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		handler.runOnBackgroundThread {
			database.offlineEventDao().deleteById(itemId)
			result.postValue(true)
		}
		return result
	}

	fun getRelatedEvents(event: PersistentEvent): LiveData<List<PersistentEvent>>{
		val data = MutableLiveData<List<PersistentEvent>>()
		handler.runOnBackgroundThread {
			val guids = database.relatedEventDao().getRelatedEventsForEventSync(event.id).map { it.relatedEventGuid }
			data.postValue(database.eventDao().findEventsByGUIDsSync(guids))
		}
		return data
	}

	companion object {
		val TAG = DetailsViewModel::class.simpleName
	}
}

