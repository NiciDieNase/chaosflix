package de.nicidienase.chaosflix.touch.eventdetails

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

class DetailsViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
) : ViewModel() {

    val downloader = Downloader(recordingApi, database)

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
}