package de.nicidienase.chaosflix.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.util.ThreadHandler
import java.util.Date

class PlayerViewModel(val database: ChaosflixDatabase) : ViewModel() {

    val handler = ThreadHandler()

    fun getPlaybackProgressLiveData(guid: String): LiveData<PlaybackProgress?> =
            database.playbackProgressDao().getProgressForEvent(guid)

    suspend fun getPlaybackProgress(guid: String) = database.playbackProgressDao().getProgressForEventSync(guid)

    fun setPlaybackProgress(eventGuid: String, progress: Long) {
        if (progress < 5_000) {
            return
        }
        handler.runOnBackgroundThread {
            database.playbackProgressDao().saveProgress(
                    PlaybackProgress(
                            progress = progress,
                            eventGuid = eventGuid,
                            watchDate = Date().time))
        }
    }

    fun deletePlaybackProgress(eventId: String) {
        handler.runOnBackgroundThread {
            database.playbackProgressDao().deleteItem(eventId)
        }
    }
}
