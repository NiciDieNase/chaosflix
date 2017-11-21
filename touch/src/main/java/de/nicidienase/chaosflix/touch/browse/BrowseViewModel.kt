package de.nicidienase.chaosflix.touch.browse

import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.sync.Downloader

class BrowseViewModel(
		val database: ChaosflixDatabase,
		val recordingApi: RecordingService,
		val streamingApi: StreamingService
) : ViewModel() {

	val downloader = Downloader(recordingApi, database)

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
}