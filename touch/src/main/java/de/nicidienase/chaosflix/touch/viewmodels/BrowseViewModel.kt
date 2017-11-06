package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.sync.Downloader
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class BrowseViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
) : ViewModel() {

    val downloader = Downloader(recordingApi, database)

    fun getConferenceGroups(): LiveData<List<ConferenceGroup>> {
        downloader.updateConferencesAndGroups()
        return database.conferenceGroupDao().getAll()
    }

    fun getConference(conferenceId: Long): LiveData<PersistentConference>
            = database.conferenceDao().findConferenceById(conferenceId)

    fun getConferencesByGroup(groupId: Long): LiveData<List<PersistentConference>>
            = database.conferenceDao().findConferenceByGroup(groupId)

    fun getEventById(eventId: Long): LiveData<PersistentEvent> {
        downloader.updateRecordingsForEvent(eventId)
        return database.eventDao().findEventById(eventId)
    }

    fun getEventsforConference(conferenceId: Long): LiveData<List<PersistentEvent>> {
        downloader.updateEventsForConference(conferenceId)
        return database.eventDao().findEventsByConference(conferenceId)
    }

    fun getRecordingForEvent(id: Long): LiveData<List<PersistentRecording>> {
        downloader.updateRecordingsForEvent(id)
        return database.recordingDao().findRecordingByEvent(id)
    }

    fun getBookmarkForEvent(id: Long): LiveData<WatchlistItem> = database.watchlistItemDao().getItemForEvent(id)

    fun getBookmarkedEvents(): LiveData<List<PersistentEvent>> = database.eventDao().findBookmarkedEvents()

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
}