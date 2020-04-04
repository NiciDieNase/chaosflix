package de.nicidienase.chaosflix.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel(val database: ChaosflixDatabase) : ViewModel() {

    fun getPlaybackProgressLiveData(guid: String): LiveData<PlaybackProgress?> =
            database.playbackProgressDao().getProgressForEvent(guid)

    suspend fun getPlaybackProgress(guid: String) = database.playbackProgressDao().getProgressForEventSync(guid)

    fun setPlaybackProgress(eventGuid: String, progress: Long) {
        if (progress < 5_000) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            database.playbackProgressDao().saveProgress(
                    PlaybackProgress(
                            progress = progress,
                            eventGuid = eventGuid,
                            watchDate = Date().time))
        }
    }

    fun deletePlaybackProgress(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.playbackProgressDao().deleteItem(eventId)
        }
    }
}
