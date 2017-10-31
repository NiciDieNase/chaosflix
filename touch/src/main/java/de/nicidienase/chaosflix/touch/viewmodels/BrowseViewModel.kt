package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.ViewModel
import android.util.Log
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.Recording
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by felix on 12.10.17.
 */
class BrowseViewModel(
        val database: ChaosflixDatabase,
        val recordingApi: RecordingService,
        val streamingApi: StreamingService
    ) : ViewModel(){

    fun getConferencesWrapper(): Observable<ConferencesWrapper>
            = recordingApi.getConferences().subscribeOn(Schedulers.io())

    fun getConferencesByGroup(group: String): Observable<MutableList<Conference>>
            = recordingApi.getConferences().map { t: ConferencesWrapper -> t.getListForTag(group) }

    fun getConference(mConferenceId: Long): Observable<Conference>
            = recordingApi.getConference(mConferenceId).subscribeOn(Schedulers.io())

    fun getEvent(apiID: Long): Observable<Event>
            = recordingApi.getEvent(apiID).subscribeOn(Schedulers.io())

    fun getRecording(id: Long): Observable<Recording>
            = recordingApi.getRecording(id).subscribeOn(Schedulers.io())

    fun getStreamingConferences(): Observable<List<LiveConference>>
            = streamingApi.getStreamingConferences().subscribeOn(Schedulers.io())



    fun createBookmark(apiId: Long) {
        database.watchlistItemDao().getItemForEvent(apiId)
                .subscribe { watchlistItem: WatchlistItem? ->
                    if (watchlistItem == null) {
                        database.watchlistItemDao()
                                .saveItem(WatchlistItem(apiId, apiId))
                    }
                }
    }

    fun getBookmark(apiId: Long): Flowable<WatchlistItem> {
        return database.watchlistItemDao().getItemForEvent(apiId)
    }

    fun removeBookmark(apiID: Long) {
        getBookmark(apiID).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { watchlistItem -> database.watchlistItemDao().deleteItem(watchlistItem) }
    }
}