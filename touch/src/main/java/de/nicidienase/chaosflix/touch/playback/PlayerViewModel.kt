package de.nicidienase.chaosflix.touch.playback

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal class PlayerViewModel(val database: ChaosflixDatabase,
                               val recordingApi: RecordingService,
                               val streamingApi: StreamingService) : ViewModel() {
    fun getPlaybackProgress(apiID: Long): LiveData<PlaybackProgress>
            = database.playbackProgressDao().getProgressForEvent(apiID)

    fun setPlaybackProgress(eventId: Long, progress: Long) {
        Single.fromCallable {
            database.playbackProgressDao().saveProgress(PlaybackProgress(eventId, progress))
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    fun deletePlaybackProgress(eventId: Long) {
        Single.fromCallable {
            database.playbackProgressDao().deleteItem(eventId)
        }
    }
}
