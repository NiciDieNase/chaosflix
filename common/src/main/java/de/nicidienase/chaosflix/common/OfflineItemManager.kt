package de.nicidienase.chaosflix.common

import android.Manifest
import android.app.DownloadManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteConstraintException
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import android.util.Log
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.ThreadHandler
import de.nicidienase.chaosflix.common.viewmodel.DetailsViewModel
import java.io.File

class OfflineItemManager(
    context: Context,
    private val offlineEventDao: OfflineEventDao,
    private val preferencesManager: PreferencesManager
) {

    val downloadStatus: MutableMap<Long, DownloadStatus> = HashMap()

    private val downloadManager: DownloadManager = context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val handler = ThreadHandler()

    private val applicationContext: Context = context.applicationContext

    fun addDownloadRefs(refs: List<Long>) {
        refs.map { downloadStatus.put(it, DownloadStatus()) }
    }

    fun updateDownloadStatus() {
        updateDownloads(downloadStatus.keys.toLongArray())
    }

    fun updateDownloadStatus(offlineEvents: List<OfflineEvent>) {
        if (offlineEvents.isNotEmpty()) {
            val downloadRef = offlineEvents.map { it.downloadReference }.toTypedArray().toLongArray()
            updateDownloads(downloadRef)
        }
    }

    private fun updateDownloads(downloadRefs: LongArray) {
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

                val statusIconRes: Int =
                        when (status) {
                            DownloadManager.STATUS_RUNNING -> R.drawable.ic_download
                            DownloadManager.STATUS_FAILED -> R.drawable.ic_error
                            DownloadManager.STATUS_PAUSED -> R.drawable.ic_paused
                            DownloadManager.STATUS_SUCCESSFUL -> R.drawable.ic_done
                            DownloadManager.STATUS_PENDING -> R.drawable.ic_download
                            else -> R.drawable.ic_error
                        }
                val statusIcon = applicationContext.resources.getDrawable(statusIconRes, null)

                downloadStatus[id] = DownloadStatus(status, statusIcon, bytesSoFar, bytesTotal)
            } while (cursor.moveToNext())
        }
    }

    fun download(event: Event, recording: Recording): LiveData<LiveEvent<State, String, Exception>> {
        val result = MutableLiveData<LiveEvent<State, String, Exception>>()
        result.postValue(LiveEvent(State.Downloading))
        handler.runOnBackgroundThread {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                result.postValue(LiveEvent(State.PermissionRequired))
            } else {
                val offlineEvent = offlineEventDao.getByEventGuidSync(event.guid)
                if (offlineEvent == null) {

                    val request = DownloadManager.Request(Uri.parse(recording.recordingUrl))
                    request.setTitle(event.title)

                    request.setDestinationUri(
                            Uri.withAppendedPath(Uri.fromFile(
                                    File(getDownloadDir())), recording.filename))

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    request.setVisibleInDownloadsUi(true)

                    if (!preferencesManager.getMetered()) {
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
                    result.postValue(LiveEvent(State.Done))
                }
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

    fun deleteOfflineItem(guid: String) {
        offlineEventDao.getByEventGuidSync(guid)?.let {
            deleteOfflineItem(it)
        }
    }

    fun deleteOfflineItem(item: OfflineEvent) {
        downloadManager.remove(item.downloadReference)
        val file = File(item.localPath)
        if (file.exists()) file.delete()
        offlineEventDao.deleteById(item.id)
    }

    inner class DownloadStatus(
        val status: Int = DownloadManager.STATUS_PENDING,
        val statusIcon: Drawable = applicationContext.resources.getDrawable(R.drawable.ic_download, null),
        val currentBytes: Int = 0,
        val totalBytes: Int = 0
    )

    class DownloadCancelHandler(
        val context: Context,
        val id: Long,
        private val offlineEventDao: OfflineEventDao,
        private val preferencesManager: PreferencesManager
    ) : BroadcastReceiver() {
        private val TAG = DownloadCancelHandler::class.java.simpleName

        val handler = ThreadHandler()

        override fun onReceive(p0: Context?, p1: Intent?) {
            val downloadId = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
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
                p0?.unregisterReceiver(this)
            }
        }
    }

    private fun getMovieDir(): String {
        val sharedPref: SharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
        var dir = sharedPref.getString("download_folder", null)
        return dir ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path
    }

    private fun getDownloadDir(): String {
        return getMovieDir() + DOWNLOAD_DIR
    }

    companion object {
        const val DOWNLOAD_DIR = "/chaosflix/"
    }

    enum class State {
        Downloading, Done, PermissionRequired
    }
}