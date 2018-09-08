package de.nicidienase.chaosflix.touch.playback

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.util.ThreadHandler

internal class PlayerViewModel(val database: ChaosflixDatabase) : ViewModel() {

    val handler = ThreadHandler()

    fun getPlaybackProgress(apiID: Long): LiveData<PlaybackProgress>
            = database.playbackProgressDao().getProgressForEvent(apiID)

    fun setPlaybackProgress(eventId: Long, progress: Long) {
        handler.runOnBackgroundThread {
            database.playbackProgressDao().saveProgress(PlaybackProgress(eventId, progress))
        }
    }

    fun deletePlaybackProgress(eventId: Long) {
        handler.runOnBackgroundThread {
            database.playbackProgressDao().deleteItem(eventId)
        }
    }
}
