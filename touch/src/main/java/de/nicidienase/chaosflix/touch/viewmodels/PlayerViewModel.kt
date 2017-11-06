package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal class PlayerViewModel(val database: ChaosflixDatabase,
                               val recordingApi: RecordingService,
                               val streamingApi: StreamingService) : ViewModel() {
    fun getPlaybackProgress(apiID: Long): LiveData<PlaybackProgress>
            = database.playbackProgressDao().getProgressForEvent(apiID)

    fun setPlaybackProgress(event: PersistentEvent, progress: Long) {
        Single.fromCallable {
            if(event.length- progress > 10){
                database.playbackProgressDao().saveProgress(PlaybackProgress(event.eventId, progress))
            } else {
                database.playbackProgressDao().saveProgress(PlaybackProgress(event.eventId, 0))
            }
        }.subscribeOn(Schedulers.io()).subscribe()
    }
}
