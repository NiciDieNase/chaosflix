package de.nicidienase.chaosflix.touch

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.*
import android.database.sqlite.SQLiteConstraintException
import android.databinding.ObservableField
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.touch.eventdetails.DetailsViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File

class OfflineItemManager(downloadRefs: List<Long>? = emptyList()) {

	val downloadStatus: MutableMap<Long, DownloadStatus>

	val downloadManager: DownloadManager
			= ChaosflixApplication.APPLICATION_CONTEXT
			.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

	init {
		downloadStatus = HashMap()
		downloadRefs?.map { downloadStatus.put(it, DownloadStatus()) }
	}

	val offlineEventDao: OfflineEventDao = DatabaseFactory.database.offlineEventDao()

	fun updateDownloadStatus() {
		updateDownloads(downloadStatus.keys.toLongArray())
	}

	fun updateDownloadStatus(offlineEvents: List<OfflineEvent>) {
		if (offlineEvents != null && offlineEvents.size > 0) {
			val downloadRef = offlineEvents.map { it.downloadReference }.toTypedArray().toLongArray() ?: longArrayOf()
			updateDownloads(downloadRef)
		}
	}

	fun updateDownloads(downloadRefs: LongArray) {
		val cursor = downloadManager.query(DownloadManager.Query().setFilterById(*downloadRefs))

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
					item?.status = status
				} else {
					downloadStatus.put(
							id,
							DownloadStatus(statusText, bytesSoFar, bytesTotal, status))
				}
			} while (cursor.moveToNext())
		}
	}

	fun download(event: PersistentEvent, recording: PersistentRecording): LiveData<Boolean> {
		val result = MutableLiveData<Boolean>()
		Completable.fromAction {
			val offlineEvent = offlineEventDao.getByEventIdSynchronous(event.eventId)
			if (offlineEvent == null) {
				val downloadManager: DownloadManager
						= ChaosflixApplication.APPLICATION_CONTEXT
						.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

				val request = DownloadManager.Request(Uri.parse(recording.recordingUrl))
				request.setTitle(event.title)
				request.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_MOVIES, DOWNLOAD_DIR + recording.filename)
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
				request.setVisibleInDownloadsUi(true)

				val sharedPref: SharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(ChaosflixApplication.APPLICATION_CONTEXT);
				val allow_metered = sharedPref.getBoolean("allow_metered_networks", false)

				if(!allow_metered){
					request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
					request.setAllowedOverMetered(false)
				}

				val downloadReference = downloadManager.enqueue(request)
				Log.d(DetailsViewModel.TAG, "download started $downloadReference")

				val cancelHandler = DownloadCancelHandler(downloadReference)
				val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
				ChaosflixApplication.APPLICATION_CONTEXT.registerReceiver(cancelHandler, intentFilter)

				try {
					offlineEventDao.insert(
							OfflineEvent(eventId = event.eventId,
									recordingId = recording.recordingId,
									localPath = getDownloadDir() + recording.filename,
									downloadReference = downloadReference))
				} catch (ex: SQLiteConstraintException) {
					Log.d(DetailsViewModel.TAG, ex.message)
				}
				result.postValue(true)
			}
		}.subscribeOn(Schedulers.io()).subscribe()
		return result
	}

	fun deleteOfflineItem(downloadId: Long) {
		val offlineEvent = offlineEventDao.getByDownloadReferenceSyncrounous(downloadId)
		deleteOfflineItem(offlineEvent)
	}

	fun deleteOfflineItem(item: OfflineEvent) {
		downloadManager.remove(item.downloadReference)
		val file = File(item.localPath)
		if (file.exists()) file.delete()
		offlineEventDao.deleteById(item.id)
	}

	inner class DownloadStatus(statusText: String = "",
	                           currentBytes: Int = 0,
	                           totalBytes: Int = 0,
	                           var status: Int = DownloadManager.STATUS_FAILED) {
		val statusText: ObservableField<String> = ObservableField()
		val currentBytes: ObservableField<Int> = ObservableField()
		val totalBytes: ObservableField<Int> = ObservableField()

		init {
			this.statusText.set(statusText)
			this.currentBytes.set(currentBytes)
			this.totalBytes.set(totalBytes)
		}
	}

	class DownloadCancelHandler(val id: Long) : BroadcastReceiver() {
		private val TAG = DownloadCancelHandler::class.simpleName

		override fun onReceive(p0: Context?, p1: Intent?) {
			val downloadId = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			if (downloadId != null && downloadId == id) {
				val offlineItemManager = OfflineItemManager(listOf(downloadId))
				offlineItemManager.updateDownloadStatus()
				val downloadStatus = offlineItemManager.downloadStatus[downloadId]
				if (downloadStatus?.status == DownloadManager.STATUS_FAILED) {
					Log.d(TAG, "Deleting item")
					Completable.fromAction {
						offlineItemManager.deleteOfflineItem(downloadId)
					}.subscribeOn(Schedulers.io()).subscribe()
				}
				p0?.unregisterReceiver(this);
			}
		}
	}

	private fun getDownloadDir(): String {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path + DOWNLOAD_DIR;
	}

	companion object {
		val DOWNLOAD_DIR = "/chaosflix/"
	}
}