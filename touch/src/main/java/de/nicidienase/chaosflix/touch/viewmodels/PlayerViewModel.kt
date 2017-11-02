package de.nicidienase.chaosflix.touch.viewmodels

import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal class PlayerViewModel(val database: ChaosflixDatabase,
                               val recordingApi: RecordingService,
                               val streamingApi: StreamingService) : ViewModel() {
    fun getRecording(id: Long): Flowable<List<PersistentRecording>> = database.recordingDao().findRecordingById(id)

    fun getEvent(id: Long): Flowable<PersistentEvent> = database.eventDao().findEventById(id)

    fun getPlaybackProgress(apiID: Long): Flowable<PlaybackProgress>
            = database.playbackProgressDao().getProgressForEvent(apiID)

    fun setPlaybackProgress(apiId: Long, progress: Long) {
        Single.fromCallable {
            database.playbackProgressDao().saveProgress(PlaybackProgress(apiId, progress))
        }.subscribeOn(Schedulers.io()).subscribe()
    }
}
