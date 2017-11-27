package de.nicidienase.chaosflix.touch.browse

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableField
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.ChaosflixApplication
import de.nicidienase.chaosflix.touch.sync.Downloader
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class BrowseViewModel(
		val database: ChaosflixDatabase,
		val recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)

	val downloadManager: DownloadManager
			= ChaosflixApplication.APPLICATION_CONTEXT
			.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

	val downloadStatus: MutableMap<Long, DownloadStatus> = HashMap()

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

	fun getLivestreams()
			= streamingApi.getStreamingConferences()

	fun getOfflineEvents(): LiveData<List<OfflineEvent>> = database.offlineEventDao().getAll()

	fun getEventById(eventId: Long) = database.eventDao().findEventById(eventId)

	fun getRecordingByid(recordingId: Long) = database.recordingDao().findRecordingById(recordingId)

	init {
		getOfflineEvents().observeForever(Observer {
			val downloadRef = it?.map { it.downloadReference }?.map { downloadStatus.put(it,DownloadStatus()) }
		})
	}

	fun updateDownloadStatus() {
		Completable.fromAction {
			getOfflineEvents().observeForever(Observer {
				val downloadRef = it?.map { it.downloadReference }?.toTypedArray()?.toLongArray() ?: longArrayOf()
				val cursor = downloadManager.query(DownloadManager.Query().setFilterById(*downloadRef))

				if (cursor.moveToFirst()) {
					do {
						val columnId = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
						val id = cursor.getLong(columnId)
						val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
						val status = cursor.getInt(columnIndex)
						val bytesSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
						val bytesSoFar = cursor.getInt(bytesSoFarIndex)
						val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
						val bytesTotal = cursor.getInt(bytesTotalIndex)

						val statusText: String =
								when (status) {
									DownloadManager.STATUS_RUNNING -> "Running"
									DownloadManager.STATUS_FAILED -> "Failed"
									DownloadManager.STATUS_PAUSED -> "Paused"
									DownloadManager.STATUS_SUCCESSFUL -> "Successful"
									DownloadManager.STATUS_PENDING -> "Pending"
									else -> "UNKNOWN"
								}
						if (downloadStatus.containsKey(id)) {
							val item = downloadStatus[id]
							if (item != null) {
								item.statusText.set(statusText)
								item.currentBytes.set(bytesSoFar)
								item.totalBytes.set(bytesTotal)
							}
						} else {
							downloadStatus.put(id, DownloadStatus(statusText, bytesSoFar, bytesTotal))
						}
					} while (cursor.moveToNext())
				}
			})
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	inner class DownloadStatus(status: String = "", currentBytes: Int = 0, totalBytes: Int = 0) {
		val statusText: ObservableField<String> = ObservableField()
		val currentBytes: ObservableField<Int> = ObservableField()
		val totalBytes: ObservableField<Int> = ObservableField()

		init {
			this.statusText.set(status)
			this.currentBytes.set(currentBytes)
			this.totalBytes.set(totalBytes)
		}

	}
}