package de.nicidienase.chaosflix.touch

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.ObservableField
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File

class OfflineItemManager(downloadRefs: List<Long>?) {

	val downloadStatus: MutableMap<Long, DownloadStatus>

	val downloadManager: DownloadManager
			= ChaosflixApplication.APPLICATION_CONTEXT
			.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

	init {
		downloadStatus = HashMap()
		downloadRefs?.map { downloadStatus.put(it, DownloadStatus()) }
	}

	fun updateDownloadStatus(offlineEvents: LiveData<List<OfflineEvent>>) {
		Completable.fromAction {
			offlineEvents.observeForever(Observer {
				if (it != null && it.size > 0) {
					val downloadRef = it.map { it.downloadReference }.toTypedArray().toLongArray() ?: longArrayOf()
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
								item?.statusText?.set(statusText)
								item?.currentBytes?.set(bytesSoFar)
								item?.totalBytes?.set(bytesTotal)
							} else {
								downloadStatus.put(id, DownloadStatus(statusText, bytesSoFar, bytesTotal))
							}
						} while (cursor.moveToNext())
					}
				}
			})
		}.subscribeOn(Schedulers.io()).subscribe()
	}

	fun deleteOfflineItem(item: OfflineEvent): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		Completable.fromAction {
			val file = File(item.localPath)
			if (file.exists()) file.delete()
			result.postValue(true)
		}.subscribeOn(Schedulers.io()).subscribe()
		return result
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