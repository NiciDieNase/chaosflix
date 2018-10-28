package de.nicidienase.chaosflix.common.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.util.ThreadHandler

class PlayerViewModel(val database: ChaosflixDatabase) : ViewModel() {

    val handler = ThreadHandler()

    fun getPlaybackProgress(guid: String): LiveData<PlaybackProgress?>
            = database.playbackProgressDao().getProgressForEvent(guid)

    fun setPlaybackProgress(eventGuid: String, progress: Long) {
        handler.runOnBackgroundThread {
            database.playbackProgressDao().saveProgress(
                    PlaybackProgress(progress = progress, eventGuid = eventGuid))
        }
    }

    fun deletePlaybackProgress(eventId: String) {
        handler.runOnBackgroundThread {
            database.playbackProgressDao().deleteItem(eventId)
        }
    }
}
