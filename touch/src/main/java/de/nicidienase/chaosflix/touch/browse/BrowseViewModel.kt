package de.nicidienase.chaosflix.touch.browse

import android.app.DownloadManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.ChaosflixApplication
import de.nicidienase.chaosflix.touch.sync.Downloader

class BrowseViewModel(
		val database: ChaosflixDatabase,
		val recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)

	val downloadManager: DownloadManager
			= ChaosflixApplication.APPLICATION_CONTEXT
			.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

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
}