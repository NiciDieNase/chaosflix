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

class BrowseViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
) : ViewModel() {

    val downloader = Downloader(recordingApi, database)

    fun updateEverything(){
        Completable.fromAction { downloader.updateEverything() }
    }

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

    fun createBookmark(apiId: Long) {
        database.watchlistItemDao().getItemForEvent(apiId)
                .observeForever { watchlistItem: WatchlistItem? ->
                    if (watchlistItem == null) {
                        database.watchlistItemDao()
                                .saveItem(WatchlistItem(apiId, apiId))
                    }
                }
    }

    fun getBookmark(apiId: Long): LiveData<WatchlistItem> {
        return database.watchlistItemDao().getItemForEvent(apiId)
    }

    fun removeBookmark(apiID: Long) {
        database.watchlistItemDao().deleteItem(apiID)
    }
}