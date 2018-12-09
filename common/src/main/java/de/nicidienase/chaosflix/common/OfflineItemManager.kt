package de.nicidienase.chaosflix.common

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.databinding.ObservableField
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.util.ThreadHandler
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import java.io.File

class OfflineItemManager(context: Context,
                         val offlineEventDao: OfflineEventDao,
                         val preferencesManager: PreferencesManager) {

	val downloadStatus: MutableMap<Long, DownloadStatus> = HashMap()

	val downloadManager: DownloadManager
			= context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

	private val handler = ThreadHandler()

	private val applicationContext: Context = context.applicationContext

	fun addDownloadRefs(refs: List<Long>){
		refs.map { downloadStatus.put(it, DownloadStatus()) }
	}

	fun updateDownloadStatus() {
		updateDownloads(downloadStatus.keys.toLongArray())
	}

	fun updateDownloadStatus(offlineEvents: List<OfflineEvent>) {
		if (offlineEvents.size > 0) {
			val downloadRef = offlineEvents.map { it.downloadReference }.toTypedArray().toLongArray()
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
		handler.runOnBackgroundThread {


			val offlineEvent = offlineEventDao.getByEventGuidSync(event.guid)
			if (offlineEvent == null) {

				val request = DownloadManager.Request(Uri.parse(recording.recordingUrl))
				request.setTitle(event.title)

                request.setDestinationUri(
                        Uri.withAppendedPath(Uri.fromFile(
                                File(getDownloadDir())), recording.filename))

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
				request.setVisibleInDownloadsUi(true)

				if(!preferencesManager.getMetered()){
					request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
					request.setAllowedOverMetered(false)
				}

				val downloadReference = downloadManager.enqueue(request)
				Log.d(DetailsViewModel.TAG, "download started $downloadReference")

				val cancelHandler = DownloadCancelHandler(applicationContext, downloadReference, offlineEventDao, preferencesManager)
				val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
				applicationContext.registerReceiver(cancelHandler, intentFilter)

				try {
					offlineEventDao.insert(
							OfflineEvent(eventGuid = event.guid,
									recordingId = recording.id,
									localPath = getDownloadDir() + recording.filename,
									downloadReference = downloadReference))
				} catch (ex: SQLiteConstraintException) {
					Log.d(DetailsViewModel.TAG, ex.message)
				}
				result.postValue(true)
			}
		}
		return result
	}

	fun deleteOfflineItem(downloadId: Long) {
		val offlineEvent = offlineEventDao.getByDownloadReferenceSync(downloadId)
		if (offlineEvent != null) {
			deleteOfflineItem(offlineEvent)
		}
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

	class DownloadCancelHandler(val context: Context,
	                            val id: Long,
	                            val offlineEventDao: OfflineEventDao,
	                            val preferencesManager: PreferencesManager) : BroadcastReceiver() {
		private val TAG = DownloadCancelHandler::class.simpleName

		val handler = ThreadHandler()

		override fun onReceive(p0: Context?, p1: Intent?) {
			val downloadId = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			if (downloadId != null && downloadId == id) {
				val offlineItemManager = OfflineItemManager(context, offlineEventDao, preferencesManager)
				offlineItemManager.addDownloadRefs(listOf(downloadId))
				offlineItemManager.updateDownloadStatus()
				val downloadStatus = offlineItemManager.downloadStatus[downloadId]
				if (downloadStatus?.status == DownloadManager.STATUS_FAILED) {
					Log.d(TAG, "Deleting item")
					handler.runOnBackgroundThread {
						offlineItemManager.deleteOfflineItem(downloadId)
					}
				}
				p0?.unregisterReceiver(this);
			}
		}
	}

	private fun getMovieDir(): String {
		val sharedPref: SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		var dir = sharedPref.getString("download_folder", null)
		if (dir == null) {
			dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path
		}
		return dir
	}

	private fun getDownloadDir(): String {
		return getMovieDir() + DOWNLOAD_DIR;
	}

	companion object {
		val DOWNLOAD_DIR = "/chaosflix/"
	}
}