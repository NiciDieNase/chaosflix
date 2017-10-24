package de.nicidienase.chaosflix.touch

import android.arch.lifecycle.ViewModel
import android.util.Log
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.PlaybackProgress
import de.nicidienase.chaosflix.common.entities.WatchlistItem
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

    private val TAG = BrowseViewModel::class.simpleName

    fun getConferencesWrapper(): Observable<ConferencesWrapper> {
        return recordingApi.getConferences()
                .doOnError({ throwable -> Log.d(TAG, throwable.cause.toString()) })
                .subscribeOn(Schedulers.io())
    }

    fun getConferencesByGroup(group: String): Observable<List<Conference>> {
        return recordingApi.conferences!!.map { conf -> conf?.conferencesBySeries.get(group) }
    }

    fun getConference(mConferenceId: Int): Observable<Conference> {
        return recordingApi.getConference(mConferenceId.toLong())
                .subscribeOn(Schedulers.io())
    }

    fun getEvent(apiID: Int): Observable<Event> {
        return recordingApi.getEvent(apiID.toLong())
                .subscribeOn(Schedulers.io())
    }

    fun getRecording(id: Long): Observable<Recording> {
        return recordingApi.getRecording(id)
                .subscribeOn(Schedulers.io())
    }

    fun getStreamingConferences(): Observable<List<LiveConference>> {
        return streamingApi.getStreamingConferences()
                .subscribeOn(Schedulers.io())
    }

    fun setPlaybackProgress(apiId: Int, progress: Long) {
        database.playbackProgressDao().getProgressForEvent(apiId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { playbackProgress: PlaybackProgress? ->
                    if(playbackProgress != null){
                        playbackProgress.progress = progress
                        database.playbackProgressDao().saveProgress(playbackProgress)
                    } else {
                        database.playbackProgressDao().saveProgress(
                                PlaybackProgress(apiId,progress))
                    }
                }
    }

    fun getPlaybackProgress(apiID: Int): Flowable<PlaybackProgress> {
        return database.playbackProgressDao().getProgressForEvent(apiID)
    }

    fun createBookmark(apiId: Int) {
        database.watchlistItemDao().getItemForEvent(apiId)
                .subscribe { watchlistItem: WatchlistItem? ->
                    if (watchlistItem == null) {
                        database.watchlistItemDao()
                                .saveItem(WatchlistItem(apiId, apiId))
                    }
                }
    }

    fun getBookmark(apiId: Int): Flowable<WatchlistItem> {
        return database.watchlistItemDao().getItemForEvent(apiId)
    }

    fun removeBookmark(apiID: Int) {
        getBookmark(apiID).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { watchlistItem -> database.watchlistItemDao().deleteItem(watchlistItem) }
    }
}