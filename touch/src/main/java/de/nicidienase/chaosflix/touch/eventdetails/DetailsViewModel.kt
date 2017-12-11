package de.nicidienase.chaosflix.touch.eventdetails

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.ChaosflixApplication
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

	fun download(event: PersistentEvent, recording: PersistentRecording): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		database.offlineEventDao().getByEventId(event.eventId).observeForever {
			if (it == null) {
				val downloadManager: DownloadManager
						= ChaosflixApplication.APPLICATION_CONTEXT
						.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

				val request = DownloadManager.Request(Uri.parse(recording.recordingUrl))
				request.setTitle(event.title)
				request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, DOWNLOAD_DIR + recording.filename)
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
				request.setVisibleInDownloadsUi(true)

				// TODO make configurable
				request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
				request.setAllowedOverMetered(false)

				val downloadReference = downloadManager.enqueue(request)

				Log.d(TAG, "download started $downloadReference")

				Completable.fromAction {
					database.offlineEventDao().insert(
							OfflineEvent(eventId = event.eventId, recordingId = recording.recordingId,
									localPath = getDownloadDir() + recording.filename, downloadReference = downloadReference))
					result.postValue(true)
				}.subscribeOn(Schedulers.io()).subscribe()
			} else {
				result.postValue(false)
			}
		}
		return result
	}

	fun getOfflineItem(eventId: Long) = database.offlineEventDao().getByEventId(eventId)

	fun offlineItemExists(eventId: Long): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		getOfflineItem(eventId).observeForever({ event: OfflineEvent? ->
			result.postValue(if (event != null) File(event.localPath).exists() else false)
		})
		return result
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		Completable.fromAction {
			val file = File(item.localPath)
			if (file.exists()) file.delete()
			database.offlineEventDao().deleteById(item.id)
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	private fun getDownloadDir(): String {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path + DOWNLOAD_DIR;
	}

	companion object {
		val DOWNLOAD_DIR = "/chaosflix/"
		val TAG = DetailsViewModel::class.simpleName
	}
}